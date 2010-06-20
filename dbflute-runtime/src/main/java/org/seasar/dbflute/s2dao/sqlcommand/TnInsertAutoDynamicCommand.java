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
package org.seasar.dbflute.s2dao.sqlcommand;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.seasar.dbflute.bhv.core.SqlExecution;
import org.seasar.dbflute.dbmeta.DBMeta;
import org.seasar.dbflute.dbmeta.name.ColumnSqlName;
import org.seasar.dbflute.jdbc.StatementFactory;
import org.seasar.dbflute.s2dao.identity.TnIdentifierGenerator;
import org.seasar.dbflute.s2dao.metadata.TnBeanMetaData;
import org.seasar.dbflute.s2dao.metadata.TnPropertyType;
import org.seasar.dbflute.s2dao.sqlhandler.TnInsertAutoHandler;
import org.seasar.dbflute.util.DfSystemUtil;

/**
 * {Refers to Seasar and Extends its class}
 * @author jflute
 */
public class TnInsertAutoDynamicCommand implements TnSqlCommand, SqlExecution {

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    private DataSource dataSource;
    private StatementFactory statementFactory;
    private TnBeanMetaData beanMetaData;
    private DBMeta targetDBMeta;
    private String[] propertyNames;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public TnInsertAutoDynamicCommand() {
    }

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    public Object execute(Object[] args) {
        final Object bean = args[0];
        final TnBeanMetaData bmd = getBeanMetaData();
        final TnPropertyType[] propertyTypes = createInsertPropertyTypes(bmd, bean, getPropertyNames());
        final String sql = createInsertSql(bmd, propertyTypes);
        final TnInsertAutoHandler handler = createInsertAutoHandler(bmd, propertyTypes);
        handler.setSql(sql);
        handler.setExceptionMessageSqlArgs(args);
        final int rows = handler.execute(args);
        return Integer.valueOf(rows);
    }

    protected String createInsertSql(TnBeanMetaData bmd, TnPropertyType[] propertyTypes) {
        StringBuilder sb = new StringBuilder(100);
        sb.append("insert into ");
        sb.append(targetDBMeta.getTableSqlName());
        sb.append(" (");
        for (int i = 0; i < propertyTypes.length; ++i) {
            TnPropertyType pt = propertyTypes[i];
            final ColumnSqlName columnSqlName = pt.getColumnSqlName();
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(columnSqlName);
        }
        sb.append(")").append(ln()).append(" values (");
        for (int i = 0; i < propertyTypes.length; ++i) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append("?");
        }
        sb.append(")");
        return sb.toString();
    }

    protected TnInsertAutoHandler createInsertAutoHandler(TnBeanMetaData bmd, TnPropertyType[] propertyTypes) {
        return new TnInsertAutoHandler(dataSource, statementFactory, bmd, propertyTypes);
    }

    protected TnPropertyType[] createInsertPropertyTypes(TnBeanMetaData bmd, Object bean, String[] propertyNames) {
        if (0 == propertyNames.length) {
            String msg = "The property name was not found in the bean: " + bean;
            throw new IllegalStateException(msg);
        }
        final List<TnPropertyType> types = new ArrayList<TnPropertyType>();
        final String timestampPropertyName = bmd.getTimestampPropertyName();
        final String versionNoPropertyName = bmd.getVersionNoPropertyName();

        for (int i = 0; i < propertyNames.length; ++i) {
            final TnPropertyType pt = bmd.getPropertyType(propertyNames[i]);
            if (pt.isPrimaryKey()) {
                final TnIdentifierGenerator generator = bmd.getIdentifierGenerator(pt.getPropertyName());
                if (!generator.isSelfGenerate()) {
                    continue;
                }
            } else {
                if (pt.getPropertyDesc().getValue(bean) == null) {
                    final String propertyName = pt.getPropertyName();
                    if (!propertyName.equalsIgnoreCase(timestampPropertyName)
                            && !propertyName.equalsIgnoreCase(versionNoPropertyName)) {
                        continue;
                    }
                }
            }
            types.add(pt);
        }
        if (types.isEmpty()) {
            String msg = "The target property type was not found in the bean: " + bean;
            throw new IllegalStateException(msg);
        }
        return (TnPropertyType[]) types.toArray(new TnPropertyType[types.size()]);
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String ln() {
        return DfSystemUtil.getLineSeparator();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    protected DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    protected StatementFactory getStatementFactory() {
        return statementFactory;
    }

    public void setStatementFactory(StatementFactory statementFactory) {
        this.statementFactory = statementFactory;
    }

    protected TnBeanMetaData getBeanMetaData() {
        return beanMetaData;
    }

    public void setBeanMetaData(TnBeanMetaData beanMetaData) {
        this.beanMetaData = beanMetaData;
    }

    public DBMeta getTargetDBMeta() {
        return targetDBMeta;
    }

    public void setTargetDBMeta(DBMeta targetDBMeta) {
        this.targetDBMeta = targetDBMeta;
    }

    protected String[] getPropertyNames() {
        return propertyNames;
    }

    public void setPropertyNames(String[] propertyNames) {
        this.propertyNames = propertyNames;
    }
}
