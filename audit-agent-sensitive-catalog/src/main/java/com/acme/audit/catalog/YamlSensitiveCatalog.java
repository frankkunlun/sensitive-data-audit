package com.acme.audit.catalog;

import com.acme.audit.api.*;
import org.yaml.snakeyaml.LoaderOptions;import org.yaml.snakeyaml.Yaml;import org.yaml.snakeyaml.constructor.SafeConstructor;
import java.io.InputStream;import java.nio.file.*;import java.util.*;import java.util.regex.Pattern;

/** Immutable local YAML catalog. Reload is performed by replacing the instance. */
public final class YamlSensitiveCatalog implements SensitiveCatalog {
    private final long version;private final List<SensitiveColumnMetadata> entries;
    private YamlSensitiveCatalog(long version,List<SensitiveColumnMetadata> entries){this.version=version;this.entries=Collections.unmodifiableList(entries);}
    public static YamlSensitiveCatalog load(Path path)throws Exception{if(!Files.isRegularFile(path))throw new IllegalArgumentException("Sensitive catalog not found: "+path);InputStream in=Files.newInputStream(path);try{
        Object raw=new Yaml(new SafeConstructor(new LoaderOptions())).load(in);if(!(raw instanceof Map))throw new IllegalArgumentException("Catalog root must be a map");Map<?,?> root=(Map<?,?>)raw;long version=number(root.get("version"),1);Object list=root.get("columns");List<SensitiveColumnMetadata> out=new ArrayList<SensitiveColumnMetadata>();
        if(list instanceof Iterable)for(Object x:(Iterable<?>)list)if(x instanceof Map){Map<?,?> m=(Map<?,?>)x;if(!bool(m.get("enabled"),true))continue;out.add(new SensitiveColumnMetadata(text(m,"datasource-id","*"),text(m,"database","*"),text(m,"schema","*"),text(m,"table","*"),text(m,"column","*"),text(m,"category","UNCLASSIFIED"),text(m,"level","L3"),text(m,"masking-rule","DROP_VALUE"),number(m.get("version"),version)));}
        return new YamlSensitiveCatalog(version,out);
    }finally{in.close();}}
    @Override public List<SensitiveColumnMetadata> match(ParsedSql sql){List<SensitiveColumnMetadata> out=new ArrayList<SensitiveColumnMetadata>();for(SensitiveColumnMetadata e:entries){if(!matchesAny(e.getTableName(),sql.getTables()))continue;if(sql.isSelectAll()||matchesAny(e.getColumnName(),sql.getColumns())||matchesAny(e.getColumnName(),sql.getConditionColumns()))out.add(e);}return out;}
    private static boolean matchesAny(String pattern,Set<String> actual){for(String x:actual){String leaf=x.contains(".")?x.substring(x.lastIndexOf('.')+1):x;if(glob(pattern,x)||glob(pattern,leaf))return true;}return false;}
    private static boolean glob(String glob,String value){StringBuilder r=new StringBuilder("^");for(char c:glob.toLowerCase(Locale.ROOT).toCharArray()){if(c=='*')r.append(".*");else if(c=='?')r.append('.');else r.append(Pattern.quote(String.valueOf(c)));}return value.toLowerCase(Locale.ROOT).matches(r.append('$').toString());}
    private static String text(Map<?,?>m,String key,String fallback){Object v=m.get(key);return v==null?fallback:String.valueOf(v);}private static long number(Object v,long fallback){try{return v==null?fallback:Long.parseLong(String.valueOf(v));}catch(Exception e){return fallback;}}private static boolean bool(Object v,boolean fallback){return v==null?fallback:Boolean.parseBoolean(String.valueOf(v));}
    @Override public long version(){return version;}
}
