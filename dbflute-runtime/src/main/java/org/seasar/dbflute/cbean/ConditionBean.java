/*
 * Copyright 2004-2009 the Seasar Foundation and the Others.
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
package org.seasar.dbflute.cbean;

import java.util.Map;

import org.seasar.dbflute.cbean.sqlclause.SqlClause;
import org.seasar.dbflute.dbmeta.DBMeta;
import org.seasar.dbflute.exception.ConditionInvokingFailureException;
import org.seasar.dbflute.jdbc.StatementConfig;

/**
 * The interface of condition-bean.
 * @author jflute
 */
public interface ConditionBean extends PagingBean {

    // ===================================================================================
    //                                                                          Table Name
    //                                                                          ==========
    /**
     * Get table DB-name.
     * @return Table DB-name. (NotNull)
     */
    String getTableDbName();

    // ===================================================================================
    //                                                                              DBMeta
    //                                                                              ======
    /**
     * Get the instance of DBMeta.
     * @return The instance of DBMeta. (NotNull)
     */
    DBMeta getDBMeta();

    // ===================================================================================
    //                                                                           SqlClause
    //                                                                           =========
    /**
     * Get SQL clause instance. {Internal}<br />
     * @return SQL clause. (NotNull)
     */
    SqlClause getSqlClause();

    // ===================================================================================
    //                                                                 PrimaryKey Handling
    //                                                                 ===================
    /**
     * Accept primary-key map-string.
     * @param primaryKeyMap Primary-key map. (NotNull and NotEmpty)
     */
    void acceptPrimaryKeyMap(Map<String, ? extends Object> primaryKeyMap);

    /**
     * Accept primary-key map-string. Delimiter is at-mark and semicolon.
     * @param primaryKeyMapString Primary-key map. (NotNull and NotEmpty)
     */
    void acceptPrimaryKeyMapString(String primaryKeyMapString);

    /**
     * Add order-by PrimaryKey asc. {order by primaryKey1 asc, primaryKey2 asc...}
     * @return this. (NotNull)
     */
    ConditionBean addOrderBy_PK_Asc();

    /**
     * Add order-by PrimaryKey desc. {order by primaryKey1 desc, primaryKey2 desc...}
     * @return this. (NotNull)
     */
    ConditionBean addOrderBy_PK_Desc();

    // ===================================================================================
    //                                                                               Query
    //                                                                               =====
    /**
     * Get the conditionQuery of the local table as interface.
     * @return The conditionQuery of the local table as interface. (NotNull)
     */
    ConditionQuery localCQ();

    // ===================================================================================
    //                                                                        Lock Setting
    //                                                                        ============
    /**
     * Lock for update. <br />
     * If you call this, your SQL lock target records for update. <br />
     * It depends whether this method supports this on the database type.
     * @return this. (NotNull)
     */
    ConditionBean lockForUpdate();

    // ===================================================================================
    //                                                                        Select Count
    //                                                                        ============
    /**
     * Set up various things for select-count-ignore-fetch-scope. {Internal}
     * This method is for INTERNAL. Don't call this!
     * @return this. (NotNull)
     */
    ConditionBean xsetupSelectCountIgnoreFetchScope();

    /**
     * Do after-care for select-count-ignore-fetch-scope. {Internal}
     * This method is for INTERNAL. Don't call this!
     * @return this. (NotNull)
     */
    ConditionBean xafterCareSelectCountIgnoreFetchScope();

    /**
     * Is set up various things for select-count-ignore-fetch-scope? {Internal}
     * This method is for INTERNAL. Don't call this!
     * @return Determination.
     */
    boolean isSelectCountIgnoreFetchScope();

    // ===================================================================================
    //                                                                    Statement Config
    //                                                                    ================
    /**
     * Configure statement JDBC options. (For example, queryTimeout, fetchSize, ...)
     * @param statementConfig The configuration of statement. (Nullable)
     */
    void configure(StatementConfig statementConfig);

    /**
     * Get the configuration of statement that is set through configure().
     * @return The configuration of statement. (Nullable)
     */
    StatementConfig getStatementConfig();

    // ===================================================================================
    //                                                                         Display SQL
    //                                                                         ===========
    /**
     * Convert this conditionBean to SQL for display.
     * @return SQL for display. (NotNull and NotEmpty)
     */
    String toDisplaySql();

    // ===================================================================================
    //                                                          Basic Status Determination
    //                                                          ==========================
    /**
     * Does it have where clauses? <br />
     * In-line where clause is NOT contained.
     * @return Determination.
     */
    boolean hasWhereClause();

    /**
     * Does it have order-by clauses? <br />
     * Whether effective or not has no influence.
     * @return Determination.
     */
    boolean hasOrderByClause();

    /**
     * Has union query or union all query?
     * @return Determination.
     */
    boolean hasUnionQueryOrUnionAllQuery();

    // ===================================================================================
    //                                                                 Reflection Invoking
    //                                                                 ===================
    /**
     * Invoke the method 'setupSelect_Xxx()' and 'withXxx()' by the path of foreign property name. <br />
     * For example, if this is based on PURCHASE, 'member.memberStatus' means as follows:
     * <pre>
     * PurchaseCB cb = new PurchaseCB();
     * cb.setupSelect_Member().withMemberStatus();
     * </pre>
     * A method with parameters (using fixed condition) is unsupported.
     * @param foreignPropertyNamePath The path string. (NotNull, NotTrimmedEmpty)
     * @throws ConditionInvokingFailureException When the method to the property is not found and the method is failed.
     */
    void invokeSetupSelect(String foreignPropertyNamePath);

    // ===================================================================================
    //                                                                      Free Parameter
    //                                                                      ==============
    /**
     * Register free parameters. {Internal}
     * @param key The key for the parameter. (NotNull)
     * @param value The value for the parameter. (Nullable)
     */
    void xregisterFreeParameter(String key, Object value);

    // ===================================================================================
    //                                                                  Query Synchronizer
    //                                                                  ==================
    /**
     * Register union-query synchronizer. {Internal}
     * @param unionQuerySynchronizer The synchronizer of union query. (Nullable)
     */
    void xregisterUnionQuerySynchronizer(UnionQuery<ConditionBean> unionQuerySynchronizer);
}
