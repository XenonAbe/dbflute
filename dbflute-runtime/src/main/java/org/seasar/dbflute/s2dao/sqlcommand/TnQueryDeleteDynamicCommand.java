/*
 * Copyright 2004-2012 the Seasar Foundation and the Others.
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

import org.seasar.dbflute.bhv.DeleteOption;
import org.seasar.dbflute.cbean.ConditionBean;
import org.seasar.dbflute.jdbc.StatementFactory;
import org.seasar.dbflute.s2dao.sqlhandler.TnCommandContextHandler;
import org.seasar.dbflute.twowaysql.context.CommandContext;

/**
 * {Created with reference to S2Container's utility and extended for DBFlute}
 * @author jflute
 */
public class TnQueryDeleteDynamicCommand extends TnAbstractQueryDynamicCommand {

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public TnQueryDeleteDynamicCommand(DataSource dataSource, StatementFactory statementFactory) {
        super(dataSource, statementFactory);
    }

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    public Object execute(Object[] args) {
        // analyze arguments
        final ConditionBean cb = extractConditionBeanWithCheck(args);
        final DeleteOption<ConditionBean> option = extractUpdateOptionWithCheck(args);

        // arguments for execution (not contains an option)
        final String[] argNames = new String[] { "pmb" };
        final Class<?>[] argTypes = new Class<?>[] { cb.getClass() };
        final Object[] realArgs = new Object[] { cb };

        // prepare context
        final CommandContext context;
        {
            final String twoWaySql = buildQueryDeleteTwoWaySql(cb, option);
            context = createCommandContext(twoWaySql, argNames, argTypes, realArgs);
        }

        // execute
        final TnCommandContextHandler handler = createCommandContextHandler(context);
        handler.setExceptionMessageSqlArgs(context.getBindVariables());
        final int rows = handler.execute(realArgs);
        return Integer.valueOf(rows);
    }

    // ===================================================================================
    //                                                                    Analyze Argument
    //                                                                    ================
    protected ConditionBean extractConditionBeanWithCheck(Object[] args) {
        assertArgument(args);
        final Object fisrtArg = args[0];
        if (!(fisrtArg instanceof ConditionBean)) {
            String msg = "The type of first argument should be " + ConditionBean.class + ":";
            msg = msg + " type=" + fisrtArg.getClass();
            throw new IllegalArgumentException(msg);
        }
        return (ConditionBean) fisrtArg;
    }

    protected DeleteOption<ConditionBean> extractUpdateOptionWithCheck(Object[] args) {
        assertArgument(args);
        if (args.length < 2) {
            return null;
        }
        final Object secondArg = args[1];
        if (secondArg == null) {
            return null;
        }
        if (!(secondArg instanceof DeleteOption<?>)) {
            String msg = "The type of second argument should be " + DeleteOption.class + ":";
            msg = msg + " type=" + secondArg.getClass();
            throw new IllegalArgumentException(msg);
        }
        @SuppressWarnings("unchecked")
        final DeleteOption<ConditionBean> option = (DeleteOption<ConditionBean>) secondArg;
        return option;
    }

    protected void assertArgument(Object[] args) {
        if (args == null || args.length <= 1) {
            String msg = "The arguments should have two argument at least! But:";
            msg = msg + " args=" + (args != null ? args.length : null);
            throw new IllegalArgumentException(msg);
        }
    }

    // ===================================================================================
    //                                                                           Build SQL
    //                                                                           =========
    protected String buildQueryDeleteTwoWaySql(ConditionBean cb, DeleteOption<ConditionBean> option) {
        if (option != null && option.isQueryDeleteForcedDirectAllowed()) {
            cb.getSqlClause().allowQueryUpdateForcedDirect();
        }
        return cb.getSqlClause().getClauseQueryDelete();
    }
}
