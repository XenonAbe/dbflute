/*
 * Copyright 2004-2013 the Seasar Foundation and the Others.
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
package org.seasar.dbflute.properties.assistant.freegen;

import java.util.List;
import java.util.Map;

import org.seasar.dbflute.util.DfCollectionUtil;

/**
 * @author jflute
 */
public class DfFreeGenTable {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Map<String, Object> _tableMap;
    protected final String _tableName;
    protected final List<Map<String, Object>> _columnList;
    protected final List<Map<String, Object>> _tableList;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfFreeGenTable(Map<String, Object> tableMap, String tableName, List<Map<String, Object>> columnList) {
        _tableMap = tableMap;
        _tableName = tableName;
        _columnList = columnList;
        _tableList = DfCollectionUtil.emptyList();
    }

    public DfFreeGenTable(Map<String, Object> tableMap, List<Map<String, Object>> tableList) {
        _tableMap = tableMap;
        _tableName = null;
        _columnList = DfCollectionUtil.emptyList();
        _tableList = tableList;
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        if (_tableName != null) {
            return "{tableName=" + _tableName + ", rowList.size()=" + _columnList.size() + "}";
        } else {
            return "{tableList.size=" + _tableList.size() + "}";
        }
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public Map<String, Object> getTableMap() {
        return _tableMap;
    }

    public boolean isOneTable() {
        return _tableName != null;
    }

    public String getTableName() {
        return _tableName;
    }

    public List<Map<String, Object>> getColumnList() {
        return _columnList;
    }

    public List<Map<String, Object>> getTableList() {
        return _tableList;
    }
}
