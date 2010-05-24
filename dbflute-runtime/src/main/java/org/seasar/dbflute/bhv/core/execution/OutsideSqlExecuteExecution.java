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
package org.seasar.dbflute.bhv.core.execution;

import javax.sql.DataSource;

import org.seasar.dbflute.jdbc.StatementFactory;
import org.seasar.dbflute.s2dao.sqlcommand.TnAbstractDynamicCommand;
import org.seasar.dbflute.s2dao.sqlhandler.TnBasicUpdateHandler;
import org.seasar.dbflute.twowaysql.context.CommandContext;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 */
public class OutsideSqlExecuteExecution extends TnAbstractDynamicCommand {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected boolean _removeEmptyLine;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public OutsideSqlExecuteExecution(DataSource dataSource, StatementFactory statementFactory) {
        super(dataSource, statementFactory);
    }

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    public Object execute(Object args[]) {
        final CommandContext ctx = apply(args);
        final TnBasicUpdateHandler updateHandler = createBasicUpdateHandler(ctx);
        final Object[] bindVariables = ctx.getBindVariables();
        updateHandler.setExceptionMessageSqlArgs(bindVariables);
        return Integer.valueOf(updateHandler.execute(bindVariables, ctx.getBindVariableTypes()));
    }

    protected TnBasicUpdateHandler createBasicUpdateHandler(CommandContext ctx) {
        String realSql = ctx.getSql();
        if (isRemoveEmptyLine()) {
            realSql = Srl.removeEmptyLine(realSql);
        }
        return new TnBasicUpdateHandler(getDataSource(), realSql, getStatementFactory());
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public boolean isRemoveEmptyLine() {
        return _removeEmptyLine;
    }

    public void setRemoveEmptyLine(boolean removeEmptyLine) {
        this._removeEmptyLine = removeEmptyLine;
    }
}
