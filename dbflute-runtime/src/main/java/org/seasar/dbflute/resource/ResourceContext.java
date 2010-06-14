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
package org.seasar.dbflute.resource;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.seasar.dbflute.AccessContext;
import org.seasar.dbflute.DBDef;
import org.seasar.dbflute.bhv.core.BehaviorCommand;
import org.seasar.dbflute.cbean.ConditionBean;
import org.seasar.dbflute.cbean.ConditionBeanContext;
import org.seasar.dbflute.cbean.sqlclause.SqlClauseCreator;
import org.seasar.dbflute.dbmeta.DBMeta;
import org.seasar.dbflute.dbmeta.DBMetaProvider;
import org.seasar.dbflute.exception.factory.SQLExceptionHandlerFactory;
import org.seasar.dbflute.exception.handler.SQLExceptionHandler;
import org.seasar.dbflute.helper.StringSet;
import org.seasar.dbflute.jdbc.ValueType;
import org.seasar.dbflute.twowaysql.SqlAnalyzer;
import org.seasar.dbflute.twowaysql.factory.SqlAnalyzerFactory;

/**
 * The context of resource.
 * @author jflute
 */
public class ResourceContext {

    // ===================================================================================
    //                                                                        Thread Local
    //                                                                        ============
    /** The thread-local for this. */
    private static final ThreadLocal<ResourceContext> threadLocal = new ThreadLocal<ResourceContext>();

    /**
     * Get the context of resource by the key.
     * @return The context of resource. (Nullable)
     */
    public static ResourceContext getResourceContextOnThread() {
        return threadLocal.get();
    }

    /**
     * Set the context of resource.
     * @param resourceContext The context of resource. (NotNull)
     */
    public static void setResourceContextOnThread(ResourceContext resourceContext) {
        threadLocal.set(resourceContext);
    }

    /**
     * Is existing the context of resource on thread?
     * @return Determination.
     */
    public static boolean isExistResourceContextOnThread() {
        return (threadLocal.get() != null);
    }

    /**
     * Clear the context of resource on thread.
     */
    public static void clearResourceContextOnThread() {
        threadLocal.set(null);
    }

    // ===================================================================================
    //                                                                         Easy-to-Use
    //                                                                         ===========
    /**
     * @return The behavior command. (NotNull)
     */
    public static BehaviorCommand<?> behaviorCommand() {
        assertResourceContextExists();
        final ResourceContext context = getResourceContextOnThread();
        final BehaviorCommand<?> behaviorCommand = context.getBehaviorCommand();
        if (behaviorCommand == null) {
            String msg = "The behavior command should exist: context=" + context;
            throw new IllegalStateException(msg);
        }
        return behaviorCommand;
    }

    /**
     * @return The current database definition. (NotNull)
     */
    public static DBDef currentDBDef() {
        if (!isExistResourceContextOnThread()) {
            return DBDef.Unknown;
        }
        final DBDef currentDBDef = getResourceContextOnThread().getCurrentDBDef();
        if (currentDBDef == null) {
            return DBDef.Unknown;
        }
        return currentDBDef;
    }

    public static boolean isCurrentDBDef(DBDef targetDBDef) {
        return currentDBDef().equals(targetDBDef);
    }

    /**
     * @return The provider of DB meta. (NotNull)
     */
    public static DBMetaProvider dbmetaProvider() {
        assertResourceContextExists();
        final ResourceContext context = getResourceContextOnThread();
        final DBMetaProvider provider = context.getDBMetaProvider();
        if (provider == null) {
            String msg = "The provider of DB meta should exist: context=" + context;
            throw new IllegalStateException(msg);
        }
        return provider;
    }

    /**
     * @param tableFlexibleName The flexible name of table. (NotNull)
     * @return The instance of DB meta. (Nullable)
     */
    public static DBMeta provideDBMeta(String tableFlexibleName) {
        if (!isExistResourceContextOnThread()) {
            return null;
        }
        final DBMetaProvider provider = getResourceContextOnThread().getDBMetaProvider();
        return provider != null ? provider.provideDBMeta(tableFlexibleName) : null;
    }

    /**
     * @param tableFlexibleName The flexible name of table. (NotNull)
     * @return The instance of DB meta. (NotNull)
     */
    public static DBMeta provideDBMetaChecked(String tableFlexibleName) {
        assertResourceContextExists();
        final ResourceContext context = getResourceContextOnThread();
        final DBMetaProvider provider = context.getDBMetaProvider();
        if (provider == null) {
            String msg = "The provider of DB meta should exist:";
            msg = msg + " tableFlexibleName=" + tableFlexibleName + " context=" + context;
            throw new IllegalStateException(msg);
        }
        return provider.provideDBMetaChecked(tableFlexibleName);
    }

    public static SqlAnalyzer createSqlAnalyzer(String sql, boolean blockNullParameter) {
        assertResourceContextExists();
        final ResourceContext context = getResourceContextOnThread();
        final SqlAnalyzerFactory factory = context.getSqlAnalyzerFactory();
        if (factory == null) {
            String msg = "The factory of SQL analyzer should exist:";
            msg = msg + " sql=" + sql + " blockNullParameter=" + blockNullParameter;
            throw new IllegalStateException(msg);
        }
        final SqlAnalyzer created = factory.create(sql, blockNullParameter);
        if (created != null) {
            return created;
        }
        String msg = "The factory should not return null:";
        msg = msg + " sql=" + sql + " factory=" + factory;
        throw new IllegalStateException(msg);
    }

    public static SQLExceptionHandler createSQLExceptionHandler() {
        assertResourceContextExists();
        final ResourceContext context = getResourceContextOnThread();
        final SQLExceptionHandlerFactory factory = context.getSQLExceptionHandlerFactory();
        if (factory == null) {
            String msg = "The factory of SQLException handler should exist.";
            throw new IllegalStateException(msg);
        }
        final SQLExceptionHandler created = factory.create();
        if (created != null) {
            return created;
        }
        String msg = "The factory should not return null: factory=" + factory;
        throw new IllegalStateException(msg);
    }

    /**
     * Is the SQLException from unique constraint? {Use both SQLState and ErrorCode}
     * @param sqlState SQLState of the SQLException. (Nullable)
     * @param errorCode ErrorCode of the SQLException. (Nullable)
     * @return Is the SQLException from unique constraint?
     */
    public static boolean isUniqueConstraintException(String sqlState, Integer errorCode) {
        if (!isExistResourceContextOnThread()) {
            return false;
        }
        SqlClauseCreator sqlClauseCreator = getResourceContextOnThread().getSqlClauseCreator();
        if (sqlClauseCreator == null) {
            return false;
        }
        return currentDBDef().dbway().isUniqueConstraintException(sqlState, errorCode);
    }

    public static String getOutsideSqlPackage() {
        final ResourceParameter resourceParameter = resourceParameter();
        return resourceParameter != null ? resourceParameter.getOutsideSqlPackage() : null;
    }

    public static String getLogDateFormat() {
        final ResourceParameter resourceParameter = resourceParameter();
        return resourceParameter != null ? resourceParameter.getLogDateFormat() : null;
    }

    public static String getLogTimestampFormat() {
        final ResourceParameter resourceParameter = resourceParameter();
        return resourceParameter != null ? resourceParameter.getLogTimestampFormat() : null;
    }

    protected static ResourceParameter resourceParameter() {
        if (!isExistResourceContextOnThread()) {
            return null;
        }
        return getResourceContextOnThread().getResourceParameter();
    }

    protected static void assertResourceContextExists() {
        if (!isExistResourceContextOnThread()) {
            String msg = "The resource context should exist!";
            throw new IllegalStateException(msg);
        }
    }

    // -----------------------------------------------------
    //                                           Access Date
    //                                           -----------
    public static Date getAccessDate() {
        return AccessContext.getAccessDateOnThread();
    }

    public static Timestamp getAccessTimestamp() {
        return AccessContext.getAccessTimestampOnThread();
    }

    // -----------------------------------------------------
    //                                         Select Column
    //                                         -------------
    public static Set<String> createSelectColumnSet(ResultSet rs) throws SQLException {
        final ResultSetMetaData rsmd = rs.getMetaData();
        final int count = rsmd.getColumnCount();
        final Set<String> columnSet = new HashSet<String>();
        for (int i = 0; i < count; ++i) {
            final String columnLabel = rsmd.getColumnLabel(i + 1);
            final int pos = columnLabel.lastIndexOf('.'); // for SQLite
            if (-1 < pos) {
                columnSet.add(columnLabel.substring(pos + 1));
            } else {
                columnSet.add(columnLabel);
            }
        }
        final Map<String, String> selectIndexReverseMap = getSelectIndexReverseMap();
        if (selectIndexReverseMap == null) {
            return columnSet;
        }
        final Set<String> realColumnSet = StringSet.createAsCaseInsensitive();
        for (String columnName : columnSet) {
            final String realColumnName = selectIndexReverseMap.get(columnName);
            if (realColumnName != null) { // mainly true
                realColumnSet.add(realColumnName);
            } else { // for derived columns and so on
                realColumnSet.add(columnName);
            }
        }
        return realColumnSet;
    }

    // -----------------------------------------------------
    //                                          Select Index
    //                                          ------------
    public static Map<String, Integer> getSelectIndexMap() {
        if (!ConditionBeanContext.isExistConditionBeanOnThread()) {
            return null;
        }
        final ConditionBean cb = ConditionBeanContext.getConditionBeanOnThread();
        return cb.getSqlClause().getSelectIndexMap();
    }

    protected static Map<String, String> getSelectIndexReverseMap() {
        if (!ConditionBeanContext.isExistConditionBeanOnThread()) {
            return null;
        }
        final ConditionBean cb = ConditionBeanContext.getConditionBeanOnThread();
        return cb.getSqlClause().getSelectIndexReverseMap();
    }

    public static Object getValue(ResultSet rs, String columnName, ValueType valueType,
            Map<String, Integer> selectIndexMap) throws SQLException { // no check
        final Integer selectIndex = selectIndexMap.get(columnName);
        if (selectIndex != null) {
            return valueType.getValue(rs, selectIndex);
        } else {
            return valueType.getValue(rs, columnName);
        }
    }

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected BehaviorCommand<?> _behaviorCommand;
    protected DBDef _currentDBDef;
    protected DBMetaProvider _dbmetaProvider;
    protected SqlClauseCreator _sqlClauseCreator;
    protected ResourceParameter _resourceParameter;
    protected SqlAnalyzerFactory _sqlAnalyzerFactory;
    protected SQLExceptionHandlerFactory _sqlExceptionHandlerFactory;

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return "{" + _behaviorCommand + ", " + _currentDBDef + ", " + _dbmetaProvider + ", " + _sqlClauseCreator + ", "
                + _resourceParameter + ", " + _sqlAnalyzerFactory + "}";
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public BehaviorCommand<?> getBehaviorCommand() {
        return _behaviorCommand;
    }

    public void setBehaviorCommand(BehaviorCommand<?> behaviorCommand) {
        _behaviorCommand = behaviorCommand;
    }

    public DBDef getCurrentDBDef() {
        return _currentDBDef;
    }

    public void setCurrentDBDef(DBDef currentDBDef) {
        _currentDBDef = currentDBDef;
    }

    public DBMetaProvider getDBMetaProvider() {
        return _dbmetaProvider;
    }

    public void setDBMetaProvider(DBMetaProvider dbmetaProvider) {
        _dbmetaProvider = dbmetaProvider;
    }

    public SqlClauseCreator getSqlClauseCreator() {
        return _sqlClauseCreator;
    }

    public void setSqlClauseCreator(SqlClauseCreator sqlClauseCreator) {
        _sqlClauseCreator = sqlClauseCreator;
    }

    public ResourceParameter getResourceParameter() {
        return _resourceParameter;
    }

    public void setResourceParameter(ResourceParameter resourceParameter) {
        _resourceParameter = resourceParameter;
    }

    public SqlAnalyzerFactory getSqlAnalyzerFactory() {
        return _sqlAnalyzerFactory;
    }

    public void setSqlAnalyzerFactory(SqlAnalyzerFactory sqlAnalyzerFactory) {
        _sqlAnalyzerFactory = sqlAnalyzerFactory;
    }

    public SQLExceptionHandlerFactory getSQLExceptionHandlerFactory() {
        return _sqlExceptionHandlerFactory;
    }

    public void setSQLExceptionHandlerFactory(SQLExceptionHandlerFactory sqlExceptionHandlerFactory) {
        _sqlExceptionHandlerFactory = sqlExceptionHandlerFactory;
    }
}
