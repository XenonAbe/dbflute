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
package org.seasar.dbflute.s2dao.metadata;

import org.seasar.dbflute.helper.beans.DfPropertyDesc;

/**
 * {Refers to Seasar and Extends its class}
 * @author jflute
 */
public interface TnBeanAnnotationReader {

    String getColumnAnnotation(DfPropertyDesc pd);

    String getTableAnnotation();

    String getVersionNoPropertyName();

    String getTimestampPropertyName();

    String getId(DfPropertyDesc pd);

    boolean hasRelationNo(DfPropertyDesc pd);

    int getRelationNo(DfPropertyDesc pd);

    String getRelationKey(DfPropertyDesc pd);

    String getValueType(DfPropertyDesc pd);
}
