package com.acme.audit.api;
public final class ReporterHealth {
    private final boolean healthy; private final long queued, sent, failed, dropped;
    public ReporterHealth(boolean healthy,long queued,long sent,long failed,long dropped){this.healthy=healthy;this.queued=queued;this.sent=sent;this.failed=failed;this.dropped=dropped;}
    public boolean isHealthy(){return healthy;} public long getQueued(){return queued;} public long getSent(){return sent;} public long getFailed(){return failed;} public long getDropped(){return dropped;}
}
