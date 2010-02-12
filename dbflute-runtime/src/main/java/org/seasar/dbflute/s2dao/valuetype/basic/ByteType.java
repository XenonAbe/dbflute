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
package org.seasar.dbflute.s2dao.valuetype.basic;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.seasar.dbflute.s2dao.valuetype.TnAbstractValueType;
import org.seasar.dbflute.util.DfTypeUtil;

/**
 * {Refers to Seasar and Extends its class}
 * @author jflute
 */
public class ByteType extends TnAbstractValueType {

    public ByteType() {
        super(Types.SMALLINT);
    }

    public Object getValue(final ResultSet resultSet, final int index) throws SQLException {
        return DfTypeUtil.toByte(resultSet.getObject(index));
    }

    public Object getValue(final ResultSet resultSet, final String columnName) throws SQLException {
        return DfTypeUtil.toByte(resultSet.getObject(columnName));
    }

    public Object getValue(final CallableStatement cs, final int index) throws SQLException {
        return DfTypeUtil.toByte(cs.getObject(index));
    }

    public Object getValue(final CallableStatement cs, final String parameterName) throws SQLException {
        return DfTypeUtil.toByte(cs.getObject(parameterName));
    }

    public void bindValue(final PreparedStatement ps, final int index, final Object value) throws SQLException {
        if (value == null) {
            setNull(ps, index);
        } else {
            ps.setByte(index, DfTypeUtil.toPrimitiveByte(value));
        }
    }

    public void bindValue(final CallableStatement cs, final String parameterName, final Object value)
            throws SQLException {
        if (value == null) {
            setNull(cs, parameterName);
        } else {
            cs.setByte(parameterName, DfTypeUtil.toPrimitiveByte(value));
        }
    }

    public String toText(Object value) {
        if (value == null) {
            return DfTypeUtil.nullText();
        }
        Byte var = DfTypeUtil.toByte(value);
        return DfTypeUtil.toText(var);
    }
}