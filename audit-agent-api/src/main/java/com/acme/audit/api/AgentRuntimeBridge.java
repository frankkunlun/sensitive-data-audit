package com.acme.audit.api;

/** Narrow runtime contract exposed to instrumentation plugins. */
public interface AgentRuntimeBridge {
    void instrumentationSucceeded(String pluginId);
    void instrumentationFailed(String pluginId, Throwable error);
}
