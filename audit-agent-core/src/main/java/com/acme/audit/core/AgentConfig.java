package com.acme.audit.core;

import com.acme.audit.api.AgentConfiguration;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

/** Validated local configuration. Missing values use safe PoC defaults. */
public final class AgentConfig implements AgentConfiguration {
    private final Map<String,Object> root;
    private final Path source;
    private AgentConfig(Map<String,Object> root, Path source) { this.root=root; this.source=source; }
    public static AgentConfig load(String path) throws Exception {
        Path source = path == null || path.trim().isEmpty() ? Paths.get("agent.yaml") : Paths.get(path.trim());
        if (!Files.isRegularFile(source)) throw new IllegalArgumentException("Agent config not found: " + source.toAbsolutePath());
        InputStream in=Files.newInputStream(source);
        try {
            Object value=new Yaml(new SafeConstructor(new LoaderOptions())).load(in);
            if (!(value instanceof Map)) throw new IllegalArgumentException("Agent config root must be a map");
            @SuppressWarnings("unchecked") Map<String,Object> map=(Map<String,Object>)value;
            return new AgentConfig(map,source.toAbsolutePath().normalize());
        } finally { in.close(); }
    }
    @SuppressWarnings("unchecked") private Map<String,Object> section(String name){Object v=root.get(name);return v instanceof Map?(Map<String,Object>)v:Collections.<String,Object>emptyMap();}
    private String text(String section,String key,String fallback){Object v=section(section).get(key);return v==null?fallback:String.valueOf(v);}
    private boolean bool(String section,String key,boolean fallback){Object v=section(section).get(key);return v==null?fallback:Boolean.parseBoolean(String.valueOf(v));}
    private int integer(String section,String key,int fallback){Object v=section(section).get(key);try{return v==null?fallback:Integer.parseInt(String.valueOf(v));}catch(Exception e){return fallback;}}
    public boolean isEnabled(){return bool("agent","enabled",true);} public boolean isFailOpen(){return bool("agent","fail-open",true);}
    public String applicationId(){return text("agent","application-id","unknown-application");} public String environment(){return text("agent","environment","UNKNOWN");}
    public String agentId(){return text("agent","agent-id",applicationId()+"-"+ProcessHandleCompat.pid());}
    public String dialect(){return text("jdbc","dialect","MYSQL");} public int maxSqlLength(){return integer("jdbc","max-sql-length",4096);}
    public int maxEventsPerRequest(){return integer("jdbc","max-events-per-request",100);}
    public int queueSize(){return integer("reporter","max-queue-size",10000);} public int batchSize(){return integer("reporter","batch-size",100);}
    public long lingerMs(){return integer("reporter","linger-ms",500);}
    public String reporterType(){return text("reporter","type","file").trim().toLowerCase(java.util.Locale.ROOT);}
    public String reporterUrl(){return text("reporter","url","");}
    public String reporterToken(){return text("reporter","token","");}
    public int reporterConnectTimeoutMs(){return integer("reporter","connect-timeout-ms",2000);}
    public int reporterReadTimeoutMs(){return integer("reporter","read-timeout-ms",5000);}
    public Path reportFile(){return resolve(text("reporter","file","logs/sensitive-audit.jsonl"));}
    public Path catalogFile(){return resolve(text("sensitive-catalog","file","sensitive-catalog.yaml"));}
    private Path resolve(String value){Path p=Paths.get(value);if(p.isAbsolute())return p;Path parent=source.getParent();return (parent==null?p:parent.resolve(p)).normalize();}
    @Override public boolean isPluginEnabled(String id){return bool("plugins",id,true);}
    static final class ProcessHandleCompat { static String pid(){String jvm=java.lang.management.ManagementFactory.getRuntimeMXBean().getName();int i=jvm.indexOf('@');return i>0?jvm.substring(0,i):"unknown";} }
}
