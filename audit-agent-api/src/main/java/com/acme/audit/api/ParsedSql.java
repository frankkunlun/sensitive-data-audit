package com.acme.audit.api;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/** Immutable, dialect-neutral SQL analysis result. */
public final class ParsedSql {
    private final SqlOperation operation;
    private final Set<String> tables;
    private final Set<String> columns;
    private final Set<String> conditionColumns;
    private final boolean selectAll;
    private final boolean parseSuccess;
    private final String failureReason;
    private final String normalizedSql;
    private final String fingerprint;

    public ParsedSql(SqlOperation operation, Set<String> tables, Set<String> columns, Set<String> conditionColumns,
                     boolean selectAll, boolean parseSuccess, String failureReason, String normalizedSql, String fingerprint) {
        this.operation = operation;
        this.tables = immutable(tables); this.columns = immutable(columns); this.conditionColumns = immutable(conditionColumns);
        this.selectAll = selectAll; this.parseSuccess = parseSuccess; this.failureReason = failureReason;
        this.normalizedSql = normalizedSql; this.fingerprint = fingerprint;
    }
    private static Set<String> immutable(Set<String> input) { return Collections.unmodifiableSet(new LinkedHashSet<String>(input)); }
    public SqlOperation getOperation() { return operation; }
    public Set<String> getTables() { return tables; }
    public Set<String> getColumns() { return columns; }
    public Set<String> getConditionColumns() { return conditionColumns; }
    public boolean isSelectAll() { return selectAll; }
    public boolean isParseSuccess() { return parseSuccess; }
    public String getFailureReason() { return failureReason; }
    public String getNormalizedSql() { return normalizedSql; }
    public String getFingerprint() { return fingerprint; }
}
