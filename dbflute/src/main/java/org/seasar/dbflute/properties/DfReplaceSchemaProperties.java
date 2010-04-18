package org.seasar.dbflute.properties;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.torque.engine.database.model.UnifiedSchema;
import org.seasar.dbflute.exception.DfIllegalPropertySettingException;
import org.seasar.dbflute.exception.DfIllegalPropertyTypeException;
import org.seasar.dbflute.exception.DfRequiredPropertyNotFoundException;
import org.seasar.dbflute.logic.factory.DfUrlAnalyzerFactory;
import org.seasar.dbflute.logic.urlanalyzer.DfUrlAnalyzer;
import org.seasar.dbflute.util.DfStringUtil;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 */
public final class DfReplaceSchemaProperties extends DfAbstractHelperProperties {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Log _log = LogFactory.getLog(DfReplaceSchemaProperties.class);

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfReplaceSchemaProperties(Properties prop) {
        super(prop);
    }

    // ===================================================================================
    //                                                          replaceSchemaDefinitionMap
    //                                                          ==========================
    public static final String KEY_replaceSchemaDefinitionMap = "replaceSchemaDefinitionMap";
    protected Map<String, Object> _replaceSchemaDefinitionMap;

    public Map<String, Object> getReplaceSchemaDefinitionMap() {
        if (_replaceSchemaDefinitionMap == null) {
            _replaceSchemaDefinitionMap = mapProp("torque." + KEY_replaceSchemaDefinitionMap, DEFAULT_EMPTY_MAP);
        }
        return _replaceSchemaDefinitionMap;
    }

    // ===================================================================================
    //                                                                            SQL File
    //                                                                            ========
    public String getReplaceSchemaSqlFile() {
        return "./playsql/replace-schema.sql";
    }

    public String getTakeFinallySqlFile() {
        return "./playsql/take-finally.sql";
    }

    // ===================================================================================
    //                                                                   Data Loading Type
    //                                                                   =================
    public String getDataLoadingType() {
        final String propString = (String) getReplaceSchemaDefinitionMap().get("dataLoadingType");
        if (propString == null) {
            return getOldStyleEnvironmentType();
        }
        return propString;
    }

    protected String getOldStyleEnvironmentType() { // Old Style!
        final String propString = (String) getReplaceSchemaDefinitionMap().get("environmentType");
        if (propString == null) {
            return "ut";
        }
        return propString;
    }

    // ===================================================================================
    //                                                                Filter Variables Map
    //                                                                ====================
    protected Map<String, String> _filterVariablesMap;

    @SuppressWarnings("unchecked")
    protected Map<String, String> getFilterVariablesMap() {
        if (_filterVariablesMap != null) {
            return _filterVariablesMap;
        }
        _filterVariablesMap = (Map<String, String>) getReplaceSchemaDefinitionMap().get("filterVariablesMap");
        if (_filterVariablesMap == null) {
            _filterVariablesMap = new HashMap<String, String>();
        }
        return _filterVariablesMap;
    }

    protected String getFilterVariablesBeginMark() {
        return "/*$";
    }

    protected String getFilterVariablesEndMark() {
        return "*/";
    }

    public String resolveFilterVariablesIfNeeds(String sql) {
        final String beginMark = getFilterVariablesBeginMark();
        final String endMark = getFilterVariablesEndMark();
        final Map<String, String> filterVariablesMap = getFilterVariablesMap();
        if (!filterVariablesMap.isEmpty() && sql.contains(beginMark) && sql.contains(endMark)) {
            final Set<String> keySet = filterVariablesMap.keySet();
            for (String key : keySet) {
                final String variableMark = beginMark + key + endMark;
                if (sql.contains(variableMark)) {
                    final String value = filterVariablesMap.get(key);
                    sql = replaceString(sql, variableMark, value);
                }
            }
        }
        return sql;
    }

    // ===================================================================================
    //                                                                             Logging
    //                                                                             =======
    public boolean isLoggingInsertSql() {
        return isProperty("isLoggingInsertSql", true, getReplaceSchemaDefinitionMap());
    }

    // ===================================================================================
    //                                                                   SQL File Encoding
    //                                                                   =================
    public String getSqlFileEncoding() {
        final String sqlFileEncoding = (String) getReplaceSchemaDefinitionMap().get("sqlFileEncoding");
        if (sqlFileEncoding != null && sqlFileEncoding.trim().length() != 0) {
            return sqlFileEncoding;
        } else {
            return "UTF-8";
        }
    }

    // ===================================================================================
    //                                                                          Skip Sheet
    //                                                                          ==========
    public String getSkipSheet() {
        final String skipSheet = (String) getReplaceSchemaDefinitionMap().get("skipSheet");
        if (skipSheet != null && skipSheet.trim().length() != 0) {
            return skipSheet;
        } else {
            return null;
        }
    }

    // ===================================================================================
    //                                                                  Increment Sequence
    //                                                                  ==================
    public boolean isIncrementSequenceToDataMax() {
        return isProperty("isIncrementSequenceToDataMax", false, getReplaceSchemaDefinitionMap());
    }

    // ===================================================================================
    //                                                               Suppress Batch Update
    //                                                               =====================
    public boolean isSuppressBatchUpdate() {
        return isProperty("isSuppressBatchUpdate", false, getReplaceSchemaDefinitionMap());
    }

    // ===================================================================================
    //                                                                  Drop Generate Only
    //                                                                  ==================
    public boolean isDropGenerateTableOnly() {
        return isProperty("isDropGenerateTableOnly", false, getReplaceSchemaDefinitionMap());
    }

    public boolean isDropGenerateProcedureOnly() {
        return isProperty("isDropGenerateProcedureOnly", false, getReplaceSchemaDefinitionMap());
    }

    // ===================================================================================
    //                                                                     Additional User
    //                                                                     ===============
    protected Map<String, Map<String, String>> _additionalUesrMap;

    public Map<String, Map<String, String>> getAdditionalUserMap() {
        if (_additionalUesrMap != null) {
            return _additionalUesrMap;
        }
        final Object obj = getReplaceSchemaDefinitionMap().get("additionalUserMap");
        if (obj != null && !(obj instanceof Map<?, ?>)) {
            String msg = "The type of the property 'additionalUserMap' should be Map: " + obj;
            throw new DfIllegalPropertyTypeException(msg);
        }
        if (obj == null) {
            _additionalUesrMap = new HashMap<String, Map<String, String>>();
        } else {
            @SuppressWarnings("unchecked")
            final Map<String, Map<String, String>> map = (Map<String, Map<String, String>>) obj;
            _additionalUesrMap = map;
        }
        return _additionalUesrMap;
    }

    protected Map<String, String> getAdditionalUserPropertyMap(String additonalUser) {
        return getAdditionalUserMap().get(additonalUser);
    }

    public Connection createAdditionalUserConnection(String additonalUser) {
        final Map<String, String> propertyMap = getAdditionalUserPropertyMap(additonalUser);
        if (propertyMap == null) {
            return null;
        }
        final String driver = getDatabaseProperties().getDatabaseDriver();
        final String url;
        {
            String property = propertyMap.get("url");
            if (property != null && property.trim().length() > 0) {
                url = property;
            } else {
                url = getDatabaseProperties().getDatabaseUrl();
            }
        }
        final DfUrlAnalyzerFactory factory = new DfUrlAnalyzerFactory(getBasicProperties(), url);
        final DfUrlAnalyzer analyzer = factory.createAnalyzer();
        final String catalog = analyzer.extractCatalog();
        final String schema = propertyMap.get("schema");
        final UnifiedSchema unifiedSchema = UnifiedSchema.createAsDynamicSchema(catalog, schema);
        final String user = propertyMap.get("user");
        final String password = propertyMap.get("password");
        _log.info("...Creating a connection for additional user");
        return createConnection(driver, url, unifiedSchema, user, password);
    }

    // ===================================================================================
    //                                                                     Additional Drop
    //                                                                     ===============
    public List<Map<String, Object>> getAdditionalDropMapList() {
        final Object obj = getReplaceSchemaDefinitionMap().get("additionalDropMapList");
        if (obj == null) {
            return new ArrayList<Map<String, Object>>();
        }
        return castToList(obj, "additionalDropMapList");
    }

    public String getAdditionalDropUrl(Map<String, Object> additionalDropMap) {
        final Object obj = additionalDropMap.get("url");
        if (obj == null) {
            return null;
        }
        return castToString(obj, "additionalDropMapList.url");
    }

    public String getAdditionalDropUser(Map<String, Object> additionalDropMap) {
        final Object obj = additionalDropMap.get("user");
        if (obj == null) {
            return null;
        }
        return castToString(obj, "additionalDropMapList.user");
    }

    public String getAdditionalDropPassword(Map<String, Object> additionalDropMap) {
        final Object obj = additionalDropMap.get("password");
        if (obj == null) {
            return null;
        }
        return castToString(obj, "additionalDropMapList.password");
    }

    @SuppressWarnings("unchecked")
    public Properties getAdditionalDropPropertiesMap(Map<String, Object> additionalDropMap) {
        Object obj = additionalDropMap.get("propertiesMap");
        if (obj == null) {
            return new Properties();
        }
        if (!(obj instanceof Map)) {
            String msg = "The schema should be Map<String, String>:";
            msg = msg + " propertiesMap=" + obj + " type=" + obj.getClass();
            throw new DfIllegalPropertyTypeException(msg);
        }
        final Properties prop = new Properties();
        prop.putAll((Map<String, String>) obj);
        return prop;
    }

    public UnifiedSchema getAdditionalDropSchema(Map<String, Object> additionalDropMap) {
        final String url = getAdditionalDropUrl(additionalDropMap);
        final String catalog;
        if (Srl.is_NotNull_and_NotTrimmedEmpty(url)) {
            final DfUrlAnalyzerFactory factory = new DfUrlAnalyzerFactory(getBasicProperties(), url);
            final DfUrlAnalyzer analyzer = factory.createAnalyzer();
            catalog = analyzer.extractCatalog();
        } else {
            catalog = getDatabaseProperties().getDatabaseCatalog();
        }
        final Object obj = additionalDropMap.get("schema");
        if (obj == null) {
            if (!isDatabaseSchemaCanBeOmitted()) {
                String msg = "The schema is required:";
                msg = msg + " additionalDropMap=" + additionalDropMap;
                throw new DfRequiredPropertyNotFoundException(msg);
            }
            return null;
        }
        final String schema = castToString(obj, "additionalDropMapList.schema");
        final UnifiedSchema unifiedSchema = UnifiedSchema.createAsDynamicSchema(catalog, schema);
        return unifiedSchema;
    }

    protected boolean isDatabaseSchemaCanBeOmitted() {
        return getBasicProperties().isDatabaseAsSchemaCanBeOmitted();
    }

    public List<String> getAdditionalDropObjectTypeList(Map<String, Object> additionalDropMap) {
        Object obj = additionalDropMap.get("objectTypeTargetList");
        if (obj == null) {
            obj = additionalDropMap.get("objectTypeList"); // old style
            if (obj == null) {
                ArrayList<String> defaultList = new ArrayList<String>();
                defaultList.add("TABLE");
                defaultList.add("VIEW");
                return defaultList;
            }
        }
        return castToList(obj, "additionalDropMapList.objectTypeTargetList");
    }

    public List<String> getAdditionalDropTableTargetList(Map<String, Object> additionalDropMap) {
        Object obj = additionalDropMap.get("tableTargetList");
        if (obj == null) {
            return new ArrayList<String>();
        }
        return castToList(obj, "additionalDropMapList.tableTargetList");
    }

    public List<String> getAdditionalDropTableExceptList(Map<String, Object> additionalDropMap) {
        final Object obj = additionalDropMap.get("tableExceptList");
        if (obj == null) {
            return new ArrayList<String>();
        }
        return castToList(obj, "additionalDropMapList.tableExceptList");
    }

    public boolean isAdditionalDropAllTable(Map<String, Object> additionalDropMap) {
        return isProperty("isDropAllTable", false, additionalDropMap);
    }

    public Connection createAdditionalDropConnection(Map<String, Object> additionalDropMap) {
        final String driver = getDatabaseProperties().getDatabaseDriver();
        String url = getAdditionalDropUrl(additionalDropMap);
        url = url != null && url.trim().length() > 0 ? url : getDatabaseProperties().getDatabaseUrl();
        String user = getAdditionalDropUser(additionalDropMap);
        String password;
        if (user != null && user.trim().length() > 0) {
            password = getAdditionalDropPassword(additionalDropMap);
            if (password == null || password.trim().length() == 0) {
                String msg = "The password is required when the user is specified:";
                msg = msg + " user=" + user + " additionalDropMap=" + additionalDropMap;
                throw new DfIllegalPropertySettingException(msg);
            }
        } else {
            user = getDatabaseProperties().getDatabaseUser();
            password = getDatabaseProperties().getDatabasePassword();
        }
        final Properties prop = getAdditionalDropPropertiesMap(additionalDropMap);
        Properties info = new Properties();
        info.putAll(prop);
        info.put("user", user);
        info.put("password", password);
        _log.info("...Creating a connection for additional drop");
        return createConnection(driver, url, getAdditionalDropSchema(additionalDropMap), info);
    }

    // ===================================================================================
    //                                                        Suppress Initializing Schema
    //                                                        ============================
    public boolean isSuppressTruncateTable() {
        return isProperty("isSuppressTruncateTable", false, getReplaceSchemaDefinitionMap());
    }

    public boolean isSuppressDropForeignKey() {
        return isProperty("isSuppressDropForeignKey", false, getReplaceSchemaDefinitionMap());
    }

    public boolean isSuppressDropTable() {
        return isProperty("isSuppressDropTable", false, getReplaceSchemaDefinitionMap());
    }

    public boolean isSuppressDropSequence() {
        return isProperty("isSuppressDropSequence", false, getReplaceSchemaDefinitionMap());
    }

    public boolean isSuppressDropProcedure() {
        return isProperty("isSuppressDropProcedure", false, getReplaceSchemaDefinitionMap());
    }

    public boolean isSuppressDropDBLink() {
        return isProperty("isSuppressDropDBLink", false, getReplaceSchemaDefinitionMap());
    }

    // ===================================================================================
    //                                                                        Other Closet
    //                                                                        ============
    public boolean isErrorContinue() { // It's closet!
        return isProperty("isErrorContinue", true, getReplaceSchemaDefinitionMap());
    }

    /**
     * @return The process command of call-back for before-take-finally. (Nullable)
     */
    public String getBeforeTakeFinally() { // It's closet!
        return (String) getReplaceSchemaDefinitionMap().get("beforeTakeFinally");
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String replaceString(String text, String fromText, String toText) {
        return DfStringUtil.replace(text, fromText, toText);
    }
}