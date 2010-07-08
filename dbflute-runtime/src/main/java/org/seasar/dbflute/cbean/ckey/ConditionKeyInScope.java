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
package org.seasar.dbflute.cbean.ckey;

import java.util.List;

import org.seasar.dbflute.cbean.coption.ConditionOption;
import org.seasar.dbflute.cbean.cvalue.ConditionValue;
import org.seasar.dbflute.cbean.sqlclause.query.QueryClause;
import org.seasar.dbflute.dbmeta.name.ColumnRealName;

/**
 * The condition-key of inScope.
 * @author jflute
 */
public class ConditionKeyInScope extends ConditionKey {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Serial version UID. (Default) */
    private static final long serialVersionUID = 1L;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    /**
     * Constructor.
     */
    protected ConditionKeyInScope() {
        _conditionKey = "inScope";
        _operand = "in";
    }

    // ===================================================================================
    //                                                                      Implementation
    //                                                                      ==============
    /**
     * {@inheritDoc}
     */
    protected boolean doIsValidRegistration(ConditionValue cvalue, Object value, ColumnRealName callerName) {
        return value != null && value instanceof List<?> && !((List<?>) value).isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    protected void doAddWhereClause(List<QueryClause> conditionList, ColumnRealName columnRealName, ConditionValue value) {
        conditionList.add(buildBindClause(columnRealName, value.getInScopeLatestLocation(), "('a1', 'a2')"));
    }

    /**
     * {@inheritDoc}
     */
    protected void doAddWhereClause(List<QueryClause> conditionList, ColumnRealName columnRealName,
            ConditionValue value, ConditionOption option) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    protected void doSetupConditionValue(ConditionValue conditionValue, Object value, String location) {
        conditionValue.setupInScope(value, location);
    }

    /**
     * {@inheritDoc}
     */
    protected void doSetupConditionValue(ConditionValue conditionValue, Object value, String location,
            ConditionOption option) {
        throw new UnsupportedOperationException();
    }
}
