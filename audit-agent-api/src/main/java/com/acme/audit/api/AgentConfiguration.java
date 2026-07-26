package com.acme.audit.api;

/** Read-only plugin configuration view. */
public interface AgentConfiguration {
    boolean isPluginEnabled(String id);
}
