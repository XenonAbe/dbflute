/*
 * Copyright 2004-2008 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.dbflute.logic.replaceschema.loaddata.impl;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.torque.engine.database.model.TypeMap;
import org.apache.torque.engine.database.model.UnifiedSchema;
import org.seasar.dbflute.DfBuildProperties;
import org.seasar.dbflute.exception.DfJDBCException;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.helper.StringKeyMap;
import org.seasar.dbflute.jdbc.ValueType;
import org.seasar.dbflute.logic.jdbc.handler.DfColumnHandler;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfColumnMetaInfo;
import org.seasar.dbflute.properties.DfBasicProperties;
import org.seasar.dbflute.s2dao.valuetype.TnValueTypes;
import org.seasar.dbflute.util.DfCollectionUtil;
import org.seasar.dbflute.util.DfSystemUtil;
import org.seasar.dbflute.util.DfTypeUtil;
import org.seasar.dbflute.util.Srl;
import org.seasar.dbflute.util.DfTypeUtil.ParseBooleanException;
import org.seasar.dbflute.util.DfTypeUtil.ParseTimeException;
import org.seasar.dbflute.util.DfTypeUtil.ParseTimestampException;

/**
 * @author jflute
 * @since 0.9.4 (2009/03/25 Wednesday)
 */
public abstract class DfAbsractDataWriter {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The data source. (NotNull) */
    protected DataSource _dataSource;

    /** The unified schema (for getting database meta data). (Nullable) */
    protected UnifiedSchema _unifiedSchema;

    /** Does it output the insert SQLs as logging? */
    protected boolean _loggingInsertSql;

    /** Does it suppress batch updates? */
    protected boolean _suppressBatchUpdate;

    /** The handler of columns for getting column meta information(as helper). */
    protected final DfColumnHandler _columnHandler = new DfColumnHandler();

    /** The cache map of meta info. The key is table name. (ordered for display) */
    protected final Map<String, Map<String, DfColumnMetaInfo>> _metaInfoCacheMap = StringKeyMap
            .createAsFlexibleOrdered();

    /** The cache map of bind type. The key is table name. (ordered for display) */
    protected final Map<String, Map<String, Class<?>>> _bindTypeCacheMap = StringKeyMap.createAsFlexibleOrdered();

    /** The cache map of string processor. The key is table name. (ordered for display) */
    protected final Map<String, Map<String, StringProcessor>> _stringProcessorCacheMap = StringKeyMap
            .createAsFlexibleOrdered();

    /** The definition list of string processor instances. (NotNull, ReadOnly) */
    protected final List<StringProcessor> _stringProcessorList = DfCollectionUtil.newArrayList();
    {
        // order has meaning if meta information does not exist
        // (but basically (always) meta information exists)
        _stringProcessorList.add(new DateStringProcessor());
        _stringProcessorList.add(new BooleanStringProcessor());
        _stringProcessorList.add(new NumberStringProcessor());
        _stringProcessorList.add(new UUIDStringProcessor());
        _stringProcessorList.add(new ArrayStringProcessor());
        _stringProcessorList.add(new XmlStringProcessor());
        _stringProcessorList.add(new RealStringProcessor());
    }

    /** The cache map of null type. The key is table name. (ordered for display) */
    protected final Map<String, Map<String, Integer>> _nullTypeCacheMap = StringKeyMap.createAsFlexibleOrdered();

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfAbsractDataWriter(DataSource dataSource) {
        _dataSource = dataSource;
    }

    // ===================================================================================
    //                                                                     Process Binding
    //                                                                     ===============
    // -----------------------------------------------------
    //                                            Null Value
    //                                            ----------
    protected boolean processNull(String tableName, String columnName, Object value, PreparedStatement ps,
            int bindCount, Map<String, DfColumnMetaInfo> columnMetaInfoMap) throws SQLException {
        if (!isNullValue(value)) {
            return false;
        }
        Map<String, Integer> cacheMap = _nullTypeCacheMap.get(tableName);
        if (cacheMap == null) {
            cacheMap = StringKeyMap.createAsFlexibleOrdered();
            _nullTypeCacheMap.put(tableName, cacheMap);
        }
        final Integer cachedType = cacheMap.get(columnName);
        if (cachedType != null) { // cache hit
            ps.setNull(bindCount, cachedType); // basically no exception
            return true;
        }
        final DfColumnMetaInfo columnMetaInfo = columnMetaInfoMap.get(columnName);
        if (columnMetaInfo != null) {
            // use mapped type at first
            final String mappedJdbcType = _columnHandler.getColumnJdbcType(columnMetaInfo);
            final Integer mappedJdbcDefValue = TypeMap.getJdbcDefValueByJdbcType(mappedJdbcType);
            try {
                ps.setNull(bindCount, mappedJdbcDefValue);
                cacheMap.put(columnName, mappedJdbcDefValue);
            } catch (SQLException e) {
                // retry by plain type
                final int plainJdbcDefValue = columnMetaInfo.getJdbcDefValue();
                try {
                    ps.setNull(bindCount, plainJdbcDefValue);
                    cacheMap.put(columnName, plainJdbcDefValue);
                } catch (SQLException ignored) {
                    final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
                    br.addNotice("Failed to execute setNull(bindCount, jdbcDefValue).");
                    br.addItem("Column");
                    br.addElement(tableName + "." + columnName);
                    br.addElement(columnMetaInfo.toString());
                    br.addItem("Mapped JDBC Type");
                    br.addElement(mappedJdbcType);
                    br.addItem("First JDBC Def-Value");
                    br.addElement(mappedJdbcDefValue);
                    br.addItem("Retry JDBC Def-Value");
                    br.addElement(plainJdbcDefValue);
                    br.addItem("Retry Message");
                    br.addElement(ignored.getMessage());
                    String msg = br.buildExceptionMessage();
                    throw new DfJDBCException(msg, e);
                }
            }
        } else { // basically no way
            Integer tryType = Types.VARCHAR; // as default
            try {
                ps.setNull(bindCount, tryType);
                cacheMap.put(columnName, tryType);
            } catch (SQLException e) {
                tryType = Types.NUMERIC;
                try {
                    ps.setNull(bindCount, tryType);
                    cacheMap.put(columnName, tryType);
                } catch (SQLException ignored) {
                    tryType = Types.TIMESTAMP;
                    try {
                        ps.setNull(bindCount, tryType);
                        cacheMap.put(columnName, tryType);
                    } catch (SQLException iignored) {
                        tryType = Types.OTHER;
                        try {
                            ps.setNull(bindCount, tryType); // last try
                            cacheMap.put(columnName, tryType);
                        } catch (SQLException iiignored) {
                            throw e;
                        }
                    }
                }
            }
        }
        return true;
    }

    protected boolean isNullValue(Object value) {
        return value == null;
    }

    // -----------------------------------------------------
    //                                     NotNull NotString
    //                                     -----------------
    protected boolean processNotNullNotString(String tableName, String columnName, Object obj, Connection conn,
            PreparedStatement ps, int bindCount, Map<String, DfColumnMetaInfo> columnMetaInfoMap) throws SQLException {
        if (!isNotNullNotString(obj)) {
            return false;
        }
        final DfColumnMetaInfo columnMetaInfo = columnMetaInfoMap.get(columnName);
        if (columnMetaInfo != null) {
            final Class<?> columnType = getBindType(tableName, columnMetaInfo);
            if (columnType != null) {
                bindNotNullValueByColumnType(tableName, columnName, conn, ps, bindCount, obj, columnType);
                return true;
            }
        }
        bindNotNullValueByInstance(tableName, columnName, conn, ps, bindCount, obj);
        return true;
    }

    protected boolean isNotNullNotString(Object obj) {
        return obj != null && !(obj instanceof String);
    }

    // -----------------------------------------------------
    //                                        NotNull String
    //                                        --------------
    protected void processNotNullString(String tableName, String columnName, String value, Connection conn,
            PreparedStatement ps, int bindCount, Map<String, DfColumnMetaInfo> columnMetaInfoMap) throws SQLException {
        if (value == null) {
            String msg = "This method is only for NotNull and StringExpression:";
            msg = msg + " value=" + value + " type=" + (value != null ? value.getClass() : "null");
            throw new IllegalStateException(msg);
        }
        value = Srl.unquoteDouble(value);
        Map<String, StringProcessor> cacheMap = _stringProcessorCacheMap.get(tableName);
        if (cacheMap == null) {
            cacheMap = StringKeyMap.createAsFlexibleOrdered();
            _stringProcessorCacheMap.put(tableName, cacheMap);
        }
        final StringProcessor processor = cacheMap.get(columnName);
        if (processor != null) { // cache hit
            final boolean processed = processor.process(tableName, columnName, value, conn, ps, bindCount,
                    columnMetaInfoMap);
            if (!processed) {
                throwColumnValueProcessingFailureException(processor, tableName, columnName, value);
            }
            return;
        }
        for (StringProcessor tryProcessor : _stringProcessorList) {
            // processing and searching target processor
            if (tryProcessor.process(tableName, columnName, value, conn, ps, bindCount, columnMetaInfoMap)) {
                cacheMap.put(columnName, tryProcessor); // use cache next times
                break;
            }
        }
    }

    protected void throwColumnValueProcessingFailureException(StringProcessor processor, String tableName,
            String columnName, String value) throws SQLException {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The column value could not be treated by the processor.");
        br.addItem("Advice");
        br.addElement("The column has string expressions judging the type of the column");
        br.addElement("by analyzing the value of first record.");
        br.addElement("But the value of second or more record did not match the type.");
        br.addElement("So confirm your expressions.");
        br.addItem("Table Name");
        br.addElement(tableName);
        br.addItem("Column Name");
        br.addElement(columnName);
        br.addItem("String Expression");
        br.addElement(value);
        br.addItem("Processor");
        br.addElement(processor);
        final String msg = br.buildExceptionMessage();
        throw new DfJDBCException(msg);
    }

    public static interface StringProcessor {
        boolean process(String tableName, String columnName, String value, Connection conn, PreparedStatement ps,
                int bindCount, Map<String, DfColumnMetaInfo> columnMetaInfoMap) throws SQLException;
    }

    protected class DateStringProcessor implements StringProcessor {

        public boolean process(String tableName, String columnName, String value, Connection conn,
                PreparedStatement ps, int bindCount, Map<String, DfColumnMetaInfo> columnMetaInfoMap)
                throws SQLException {
            return processDate(tableName, columnName, value, conn, ps, bindCount, columnMetaInfoMap);
        }

        @Override
        public String toString() {
            return buildProcessorToString(this);
        }
    }

    protected class BooleanStringProcessor implements StringProcessor {

        public boolean process(String tableName, String columnName, String value, Connection conn,
                PreparedStatement ps, int bindCount, Map<String, DfColumnMetaInfo> columnMetaInfoMap)
                throws SQLException {
            return processBoolean(tableName, columnName, value, conn, ps, bindCount, columnMetaInfoMap);
        }

        @Override
        public String toString() {
            return buildProcessorToString(this);
        }
    }

    protected class NumberStringProcessor implements StringProcessor {

        public boolean process(String tableName, String columnName, String value, Connection conn,
                PreparedStatement ps, int bindCount, Map<String, DfColumnMetaInfo> columnMetaInfoMap)
                throws SQLException {
            return processNumber(tableName, columnName, value, conn, ps, bindCount, columnMetaInfoMap);
        }

        @Override
        public String toString() {
            return buildProcessorToString(this);
        }
    }

    protected class UUIDStringProcessor implements StringProcessor {

        public boolean process(String tableName, String columnName, String value, Connection conn,
                PreparedStatement ps, int bindCount, Map<String, DfColumnMetaInfo> columnMetaInfoMap)
                throws SQLException {
            return processUUID(tableName, columnName, value, conn, ps, bindCount, columnMetaInfoMap);
        }

        @Override
        public String toString() {
            return buildProcessorToString(this);
        }
    }

    protected class ArrayStringProcessor implements StringProcessor {

        public boolean process(String tableName, String columnName, String value, Connection conn,
                PreparedStatement ps, int bindCount, Map<String, DfColumnMetaInfo> columnMetaInfoMap)
                throws SQLException {
            return processArray(tableName, columnName, value, ps, bindCount, columnMetaInfoMap);
        }

        @Override
        public String toString() {
            return buildProcessorToString(this);
        }
    }

    protected class XmlStringProcessor implements StringProcessor {

        public boolean process(String tableName, String columnName, String value, Connection conn,
                PreparedStatement ps, int bindCount, Map<String, DfColumnMetaInfo> columnMetaInfoMap)
                throws SQLException {
            return processXml(tableName, columnName, value, ps, bindCount, columnMetaInfoMap);
        }

        @Override
        public String toString() {
            return buildProcessorToString(this);
        }
    }

    protected class RealStringProcessor implements StringProcessor {

        public boolean process(String tableName, String columnName, String value, Connection conn,
                PreparedStatement ps, int bindCount, Map<String, DfColumnMetaInfo> columnMetaInfoMap)
                throws SQLException {
            ps.setString(bindCount, value);
            return true;
        }

        @Override
        public String toString() {
            return buildProcessorToString(this);
        }
    }

    protected String buildProcessorToString(StringProcessor processor) {
        return DfTypeUtil.toClassTitle(processor);
    }

    // -----------------------------------------------------
    //                                                  Date
    //                                                  ----
    protected boolean processDate(String tableName, String columnName, String value, Connection conn,
            PreparedStatement ps, int bindCount, Map<String, DfColumnMetaInfo> columnMetaInfoMap) throws SQLException {
        if (value == null) {
            return false; // basically no way
        }
        final DfColumnMetaInfo columnMetaInfo = columnMetaInfoMap.get(columnName);
        if (columnMetaInfo != null) {
            final Class<?> columnType = getBindType(tableName, columnMetaInfo);
            if (columnType != null) {
                if (!java.util.Date.class.isAssignableFrom(columnType)) {
                    return false;
                }
                bindNotNullValueByColumnType(tableName, columnName, conn, ps, bindCount, value, columnType);
                return true;
            }
        }
        try {
            Timestamp timestamp = DfTypeUtil.toTimestamp(value);
            ps.setTimestamp(bindCount, timestamp);
            return true;
        } catch (ParseTimestampException ignored) {
            // retry as time
            try {
                Time time = DfTypeUtil.toTime(value);
                ps.setTime(bindCount, time);
                return true;
            } catch (ParseTimeException ignored2) {
            }
            return false; // couldn't parse as timestamp and time
        }
    }

    // -----------------------------------------------------
    //                                               Boolean
    //                                               -------
    protected boolean processBoolean(String tableName, String columnName, String value, Connection conn,
            PreparedStatement ps, int bindCount, Map<String, DfColumnMetaInfo> columnMetaInfoMap) throws SQLException {
        if (value == null) {
            return false; // basically no way
        }
        final DfColumnMetaInfo columnMetaInfo = columnMetaInfoMap.get(columnName);
        if (columnMetaInfo != null) {
            final Class<?> columnType = getBindType(tableName, columnMetaInfo);
            if (columnType != null) {
                if (!Boolean.class.isAssignableFrom(columnType)) {
                    return false;
                }
                bindNotNullValueByColumnType(tableName, columnName, conn, ps, bindCount, value, columnType);
                return true;
            }
        }
        try {
            Boolean booleanValue = DfTypeUtil.toBoolean(value);
            ps.setBoolean(bindCount, booleanValue);
            return true;
        } catch (ParseBooleanException ignored) {
            return false; // couldn't parse as boolean
        }
    }

    // -----------------------------------------------------
    //                                                Number
    //                                                ------
    protected boolean processNumber(String tableName, String columnName, String value, Connection conn,
            PreparedStatement ps, int bindCount, Map<String, DfColumnMetaInfo> columnMetaInfoMap) throws SQLException {
        if (value == null) {
            return false; // basically no way
        }
        final DfColumnMetaInfo columnMetaInfo = columnMetaInfoMap.get(columnName);
        if (columnMetaInfo != null) {
            final Class<?> columnType = getBindType(tableName, columnMetaInfo);
            if (columnType != null) {
                if (!Number.class.isAssignableFrom(columnType)) {
                    return false;
                }
                bindNotNullValueByColumnType(tableName, columnName, conn, ps, bindCount, value, columnType);
                return true;
            }
        }
        value = filterBigDecimalValue(value);
        if (!isBigDecimalValue(value)) {
            return false;
        }
        final BigDecimal bigDecimalValue = getBigDecimalValue(columnName, value);
        try {
            final long longValue = bigDecimalValue.longValueExact();
            ps.setLong(bindCount, longValue);
            return true;
        } catch (ArithmeticException e) {
            ps.setBigDecimal(bindCount, bigDecimalValue);
            return true;
        }
    }

    protected String filterBigDecimalValue(String value) {
        if (value == null) {
            return null;
        }
        value = value.trim();
        return value;
    }

    protected boolean isBigDecimalValue(String value) {
        if (value == null) {
            return false;
        }
        try {
            new BigDecimal(value);
            return true;
        } catch (NumberFormatException e) {
        }
        return false;
    }

    protected BigDecimal getBigDecimalValue(String columnName, String value) {
        try {
            return new BigDecimal(value);
        } catch (RuntimeException e) {
            String msg = "The value should be big decimal: ";
            msg = msg + " columnName=" + columnName + " value=" + value;
            throw new IllegalStateException(msg, e);
        }
    }

    // -----------------------------------------------------
    //                                                  UUID
    //                                                  ----
    protected boolean processUUID(String tableName, String columnName, String value, Connection conn,
            PreparedStatement ps, int bindCount, Map<String, DfColumnMetaInfo> columnMetaInfoMap) throws SQLException {
        if (value == null) {
            return false; // basically no way
        }
        final DfColumnMetaInfo columnMetaInfo = columnMetaInfoMap.get(columnName);
        if (columnMetaInfo != null) {
            final Class<?> columnType = getBindType(tableName, columnMetaInfo);
            if (columnType != null) {
                if (!UUID.class.isAssignableFrom(columnType)) {
                    return false;
                }
                bindNotNullValueByColumnType(tableName, columnName, conn, ps, bindCount, value, columnType);
                return true;
            }
        }
        // unsupported when meta information does not exist
        return false;
    }

    protected String filterUUIDValue(String value) {
        if (value == null) {
            return null;
        }
        value = value.trim();
        return value;
    }

    // -----------------------------------------------------
    //                                                 ARRAY
    //                                                 -----
    protected boolean processArray(String tableName, String columnName, String value, PreparedStatement ps,
            int bindCount, Map<String, DfColumnMetaInfo> columnMetaInfoMap) throws SQLException {
        if (value == null) {
            return false; // basically no way
        }
        final DfColumnMetaInfo columnMetaInfo = columnMetaInfoMap.get(columnName);
        if (columnMetaInfo != null) {
            if (getBasicProperties().isDatabasePostgreSQL()) {
                //rsMeta#getColumnTypeName() returns value starts with "_" if
                //rsMeta#getColumnType() returns Types.ARRAY in PostgreSQL.
                //  e.g. UUID[] -> _uuid
                final int jdbcDefValue = columnMetaInfo.getJdbcDefValue();
                final String dbTypeName = columnMetaInfo.getDbTypeName();
                if (jdbcDefValue != Types.ARRAY || !dbTypeName.startsWith("_")) {
                    return false;
                }
                value = filterArrayValue(value);
                ps.setObject(bindCount, value, Types.OTHER);
                return true;
            }
        }
        // unsupported when meta information does not exist
        return false;
    }

    protected String filterArrayValue(String value) {
        if (value == null) {
            return null;
        }
        value = value.trim();
        return value;
    }

    // -----------------------------------------------------
    //                                                   XML
    //                                                   ---
    protected boolean processXml(String tableName, String columnName, String value, PreparedStatement ps,
            int bindCount, Map<String, DfColumnMetaInfo> columnMetaInfoMap) throws SQLException {
        if (value == null) {
            return false; // basically no way
        }
        final DfColumnMetaInfo columnMetaInfo = columnMetaInfoMap.get(columnName);
        if (columnMetaInfo != null) {
            if (getBasicProperties().isDatabasePostgreSQL()) {
                final String dbTypeName = columnMetaInfo.getDbTypeName();
                if (!dbTypeName.startsWith("xml")) {
                    return false;
                }
                value = filterXmlValue(value);
                ps.setObject(bindCount, value, Types.OTHER);
                return true;
            }
        }
        // unsupported when meta information does not exist
        return false;
    }

    protected String filterXmlValue(String value) {
        if (value == null) {
            return null;
        }
        value = value.trim();
        return value;
    }

    // ===================================================================================
    //                                                                          Bind Value
    //                                                                          ==========
    /**
     * Bind not null value by bind type of column. <br />
     * This contains type conversion of value.
     * @param tableName The name of table. (NotNull)
     * @param columnName The name of column. (NotNull)
     * @param conn The connection for the database. (NotNull)
     * @param ps The prepared statement. (NotNull)
     * @param bindCount The count of binding.
     * @param value The bound value. (NotNull)
     * @param bindType The bind type of the column. (NotNull)
     * @throws SQLException
     */
    protected void bindNotNullValueByColumnType(String tableName, String columnName, Connection conn,
            PreparedStatement ps, int bindCount, Object value, Class<?> bindType) throws SQLException {
        final ValueType valueType = TnValueTypes.getValueType(bindType);
        try {
            valueType.bindValue(conn, ps, bindCount, value);
        } catch (SQLException e) {
            String msg = "Look! Read the message below." + ln();
            msg = msg + "/- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -" + ln();
            msg = msg + "Failed to bind the value!" + ln();
            msg = msg + ln();
            msg = msg + "[Column]" + ln() + tableName + "." + columnName + ln();
            msg = msg + ln();
            msg = msg + "[Bind Type]" + ln() + bindType + ln();
            msg = msg + ln();
            msg = msg + "[Value Type]" + ln() + valueType + ln();
            msg = msg + ln();
            msg = msg + "[Bound Value]" + ln() + value + ln();
            msg = msg + "- - - - - - - - - -/";
            throw new DfJDBCException(msg, e);
        }
    }

    /**
     * Bind not null value by instance.
     * @param ps The prepared statement. (NotNull)
     * @param bindCount The count of binding.
     * @param obj The bound value. (NotNull)
     * @throws SQLException
     */
    protected void bindNotNullValueByInstance(String tableName, String columnName, Connection conn,
            PreparedStatement ps, int bindCount, Object obj) throws SQLException {
        bindNotNullValueByColumnType(tableName, columnName, conn, ps, bindCount, obj, obj.getClass());
    }

    // ===================================================================================
    //                                                                    Column Bind Type
    //                                                                    ================
    /**
     * Get the bind type to find a value type.
     * @param columnMetaInfo The meta information of column. (NotNull)
     * @return The type of column. (Nullable: However Basically NotNull)
     */
    protected Class<?> getBindType(String tableName, DfColumnMetaInfo columnMetaInfo) {
        Map<String, Class<?>> cacheMap = _bindTypeCacheMap.get(tableName);
        if (cacheMap == null) {
            cacheMap = StringKeyMap.createAsFlexibleOrdered();
            _bindTypeCacheMap.put(tableName, cacheMap);
        }
        final String columnName = columnMetaInfo.getColumnName();
        Class<?> bindType = cacheMap.get(columnName);
        if (bindType != null) { // cache hit
            return bindType;
        }

        // use mapped JDBC defined value if found (basically found)
        // because it has already been resolved about JDBC specification per DBMS
        final String jdbcType = _columnHandler.getColumnJdbcType(columnMetaInfo);
        Integer jdbcDefValue = TypeMap.getJdbcDefValueByJdbcType(jdbcType);
        if (jdbcDefValue == null) { // basically no way
            jdbcDefValue = columnMetaInfo.getJdbcDefValue(); // as plain
        }

        // ReplaceSchema uses an own original mapping way
        // (not uses Generate mapping)
        // it's simple mapping (for string processor)
        if (jdbcDefValue == Types.CHAR || jdbcDefValue == Types.VARCHAR || jdbcDefValue == Types.LONGVARCHAR
                || jdbcDefValue == Types.CLOB) {
            bindType = String.class;
        } else if (jdbcDefValue == Types.TINYINT || jdbcDefValue == Types.SMALLINT || jdbcDefValue == Types.INTEGER) {
            bindType = Integer.class;
        } else if (jdbcDefValue == Types.BIGINT) {
            bindType = Long.class;
        } else if (jdbcDefValue == Types.DECIMAL || jdbcDefValue == Types.NUMERIC) {
            bindType = BigDecimal.class;
        } else if (jdbcDefValue == Types.TIMESTAMP) {
            bindType = Timestamp.class;
        } else if (jdbcDefValue == Types.TIME) {
            bindType = Time.class;
        } else if (jdbcDefValue == Types.DATE) {
            // it depends on value type settings
            // that which is bound java.sql.Date or java.sql.Timestamp
            bindType = java.util.Date.class;
        } else if (jdbcDefValue == Types.BIT || jdbcDefValue == Types.BOOLEAN) {
            bindType = Boolean.class;
        } else if (jdbcDefValue == Types.OTHER && TypeMap.UUID.equalsIgnoreCase(jdbcType)) {
            // [UUID Headache]: The reason why UUID type has not been supported yet on JDBC.
            bindType = UUID.class;
        } else {
            bindType = Object.class;
        }
        cacheMap.put(columnName, bindType);
        return bindType;
    }

    // ===================================================================================
    //                                                                    Column Meta Info
    //                                                                    ================
    protected Map<String, DfColumnMetaInfo> getColumnMetaInfo(String tableName) {
        if (_metaInfoCacheMap.containsKey(tableName)) {
            return _metaInfoCacheMap.get(tableName);
        }
        final Map<String, DfColumnMetaInfo> columnMetaInfoMap = StringKeyMap.createAsFlexible();
        Connection conn = null;
        try {
            conn = _dataSource.getConnection();
            final DatabaseMetaData metaData = conn.getMetaData();
            final List<DfColumnMetaInfo> columnList = _columnHandler.getColumnList(metaData, _unifiedSchema, tableName);
            for (DfColumnMetaInfo columnMetaInfo : columnList) {
                columnMetaInfoMap.put(columnMetaInfo.getColumnName(), columnMetaInfo);
            }
            _metaInfoCacheMap.put(tableName, columnMetaInfoMap);
            return columnMetaInfoMap;
        } catch (SQLException e) {
            String msg = "Failed to get column meta informations: table=" + tableName;
            throw new IllegalStateException(msg, e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected DfBuildProperties getProperties() {
        return DfBuildProperties.getInstance();
    }

    protected DfBasicProperties getBasicProperties() {
        return getProperties().getBasicProperties();
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String ln() {
        return DfSystemUtil.getLineSeparator();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public UnifiedSchema getUnifiedSchema() {
        return _unifiedSchema;
    }

    public void setUnifiedSchema(UnifiedSchema unifiedSchema) {
        _unifiedSchema = unifiedSchema;
    }

    public boolean isLoggingInsertSql() {
        return _loggingInsertSql;
    }

    public void setLoggingInsertSql(boolean loggingInsertSql) {
        this._loggingInsertSql = loggingInsertSql;
    }

    public boolean isSuppressBatchUpdate() {
        return _suppressBatchUpdate;
    }

    public void setSuppressBatchUpdate(boolean suppressBatchUpdate) {
        this._suppressBatchUpdate = suppressBatchUpdate;
    }
}
