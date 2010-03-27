package org.seasar.dbflute.logic.factory;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.seasar.dbflute.logic.schemainitializer.DfSchemaInitializer;
import org.seasar.dbflute.logic.schemainitializer.DfSchemaInitializerDB2;
import org.seasar.dbflute.logic.schemainitializer.DfSchemaInitializerH2;
import org.seasar.dbflute.logic.schemainitializer.DfSchemaInitializerJdbc;
import org.seasar.dbflute.logic.schemainitializer.DfSchemaInitializerMySQL;
import org.seasar.dbflute.logic.schemainitializer.DfSchemaInitializerOracle;
import org.seasar.dbflute.logic.schemainitializer.DfSchemaInitializerPostgreSQL;
import org.seasar.dbflute.logic.schemainitializer.DfSchemaInitializerSqlServer;
import org.seasar.dbflute.properties.DfBasicProperties;
import org.seasar.dbflute.properties.DfDatabaseProperties;
import org.seasar.dbflute.properties.DfReplaceSchemaProperties;

/**
 * @author jflute
 */
public class DfSchemaInitializerFactory {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected DataSource _dataSource;
    protected DfBasicProperties _basicProperties;
    protected DfDatabaseProperties _databaseProperties;
    protected DfReplaceSchemaProperties _replaceSchemaProperties;
    protected InitializeType _initializeType;
    protected Map<String, Object> _additionalDropMap;

    public enum InitializeType {
        MAIN, ADDTIONAL
    }

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfSchemaInitializerFactory(DataSource dataSource, DfBasicProperties basicProperties,
            DfDatabaseProperties databaseProperties, DfReplaceSchemaProperties replaceSchemaProperties,
            InitializeType initializeType) {
        _dataSource = dataSource;
        _basicProperties = basicProperties;
        _databaseProperties = databaseProperties;
        _replaceSchemaProperties = replaceSchemaProperties;
        _initializeType = initializeType;
    }

    // ===================================================================================
    //                                                                              Create
    //                                                                              ======
    public DfSchemaInitializer createSchemaInitializer() {
        final DfSchemaInitializer initializer;
        if (_basicProperties.isDatabaseMySQL()) {
            initializer = createSchemaInitializerMySQL();
        } else if (_basicProperties.isDatabasePostgreSQL()) {
            initializer = createSchemaInitializerPostgreSQL();
        } else if (_basicProperties.isDatabaseOracle()) {
            initializer = createSchemaInitializerOracle();
        } else if (_basicProperties.isDatabaseDB2()) {
            initializer = createSchemaInitializerDB2();
        } else if (_basicProperties.isDatabaseSQLServer()) {
            initializer = createSchemaInitializerSqlServer();
        } else if (_basicProperties.isDatabaseH2()) {
            initializer = createSchemaInitializerH2();
        } else {
            initializer = createSchemaInitializerJdbc();
        }
        return initializer;
    }

    protected DfSchemaInitializer createSchemaInitializerJdbc() {
        final DfSchemaInitializerJdbc initializer = new DfSchemaInitializerJdbc();
        setupSchemaInitializerJdbcProperties(initializer);
        return initializer;
    }

    protected DfSchemaInitializer createSchemaInitializerPostgreSQL() {
        final DfSchemaInitializerPostgreSQL initializer = new DfSchemaInitializerPostgreSQL();
        setupSchemaInitializerJdbcProperties(initializer);
        return initializer;
    }

    protected DfSchemaInitializer createSchemaInitializerOracle() {
        final DfSchemaInitializerOracle initializer = new DfSchemaInitializerOracle();
        setupSchemaInitializerJdbcProperties(initializer);
        return initializer;
    }

    protected DfSchemaInitializer createSchemaInitializerDB2() {
        final DfSchemaInitializerDB2 initializer = new DfSchemaInitializerDB2();
        setupSchemaInitializerJdbcProperties(initializer);
        return initializer;
    }

    protected DfSchemaInitializer createSchemaInitializerMySQL() {
        final DfSchemaInitializerMySQL initializer = new DfSchemaInitializerMySQL();
        setupSchemaInitializerJdbcProperties(initializer);
        return initializer;
    }

    protected DfSchemaInitializer createSchemaInitializerSqlServer() {
        final DfSchemaInitializerSqlServer initializer = new DfSchemaInitializerSqlServer();
        setupSchemaInitializerJdbcProperties(initializer);
        return initializer;
    }

    protected DfSchemaInitializer createSchemaInitializerH2() {
        final DfSchemaInitializerH2 initializer = new DfSchemaInitializerH2();
        setupSchemaInitializerJdbcProperties(initializer);
        return initializer;
    }

    protected void setupSchemaInitializerJdbcProperties(DfSchemaInitializerJdbc initializer) {
        setupDetailExecutionHandling(initializer);

        if (_initializeType.equals(InitializeType.MAIN)) {
            initializer.setDataSource(_dataSource);
            initializer.setSchema(_databaseProperties.getDatabaseSchema());
            initializer.setDropGenerateTableOnly(_replaceSchemaProperties.isDropGenerateTableOnly());
            initializer.setDropGenerateProcedureOnly(_replaceSchemaProperties.isDropGenerateProcedureOnly());
            return;
        }

        if (_initializeType.equals(InitializeType.ADDTIONAL)) {
            // Here 'Additional'!
            if (_additionalDropMap == null) {
                String msg = "The additional drop map should exist if the initialize type is additional!";
                throw new IllegalStateException(msg);
            }
            initializer.setDataSource(getAdditionalDataSource());
            final String schemaName = getAdditionalDropSchema(_additionalDropMap);
            initializer.setSchema(schemaName);
            initializer.setTableNameWithSchema(true); // because it may be other schema!
            initializer.setDropObjectTypeList(getAdditionalDropObjectTypeList(_additionalDropMap));
            initializer.setDropTableTargetList(getAdditionalDropTableTargetList(_additionalDropMap));
            initializer.setDropTableExceptList(getAdditionalDropTableExceptList(_additionalDropMap));
            initializer.setDropGenerateTableOnly(false);
            return;
        }

        String msg = "Unknown initialize type: " + _initializeType;
        throw new IllegalStateException(msg);
    }

    protected DataSource getAdditionalDataSource() {
        return new DataSource() {
            public void setLoginTimeout(int i) throws SQLException {
            }

            public void setLogWriter(PrintWriter printwriter) throws SQLException {
            }

            public int getLoginTimeout() throws SQLException {
                return 0;
            }

            public PrintWriter getLogWriter() throws SQLException {
                return null;
            }

            public Connection getConnection(String s, String s1) throws SQLException {
                return null;
            }

            public Connection getConnection() throws SQLException {
                return _replaceSchemaProperties.createAdditionalDropConnection(_additionalDropMap);
            }
        };
    }

    protected void setupDetailExecutionHandling(DfSchemaInitializerJdbc initializer) {
        initializer.setSuppressTruncateTable(_replaceSchemaProperties.isSuppressTruncateTable());
        initializer.setSuppressDropForeignKey(_replaceSchemaProperties.isSuppressDropForeignKey());
        initializer.setSuppressDropTable(_replaceSchemaProperties.isSuppressDropTable());
        initializer.setSuppressDropSequence(_replaceSchemaProperties.isSuppressDropSequence());
        initializer.setSuppressDropProcedure(_replaceSchemaProperties.isSuppressDropProcedure());
        initializer.setSuppressDropDBLink(_replaceSchemaProperties.isSuppressDropDBLink());
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    protected String getAdditionalDropSchema(Map<String, Object> map) {
        return _replaceSchemaProperties.getAdditionalDropSchema(map);
    }

    protected List<String> getAdditionalDropObjectTypeList(Map<String, Object> map) {
        return _replaceSchemaProperties.getAdditionalDropObjectTypeList(map);
    }

    protected List<String> getAdditionalDropTableTargetList(Map<String, Object> map) {
        return _replaceSchemaProperties.getAdditionalDropTableTargetList(map);
    }

    protected List<String> getAdditionalDropTableExceptList(Map<String, Object> map) {
        return _replaceSchemaProperties.getAdditionalDropTableExceptList(map);
    }

    protected boolean isAdditionalDropAllTable(Map<String, Object> map) {
        return _replaceSchemaProperties.isAdditionalDropAllTable(map);
    }

    public Map<String, Object> getAdditionalDropMap() {
        return _additionalDropMap;
    }

    public void setAdditionalDropMap(Map<String, Object> additionalDropMap) {
        this._additionalDropMap = additionalDropMap;
    }
}
