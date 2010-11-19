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
package org.seasar.dbflute.logic.jdbc.metadata.synonym;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.torque.engine.database.model.UnifiedSchema;
import org.seasar.dbflute.exception.SQLFailureException;
import org.seasar.dbflute.helper.StringKeyMap;
import org.seasar.dbflute.helper.jdbc.facade.DfJdbcFacade;
import org.seasar.dbflute.logic.jdbc.handler.DfProcedureHandler;
import org.seasar.dbflute.logic.jdbc.metadata.DfAbstractMetaDataExtractor;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfProcedureMetaInfo;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfProcedureSynonymMetaInfo;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfSynonymMetaInfo;

/**
 * @author jflute
 * @since 0.9.6.2 (2009/12/08 Tuesday)
 */
public class DfProcedureSynonymExtractorOracle extends DfAbstractMetaDataExtractor implements
        DfProcedureSynonymExtractor {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Log _log = LogFactory.getLog(DfProcedureSynonymExtractorOracle.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected DataSource _dataSource;
    protected List<UnifiedSchema> _targetSchemaList;

    // ===================================================================================
    //                                                                             Extract
    //                                                                             =======
    public Map<String, DfProcedureSynonymMetaInfo> extractProcedureSynonymMap() {
        final Map<String, DfProcedureSynonymMetaInfo> procedureSynonymMap = StringKeyMap.createAsFlexibleOrdered();
        final String sql = buildSynonymSelect();
        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;
        try {
            conn = _dataSource.getConnection();
            final DatabaseMetaData metaData = conn.getMetaData();
            final Map<String, DfProcedureMetaInfo> procedureMap = new LinkedHashMap<String, DfProcedureMetaInfo>();
            final List<DfProcedureMetaInfo> procedureList = new ArrayList<DfProcedureMetaInfo>();
            final DfProcedureHandler procedureHandler = new DfProcedureHandler();
            for (UnifiedSchema unifiedSchema : _targetSchemaList) {
                // get new procedure list because different instances is needed at this process
                procedureList.addAll(procedureHandler.getPlainProcedureList(_dataSource, metaData, unifiedSchema));
            }
            for (DfProcedureMetaInfo metaInfo : procedureList) {
                final String procedureKeyName = metaInfo.getProcedureFullQualifiedName();
                procedureMap.put(procedureKeyName, metaInfo);
            }
            st = conn.createStatement();
            _log.info(sql);
            rs = st.executeQuery(sql);
            while (rs.next()) {
                final UnifiedSchema synonymOwner = createAsDynamicSchema(null, rs.getString("OWNER"));
                final String synonymName = rs.getString("SYNONYM_NAME");
                final UnifiedSchema tableOwner = createAsDynamicSchema(null, rs.getString("TABLE_OWNER"));
                final String tableName = rs.getString("TABLE_NAME");
                final String dbLinkName = rs.getString("DB_LINK");

                final DfSynonymMetaInfo synonymMetaInfo = new DfSynonymMetaInfo();

                // Basic
                synonymMetaInfo.setSynonymOwner(synonymOwner);
                synonymMetaInfo.setSynonymName(synonymName);
                synonymMetaInfo.setTableOwner(tableOwner);
                synonymMetaInfo.setTableName(tableName);
                synonymMetaInfo.setDBLinkName(dbLinkName);

                // Select-able?
                judgeSynonymSelectable(synonymMetaInfo);

                if (synonymMetaInfo.isSelectable()) {
                    continue; // select-able synonyms are out of target
                }
                if (dbLinkName != null && dbLinkName.trim().length() > 0) {
                    continue; // It's a DB Link Synonym!
                }
                if (!tableOwner.hasSchema()) {
                    continue; // basically no way because it may be for DB Link Synonym
                }

                final String procedureKey = tableOwner.buildSchemaQualifiedName(tableName);
                final DfProcedureMetaInfo procedureMetaInfo = procedureMap.get(procedureKey);
                if (procedureMetaInfo == null) {
                    // Synonym for Package Procedure has several problems.
                    // So it is not supported here.
                    //for (String schemaName : _schemaList) {
                    //    procedureMetaInfo = procedureMap.get(schemaName + "." + procedureKey);
                    //    if (procedureMetaInfo != null) {
                    //        break; // comes first  
                    //    }
                    //}
                    //if (procedureMetaInfo == null) {
                    continue;
                    //}
                }
                procedureMetaInfo.setProcedureSynonym(true);
                final DfProcedureSynonymMetaInfo procedureSynonymMetaInfo = new DfProcedureSynonymMetaInfo();
                procedureSynonymMetaInfo.setProcedureMetaInfo(procedureMetaInfo);
                procedureSynonymMetaInfo.setSynonymMetaInfo(synonymMetaInfo);
                final String synonymKey = buildSynonymMapKey(synonymOwner, synonymName);
                procedureSynonymMap.put(synonymKey, procedureSynonymMetaInfo);
            }
        } catch (SQLException e) {
            String msg = "Failed to get procedure synonyms: sql=" + sql;
            throw new SQLFailureException(msg, e);
        } finally {
            if (st != null) {
                try {
                    st.close();
                } catch (SQLException ignored) {
                }
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignored) {
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
        return procedureSynonymMap;
    }

    protected String buildSynonymSelect() {
        final StringBuilder sb = new StringBuilder();
        int count = 0;
        for (UnifiedSchema unifiedSchema : _targetSchemaList) {
            if (count > 0) {
                sb.append(", ");
            }
            sb.append("'").append(unifiedSchema.getPureSchema()).append("'");
            ++count;
        }
        final String sql = "select * from ALL_SYNONYMS where OWNER in (" + sb.toString() + ")";
        return sql;
    }

    protected String buildSynonymMapKey(UnifiedSchema synonymOwner, String synonymName) {
        return synonymOwner.buildSchemaQualifiedName(synonymName);
    }

    protected void judgeSynonymSelectable(DfSynonymMetaInfo info) {
        final DfJdbcFacade facade = new DfJdbcFacade(_dataSource);
        final String synonymSqlName = info.buildSynonymSqlName();
        final String sql = "select * from " + synonymSqlName + " where 0 = 1";
        try {
            final List<String> columnList = new ArrayList<String>();
            columnList.add("dummy");
            facade.selectStringList(sql, columnList);
            info.setSelectable(true);
        } catch (RuntimeException ignored) {
            info.setSelectable(false);
        }
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public void setDataSource(DataSource dataSource) {
        _dataSource = dataSource;
    }

    public void setTargetSchemaList(List<UnifiedSchema> targetSchemaList) {
        this._targetSchemaList = targetSchemaList;
    }
}
