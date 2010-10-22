/*
 * Copyright 2004-2010 the Seasar Foundation and the Others.
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
package org.seasar.dbflute.s2dao.sqlhandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.seasar.dbflute.CallbackContext;
import org.seasar.dbflute.QLog;
import org.seasar.dbflute.exception.handler.SQLExceptionHandler;
import org.seasar.dbflute.jdbc.SqlLogHandler;
import org.seasar.dbflute.jdbc.SqlResultHandler;
import org.seasar.dbflute.jdbc.StatementFactory;
import org.seasar.dbflute.jdbc.ValueType;
import org.seasar.dbflute.resource.InternalMapContext;
import org.seasar.dbflute.resource.ResourceContext;
import org.seasar.dbflute.s2dao.extension.TnSqlLogRegistry;
import org.seasar.dbflute.s2dao.valuetype.TnValueTypes;
import org.seasar.dbflute.twowaysql.DisplaySqlBuilder;
import org.seasar.dbflute.util.DfSystemUtil;

/**
 * {Refers to Seasar and Extends its class}
 * @author jflute
 */
public class TnBasicHandler {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DataSource _dataSource;
    protected final StatementFactory _statementFactory;
    protected String _sql;
    protected Object[] _exceptionMessageSqlArgs;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    /**
     * Constructor. You need to set SQL later.
     * @param dataSource The data source. (NotNull)
     * @param statementFactory The factory of statement. (NotNull)
     */
    public TnBasicHandler(DataSource dataSource, StatementFactory statementFactory) {
        this(dataSource, statementFactory, null);
    }

    /**
     * Constructor. (full property parameter)
     * @param dataSource The data source. (NotNull)
     * @param statementFactory The factory of statement. (NotNull)
     * @param sql The SQL string. (NotNull)
     */
    public TnBasicHandler(DataSource dataSource, StatementFactory statementFactory, String sql) {
        _dataSource = dataSource;
        _statementFactory = statementFactory;
        setSql(sql);
    }

    // ===================================================================================
    //                                                                        Common Logic
    //                                                                        ============
    // -----------------------------------------------------
    //                                    Arguments Handling
    //                                    ------------------
    /**
     * @param ps Prepared statement. (NotNull)
     * @param args The arguments for binding. (Nullable)
     * @param valueTypes The types of binding value. (NotNull)
     */
    protected void bindArgs(PreparedStatement ps, Object[] args, ValueType[] valueTypes) {
        if (args == null) {
            return;
        }
        try {
            for (int i = 0; i < args.length; ++i) {
                final ValueType valueType = valueTypes[i];
                valueType.bindValue(ps, i + 1, args[i]);
            }
        } catch (SQLException e) {
            handleSQLException(e, ps);
        }
    }

    /**
     * @param ps Prepared statement. (NotNull)
     * @param args The arguments for binding. (Nullable)
     * @param argTypes The types of arguments. (NotNull)
     */
    protected void bindArgs(PreparedStatement ps, Object[] args, Class<?>[] argTypes) {
        bindArgs(ps, args, argTypes, 0);
    }

    /**
     * @param ps Prepared statement. (NotNull)
     * @param args The arguments for binding. (Nullable)
     * @param argTypes The types of arguments. (NotNull)
     * @param beginIndex The index for beginning of binding.
     */
    protected void bindArgs(PreparedStatement ps, Object[] args, Class<?>[] argTypes, int beginIndex) {
        if (args == null) {
            return;
        }
        try {
            for (int i = beginIndex; i < args.length; ++i) {
                final ValueType valueType = findValueType(argTypes[i], args[i]);
                valueType.bindValue(ps, i + 1, args[i]);
            }
        } catch (SQLException e) {
            handleSQLException(e, ps);
        }
    }

    protected ValueType findValueType(Class<?> type, Object instance) {
        return TnValueTypes.findByTypeOrValue(type, instance);
    }

    protected Class<?>[] getArgTypes(Object[] args) {
        if (args == null) {
            return null;
        }
        Class<?>[] argTypes = new Class[args.length];
        for (int i = 0; i < args.length; ++i) {
            Object arg = args[i];
            if (arg != null) {
                argTypes[i] = arg.getClass();
            }
        }
        return argTypes;
    }

    // -----------------------------------------------------
    //                                           SQL Logging
    //                                           -----------
    protected void logSql(Object[] args, Class<?>[] argTypes) {
        // [SqlLogHandler]
        final SqlLogHandler sqlLogHandler = getSqlLogHander();
        final boolean existsSqlLogHandler = sqlLogHandler != null;

        // [SqlResultHandler]
        final SqlResultHandler sqlResultHander = getSqlResultHander();
        final boolean existsSqlResultHandler = sqlResultHander != null;

        // [SqlLogRegistry]
        final Object sqlLogRegistry = TnSqlLogRegistry.findContainerSqlLogRegistry();
        final boolean existsSqlLogRegistry = sqlLogRegistry != null;

        if (isLogEnabled() || existsSqlLogHandler || existsSqlResultHandler || existsSqlLogRegistry) {
            final String displaySql = getDisplaySql(args);
            if (isLogEnabled()) {
                log((isContainsLineSeparatorInSql() ? getLineSeparator() : "") + displaySql);
            }
            if (existsSqlLogHandler) { // DBFlute provides
                sqlLogHandler.handle(getSql(), displaySql, args, argTypes);
            }
            if (existsSqlLogRegistry) { // S2Container provides
                TnSqlLogRegistry.push(getSql(), displaySql, args, argTypes, sqlLogRegistry);
            }
            putObjectToMapContext("df:DisplaySql", displaySql);
        }
    }

    protected void putObjectToMapContext(String key, Object value) {
        InternalMapContext.setObject(key, value);
    }

    protected boolean isLogEnabled() {
        return QLog.isLogEnabled();
    }

    protected void log(String msg) {
        QLog.log(msg);
    }

    protected String getDisplaySql(Object[] args) {
        String logDateFormat = ResourceContext.getLogDateFormat();
        String logTimestampFormat = ResourceContext.getLogTimestampFormat();
        return DisplaySqlBuilder.buildDisplaySql(_sql, args, logDateFormat, logTimestampFormat);
    }

    protected SqlLogHandler getSqlLogHander() {
        if (!CallbackContext.isExistCallbackContextOnThread()) {
            return null;
        }
        return CallbackContext.getCallbackContextOnThread().getSqlLogHandler();
    }

    protected SqlResultHandler getSqlResultHander() {
        if (!CallbackContext.isExistCallbackContextOnThread()) {
            return null;
        }
        return CallbackContext.getCallbackContextOnThread().getSqlResultHandler();
    }

    protected boolean isContainsLineSeparatorInSql() {
        return _sql != null ? _sql.contains(getLineSeparator()) : false;
    }

    // -----------------------------------------------------
    //                                               Various
    //                                               -------
    protected String getBindVariableText(Object bindVariable) {
        String logDateFormat = ResourceContext.getLogDateFormat();
        String logTimestampFormat = ResourceContext.getLogTimestampFormat();
        return DisplaySqlBuilder.getBindVariableText(bindVariable, logDateFormat, logTimestampFormat);
    }

    // ===================================================================================
    //                                                                   Exception Handler
    //                                                                   =================
    protected void handleSQLException(SQLException e, Statement st) {
        handleSQLException(e, st, false);
    }

    protected void handleSQLException(SQLException e, Statement st, boolean uniqueConstraintValid) {
        final String executedSql = _sql;
        final String displaySql = buildExceptionMessageSql();
        createSQLExceptionHandler().handleSQLException(e, st, uniqueConstraintValid, executedSql, displaySql);
    }

    protected SQLExceptionHandler createSQLExceptionHandler() {
        return ResourceContext.createSQLExceptionHandler();
    }

    protected String buildExceptionMessageSql() {
        String displaySql = null;
        if (_sql != null && _exceptionMessageSqlArgs != null) {
            try {
                displaySql = getDisplaySql(_exceptionMessageSqlArgs);
            } catch (RuntimeException ignored) {
            }
        }
        return displaySql;
    }

    // ===================================================================================
    //                                                                      JDBC Delegator
    //                                                                      ==============
    protected Connection getConnection() {
        if (_dataSource == null) {
            throw new IllegalStateException("The dataSource should not be null!");
        }
        try {
            return _dataSource.getConnection();
        } catch (SQLException e) {
            handleSQLException(e, null);
            return null;// unreachable
        }
    }

    protected PreparedStatement prepareStatement(Connection conn) {
        if (_sql == null) {
            throw new IllegalStateException("The sql should not be null!");
        }
        return _statementFactory.createPreparedStatement(conn, _sql);
    }

    protected int executeUpdate(PreparedStatement ps) {
        try {
            return ps.executeUpdate();
        } catch (SQLException e) {
            handleSQLException(e, ps, true);
            return 0;// unreachable
        }
    }

    protected void setFetchSize(Statement statement, int fetchSize) {
        if (statement == null) {
            return;
        }
        try {
            statement.setFetchSize(fetchSize);
        } catch (SQLException e) {
            handleSQLException(e, statement);
        }
    }

    protected void setMaxRows(Statement statement, int maxRows) {
        if (statement == null) {
            return;
        }
        try {
            statement.setMaxRows(maxRows);
        } catch (SQLException e) {
            handleSQLException(e, statement);
        }
    }

    protected void close(Statement statement) {
        if (statement == null) {
            return;
        }
        try {
            statement.close();
        } catch (SQLException e) {
            handleSQLException(e, statement);
        }
    }

    protected void close(ResultSet resultSet) {
        if (resultSet == null) {
            return;
        }
        try {
            resultSet.close();
        } catch (SQLException e) {
            handleSQLException(e, null);
        }
    }

    protected void close(Connection conn) {
        if (conn == null) {
            return;
        }
        try {
            conn.close();
        } catch (SQLException e) {
            handleSQLException(e, null);
        }
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String getLineSeparator() {
        return DfSystemUtil.getLineSeparator();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public DataSource getDataSource() {
        return _dataSource;
    }

    public StatementFactory getStatementFactory() {
        return _statementFactory;
    }

    public String getSql() {
        return _sql;
    }

    public void setSql(String sql) {
        this._sql = sql;
    }

    public void setExceptionMessageSqlArgs(Object[] exceptionMessageSqlArgs) {
        this._exceptionMessageSqlArgs = exceptionMessageSqlArgs;
    }
}
