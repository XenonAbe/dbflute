/*
 * Copyright 2004-2007 the Seasar Foundation and the Others.
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
package org.seasar.dbflute.logic.jdbc.handler;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.torque.engine.database.model.UnifiedSchema;
import org.seasar.dbflute.exception.DfIllegalPropertySettingException;
import org.seasar.dbflute.helper.StringSet;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfPrimaryKeyMetaInfo;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfTableMetaInfo;
import org.seasar.dbflute.util.DfCollectionUtil;

/**
 * @author jflute
 */
public class DfUniqueKeyHandler extends DfAbstractMetaDataHandler {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Log _log = LogFactory.getLog(DfUniqueKeyHandler.class);

    // ===================================================================================
    //                                                                         Primary Key
    //                                                                         ===========
    /**
     * Retrieves an info of the columns composing the primary key for a given table.
     * @param metaData JDBC meta data. (NotNull)
     * @param tableInfo The meta information of table. (NotNull)
     * @return The meta information of primary keys. (NotNull)
     * @throws SQLException
     */
    public DfPrimaryKeyMetaInfo getPrimaryKey(DatabaseMetaData metaData, DfTableMetaInfo tableInfo) throws SQLException {
        final UnifiedSchema unifiedSchema = tableInfo.getUnifiedSchema();
        final String tableName = tableInfo.getTableName();
        return getPrimaryKey(metaData, unifiedSchema, tableName);
    }

    /**
     * Retrieves an info of the columns composing the primary key for a given table.
     * @param metaData JDBC meta data. (NotNull)
     * @param unifiedSchema The unified schema that can contain catalog name and no-name mark. (Nullable)
     * @param tableName The name of table. (NotNull)
     * @return The meta information of primary keys. (NotNull)
     * @throws SQLException
     */
    public DfPrimaryKeyMetaInfo getPrimaryKey(DatabaseMetaData metaData, UnifiedSchema unifiedSchema, String tableName)
            throws SQLException {
        final DfPrimaryKeyMetaInfo info = new DfPrimaryKeyMetaInfo();
        if (isPrimaryKeyExtractingUnsupported()) {
            if (isDatabaseMsAccess()) {
                return processMSAccess(metaData, unifiedSchema, tableName, info);
            }
            return info;
        }
        ResultSet parts = null;
        ResultSet lowerSpare = null;
        ResultSet upperSpare = null;
        try {
            parts = getPrimaryKeyResultSetFromDBMeta(metaData, unifiedSchema, tableName);
            if (parts != null) {
                while (parts.next()) {
                    final String columnName = getPrimaryKeyColumnNameFromDBMeta(parts);
                    final String pkName = getPrimaryKeyNameFromDBMeta(parts);
                    info.addPrimaryKey(columnName, pkName);
                }
            }
            if (!info.hasPrimaryKey()) { // for lower case
                lowerSpare = getPrimaryKeyResultSetFromDBMeta(metaData, unifiedSchema, tableName.toLowerCase());
                if (lowerSpare != null) {
                    while (lowerSpare.next()) {
                        final String columnName = getPrimaryKeyColumnNameFromDBMeta(lowerSpare);
                        final String pkName = getPrimaryKeyNameFromDBMeta(lowerSpare);
                        info.addPrimaryKey(columnName, pkName);
                    }
                }
            }
            if (!info.hasPrimaryKey()) { // for upper case
                upperSpare = getPrimaryKeyResultSetFromDBMeta(metaData, unifiedSchema, tableName.toUpperCase());
                if (upperSpare != null) {
                    while (upperSpare.next()) {
                        final String columnName = getPrimaryKeyColumnNameFromDBMeta(upperSpare);
                        final String pkName = getPrimaryKeyNameFromDBMeta(upperSpare);
                        info.addPrimaryKey(columnName, pkName);
                    }
                }
            }
            // check except columns
            assertPrimaryKeyNotExcepted(info, unifiedSchema, tableName);
        } finally {
            if (parts != null) {
                parts.close();
            }
            if (lowerSpare != null) {
                lowerSpare.close();
            }
            if (upperSpare != null) {
                upperSpare.close();
            }
        }
        return info;
    }

    protected ResultSet getPrimaryKeyResultSetFromDBMeta(DatabaseMetaData dbMeta, UnifiedSchema unifiedSchema,
            String tableName) {
        try {
            final String catalogName = unifiedSchema.getPureCatalog();
            final String schemaName = unifiedSchema.getPureSchema();
            return dbMeta.getPrimaryKeys(catalogName, schemaName, tableName);
        } catch (SQLException ignored) {
            // patch: MySQL throws SQLException when the table was not found
            return null;
        }
    }

    protected String getPrimaryKeyColumnNameFromDBMeta(ResultSet resultSet) throws SQLException {
        return resultSet.getString(4); // COLUMN_NAME
    }

    protected String getPrimaryKeyNameFromDBMeta(ResultSet resultSet) throws SQLException {
        return resultSet.getString(6); // PK_NAME
    }

    protected void assertPrimaryKeyNotExcepted(DfPrimaryKeyMetaInfo info, UnifiedSchema unifiedSchema, String tableName) {
        final List<String> primaryKeyList = info.getPrimaryKeyList();
        for (String primaryKey : primaryKeyList) {
            if (isColumnExcept(unifiedSchema, tableName, primaryKey)) {
                String msg = "PK columns are unsupported on 'columnExcept' property:";
                msg = msg + " unifiedSchema=" + unifiedSchema + " tableName=" + tableName;
                msg = msg + " primaryKey=" + primaryKey;
                throw new DfIllegalPropertySettingException(msg);
            }
        }
    }

    /**
     * @param metaData JDBC meta data. (NotNull)
     * @param unifiedSchema The unified schema. (NotNull)
     * @param tableName The name of table. (NotNull)
     * @param info The empty meta information of primary key. (NotNull)
     * @return The meta information of primary key. (NotNull)
     * @throws SQLException
     */
    protected DfPrimaryKeyMetaInfo processMSAccess(DatabaseMetaData metaData, UnifiedSchema unifiedSchema,
            String tableName, DfPrimaryKeyMetaInfo info) throws SQLException {
        // it can get from unique key from JDBC of MS Access
        final List<String> emptyList = DfCollectionUtil.emptyList();
        final Map<String, Map<Integer, String>> uqMap = getUniqueKeyMap(metaData, unifiedSchema, tableName, emptyList);
        final String pkName = "PrimaryKey";
        final Map<Integer, String> pkMap = uqMap.get(pkName);
        if (pkMap == null) {
            return info;
        }
        final Set<Entry<Integer, String>> entrySet = pkMap.entrySet();
        for (Entry<Integer, String> entry : entrySet) {
            info.addPrimaryKey(entry.getValue(), pkName);
        }
        return info;
    }

    // ===================================================================================
    //                                                                          Unique Key
    //                                                                          ==========
    /**
     * Retrieves an map of the columns composing the unique key for a given table.
     * @param metaData JDBC meta data. (NotNull)
     * @param tableInfo The meta information of table. (NotNull)
     * @return The meta information map of unique keys. The key is unique key name. (NotNull)
     * @throws SQLException
     */
    public Map<String, Map<Integer, String>> getUniqueKeyMap(DatabaseMetaData metaData, DfTableMetaInfo tableInfo)
            throws SQLException { // Non Primary Key Only
        final UnifiedSchema unifiedSchema = tableInfo.getUnifiedSchema();
        final String tableName = tableInfo.getTableName();
        if (tableInfo.isTableTypeView()) {
            return newLinkedHashMap();
        }
        final DfPrimaryKeyMetaInfo pkInfo = getPrimaryKey(metaData, tableInfo);
        return getUniqueKeyMap(metaData, unifiedSchema, tableName, pkInfo.getPrimaryKeyList());
    }

    /**
     * Retrieves an map of the columns composing the unique key for a given table.
     * @param metaData JDBC meta data. (NotNull)
     * @param unifiedSchema The unified schema that can contain catalog name and no-name mark. (Nullable)
     * @param tableName The name of table. (NotNull)
     * @return The meta information map of unique keys. The key is unique key name. (NotNull)
     * @throws SQLException
     */
    public Map<String, Map<Integer, String>> getUniqueKeyMap(DatabaseMetaData metaData, UnifiedSchema unifiedSchema,
            String tableName, List<String> pkList) throws SQLException { // non primary key only
        Map<String, Map<Integer, String>> resultMap = doGetUniqueKeyMap(metaData, unifiedSchema, tableName, pkList);
        if (resultMap.isEmpty()) { // for lower case
            resultMap = doGetUniqueKeyMap(metaData, unifiedSchema, tableName.toLowerCase(), pkList);
        }
        if (resultMap.isEmpty()) { // for upper case
            resultMap = doGetUniqueKeyMap(metaData, unifiedSchema, tableName.toUpperCase(), pkList);
        }
        return resultMap;
    }

    protected Map<String, Map<Integer, String>> doGetUniqueKeyMap(DatabaseMetaData metaData,
            UnifiedSchema unifiedSchema, String tableName, List<String> pkList) throws SQLException { // non primary key only
        final StringSet pkSet = StringSet.createAsFlexible();
        pkSet.addAll(pkList);
        final Map<String, Map<Integer, String>> uniqueKeyMap = newLinkedHashMap();
        ResultSet parts = null;
        try {
            final boolean uniqueKeyOnly = true;
            final String catalogName = unifiedSchema.getPureCatalog();
            final String schemaName = unifiedSchema.getPureSchema();
            parts = metaData.getIndexInfo(catalogName, schemaName, tableName, uniqueKeyOnly, true);
            while (parts.next()) {
                // /- - - - - - - - - - - - - - - - - - - - - - - -
                // same policy as table process about JDBC handling
                // (see DfTableHandler.java)
                // - - - - - - - - - -/

                final boolean isNonUnique;
                {
                    final Boolean nonUnique = parts.getBoolean(4);
                    isNonUnique = (nonUnique != null && nonUnique);
                }
                if (isNonUnique) {
                    continue;
                }

                final String indexType;
                {
                    indexType = parts.getString(7);
                }

                final String columnName = parts.getString(9);
                if (columnName == null || columnName.trim().length() == 0) {
                    continue;
                }

                if (pkSet.contains(columnName)) {
                    continue;
                }

                // check except columns
                if (isColumnExcept(unifiedSchema, tableName, columnName)) {
                    assertUQColumnNotExcepted(unifiedSchema, tableName, columnName);
                }

                final String indexName = parts.getString(6);
                final Integer ordinalPosition;
                {
                    final String ordinalPositionString = parts.getString(8);
                    if (ordinalPositionString == null) {
                        String msg = "The unique columnName should have ordinal-position but null: ";
                        msg = msg + " columnName=" + columnName + " indexType=" + indexType;
                        _log.warn(msg);
                        continue;
                    }
                    try {
                        ordinalPosition = Integer.parseInt(ordinalPositionString);
                    } catch (NumberFormatException e) {
                        String msg = "The unique column should have ordinal-position as number but: ";
                        msg = msg + ordinalPositionString + " columnName=" + columnName + " indexType=" + indexType;
                        _log.warn(msg);
                        continue;
                    }
                }

                if (uniqueKeyMap.containsKey(indexName)) {
                    final Map<Integer, String> uniqueElementMap = uniqueKeyMap.get(indexName);
                    uniqueElementMap.put(ordinalPosition, columnName);
                } else {
                    final Map<Integer, String> uniqueElementMap = new LinkedHashMap<Integer, String>();
                    uniqueElementMap.put(ordinalPosition, columnName);
                    uniqueKeyMap.put(indexName, uniqueElementMap);
                }
            }
        } finally {
            if (parts != null) {
                parts.close();
            }
        }
        return uniqueKeyMap;
    }

    protected void assertUQColumnNotExcepted(UnifiedSchema unifiedSchema, String tableName, String columnName) {
        if (isColumnExcept(unifiedSchema, tableName, columnName)) {
            String msg = "UQ columns are unsupported on 'columnExcept' property:";
            msg = msg + " unifiedSchema=" + unifiedSchema;
            msg = msg + " tableName=" + tableName;
            msg = msg + " columnName=" + columnName;
            throw new DfIllegalPropertySettingException(msg);
        }
    }
}