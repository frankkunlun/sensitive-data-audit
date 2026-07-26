package com.acme.audit.core;
import com.acme.audit.api.UserContext;import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;
class AuditContextManagerTest {
 @Test void activationRestoresPreviousContext(){AuditContextManager m=new AuditContextManager();AuditContext a=new AuditContext("a","r","GET","/",null,"now",0,UserContext.UNKNOWN);m.set(a);AuditContextManager.Snapshot s=m.snapshot();m.clear();try(AuditContextManager.Scope ignored=m.activate(s)){assertSame(a,m.current());}assertNull(m.current());}
}
