package org.seasar.dbflute.properties;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.seasar.dbflute.exception.DfTableColumnNameNonCompilableConnectorException;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.helper.StringSet;
import org.seasar.dbflute.util.DfCollectionUtil;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 */
public final class DfLittleAdjustmentProperties extends DfAbstractHelperProperties {

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfLittleAdjustmentProperties(Properties prop) {
        super(prop);
    }

    // ===================================================================================
    //                                                               Little Adjustment Map
    //                                                               =====================
    public static final String KEY_littleAdjustmentMap = "littleAdjustmentMap";
    protected Map<String, Object> _littleAdjustmentMap;

    public Map<String, Object> getLittleAdjustmentMap() {
        if (_littleAdjustmentMap == null) {
            _littleAdjustmentMap = mapProp("torque." + KEY_littleAdjustmentMap, DEFAULT_EMPTY_MAP);
        }
        return _littleAdjustmentMap;
    }

    public String getProperty(String key, String defaultValue) {
        return getPropertyIfNotBuildProp(key, defaultValue, getLittleAdjustmentMap());
    }

    public boolean isProperty(String key, boolean defaultValue) {
        return isPropertyIfNotBuildProp(key, defaultValue, getLittleAdjustmentMap());
    }

    // ===================================================================================
    //                                                     Adding Schema to Table SQL Name
    //                                                     ===============================
    public boolean isAvailableAddingSchemaToTableSqlName() {
        return isProperty("isAvailableAddingSchemaToTableSqlName", false);
    }

    public boolean isAvailableAddingCatalogToTableSqlName() {
        return isProperty("isAvailableAddingCatalogToTableSqlName", false);
    }

    // ===================================================================================
    //                                                                 Database Dependency
    //                                                                 ===================
    public boolean isAvailableDatabaseDependency() {
        return isProperty("isAvailableDatabaseDependency", false);
    }
    
    // ===================================================================================
    //                                                                         Native JDBC
    //                                                                         ===========
    public boolean isAvailableDatabaseNativeJDBC() {
        // for example, using oracle.sql.DATE on Oracle gives us best performances
        return isProperty("isAvailableDatabaseNativeJDBC", false);
    }

    // ===================================================================================
    //                                                                            Behavior
    //                                                                            ========
    public boolean isAvailableNonPrimaryKeyWritable() {
        return isProperty("isAvailableNonPrimaryKeyWritable", false);
    }

    // ===================================================================================
    //                                                                      Classification
    //                                                                      ==============
    public boolean isCheckSelectedClassification() {
        return isProperty("isCheckSelectedClassification", false);
    }

    public boolean isForceClassificationSetting() {
        return isProperty("isForceClassificationSetting", false);
    }

    public boolean isCDefToStringReturnsName() { // It's closet!
        return isProperty("isCDefToStringReturnsName", false);
    }

    public boolean isMakeEntityOldStyleClassify() { // It's closet!
        return isProperty("isMakeEntityOldStyleClassify", true);
    }

    // ===================================================================================
    //                                                                              Entity
    //                                                                              ======
    public boolean isMakeEntityChaseRelation() {
        return isProperty("isMakeEntityChaseRelation", false);
    }

    public boolean isEntityConvertEmptyStringToNull() {
        return isProperty("isEntityConvertEmptyStringToNull", false);
    }

    // ===================================================================================
    //                                                                      ConditionQuery
    //                                                                      ==============
    public boolean isMakeConditionQueryEqualEmptyString() {
        return isProperty("isMakeConditionQueryEqualEmptyString", false);
    }

    public boolean isMakeConditionQueryNotEqualAsStandard() { // It's closet!
        // DBFlute had used tradition for a long time
        // but default value is true (uses standard) since 0.9.7.2
        return isProperty("isMakeConditionQueryNotEqualAsStandard", true);
    }

    public String getConditionQueryNotEqualDefinitionName() {
        // for AbstractConditionQuery's definition name
        return isMakeConditionQueryNotEqualAsStandard() ? "CK_NES" : "CK_NET";
    }

    // ===================================================================================
    //                                                                     Make Deprecated
    //                                                                     ===============
    public boolean isMakeDeprecated() {
        return isProperty("isMakeDeprecated", false);
    }

    public boolean isMakeRecentlyDeprecated() {
        return isProperty("isMakeRecentlyDeprecated", true);
    }

    // ===================================================================================
    //                                                                  Extended Component
    //                                                                  ==================
    public boolean hasExtendedImplementedInvokerAssistantClass() {
        String str = getExtendedImplementedInvokerAssistantClass();
        return str != null && str.trim().length() > 0 && !str.trim().equals("null");
    }

    public String getExtendedImplementedInvokerAssistantClass() { // Java Only
        return getProperty("extendedImplementedInvokerAssistantClass", null);
    }

    public boolean hasExtendedImplementedCommonColumnAutoSetupperClass() {
        String str = getExtendedImplementedCommonColumnAutoSetupperClass();
        return str != null && str.trim().length() > 0 && !str.trim().equals("null");
    }

    public String getExtendedImplementedCommonColumnAutoSetupperClass() { // Java Only
        return getProperty("extendedImplementedCommonColumnAutoSetupperClass", null);
    }

    public boolean hasExtendedS2DaoSettingClassValid() {
        String str = getExtendedS2DaoSettingClass();
        return str != null && str.trim().length() > 0 && !str.trim().equals("null");
    }

    public String getExtendedS2DaoSettingClass() { // CSharp Only
        return getProperty("extendedS2DaoSettingClass", null);
    }

    // ===================================================================================
    //                                                                          Short Char
    //                                                                          ==========
    public boolean isShortCharHandlingValid() {
        return !getShortCharHandlingMode().equalsIgnoreCase("NONE");
    }

    public String getShortCharHandlingMode() {
        String property = getProperty("shortCharHandlingMode", "NONE");
        return property.toUpperCase();
    }

    public String getShortCharHandlingModeCode() {
        return getShortCharHandlingMode().substring(0, 1);
    }

    // ===================================================================================
    //                                                                               Quote
    //                                                                               =====
    // -----------------------------------------------------
    //                                                 Table
    //                                                 -----
    protected Set<String> _quoteTableNameSet;

    protected Set<String> getQuoteTableNameSet() { // It's closet!
        if (_quoteTableNameSet != null) {
            return _quoteTableNameSet;
        }
        final Map<String, Object> littleAdjustmentMap = getLittleAdjustmentMap();
        final Object obj = littleAdjustmentMap.get("quoteTableNameList");
        if (obj == null) {
            return new HashSet<String>();
        }
        final List<String> list = castToList(obj, "littleAdjustmentMap.quoteTableNameList");
        _quoteTableNameSet = StringSet.createAsFlexible();
        _quoteTableNameSet.addAll(list);
        return _quoteTableNameSet;
    }

    public boolean isQuoteTable(String tableName) {
        return getQuoteTableNameSet().contains(tableName);
    }

    public String quoteTableNameIfNeeds(String tableName) {
        return quoteTableNameIfNeeds(tableName, false);
    }

    public String quoteTableNameIfNeeds(String tableName, boolean directUse) {
        if (!isQuoteTable(tableName) && !containsNonCompilableConnector(tableName)) {
            return tableName;
        }
        return doQuoteName(tableName, directUse);
    }

    // -----------------------------------------------------
    //                                                Column
    //                                                ------
    // *basically unsupported about column's quotation
    public boolean isQuoteColumn(String tableName) { // non property
        return false; // fixed
    }

    public String quoteColumnNameIfNeeds(String columnName) { // non property
        return quoteColumnNameIfNeeds(columnName, false);
    }

    public String quoteColumnNameIfNeeds(String columnName, boolean directUse) {
        if (!isQuoteColumn(columnName) && !containsNonCompilableConnector(columnName)) {
            return columnName;
        }
        return doQuoteName(columnName, directUse);
    }

    // -----------------------------------------------------
    //                                                 Quote
    //                                                 -----
    protected String doQuoteName(String name, boolean directUse) {
        final String beginQuote;
        final String endQuote;
        if (getBasicProperties().isDatabaseSQLServer()) {
            beginQuote = "[";
            endQuote = "]";
        } else {
            beginQuote = directUse ? "\"" : "\\\"";
            endQuote = beginQuote;
        }
        return beginQuote + name + endQuote;
    }

    // -----------------------------------------------------
    //                                        Non Compilable
    //                                        --------------
    public boolean isSuppressNonCompilableConnectorLimiter() { // It's closet
        return isProperty("isSuppressNonCompilableConnectorLimiter", false);
    }

    public String filterJavaNameNonCompilableConnector(String javaName, NonCompilableChecker checker) {
        checkNonCompilableConnector(checker.name(), checker.disp());
        final List<String> connectorList = getNonCompilableConnectorList();
        for (String connector : connectorList) {
            javaName = Srl.replace(javaName, connector, "_");
        }
        return javaName;
    }

    public static interface NonCompilableChecker {
        String name();

        String disp();
    }

    public void checkNonCompilableConnector(String name, String disp) {
        if (isSuppressNonCompilableConnectorLimiter()) {
            return;
        }
        if (containsNonCompilableConnector(name)) {
            final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
            br.addNotice("Non-compilable connectors in a table/column name were found.");
            br.addItem("Advice");
            br.addElement("Non-compilable connectors are unsupported.");
            br.addElement("For example, 'HYPHEN-TABLE' and 'SPACE COLUMN' and so on...");
            br.addElement("You should change the names like this:");
            br.addElement("  'HYPHEN-TABLE' -> HYPHEN_TABLE");
            br.addElement("  'SPACE COLUMN' -> SPACE_COLUMN");
            br.addElement("");
            br.addElement("If you cannot change by any possibility, you can suppress its limiter.");
            br.addElement(" -> isSuppressNonCompilableConnectorLimiter in littleAdjustmentMap.dfprop.");
            br.addElement("However several functions may not work. It's a restriction.");
            br.addItem("Target Object");
            br.addElement(disp);
            final String msg = br.buildExceptionMessage();
            throw new DfTableColumnNameNonCompilableConnectorException(msg);
        }
    }

    protected boolean containsNonCompilableConnector(String tableName) {
        final List<String> connectorList = getNonCompilableConnectorList();
        return Srl.containsAny(tableName, connectorList.toArray(new String[] {}));
    }

    protected List<String> getNonCompilableConnectorList() {
        return DfCollectionUtil.newArrayList("-", " "); // non property
    }

    // ===================================================================================
    //                                                                          Value Type
    //                                                                          ==========
    // S2Dao.NET does not implement ValueType attribute,
    // so this property is INVALID now. At the future,
    // DBFlute may implement ValueType Framework. 
    public boolean isUseAnsiStringTypeToNotUnicode() { // It's closet! CSharp Only
        return isProperty("isUseAnsiStringTypeToNotUnicode", false);
    }

    // ===================================================================================
    //                                                                   Alternate Control
    //                                                                   =================
    public boolean isAlternateGenerateControlValid() {
        final String str = getAlternateGenerateControl();
        return str != null && str.trim().length() > 0 && !str.trim().equals("null");
    }

    public String getAlternateGenerateControl() { // It's closet!
        return getProperty("alternateGenerateControl", null);
    }

    public boolean isAlternateSql2EntityControlValid() {
        final String str = getAlternateSql2EntityControl();
        return str != null && str.trim().length() > 0 && !str.trim().equals("null");
    }

    public String getAlternateSql2EntityControl() { // It's closet!
        return getProperty("alternateSql2EntityControl", null);
    }

    // ===================================================================================
    //                                                                       Stop Generate
    //                                                                       =============
    public boolean isStopGenerateExtendedBhv() { // It's closet and secret!
        return isProperty("isStopGenerateExtendedBhv", false);
    }

    public boolean isStopGenerateExtendedDao() { // It's closet and secret!
        return isProperty("isStopGenerateExtendedDao", false);
    }

    public boolean isStopGenerateExtendedEntity() { // It's closet and secret!
        return isProperty("isStopGenerateExtendedEntity", false);
    }

    // ===================================================================================
    //                                                              Delete Old Table Class
    //                                                              ======================
    public boolean isDeleteOldTableClass() { // It's closet and internal!
        // The default value is true since 0.8.8.1.
        return isProperty("isDeleteOldTableClass", true);
    }

    // ===================================================================================
    //                                                          Skip Generate If Same File
    //                                                          ==========================
    public boolean isSkipGenerateIfSameFile() { // It's closet and internal!
        // The default value is true since 0.7.8.
        return isProperty("isSkipGenerateIfSameFile", true);
    }

    // ===================================================================================
    //                                              ToLower in Generator Underscore Method
    //                                              ======================================
    public boolean isAvailableToLowerInGeneratorUnderscoreMethod() { // It's closet and internal!
        return isProperty("isAvailableToLowerInGeneratorUnderscoreMethod", true);
    }

    // ===================================================================================
    //                                                                      Flat Expansion
    //                                                                      ==============
    public boolean isMakeFlatExpansion() { // It's closet until review!
        return isProperty("isMakeFlatExpansion", false);
    }

    // ===================================================================================
    //                                                                               S2Dao
    //                                                                               =====
    public boolean isMakeDaoInterface() { // It's closet! CSharp Only
        if (isTargetLanguageCSharp()) {
            return true; // It is not implemented at CSharp yet
        }
        final boolean makeDaoInterface = booleanProp("torque.isMakeDaoInterface", false);
        if (makeDaoInterface) {
            String msg = "Dao interfaces are unsupported since DBFlute-0.8.7!";
            throw new UnsupportedOperationException(msg);
        }
        return false;
    }

    protected boolean isTargetLanguageCSharp() {
        return getBasicProperties().isTargetLanguageCSharp();
    }

    // ===================================================================================
    //                                                                          Compatible
    //                                                                          ==========
    public boolean isCompatibleAutoMappingOldStyle() { // It's closet!
        return isProperty("isCompatibleAutoMappingOldStyle", false);
    }
}