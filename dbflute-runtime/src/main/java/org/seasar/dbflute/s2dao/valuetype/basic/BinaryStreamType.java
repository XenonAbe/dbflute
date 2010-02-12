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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.seasar.dbflute.s2dao.valuetype.TnAbstractValueType;
import org.seasar.dbflute.util.DfResourceUtil;
import org.seasar.dbflute.util.DfTypeUtil;

/**
 * {Refers to Seasar and Extends its class}
 * @author jflute
 */
public class BinaryStreamType extends TnAbstractValueType {

    public BinaryStreamType() {
        super(Types.BINARY);
    }

    public Object getValue(ResultSet resultSet, int index) throws SQLException {
        return resultSet.getBinaryStream(index);
    }

    public Object getValue(ResultSet resultSet, String columnName) throws SQLException {
        return resultSet.getBinaryStream(columnName);
    }

    public Object getValue(CallableStatement cs, int index) throws SQLException {
        return toBinaryStream(cs.getBytes(index));
    }

    public Object getValue(CallableStatement cs, String parameterName) throws SQLException {
        return toBinaryStream(cs.getBytes(parameterName));
    }

    private InputStream toBinaryStream(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        return new ByteArrayInputStream(bytes);
    }

    public void bindValue(PreparedStatement ps, int index, Object value) throws SQLException {
        if (value == null) {
            setNull(ps, index);
        } else if (value instanceof InputStream) {
            InputStream is = (InputStream) value;
            ps.setBinaryStream(index, is, DfResourceUtil.available(is));
        } else {
            ps.setObject(index, value);
        }
    }

    public void bindValue(CallableStatement cs, String parameterName, Object value) throws SQLException {
        if (value == null) {
            setNull(cs, parameterName);
        } else if (value instanceof InputStream) {
            InputStream is = (InputStream) value;
            cs.setBinaryStream(parameterName, is, DfResourceUtil.available(is));
        } else {
            cs.setObject(parameterName, value);
        }
    }

    public String toText(Object value) {
        if (value == null) {
            return DfTypeUtil.nullText();
        }
        return DfTypeUtil.toText(value);
    }
}