package com.acme.audit.plugin.jdbc;

import com.acme.audit.api.*;import com.acme.audit.core.*;import net.bytebuddy.agent.builder.AgentBuilder;import net.bytebuddy.asm.Advice;import net.bytebuddy.description.type.TypeDescription;import net.bytebuddy.dynamic.DynamicType;import net.bytebuddy.implementation.bytecode.assign.Assigner;import net.bytebuddy.utility.JavaModule;
import java.lang.instrument.Instrumentation;import java.security.ProtectionDomain;
import static net.bytebuddy.matcher.ElementMatchers.*;

/** JDBC implementation-class instrumentation; no driver or pool dependency is required. */
public final class JdbcInstrumentationPlugin implements AuditInstrumentationPlugin {
    public String pluginId(){return "jdbc";}public boolean isEnabled(AgentConfiguration c){return c.isPluginEnabled(pluginId());}
    public void install(Instrumentation inst,AgentRuntimeBridge bridge){try{AgentBuilder builder=new AgentBuilder.Default().ignore(nameStartsWith("com.acme.audit.").or(nameStartsWith("net.bytebuddy.")).or(isInterface()))
      .type(hasSuperType(named("java.sql.Connection"))).transform(transform(ConnectionAdvice.class,named("prepareStatement").or(named("prepareCall")).and(takesArgument(0,String.class))))
      .type(hasSuperType(named("java.sql.PreparedStatement"))).transform(transform(ParameterAdvice.class,nameStartsWith("set").and(takesArgument(0,int.class)).and(takesArguments(2).or(takesArguments(3)))))
      .type(hasSuperType(named("java.sql.PreparedStatement"))).transform(transform(PreparedExecuteAdvice.class,namedOneOf("execute","executeQuery","executeUpdate","executeLargeUpdate","executeBatch").and(takesArguments(0))))
      .type(hasSuperType(named("java.sql.Statement")).and(not(hasSuperType(named("java.sql.PreparedStatement"))))).transform(transform(StatementExecuteAdvice.class,namedOneOf("execute","executeQuery","executeUpdate","executeLargeUpdate").and(takesArgument(0,String.class))))
      .type(hasSuperType(named("java.sql.ResultSet"))).transform(transform(ResultSetNextAdvice.class,named("next").and(takesArguments(0))))
      .type(hasSuperType(named("java.sql.ResultSet"))).transform(transform(ResultSetCloseAdvice.class,named("close").and(takesArguments(0))));
      builder.installOn(inst);bridge.instrumentationSucceeded(pluginId());}catch(Throwable e){bridge.instrumentationFailed(pluginId(),e);}}
    private static AgentBuilder.Transformer transform(final Class<?> advice,final net.bytebuddy.matcher.ElementMatcher<? super net.bytebuddy.description.method.MethodDescription> methods){return new AgentBuilder.Transformer(){public DynamicType.Builder<?> transform(DynamicType.Builder<?> b,TypeDescription t,ClassLoader l,JavaModule m,ProtectionDomain p){return b.visit(Advice.to(advice).on(methods));}};}
    public static final class ConnectionAdvice {@Advice.OnMethodExit(suppress=Throwable.class)public static void exit(@Advice.Argument(0)String sql,@Advice.Return(typing=Assigner.Typing.DYNAMIC)Object statement){AuditRuntime r=RuntimeAccess.get();if(r!=null)r.registerStatement(statement,sql);}}
    public static final class ParameterAdvice {@Advice.OnMethodEnter(suppress=Throwable.class)public static void enter(@Advice.This Object statement,@Advice.Argument(0)int index,@Advice.Argument(value=1,typing=Assigner.Typing.DYNAMIC)Object value){AuditRuntime r=RuntimeAccess.get();if(r!=null)r.recordParameter(statement,index,value);}}
    public static final class PreparedExecuteAdvice {
      @Advice.OnMethodEnter(suppress=Throwable.class)public static long enter(){AuditRuntime r=RuntimeAccess.get();return r==null?0:r.executionStarted();}
      @Advice.OnMethodExit(onThrowable=Throwable.class,suppress=Throwable.class)public static void exit(@Advice.This Object statement,@Advice.Enter long start,@Advice.Return(typing=Assigner.Typing.DYNAMIC)Object result,@Advice.Thrown Throwable error){AuditRuntime r=RuntimeAccess.get();if(r!=null)r.executionFinished(statement,null,start,result,error);}}
    public static final class StatementExecuteAdvice {
      @Advice.OnMethodEnter(suppress=Throwable.class)public static long enter(){AuditRuntime r=RuntimeAccess.get();return r==null?0:r.executionStarted();}
      @Advice.OnMethodExit(onThrowable=Throwable.class,suppress=Throwable.class)public static void exit(@Advice.This Object statement,@Advice.Argument(0)String sql,@Advice.Enter long start,@Advice.Return(typing=Assigner.Typing.DYNAMIC)Object result,@Advice.Thrown Throwable error){AuditRuntime r=RuntimeAccess.get();if(r!=null)r.executionFinished(statement,sql,start,result,error);}}
    public static final class ResultSetNextAdvice {@Advice.OnMethodExit(onThrowable=Throwable.class,suppress=Throwable.class)public static void exit(@Advice.This Object rs,@Advice.Return boolean row,@Advice.Thrown Throwable error){if(error==null){AuditRuntime r=RuntimeAccess.get();if(r!=null)r.resultSetNext(rs,row);}}}
    public static final class ResultSetCloseAdvice {@Advice.OnMethodExit(suppress=Throwable.class)public static void exit(@Advice.This Object rs){AuditRuntime r=RuntimeAccess.get();if(r!=null)r.resultSetClosed(rs);}}
}
