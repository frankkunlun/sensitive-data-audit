package com.acme.audit.catalog;
import com.acme.audit.api.*;import org.junit.jupiter.api.Test;import java.nio.file.*;import java.util.*;import static org.junit.jupiter.api.Assertions.*;
class YamlSensitiveCatalogTest {
 @Test void matchesCaseInsensitiveColumn()throws Exception{Path f=Files.createTempFile("catalog",".yaml");Files.write(f,Arrays.asList("version: 7","columns:","  - table: customer","    column: id_no","    category: identity","    level: L4"));YamlSensitiveCatalog c=YamlSensitiveCatalog.load(f);ParsedSql p=new ParsedSql(SqlOperation.SELECT,new LinkedHashSet<String>(Arrays.asList("CUSTOMER")),new LinkedHashSet<String>(Arrays.asList("ID_NO")),Collections.<String>emptySet(),false,true,null,"x","f");assertEquals(1,c.match(p).size());assertEquals(7,c.version());}
}
