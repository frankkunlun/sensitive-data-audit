package com.acme.audit.sql;

import com.acme.audit.api.*;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.merge.Merge;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.TablesNamesFinder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** JSqlParser-backed parser with a conservative lexical column extractor. */
public final class JSqlAuditParser implements SqlDialectParser {
    private static final Pattern STRING=Pattern.compile("'(?:''|[^'])*'");
    private static final Pattern NUMBER=Pattern.compile("(?<![\\w.])[-+]?\\d+(?:\\.\\d+)?(?![\\w.])");
    private static final Pattern CONDITION=Pattern.compile("(?i)([a-z_][\\w$]*(?:\\.[a-z_][\\w$]*)?)\\s*(?:=|<>|!=|<=|>=|<|>|\\bLIKE\\b|\\bIN\\b|\\bIS\\b)");
    @Override public ParsedSql parse(String sql,String dialect){
        String normalized=normalize(sql);String fingerprint=sha256(normalized);Set<String> tables=new LinkedHashSet<String>();Set<String> columns=new LinkedHashSet<String>();Set<String> conditions=new LinkedHashSet<String>();boolean all=false;
        try {Statement statement=CCJSqlParserUtil.parse(sql);SqlOperation op=operation(statement);for(String table:new TablesNamesFinder().getTableList(statement))tables.add(clean(table));
            String upper=stripComments(sql).toUpperCase(Locale.ROOT);all=Pattern.compile("(?i)(?:SELECT|,)\\s*(?:[a-z_][\\w$]*\\.)?\\*").matcher(upper).find();extractColumns(sql,op,columns);Matcher cm=CONDITION.matcher(stripStrings(sql));while(cm.find())conditions.add(clean(cm.group(1)));
            return new ParsedSql(op,tables,columns,conditions,all,true,null,normalized,fingerprint);
        }catch(Throwable error){fallbackTables(sql,tables);return new ParsedSql(guess(sql),tables,columns,conditions,all,false,error.getClass().getSimpleName(),normalized,fingerprint);}
    }
    public static String normalize(String sql){if(sql==null)return "";String s=stripComments(sql);s=STRING.matcher(s).replaceAll("?");s=NUMBER.matcher(s).replaceAll("?");s=s.replaceAll("\\s+"," ").trim();s=s.replaceAll("\\s*([,=()])\\s*","$1");return s.toUpperCase(Locale.ROOT);}
    private static SqlOperation operation(Statement s){if(s instanceof Select)return SqlOperation.SELECT;if(s instanceof Insert)return SqlOperation.INSERT;if(s instanceof Update)return SqlOperation.UPDATE;if(s instanceof Delete)return SqlOperation.DELETE;if(s instanceof Merge)return SqlOperation.MERGE;return SqlOperation.UNKNOWN;}
    private static SqlOperation guess(String sql){String s=stripComments(sql).trim().toUpperCase(Locale.ROOT);for(SqlOperation x:SqlOperation.values())if(s.startsWith(x.name()+" "))return x;return SqlOperation.UNKNOWN;}
    private static void extractColumns(String sql,SqlOperation op,Set<String> out){String clean=stripStrings(stripComments(sql));String upper=clean.toUpperCase(Locale.ROOT);String part="";
        if(op==SqlOperation.SELECT){int a=upper.indexOf("SELECT")+6,b=indexWord(upper,"FROM",a);if(b>a)part=clean.substring(a,b);}
        else if(op==SqlOperation.UPDATE){int a=indexWord(upper,"SET",0),b=indexWord(upper,"WHERE",a+3);if(a>=0)part=clean.substring(a+3,b<0?clean.length():b);}
        else if(op==SqlOperation.INSERT){int into=indexWord(upper,"INTO",0),a=clean.indexOf('(',into),b=a<0?-1:clean.indexOf(')',a);if(a>=0&&b>a)part=clean.substring(a+1,b);}
        for(String token:part.split(",")){String x=token.trim();if(x.equals("*")||x.endsWith(".*"))continue;if(op==SqlOperation.UPDATE&&x.contains("="))x=x.substring(0,x.indexOf('='));x=x.replaceAll("(?i)\\s+AS\\s+.*$","").trim();Matcher m=Pattern.compile("([a-zA-Z_][\\w$]*(?:\\.[a-zA-Z_][\\w$]*)?)\\s*$").matcher(x);if(m.find())out.add(clean(m.group(1)));}
    }
    private static int indexWord(String s,String word,int from){Matcher m=Pattern.compile("\\b"+word+"\\b").matcher(s);return m.find(Math.max(0,from))?m.start():-1;}
    private static void fallbackTables(String sql,Set<String> tables){Matcher m=Pattern.compile("(?i)\\b(?:FROM|JOIN|UPDATE|INTO|DELETE\\s+FROM)\\s+([a-z_][\\w$]*(?:\\.[a-z_][\\w$]*)?)").matcher(stripStrings(sql));while(m.find())tables.add(clean(m.group(1)));}
    private static String stripComments(String s){return s==null?"":s.replaceAll("(?s)/\\*.*?\\*/"," ").replaceAll("--[^\\r\\n]*"," ");}private static String stripStrings(String s){return STRING.matcher(s).replaceAll("''");}
    private static String clean(String x){String s=x.replace("`","").replace("\"","").trim();return s.toLowerCase(Locale.ROOT);}
    private static String sha256(String value){try{byte[] d=MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));StringBuilder b=new StringBuilder();for(byte x:d)b.append(String.format("%02x",x));return b.toString();}catch(Exception e){return "HASH_ERROR";}}
}
