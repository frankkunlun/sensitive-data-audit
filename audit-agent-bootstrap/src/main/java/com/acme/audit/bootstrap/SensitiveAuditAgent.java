package com.acme.audit.bootstrap;

import com.acme.audit.api.*;import com.acme.audit.catalog.YamlSensitiveCatalog;import com.acme.audit.core.*;import com.acme.audit.plugin.jdbc.JdbcInstrumentationPlugin;import com.acme.audit.plugin.security.SpringSecurityUserResolver;import com.acme.audit.plugin.servlet.ServletInstrumentationPlugin;import com.acme.audit.reporter.AsyncJsonLineFileReporter;import com.acme.audit.reporter.HttpJsonBatchReporter;import com.acme.audit.sql.JSqlAuditParser;
import java.lang.instrument.Instrumentation;import java.util.*;

/** JVM agent entry point. Initialization and every plugin installation are fail-open. */
public final class SensitiveAuditAgent {
    private static volatile AuditRuntime runtime;
    private SensitiveAuditAgent(){}
    public static void premain(String arguments,Instrumentation instrumentation){start(arguments,instrumentation);}
    public static void agentmain(String arguments,Instrumentation instrumentation){start(arguments,instrumentation);}
    private static synchronized void start(String arguments,Instrumentation instrumentation){if(runtime!=null)return;try{AgentConfig config=AgentConfig.load(arguments);if(!config.isEnabled()){System.err.println("[sensitive-audit-agent] disabled by configuration");return;}
        AuditEventReporter reporter=reporter(config); AuditRuntime created=new AuditRuntime(config,new JSqlAuditParser(),YamlSensitiveCatalog.load(config.catalogFile()),reporter);created.addResolver(new SpringSecurityUserResolver());RuntimeAccess.initialize(created);runtime=created;
        List<AuditInstrumentationPlugin> plugins=Arrays.<AuditInstrumentationPlugin>asList(new ServletInstrumentationPlugin(),new JdbcInstrumentationPlugin());for(AuditInstrumentationPlugin p:plugins)if(p.isEnabled(config))p.install(instrumentation,created);
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable(){public void run(){AuditRuntime r=runtime;if(r!=null)r.close();}},"sensitive-audit-shutdown"));System.err.println("[sensitive-audit-agent] loaded application="+config.applicationId()+" environment="+config.environment());
      }catch(Throwable error){System.err.println("[sensitive-audit-agent] startup failed; application continues: "+error);}}
    private static AuditEventReporter reporter(AgentConfig c)throws Exception{if("http".equals(c.reporterType())){if(c.reporterUrl().trim().isEmpty())throw new IllegalArgumentException("reporter.url is required for HTTP reporter");return new HttpJsonBatchReporter(new java.net.URL(c.reporterUrl()),c.reporterToken(),c.queueSize(),c.batchSize(),c.lingerMs(),c.reporterConnectTimeoutMs(),c.reporterReadTimeoutMs());}return new AsyncJsonLineFileReporter(c.reportFile(),c.queueSize(),c.batchSize(),c.lingerMs());}
}
