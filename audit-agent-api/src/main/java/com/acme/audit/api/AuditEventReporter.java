package com.acme.audit.api;
import java.util.List;
public interface AuditEventReporter extends AutoCloseable {
    void report(List<AuditEvent> events);
    ReporterHealth health();
    void close();
}
