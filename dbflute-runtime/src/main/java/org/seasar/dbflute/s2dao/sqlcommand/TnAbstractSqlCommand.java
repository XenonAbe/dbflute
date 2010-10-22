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
package org.seasar.dbflute.s2dao.sqlcommand;

import javax.sql.DataSource;

import org.seasar.dbflute.bhv.core.SqlExecution;
import org.seasar.dbflute.jdbc.StatementFactory;

/**
 * {Refers to Seasar and Extends its class}
 * @author jflute
 */
public abstract class TnAbstractSqlCommand implements TnSqlCommand, SqlExecution {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    private DataSource _dataSource;
    private StatementFactory _statementFactory;
    private String _sql;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public TnAbstractSqlCommand(DataSource dataSource, StatementFactory statementFactory) {
        this._dataSource = dataSource;
        this._statementFactory = statementFactory;
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
}
