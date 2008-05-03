package org.seasar.dbflute.helper.jdbc.sqlfile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.ant.BuildException;
import org.seasar.dbflute.DfBuildProperties;
import org.seasar.dbflute.helper.jdbc.DfRunnerInformation;
import org.seasar.dbflute.util.DfStringUtil;

public abstract class DfSqlFileRunnerBase implements DfSqlFileRunner {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Log instance. */
    private static Log _log = LogFactory.getLog(DfSqlFileRunnerBase.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfRunnerInformation _runInfo;
    protected int _goodSqlCount = 0;
    protected int _totalSqlCount = 0;
    protected File _srcFile;
    protected DataSource _dataSource;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfSqlFileRunnerBase(DfRunnerInformation runInfo, DataSource dataSource) {
        _runInfo = runInfo;
        _dataSource = dataSource;
    }

    public void setSrc(File src) {
        this._srcFile = src;
    }

    public int getGoodSqlCount() {
        return _goodSqlCount;
    }

    public int getTotalSqlCount() {
        return _totalSqlCount;
    }

    // ===================================================================================
    //                                                                     Run Transaction
    //                                                                     ===============
    public void runTransaction() {
        _goodSqlCount = 0;
        _totalSqlCount = 0;
        if (_srcFile == null) {
            throw new BuildException("Attribute[_srcFile] must not be null.");
        }

        Reader reader = null;
        Connection connection = null;
        Statement statement = null;
        try {
            reader = (_runInfo.isEncodingNull()) ? newFileReader() : newInputStreamReader();
            final List<String> sqlList = extractSqlList(reader);

            connection = getConnection();
            statement = newStatement(connection);
            for (String sql : sqlList) {
                if (!isTargetSql(sql)) {
                    continue;
                }
                _totalSqlCount++;
                final String realSql = filterSql(sql);
                traceSql(realSql);
                execSQL(statement, realSql);
            }
            if (!connection.getAutoCommit()) {
                if (_runInfo.isRollbackOnly()) {
                    connection.rollback();
                } else {
                    connection.commit();
                }
            }
        } catch (SQLException e) {
            throw new BuildException("Transaction#runTransaction() threw the exception!", e);
        } finally {
            try {
                if (connection != null && !connection.getAutoCommit()) {
                    connection.rollback();
                }
            } catch (SQLException ignored) {
                _log.warn("Connection#rollback() threw the exception!", ignored);
            }
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException ignored) {
                _log.warn("Statement#close() threw the exception!", ignored);
            } finally {
                statement = null;
            }
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ignored) {
                _log.warn("Connection#close() threw the exception!", ignored);
            } finally {
                connection = null;
            }
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ignored) {
                _log.warn("Reader#close() threw the exception: " + reader, ignored);
            } finally {
                reader = null;
            }
        }
        traceResult(_goodSqlCount, _totalSqlCount);
    }

    protected boolean isTargetSql(String sql) {
        return true;
    }
    
    protected void traceSql(String sql) {
        _log.info(sql);
    }

    protected void traceResult(int goodSqlCount, int totalSqlCount) {
        _log.info("  --> success=" + goodSqlCount + " failure=" + (totalSqlCount - goodSqlCount));
    }

    protected String filterSql(String sql) {// for Override
        return sql;
    }

    protected FileReader newFileReader() {
        try {
            return new FileReader(_srcFile);
        } catch (FileNotFoundException e) {
            throw new BuildException("The file does not exist: " + _srcFile, e);
        }
    }

    protected InputStreamReader newInputStreamReader() {
        try {
            return new InputStreamReader(new FileInputStream(_srcFile), _runInfo.getEncoding());
        } catch (FileNotFoundException e) {
            throw new BuildException("The file does not exist: " + _srcFile, e);
        } catch (UnsupportedEncodingException e) {
            throw new BuildException("The encoding is unsupported: " + _runInfo.getEncoding(), e);
        }
    }

    protected Connection getConnection() {
        try {
            final Connection connection = _dataSource.getConnection();
            connection.setAutoCommit(_runInfo.isAutoCommit());
            return connection;
        } catch (SQLException e) {
            throw new RuntimeException("getDataSource().getConnection() threw the exception!", e);
        }
    }

    protected Statement newStatement(Connection connection) {
        try {
            return connection.createStatement();
        } catch (SQLException e) {
            String msg = "Connection#createStatement() threw the exception: _connection=";
            throw new BuildException(msg + connection, e);
        }
    }

    // ===================================================================================
    //                                                                         Extract SQL
    //                                                                         ===========
    protected List<String> extractSqlList(Reader reader) {
        final List<String> sqlList = new ArrayList<String>();
        final BufferedReader in = new BufferedReader(reader);
        final DelimiterChanger delimiterChanger = newDelimterChanger();
        try {
            String sql = "";
            String line = "";
            boolean inGroup = false;
            boolean isAlreadyProcessUTF8Bom = false;
            while ((line = in.readLine()) != null) {
                if (!isAlreadyProcessUTF8Bom) {
                    line = removeUTF8BomIfNeeds(line);
                    isAlreadyProcessUTF8Bom = true;
                }
                if (isSqlTrimAndRemoveLineSeparator()) {
                    line = line.trim();
                }

                // SQL defines "--" as a comment to EOL
                // and in Oracle it may contain a hint
                // so we cannot just remove it, instead we must end it
                if (line.trim().startsWith("--")) {// If this line is comment only, ...
                    // * * * * * * * * * * *
                    // Line for Line Comment
                    // * * * * * * * * * * *
                    
                    // Group Specification
                    if (line.trim().contains("#df:begin#")) {
                        inGroup = true;
                        continue;
                    } else if (line.trim().contains("#df:end#")) {
                        inGroup = false;
                        sqlList.add(sql);
                        sql = "";
                        continue;
                    }
                    
                    // Real Line Comment
                    line = replaceCommentQuestionMarkIfNeeds(line);
                    sql = sql + line + getLineSeparator();
                } else {
                    // * * * * * * * * * *
                    // Line for SQL Clause
                    // * * * * * * * * * *
                    final String lineConnect = isSqlTrimAndRemoveLineSeparator() ? " " : "";
                    if (line.indexOf("--") >= 0) {// If this line contains both sql and comment, ...
                        // With Line Comment
                        line = replaceCommentQuestionMarkIfNeeds(line);
                        sql = sql + lineConnect + line + getLineSeparator();
                    } else {
                        // SQL Clause Only
                        final String lineTerminator = isSqlTrimAndRemoveLineSeparator() ? "" : getLineSeparator();
                        sql = sql + lineConnect + line + lineTerminator;
                    }
                }

                if (inGroup) {
                    sql = sql + getLineSeparator();
                    continue;
                }
                if (sql.trim().endsWith(_runInfo.getDelimiter())) {
                    // * * * * * * * *
                    // End of the SQL
                    // * * * * * * * *
                    sql = sql.trim();
                    sql = sql.substring(0, sql.length() - _runInfo.getDelimiter().length());
                    sql = sql.trim();
                    if ("".equals(sql)) {
                        continue;
                    }
                    if (!delimiterChanger.isDelimiterChanger(sql)) {
                        sqlList.add(sql);
                        sql = "";
                    } else {
                        _runInfo.setDelimiter(delimiterChanger.getNewDelimiter(sql, _runInfo.getDelimiter()));
                        sql = "";
                    }
                }
            }
            sql = sql.trim();
            if (sql.length() > 0) {
                sqlList.add(sql);// for Last SQL
            }
        } catch (IOException e) {
            throw new RuntimeException("The method 'extractSqlList()' threw the exception!", e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignore) {
                    ignore.printStackTrace();
                }
            }
        }
        return sqlList;
    }
    
    public DelimiterChanger newDelimterChanger() {
        final String databaseName = DfBuildProperties.getInstance().getBasicProperties().getDatabaseName();
        final String className = DelimiterChanger.class.getName() + "_" + databaseName;
        DelimiterChanger changer = null;
        try {
            changer = (DelimiterChanger) Class.forName(className).newInstance();
        } catch (Exception ignore) {
            changer = new DelimiterChanger_null();
        }
        return changer;
    }

    protected String removeUTF8BomIfNeeds(String str) {
        if (_runInfo.isEncodingNull()) {
            return str;
        }
        if ("UTF-8".equalsIgnoreCase(_runInfo.getEncoding()) && str.length() > 0 && str.charAt(0) == '\uFEFF') {
            str = str.substring(1);
        }
        return str;
    }

    protected String replaceCommentQuestionMarkIfNeeds(String line) {
        final int lineCommentIndex = line.indexOf("--");
        if (lineCommentIndex < 0) {
            return line;
        }
        final String sqlClause;
        if (lineCommentIndex == 0) {
            sqlClause = "";
        } else {
            sqlClause = line.substring(0, lineCommentIndex);
        }
        String lineComment = line.substring(lineCommentIndex);
        if (lineComment.indexOf("?") >= 0) {
            lineComment = DfStringUtil.replace(line, "?", "Q");
        }
        return sqlClause + lineComment;
    }

    // ===================================================================================
    //                                                                        For Override
    //                                                                        ============
    /**
     * Execute the SQL statement.
     * @param statement Statement. (NotNull)
     * @param sql SQL. (NotNull)
     */
    abstract protected void execSQL(Statement statement, String sql);
    
    /**
     * @return Determination.
     */
    protected boolean isSqlTrimAndRemoveLineSeparator() {
        return false;// as Default
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String getLineSeparator() {
        return System.getProperty("line.separator");
    }

    // ===================================================================================
    //                                                                   Delimiter Changer
    //                                                                   =================
    protected static interface DelimiterChanger {
        public boolean isDelimiterChanger(String sql);

        public String getNewDelimiter(String sql, String preDelimiter);
    }

    protected static class DelimiterChanger_firebird implements DelimiterChanger {
        public static final String CHANGE_COMMAND = "set term ";
        public static final int CHANGE_COMMAND_LENGTH = CHANGE_COMMAND.length();

        public boolean isDelimiterChanger(String sql) {
            sql = sql.trim();
            if (sql.length() > CHANGE_COMMAND_LENGTH) {
                if (sql.substring(0, CHANGE_COMMAND_LENGTH).equalsIgnoreCase(CHANGE_COMMAND)) {
                    return true;
                }
            }
            return false;
        }

        public String getNewDelimiter(String sql, String preDelimiter) {
            String tmp = sql.substring(CHANGE_COMMAND.length());
            if (tmp.indexOf(" ") >= 0) {
                tmp = tmp.substring(0, tmp.indexOf(" "));
            }
            return tmp;
        }
    }

    protected static class DelimiterChanger_mysql implements DelimiterChanger {
        public static final String CHANGE_COMMAND = "delimiter ";
        public static final int CHANGE_COMMAND_LENGTH = CHANGE_COMMAND.length();

        public boolean isDelimiterChanger(String sql) {
            sql = sql.trim();
            if (sql.length() > CHANGE_COMMAND_LENGTH) {
                if (sql.substring(0, CHANGE_COMMAND_LENGTH).equalsIgnoreCase(CHANGE_COMMAND)) {
                    return true;
                }
            }
            return false;
        }

        public String getNewDelimiter(String sql, String preDelimiter) {
            String tmp = sql.substring(CHANGE_COMMAND.length());
            if (tmp.indexOf(" ") >= 0) {
                tmp = tmp.substring(0, tmp.indexOf(" "));
            }
            return tmp;
        }
    }

    protected static class DelimiterChanger_null implements DelimiterChanger {

        public boolean isDelimiterChanger(String sql) {
            return false;
        }

        public String getNewDelimiter(String sql, String preDelimiter) {
            return preDelimiter;
        }
    }

}
