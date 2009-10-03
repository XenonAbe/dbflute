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
package org.seasar.dbflute.twowaysql.node;

import java.lang.reflect.Array;
import java.util.List;

import org.seasar.dbflute.outsidesql.ParameterBean;
import org.seasar.dbflute.twowaysql.context.CommandContext;
import org.seasar.dbflute.util.DfStringUtil;

/**
 * @author jflute
 */
public class BindVariableNode extends AbstractNode {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected String _expression;
    protected String _testValue;
    protected List<String> _nameList;
    protected String _specifiedSql;
    protected boolean _blockNullParameter;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public BindVariableNode(String expression, String testValue, String specifiedSql, boolean blockNullParameter) {
        this._expression = expression;
        this._testValue = testValue;
        this._nameList = DfStringUtil.splitList(expression, ".");
        this._specifiedSql = specifiedSql;
        this._blockNullParameter = blockNullParameter;
    }

    // ===================================================================================
    //                                                                              Accept
    //                                                                              ======
    public void accept(CommandContext ctx) {
        final String firstName = _nameList.get(0);
        assertFirstName(ctx, firstName);
        final Object value = ctx.getArg(firstName);
        final Class<?> clazz = ctx.getArgType(firstName);
        final ValueAndType valueAndType = new ValueAndType();
        valueAndType.setTargetValue(value);
        valueAndType.setTargetType(clazz);
        setupValueAndType(valueAndType);

        if (_blockNullParameter && valueAndType.getTargetValue() == null) {
            throwBindOrEmbeddedParameterNullValueException(valueAndType);
        }
        if (!isInScope()) {
            // Main Root
            ctx.addSql("?", valueAndType.getTargetValue(), valueAndType.getTargetType());
        } else {
            if (List.class.isAssignableFrom(valueAndType.getTargetType())) {
                bindArray(ctx, ((List<?>) valueAndType.getTargetValue()).toArray());
            } else if (valueAndType.getTargetType().isArray()) {
                bindArray(ctx, valueAndType.getTargetValue());
            } else {
                ctx.addSql("?", valueAndType.getTargetValue(), valueAndType.getTargetType());
            }
        }
        if (valueAndType.isValidRearOption()) {
            ctx.addSql(valueAndType.buildRearOptionOnSql());
        }
    }

    protected void assertFirstName(CommandContext ctx, String firstName) {
        final Object arg = ctx.getArg("df:noway");
        if (arg == null) {
            return; // Because the argument has several elements.
        }
        if ((arg instanceof ParameterBean) && !firstName.equals("pmb")) {
            throwBindOrEmbeddedCommentIllegalParameterBeanSpecificationException();
        }
    }

    protected void setupValueAndType(ValueAndType valueAndType) {
        final ValueAndTypeSetupper setuper = new ValueAndTypeSetupper(_expression, _nameList, _specifiedSql, true);
        setuper.setupValueAndType(valueAndType);
    }

    protected void throwBindOrEmbeddedParameterNullValueException(ValueAndType valueAndType) {
        NodeExceptionHandler.throwBindOrEmbeddedCommentParameterNullValueException(_expression, valueAndType
                .getTargetType(), _specifiedSql, true);
    }

    protected boolean isInScope() {
        return _testValue != null && _testValue.startsWith("(") && _testValue.endsWith(")");
    }

    protected void bindArray(CommandContext ctx, Object array) {
        if (array == null) {
            return;
        }
        final int length = Array.getLength(array);
        if (length == 0) {
            throwBindOrEmbeddedParameterEmptyListException();
        }
        Class<?> clazz = null;
        for (int i = 0; i < length; ++i) {
            final Object currentElement = Array.get(array, i);
            if (currentElement != null) {
                clazz = currentElement.getClass();
                break;
            }
        }
        if (clazz == null) {
            throwBindOrEmbeddedParameterNullOnlyListException();
        }
        boolean existsValidElements = false;
        ctx.addSql("(");
        for (int i = 0; i < length; ++i) {
            final Object currentElement = Array.get(array, i);
            if (currentElement != null) {
                if (!existsValidElements) {
                    ctx.addSql("?", currentElement, clazz);
                    existsValidElements = true;
                } else {
                    ctx.addSql(", ?", currentElement, clazz);
                }
            }
        }
        ctx.addSql(")");
    }

    protected void throwBindOrEmbeddedCommentIllegalParameterBeanSpecificationException() {
        NodeExceptionHandler.throwBindOrEmbeddedCommentIllegalParameterBeanSpecificationException(_expression,
                _specifiedSql, true);
    }

    protected void throwBindOrEmbeddedParameterEmptyListException() {
        NodeExceptionHandler.throwBindOrEmbeddedCommentParameterEmptyListException(_expression, _specifiedSql, true);
    }

    protected void throwBindOrEmbeddedParameterNullOnlyListException() {
        NodeExceptionHandler.throwBindOrEmbeddedCommentParameterNullOnlyListException(_expression, _specifiedSql, true);
    }
}
