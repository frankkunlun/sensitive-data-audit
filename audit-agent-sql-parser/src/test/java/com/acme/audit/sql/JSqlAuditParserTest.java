package com.acme.audit.sql;
import com.acme.audit.api.*;import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;
class JSqlAuditParserTest {
 @Test void parsesSelectAndProducesStableFingerprint(){JSqlAuditParser p=new JSqlAuditParser();ParsedSql a=p.parse("select customer_id, id_no, mobile from customer where customer_id = 12","MYSQL");ParsedSql b=p.parse("SELECT customer_id,id_no,mobile FROM customer WHERE customer_id=99","MYSQL");assertTrue(a.isParseSuccess());assertEquals(SqlOperation.SELECT,a.getOperation());assertTrue(a.getTables().contains("customer"));assertTrue(a.getColumns().contains("id_no"));assertEquals(a.getFingerprint(),b.getFingerprint());}
 @Test void failureIsFailOpenAndStillFingerprints(){ParsedSql p=new JSqlAuditParser().parse("select ??? from","MYSQL");assertFalse(p.isParseSuccess());assertNotNull(p.getFingerprint());}
}
