package com.acme.audit.core;

/** Thread-local context with explicit snapshot activation for executor wrappers. */
public final class AuditContextManager {
    private final ThreadLocal<AuditContext> local=new ThreadLocal<AuditContext>();
    public AuditContext current(){return local.get();} void set(AuditContext value){local.set(value);} public void clear(){local.remove();}
    public Snapshot snapshot(){return new Snapshot(local.get());}
    public Scope activate(Snapshot snapshot){AuditContext previous=local.get();if(snapshot.context==null)local.remove();else local.set(snapshot.context);return new Scope(this,previous);}
    public static final class Snapshot { private final AuditContext context; private Snapshot(AuditContext context){this.context=context;} }
    public static final class Scope implements AutoCloseable { private final AuditContextManager manager;private final AuditContext previous;private boolean closed;
        private Scope(AuditContextManager manager,AuditContext previous){this.manager=manager;this.previous=previous;}
        public void close(){if(!closed){if(previous==null)manager.local.remove();else manager.local.set(previous);closed=true;}}
    }
}
