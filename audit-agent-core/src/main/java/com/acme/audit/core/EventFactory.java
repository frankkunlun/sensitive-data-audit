package com.acme.audit.core;

import com.acme.audit.api.AuditEvent;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

final class EventFactory {
    private final AtomicLong sequence=new AtomicLong(); private String previousHash="GENESIS";
    synchronized AuditEvent create(String type,Map<String,Object> payload){
        long seq=sequence.incrementAndGet();String id=UUID.randomUUID().toString();String time=OffsetDateTime.now().toString();
        String hash=sha256("1.0|"+id+'|'+type+'|'+time+'|'+seq+'|'+previousHash+'|'+canonical(payload));
        AuditEvent event=new AuditEvent("1.0",id,type,time,seq,payload,previousHash,hash);previousHash=hash;return event;
    }
    private static String canonical(Object v){
        if(v==null)return "null";if(v instanceof Map){StringBuilder b=new StringBuilder("{");for(Object key:new TreeSet<Object>(((Map<?,?>)v).keySet()))b.append(key).append(':').append(canonical(((Map<?,?>)v).get(key))).append(';');return b.append('}').toString();}
        if(v instanceof Iterable){StringBuilder b=new StringBuilder("[");for(Object x:(Iterable<?>)v)b.append(canonical(x)).append(';');return b.append(']').toString();}return String.valueOf(v);
    }
    static String sha256(String value){try{byte[] d=MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));StringBuilder b=new StringBuilder();for(byte x:d)b.append(String.format("%02x",x));return b.toString();}catch(Exception e){return "HASH_ERROR";}}
}
