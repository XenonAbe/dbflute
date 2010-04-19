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
package org.seasar.dbflute.logic.jdbc.metadata.info;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.torque.engine.database.model.UnifiedSchema;
import org.seasar.dbflute.logic.jdbc.metadata.comment.DfDbCommentExtractor.UserColComments;
import org.seasar.dbflute.util.DfSystemUtil;

/**
 * @author jflute
 */
public class DfSynonymMetaInfo {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected UnifiedSchema _synonymOwner;
    protected String _synonymName;
    protected UnifiedSchema _tableOwner;
    protected String _tableName;
    protected DfPrimaryKeyMetaInfo _primaryKey;
    protected boolean _autoIncrement;
    protected Map<String, Map<Integer, String>> _uniqueKeyMap;
    protected Map<String, DfForeignKeyMetaInfo> _foreignKeyMap;
    protected Map<String, Map<Integer, String>> _indexMap;
    protected String _dbLinkName;
    protected List<DfColumnMetaInfo> _columnMetaInfoList4DBLink;
    protected boolean _selectable;
    protected boolean _procedureSynonym;
    protected boolean _sequenceSynonym;
    protected String _tableComment;
    protected Map<String, UserColComments> _columnCommentMap;

    // ===================================================================================
    //                                                                       Determination
    //                                                                       =============
    public boolean isDBLink() {
        return _dbLinkName != null;
    }

    public boolean hasTableComment() {
        return _tableComment != null && _tableComment.trim().length() > 0;
    }

    public boolean hasColumnCommentMap() {
        return _columnCommentMap != null && !_columnCommentMap.isEmpty();
    }

    // ===================================================================================
    //                                                                       Name Building
    //                                                                       =============
    public String buildSynonymFullQualifiedName() {
        return _synonymOwner.buildFullQualifiedName(_synonymName);
    }

    public String buildSynonymSchemaQualifiedName() {
        return _synonymOwner.buildSchemaQualifiedName(_synonymName);
    }

    public String buildSynonymSqlName() {
        return _synonymOwner.buildSqlName(_synonymName);
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        String comment = "";
        if (_tableComment != null) {
            final String ln = DfSystemUtil.getLineSeparator();
            final int indexOf = _tableComment.indexOf(ln);
            if (indexOf > 0) { // not contain 0 because ignore first line separator
                comment = _tableComment.substring(0, indexOf) + "...";
            } else {
                comment = _tableComment;
            }
        }
        String columns = "";
        if (_columnMetaInfoList4DBLink != null) {
            columns = "(" + _columnMetaInfoList4DBLink.size() + " columns for DB link)";
        }
        final String synonymSchema = _synonymOwner != null ? _synonymOwner.getPureSchema() : "";
        final String tableSchema = _tableOwner != null ? _tableOwner.getPureSchema() : "";
        return synonymSchema + "." + _synonymName + ":{" + (_dbLinkName != null ? _dbLinkName : tableSchema) + "."
                + _tableName + columns + ", PK=" + _primaryKey + (_autoIncrement ? ", ID" : "") + ", "
                + (_uniqueKeyMap != null ? "UQ=" + _uniqueKeyMap.size() : null) + ", "
                + (_foreignKeyMap != null ? "FK=" + _foreignKeyMap.size() : null) + ", "
                + (_selectable ? "selectable" : "unselectable") + "}"
                + ((comment != null && comment.trim().length() > 0) ? " // " + comment : "");
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public UnifiedSchema getSynonymOwner() {
        return _synonymOwner;
    }

    public void setSynonymOwner(UnifiedSchema synonymOwner) {
        this._synonymOwner = synonymOwner;
    }

    public String getSynonymName() {
        return _synonymName;
    }

    public void setSynonymName(String synonymName) {
        this._synonymName = synonymName;
    }

    public UnifiedSchema getTableOwner() {
        return _tableOwner;
    }

    public void setTableOwner(UnifiedSchema tableOwner) {
        this._tableOwner = tableOwner;
    }

    public String getTableName() {
        return _tableName;
    }

    public void setTableName(String tableName) {
        this._tableName = tableName;
    }

    public DfPrimaryKeyMetaInfo getPrimaryKey() {
        return _primaryKey;
    }

    public void setPrimaryKey(DfPrimaryKeyMetaInfo primaryKey) {
        this._primaryKey = primaryKey;
    }

    public boolean isAutoIncrement() {
        return _autoIncrement;
    }

    public void setAutoIncrement(boolean autoIncrement) {
        this._autoIncrement = autoIncrement;
    }

    public Map<String, Map<Integer, String>> getUniqueKeyMap() {
        return _uniqueKeyMap != null ? _uniqueKeyMap : new HashMap<String, Map<Integer, String>>();
    }

    public void setUniqueKeyMap(Map<String, Map<Integer, String>> uniqueKeyMap) {
        this._uniqueKeyMap = uniqueKeyMap;
    }

    public Map<String, DfForeignKeyMetaInfo> getForeignKeyMap() {
        return _foreignKeyMap != null ? _foreignKeyMap : new HashMap<String, DfForeignKeyMetaInfo>();
    }

    public void setForeignKeyMap(Map<String, DfForeignKeyMetaInfo> foreignKeyMap) {
        this._foreignKeyMap = foreignKeyMap;
    }

    public Map<String, Map<Integer, String>> getIndexMap() {
        return _indexMap != null ? _indexMap : new HashMap<String, Map<Integer, String>>();
    }

    public void setIndexMap(Map<String, Map<Integer, String>> indexMap) {
        this._indexMap = indexMap;
    }

    public String getDBLinkName() {
        return _dbLinkName;
    }

    public void setDBLinkName(String dbLinkName) {
        this._dbLinkName = dbLinkName;
    }

    public List<DfColumnMetaInfo> getColumnMetaInfoList4DBLink() {
        return _columnMetaInfoList4DBLink != null ? _columnMetaInfoList4DBLink : new ArrayList<DfColumnMetaInfo>();
    }

    public void setColumnMetaInfoList4DBLink(List<DfColumnMetaInfo> columnMetaInfoList4DBLink) {
        this._columnMetaInfoList4DBLink = columnMetaInfoList4DBLink;
    }

    public boolean isSelectable() {
        return _selectable;
    }

    public void setSelectable(boolean selectable) {
        this._selectable = selectable;
    }

    public String getTableComment() {
        return _tableComment;
    }

    public void setTableComment(String tableComment) {
        this._tableComment = tableComment;
    }

    public Map<String, UserColComments> getColumnCommentMap() {
        return _columnCommentMap;
    }

    public void setColumnCommentMap(Map<String, UserColComments> columnCommentMap) {
        this._columnCommentMap = columnCommentMap;
    }
}
