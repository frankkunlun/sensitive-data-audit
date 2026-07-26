package com.acme.audit.reporter;
import com.acme.audit.api.*;import org.junit.jupiter.api.Test;import java.nio.file.*;import java.util.*;import static org.junit.jupiter.api.Assertions.*;
class AsyncJsonLineFileReporterTest {
 @Test void writesJsonLineOnClose()throws Exception{Path f=Files.createTempFile("audit",".jsonl");AsyncJsonLineFileReporter r=new AsyncJsonLineFileReporter(f,10,2,20);r.report(Collections.singletonList(new AuditEvent("1","id","TEST","now",1,Collections.<String,Object>singletonMap("safe","yes"),"p","h")));r.close();String s=new String(Files.readAllBytes(f),"UTF-8");assertTrue(s.contains("\"eventId\":\"id\""));}
}
