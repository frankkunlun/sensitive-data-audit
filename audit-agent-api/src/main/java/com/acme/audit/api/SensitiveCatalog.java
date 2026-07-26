package com.acme.audit.api;
import java.util.List;
public interface SensitiveCatalog { List<SensitiveColumnMetadata> match(ParsedSql parsedSql); long version(); }
