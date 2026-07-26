package com.acme.audit.api;

import java.lang.instrument.Instrumentation;

/** Installs one fail-open instrumentation concern. */
public interface AuditInstrumentationPlugin {
    String pluginId();
    boolean isEnabled(AgentConfiguration config);
    void install(Instrumentation instrumentation, AgentRuntimeBridge runtime);
}
