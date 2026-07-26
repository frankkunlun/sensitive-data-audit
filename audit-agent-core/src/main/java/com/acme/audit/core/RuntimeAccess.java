package com.acme.audit.core;
/** The sole static bridge used by injected advice. */
public final class RuntimeAccess {
    private static volatile AuditRuntime runtime;
    private RuntimeAccess(){}
    public static void initialize(AuditRuntime value){runtime=value;}
    public static AuditRuntime get(){return runtime;}
}
