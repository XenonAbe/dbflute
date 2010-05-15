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
package org.seasar.dbflute.bhv.outsidesql;

import org.seasar.dbflute.DBDef;
import org.seasar.dbflute.bhv.core.BehaviorCommand;
import org.seasar.dbflute.bhv.core.BehaviorCommandInvoker;
import org.seasar.dbflute.bhv.core.command.OutsideSqlSelectCursorCommand;
import org.seasar.dbflute.jdbc.CursorHandler;
import org.seasar.dbflute.jdbc.StatementConfig;
import org.seasar.dbflute.outsidesql.OutsideSqlOption;

/**
 * The cursor executor of outside-SQL.
 * @param <PARAMETER_BEAN> The type of parameter-bean.
 * @author jflute
 */
public class OutsideSqlCursorExecutor<PARAMETER_BEAN> {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The invoker of behavior command. (NotNull) */
    protected final BehaviorCommandInvoker _behaviorCommandInvoker;

    /** The option of outside-SQL. (NotNull) */
    protected final OutsideSqlOption _outsideSqlOption;

    /** The DB name of table. (NotNull) */
    protected final String _tableDbName;

    /** The current database definition. (NotNull) */
    protected DBDef _currentDBDef;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public OutsideSqlCursorExecutor(BehaviorCommandInvoker behaviorCommandInvoker, OutsideSqlOption outsideSqlOption,
            String tableDbName, DBDef currentDBDef) {
        this._behaviorCommandInvoker = behaviorCommandInvoker;
        this._outsideSqlOption = outsideSqlOption;
        this._tableDbName = tableDbName;
        this._currentDBDef = currentDBDef;
    }

    // ===================================================================================
    //                                                                              Select
    //                                                                              ======
    /**
     * Select the cursor of the entity by outside-SQL.
     * <pre>
     * String path = MemberBhv.PATH_selectSimpleMember;
     * SimpleMemberPmb pmb = new SimpleMemberPmb();
     * pmb.setMemberName_PrefixSearch("S");
     * memberBhv.outsideSql().cursorHandling()
     *         .selectCursor(path, pmb, new PurchaseSummaryMemberCursorHandler() {
     *     public void Object fetchCursor(PurchaseSummaryMemberCursor cursor) throws SQLException {
     *         while (cursor.next()) {
     *             Integer memberId = cursor.getMemberId();
     *             String memberName = cursor.getMemberName();
     *             ...
     *         }
     *         return null;
     *     }
     * });
     * </pre>
     * It needs to use type-safe-cursor instead of customize-entity.
     * The way to generate it is following:
     * <pre>
     * -- #df:entity#
     * -- +cursor+
     * </pre>
     * @param path The path of SQL file. (NotNull)
     * @param pmb The parameter-bean. Allowed types are Bean object and Map object. (Nullable)
     * @param handler The handler of cursor. (NotNull)
     * @return The result object that the cursor handler returns. (Nullable)
     * @exception org.seasar.dbflute.exception.OutsideSqlNotFoundException When the outside-SQL is not found.
     */
    public Object selectCursor(String path, PARAMETER_BEAN pmb, CursorHandler handler) {
        return invoke(createSelectCursorCommand(path, pmb, handler));
    }

    // ===================================================================================
    //                                                                    Behavior Command
    //                                                                    ================
    protected BehaviorCommand<Object> createSelectCursorCommand(String path, PARAMETER_BEAN pmb, CursorHandler handler) {
        return xsetupCommand(newOutsideSqlSelectCursorCommand(), path, pmb, handler);
    }

    protected OutsideSqlSelectCursorCommand newOutsideSqlSelectCursorCommand() {
        return new OutsideSqlSelectCursorCommand();
    }

    protected OutsideSqlSelectCursorCommand xsetupCommand(OutsideSqlSelectCursorCommand command, String path,
            PARAMETER_BEAN pmb, CursorHandler handler) {
        command.setTableDbName(_tableDbName);
        _behaviorCommandInvoker.injectComponentProperty(command);
        command.setOutsideSqlPath(path);
        command.setParameterBean(pmb);
        command.setOutsideSqlOption(_outsideSqlOption);
        command.setCurrentDBDef(_currentDBDef);
        command.setCursorHandler(handler);
        return command;
    }

    /**
     * Invoke the command of behavior.
     * @param <RESULT> The type of result.
     * @param behaviorCommand The command of behavior. (NotNull)
     * @return The instance of result. (Nullable)
     */
    protected <RESULT> RESULT invoke(BehaviorCommand<RESULT> behaviorCommand) {
        return _behaviorCommandInvoker.invoke(behaviorCommand);
    }

    // ===================================================================================
    //                                                                              Option
    //                                                                              ======
    public OutsideSqlCursorExecutor<PARAMETER_BEAN> dynamicBinding() {
        _outsideSqlOption.dynamicBinding();
        return this;
    }

    public OutsideSqlCursorExecutor<PARAMETER_BEAN> configure(StatementConfig statementConfig) {
        _outsideSqlOption.setStatementConfig(statementConfig);
        return this;
    }
}
