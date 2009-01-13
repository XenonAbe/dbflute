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
package org.dbflute.s2dao.metadata;

import java.util.Set;

import org.dbflute.s2dao.identity.TnIdentifierGenerator;
import org.seasar.extension.jdbc.ColumnNotFoundRuntimeException;
import org.seasar.extension.jdbc.PropertyType;
import org.seasar.framework.beans.PropertyNotFoundRuntimeException;

/**
 * It draws upon S2Dao.
 * @author jflute
 */
public interface TnBeanMetaData extends TnDtoMetaData {

    public String getTableName();

    public PropertyType getVersionNoPropertyType() throws PropertyNotFoundRuntimeException;

    public String getVersionNoPropertyName();

    public boolean hasVersionNoPropertyType();

    public PropertyType getTimestampPropertyType() throws PropertyNotFoundRuntimeException;

    public String getTimestampPropertyName();

    public boolean hasTimestampPropertyType();

    public String convertFullColumnName(String alias);

    public PropertyType getPropertyTypeByAliasName(String aliasName) throws ColumnNotFoundRuntimeException;

    public PropertyType getPropertyTypeByColumnName(String columnName) throws ColumnNotFoundRuntimeException;

    public boolean hasPropertyTypeByColumnName(String columnName);

    public boolean hasPropertyTypeByAliasName(String aliasName);

    public int getRelationPropertyTypeSize();

    public TnRelationPropertyType getRelationPropertyType(int index);

    public TnRelationPropertyType getRelationPropertyType(String propertyName) throws PropertyNotFoundRuntimeException;

    public int getPrimaryKeySize();

    public String getPrimaryKey(int index);

    public int getIdentifierGeneratorSize();

    public TnIdentifierGenerator getIdentifierGenerator(int index);

    public TnIdentifierGenerator getIdentifierGenerator(String propertyName);

    public Set<String> getModifiedPropertyNames(Object bean);

}
