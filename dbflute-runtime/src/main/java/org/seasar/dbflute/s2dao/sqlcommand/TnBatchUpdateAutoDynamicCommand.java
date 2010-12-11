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

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.seasar.dbflute.bhv.UpdateOption;
import org.seasar.dbflute.cbean.ConditionBean;
import org.seasar.dbflute.jdbc.StatementFactory;
import org.seasar.dbflute.s2dao.metadata.TnPropertyType;
import org.seasar.dbflute.s2dao.sqlhandler.TnBatchUpdateAutoHandler;

/**
 * {Created with reference to S2Container's utility and extended for DBFlute}
 * @author jflute
 */
public class TnBatchUpdateAutoDynamicCommand extends TnUpdateAutoDynamicCommand {

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public TnBatchUpdateAutoDynamicCommand(DataSource dataSource, StatementFactory statementFactory) {
        super(dataSource, statementFactory);
    }

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    @Override
    protected Object doExecute(Object bean, TnPropertyType[] propertyTypes, String sql,
            UpdateOption<ConditionBean> option) {
        final List<?> beanList;
        if (bean instanceof List<?>) {
            beanList = (List<?>) bean;
        } else {
            String msg = "The argument 'args[0]' should be list: " + bean;
            throw new IllegalArgumentException(msg);
        }
        final TnBatchUpdateAutoHandler handler = createUpdateBatchAutoHandler(propertyTypes, sql, option);
        handler.setExceptionMessageSqlArgs(new Object[] { beanList });
        return handler.executeBatch(beanList);
    }

    // ===================================================================================
    //                                                                       Update Column
    //                                                                       =============
    // Batch Update does not use modified properties
    @Override
    protected Set<?> getModifiedPropertyNames(Object bean) {
        return Collections.EMPTY_SET;
    }

    @Override
    protected boolean isModifiedProperty(Set<?> modifiedSet, TnPropertyType pt) {
        return true; // as default (all columns are updated)
    }

    // ===================================================================================
    //                                                                             Handler
    //                                                                             =======
    protected TnBatchUpdateAutoHandler createUpdateBatchAutoHandler(TnPropertyType[] boundPropTypes, String sql,
            UpdateOption<ConditionBean> option) {
        final TnBatchUpdateAutoHandler handler = new TnBatchUpdateAutoHandler(getDataSource(), getStatementFactory(),
                _beanMetaData, boundPropTypes);
        handler.setOptimisticLockHandling(_optimisticLockHandling);
        handler.setVersionNoAutoIncrementOnMemory(_versionNoAutoIncrementOnMemory);
        handler.setSql(sql);
        return handler;
    }

    // ===================================================================================
    //                                                                  Non Update Message
    //                                                                  ==================
    @Override
    protected String createNonUpdateLogMessage(final Object bean) {
        throw new IllegalStateException("no way when BatchUpdate");
    }
}