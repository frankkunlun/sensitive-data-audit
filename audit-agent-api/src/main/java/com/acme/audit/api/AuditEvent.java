package com.acme.audit.api;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Deterministically ordered event envelope. */
public final class AuditEvent {
    private final String schemaVersion, eventId, eventType, eventTime;
    private final long sequence;
    private final Map<String,Object> payload;
    private final String previousHash, recordHash;
    public AuditEvent(String schemaVersion, String eventId, String eventType, String eventTime, long sequence,
                      Map<String,Object> payload, String previousHash, String recordHash) {
        this.schemaVersion=schemaVersion; this.eventId=eventId; this.eventType=eventType; this.eventTime=eventTime; this.sequence=sequence;
        this.payload=Collections.unmodifiableMap(new LinkedHashMap<String,Object>(payload)); this.previousHash=previousHash; this.recordHash=recordHash;
    }
    public String getSchemaVersion(){return schemaVersion;} public String getEventId(){return eventId;} public String getEventType(){return eventType;}
    public String getEventTime(){return eventTime;} public long getSequence(){return sequence;} public Map<String,Object> getPayload(){return payload;}
    public String getPreviousHash(){return previousHash;} public String getRecordHash(){return recordHash;}
}
