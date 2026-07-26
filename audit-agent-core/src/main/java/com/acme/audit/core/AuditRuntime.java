package com.acme.audit.core;

import com.acme.audit.api.*;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/** Fail-open coordinator for request, SQL, catalog, integrity and reporting concerns. */
public final class AuditRuntime implements AgentRuntimeBridge, AutoCloseable {
    private final AgentConfig config; private final SqlDialectParser parser; private final SensitiveCatalog catalog; private final AuditEventReporter reporter;
    private final AuditContextManager contexts=new AuditContextManager(); private final StatementRegistry registry=new StatementRegistry(); private final EventFactory events=new EventFactory();
    private final List<UserContextResolver> resolvers=new CopyOnWriteArrayList<UserContextResolver>();
    private final AtomicLong instrumentationSuccess=new AtomicLong(),instrumentationFailure=new AtomicLong(),internalErrors=new AtomicLong(),jdbcCalls=new AtomicLong();
    public AuditRuntime(AgentConfig config,SqlDialectParser parser,SensitiveCatalog catalog,AuditEventReporter reporter){this.config=config;this.parser=parser;this.catalog=catalog;this.reporter=reporter;}
    public AgentConfig config(){return config;} public AuditContextManager contextManager(){return contexts;}
    public void addResolver(UserContextResolver resolver){resolvers.add(resolver);Collections.sort(resolvers,new Comparator<UserContextResolver>(){public int compare(UserContextResolver a,UserContextResolver b){return Integer.compare(a.order(),b.order());}});}
    @Override public void instrumentationSucceeded(String id){instrumentationSuccess.incrementAndGet();log("installed plugin "+id,null);}
    @Override public void instrumentationFailed(String id,Throwable error){instrumentationFailure.incrementAndGet();log("failed plugin "+id,error);}

    public RequestToken beginRequest(Object request){
        try {if(contexts.current()!=null)return new RequestToken(false);
            String trace=firstHeader(request,"traceparent","X-Trace-Id");if(trace==null||trace.length()>128)trace=UUID.randomUUID().toString().replace("-","");
            String requestId=firstHeader(request,"X-Request-Id");if(requestId==null)requestId="REQ-"+UUID.randomUUID();
            UserContext user=resolveUser(request); AuditContext context=new AuditContext(trace,requestId,or(SafeReflection.text(request,"getMethod"),"UNKNOWN"),or(SafeReflection.text(request,"getRequestURI"),"UNKNOWN"),clientIp(request),OffsetDateTime.now().toString(),System.nanoTime(),user);
            contexts.set(context); return new RequestToken(true);
        } catch(Throwable error){internal(error);return new RequestToken(false);}
    }
    public void endRequest(RequestToken token,Object response,Throwable failure){
        if(token==null||!token.owner)return;AuditContext context=contexts.current();try{if(context!=null&&context.sensitiveAccessCount>0){
            Map<String,Object> p=base(context);Map<String,Object> request=new LinkedHashMap<String,Object>();request.put("protocol","HTTP");request.put("method",context.method);request.put("uri",context.uri);request.put("sourceIp",context.sourceIp);request.put("startTime",context.startTime);request.put("durationMs",millis(context.startNanos));int status=SafeReflection.integer(response,"getStatus",failure==null?200:500);request.put("statusCode",status);request.put("success",failure==null&&status<400);if(failure!=null)request.put("exceptionType",failure.getClass().getName());p.put("request",request);Map<String,Object> summary=new LinkedHashMap<String,Object>();summary.put("sensitive",true);summary.put("dataAccessCount",context.sensitiveAccessCount);summary.put("truncated",context.truncated);p.put("dataSummary",summary);emit("REQUEST_AUDIT_EVENT",p);
        }}catch(Throwable error){internal(error);}finally{contexts.clear();}
    }
    public void registerStatement(Object statement,String sql){try{if(sql!=null&&sql.length()>config.maxSqlLength())sql=sql.substring(0,config.maxSqlLength());registry.register(statement,sql);}catch(Throwable e){internal(e);}}
    public void recordParameter(Object statement,int index,Object value){try{registry.parameter(statement,index,value);}catch(Throwable e){internal(e);}}
    public long executionStarted(){return System.nanoTime();}
    public void executionFinished(Object statement,String directSql,long started,Object result,Throwable failure){
        try {jdbcCalls.incrementAndGet();StatementRegistry.StatementState state=registry.state(statement,directSql);if(state==null||state.sql==null)return;
            ParsedSql parsed=parser.parse(state.sql,config.dialect());List<SensitiveColumnMetadata> matches=catalog.match(parsed);if(matches.isEmpty())return;
            AuditContext context=contexts.current();if(context!=null){if(context.sensitiveAccessCount>=config.maxEventsPerRequest()){context.truncated=true;return;}context.sensitiveAccessCount++;}
            Map<String,Object> p=context==null?standaloneBase():base(context);p.put("datasourceId","default");p.put("databaseType",config.dialect());p.put("sqlOperation",parsed.getOperation().name());p.put("sqlFingerprint",parsed.getFingerprint());p.put("normalizedSql",parsed.getNormalizedSql());p.put("tables",parsed.getTables());p.put("columns",parsed.getColumns());p.put("conditionColumns",parsed.getConditionColumns());p.put("parseSuccess",parsed.isParseSuccess());if(!parsed.isParseSuccess())p.put("parseFailure",parsed.getFailureReason());p.put("durationMs",millis(started));p.put("success",failure==null);if(failure!=null)p.put("exceptionType",failure.getClass().getName());p.put("affectedRows",affected(result));p.put("parameters",new ArrayList<Map<String,Object>>(state.parameters.values()));p.put("sensitiveFields",sensitiveFields(matches));p.put("highestSensitivityLevel",highest(matches));
            if(result instanceof java.sql.ResultSet){registry.result(result,new StatementRegistry.PendingResult(p));}else emit("DATA_ACCESS_EVENT",p);
        }catch(Throwable error){internal(error);}
    }
    public void resultSetNext(Object resultSet,boolean hasRow){try{StatementRegistry.PendingResult pending=registry.result(resultSet);if(pending==null||pending.emitted)return;if(hasRow)pending.rows++;else finalizeResult(resultSet,pending);}catch(Throwable e){internal(e);}}
    public void resultSetClosed(Object resultSet){try{StatementRegistry.PendingResult p=registry.removeResult(resultSet);if(p!=null&&!p.emitted)finalizeResult(null,p);}catch(Throwable e){internal(e);}}
    private void finalizeResult(Object key,StatementRegistry.PendingResult pending){pending.emitted=true;pending.payload.put("returnedRows",pending.rows);emit("DATA_ACCESS_EVENT",pending.payload);if(key!=null)registry.removeResult(key);}
    private UserContext resolveUser(Object request){for(UserContextResolver r:resolvers)try{UserContext u=r.resolve(request);if(u!=null&&u.isAuthenticated())return u;}catch(Throwable e){internal(e);}return UserContext.UNKNOWN;}
    private Map<String,Object> base(AuditContext c){Map<String,Object> p=application();Map<String,Object> trace=new LinkedHashMap<String,Object>();trace.put("traceId",c.traceId);trace.put("requestId",c.requestId);p.put("trace",trace);p.put("user",user(c.user));Map<String,Object> function=new LinkedHashMap<String,Object>();function.put("functionCode",c.method+" "+c.uri);function.put("functionName",c.uri);function.put("operationType",operation(c.method,c.uri));p.put("function",function);return p;}
    private Map<String,Object> standaloneBase(){Map<String,Object> p=application();Map<String,Object> t=new LinkedHashMap<String,Object>();t.put("traceId",UUID.randomUUID().toString().replace("-",""));t.put("requestId",null);p.put("trace",t);p.put("user",user(UserContext.UNKNOWN));return p;}
    private Map<String,Object> application(){Map<String,Object> p=new LinkedHashMap<String,Object>();Map<String,Object> a=new LinkedHashMap<String,Object>();a.put("applicationId",config.applicationId());a.put("environment",config.environment());a.put("instanceId",config.agentId());a.put("agentVersion","1.0.0-SNAPSHOT");p.put("application",a);return p;}
    private static Map<String,Object> user(UserContext u){Map<String,Object> m=new LinkedHashMap<String,Object>();m.put("userId",u.getUserId());m.put("userName",u.getUserName());m.put("organizationId",u.getOrganizationId());m.put("roleCodes",u.getRoleCodes());m.put("authenticated",u.isAuthenticated());return m;}
    private static List<Map<String,Object>> sensitiveFields(List<SensitiveColumnMetadata> list){List<Map<String,Object>> out=new ArrayList<Map<String,Object>>();for(SensitiveColumnMetadata x:list){Map<String,Object> m=new LinkedHashMap<String,Object>();m.put("name",x.getTableName()+"."+x.getColumnName());m.put("category",x.getDataCategory());m.put("level",x.getSensitivityLevel());out.add(m);}return out;}
    private static String highest(List<SensitiveColumnMetadata> list){String max="L1";for(SensitiveColumnMetadata x:list)if(x.getSensitivityLevel()!=null&&x.getSensitivityLevel().compareTo(max)>0)max=x.getSensitivityLevel();return max;}
    private static long affected(Object value){if(value instanceof Number)return ((Number)value).longValue();if(value instanceof int[]){long sum=0;for(int x:(int[])value)if(x>0)sum+=x;return sum;}return 0;}
    private void emit(String type,Map<String,Object> payload){reporter.report(Collections.singletonList(events.create(type,payload)));}
    private static String firstHeader(Object request,String... names){for(String n:names){String v=SafeReflection.text(request,"getHeader",n);if(v!=null&&!v.trim().isEmpty())return v.trim();}return null;}
    private static String clientIp(Object request){String remote=SafeReflection.text(request,"getRemoteAddr");return or(remote,"UNKNOWN");} // Proxy headers require an explicit trusted-proxy policy and are intentionally ignored by default.
    private static String operation(String method,String uri){String u=uri==null?"":uri.toLowerCase(Locale.ROOT);if(u.contains("export"))return "EXPORT";if(u.contains("download"))return "DOWNLOAD";if("GET".equals(method))return "QUERY";if("DELETE".equals(method))return "DELETE";if("POST".equals(method))return "CREATE";if("PUT".equals(method)||"PATCH".equals(method))return "UPDATE";return "UNKNOWN";}
    private static String or(String value,String fallback){return value==null?fallback:value;} private static long millis(long started){return Math.max(0,(System.nanoTime()-started)/1_000_000L);}
    private void internal(Throwable e){internalErrors.incrementAndGet();log("internal error",e);} private static void log(String message,Throwable error){System.err.println("[sensitive-audit-agent] "+message+(error==null?"":": "+error));}
    public Map<String,Long> metrics(){Map<String,Long> m=new LinkedHashMap<String,Long>();m.put("instrumentation_success_total",instrumentationSuccess.get());m.put("instrumentation_failure_total",instrumentationFailure.get());m.put("jdbc_calls_observed_total",jdbcCalls.get());m.put("agent_internal_error_total",internalErrors.get());return m;}
    @Override public void close(){reporter.close();}
    public static final class RequestToken { final boolean owner;RequestToken(boolean owner){this.owner=owner;} }
}
