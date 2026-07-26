package com.acme.audit.api;

/** One enabled item in the sensitive-data catalog. */
public final class SensitiveColumnMetadata {
    private final String datasourceId, databaseName, schemaName, tableName, columnName, dataCategory, sensitivityLevel, maskingRule;
    private final long version;
    public SensitiveColumnMetadata(String datasourceId, String databaseName, String schemaName, String tableName, String columnName,
                                   String dataCategory, String sensitivityLevel, String maskingRule, long version) {
        this.datasourceId=datasourceId; this.databaseName=databaseName; this.schemaName=schemaName; this.tableName=tableName;
        this.columnName=columnName; this.dataCategory=dataCategory; this.sensitivityLevel=sensitivityLevel; this.maskingRule=maskingRule; this.version=version;
    }
    public String getDatasourceId(){return datasourceId;} public String getDatabaseName(){return databaseName;}
    public String getSchemaName(){return schemaName;} public String getTableName(){return tableName;} public String getColumnName(){return columnName;}
    public String getDataCategory(){return dataCategory;} public String getSensitivityLevel(){return sensitivityLevel;}
    public String getMaskingRule(){return maskingRule;} public long getVersion(){return version;}
}
