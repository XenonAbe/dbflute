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
package org.seasar.dbflute.helper.jdbc.schemainitializer;

import javax.sql.DataSource;

import org.seasar.dbflute.helper.jdbc.generatedsql.DfGeneratedSqlExecutor;
import org.seasar.dbflute.helper.jdbc.generatedsql.DfGeneratedSqlExecutorImpl;

/**
 * The schema initializer for SqlServer.
 * @author jflute
 */
public class DfSchemaInitializerSqlServer implements DfSchemaInitializer {
    protected DataSource _dataSource;

    public void setDataSource(DataSource dataSource) {
        _dataSource = dataSource;
    }

    public void initializeSchema() {
        dropForeignKey();
        dropTable();
    }

    protected void dropForeignKey() {
        final DfGeneratedSqlExecutor generatedSqlExecutor = createGeneratedSqlExecutor();
        generatedSqlExecutor.execute(getDropForeignKeySql(), "sql");
    }

    protected void dropTable() {
        final DfGeneratedSqlExecutor generatedSqlExecutor = createGeneratedSqlExecutor();
        generatedSqlExecutor.execute(getDropTableSql(), "sql");
    }

    protected DfGeneratedSqlExecutor createGeneratedSqlExecutor() {
        final DfGeneratedSqlExecutorImpl generatedSqlExecutorImpl = new DfGeneratedSqlExecutorImpl();
        generatedSqlExecutorImpl.setDataSource(_dataSource);
        return generatedSqlExecutorImpl;
    }

    protected String getDropForeignKeySql() {
        final String lineSeparator = System.getProperty("line.separator");
        final StringBuilder sb = new StringBuilder();
        sb.append("select 'ALTER TABLE ' + parentObj.name + ' DROP CONSTRAINT ' + baseObj.name as sql");
        sb.append(lineSeparator);
        sb.append("  from sysobjects baseObj");
        sb.append(lineSeparator);
        sb.append("    left outer join sysobjects parentObj on baseObj.parent_obj = parentObj.id");
        sb.append(lineSeparator);
        sb.append(" where baseObj.type = 'F'");
        sb.append(lineSeparator);
        return sb.toString();
    }

    protected String getDropTableSql() {
        final String lineSeparator = System.getProperty("line.separator");
        final StringBuilder sb = new StringBuilder();
        sb.append("select 'DROP TABLE ' + name as sql");
        sb.append(lineSeparator);
        sb.append("  from sysobjects");
        sb.append(lineSeparator);
        sb.append(" where type = 'U'");
        sb.append(lineSeparator);
        return sb.toString();
    }
}