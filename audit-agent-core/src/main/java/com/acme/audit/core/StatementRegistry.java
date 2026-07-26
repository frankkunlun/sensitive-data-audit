package com.acme.audit.core;

import java.util.*;

final class StatementRegistry {
    private final Map<Object,StatementState> statements=Collections.synchronizedMap(new WeakHashMap<Object,StatementState>());
    private final Map<Object,PendingResult> results=Collections.synchronizedMap(new WeakHashMap<Object,PendingResult>());
    void register(Object statement,String sql){if(statement!=null&&sql!=null)statements.put(statement,new StatementState(sql));}
    StatementState state(Object statement,String directSql){StatementState state=statements.get(statement);if(state==null&&directSql!=null){state=new StatementState(directSql);statements.put(statement,state);}return state;}
    void parameter(Object statement,int index,Object value){StatementState state=statements.get(statement);if(state!=null)state.parameter(index,value);}
    void result(Object rs,PendingResult value){if(rs!=null)results.put(rs,value);} PendingResult result(Object rs){return results.get(rs);} PendingResult removeResult(Object rs){return results.remove(rs);}
    static final class StatementState { final String sql; final Map<Integer,Map<String,Object>> parameters=new TreeMap<Integer,Map<String,Object>>();
        StatementState(String sql){this.sql=sql;} void parameter(int index,Object value){Map<String,Object> safe=new LinkedHashMap<String,Object>();safe.put("index",index);safe.put("jdbcType",value==null?"NULL":value.getClass().getSimpleName());safe.put("nullValue",value==null);safe.put("length",value instanceof CharSequence?((CharSequence)value).length():-1);safe.put("value","DROPPED");parameters.put(index,safe);}
    }
    static final class PendingResult { final Map<String,Object> payload;int rows;boolean emitted;PendingResult(Map<String,Object> payload){this.payload=payload;} }
}
