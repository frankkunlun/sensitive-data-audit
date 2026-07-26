package com.acme.audit.core;

import com.acme.audit.api.UserContext;

/** Small request context; it deliberately contains no request or response object. */
public final class AuditContext {
    final String traceId, requestId, method, uri, sourceIp, startTime; final long startNanos;
    final UserContext user; int sensitiveAccessCount; boolean truncated;
    AuditContext(String traceId,String requestId,String method,String uri,String sourceIp,String startTime,long startNanos,UserContext user){
        this.traceId=traceId;this.requestId=requestId;this.method=method;this.uri=uri;this.sourceIp=sourceIp;this.startTime=startTime;this.startNanos=startNanos;this.user=user;
    }
    public String getTraceId(){return traceId;} public String getRequestId(){return requestId;} public UserContext getUser(){return user;}
}
