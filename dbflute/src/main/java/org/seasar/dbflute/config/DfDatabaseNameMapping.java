/*
 * Copyright 2004-2008 the Seasar Foundation and the Others.
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
package org.seasar.dbflute.config;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.seasar.dbflute.DBDef;
import org.seasar.dbflute.helper.mapstring.MapListString;
import org.seasar.dbflute.helper.mapstring.impl.MapListStringImpl;

/**
 * @author jflute
 */
public class DfDatabaseNameMapping {

    private static final DfDatabaseNameMapping _instance = new DfDatabaseNameMapping();

    // ===============================================================================
    //                                                                       Attribute
    //                                                                       =========
    protected final String _databaseNameMappingString;
    {
        String tmp = "map:{";
        tmp = tmp + "     ; mysql      = map:{generateName = MySql      ; defName = mysql}";
        tmp = tmp + "     ; postgresql = map:{generateName = PostgreSql ; defName = postgresql}";
        tmp = tmp + "     ; oracle     = map:{generateName = Oracle     ; defName = oracle}";
        tmp = tmp + "     ; db2        = map:{generateName = Db2        ; defName = db2}";
        tmp = tmp + "     ; mssql      = map:{generateName = SqlServer  ; defName = sqlserver}";
        tmp = tmp + "     ; h2         = map:{generateName = H2         ; defName = h2}";
        tmp = tmp + "     ; derby      = map:{generateName = Derby      ; defName = derby}";
        tmp = tmp + "     ; firebird   = map:{generateName = Firebird   ; defName = firebird}";
        tmp = tmp + "     ; interbase  = map:{generateName = Interbase  ; defName = unknown}";
        tmp = tmp + "     ; msaccess   = map:{generateName = Default    ; defName = msaccess}";
        tmp = tmp + "     ; default    = map:{generateName = Default    ; defName = unknown}";
        tmp = tmp + "}";
        _databaseNameMappingString = tmp;
    }
    protected final Map<String, Map<String, String>> _databaseNameMappingMap;
    {
        _databaseNameMappingMap = analyze();
    }

    private DfDatabaseNameMapping() {
    }

    public static DfDatabaseNameMapping getInstance() {
        return _instance;
    }

    // ===============================================================================
    //                                                                       Analyzing
    //                                                                       =========
    protected Map<String, Map<String, String>> analyze() {
        final MapListString mapListString = new MapListStringImpl();
        mapListString.setDelimiter(";");
        final Map<String, Object> map = mapListString.generateMap(_databaseNameMappingString);
        final Map<String, Map<String, String>> realMap = new LinkedHashMap<String, Map<String, String>>();
        final Set<Entry<String, Object>> entrySet = map.entrySet();
        for (Entry<String, Object> entry : entrySet) {
            final String key = entry.getKey();
            final Map<?, ?> elementMap = (Map<?, ?>) entry.getValue();
            final Map<String, String> elementRealMap = new LinkedHashMap<String, String>();
            final Set<?> elementEntrySet = elementMap.entrySet();
            for (Object object : elementEntrySet) {
                @SuppressWarnings("unchecked")
                final Entry<Object, Object> elementEntry = (Entry<Object, Object>) object;
                final Object elementKey = elementEntry.getKey();
                final Object elementValue = elementEntry.getValue();
                elementRealMap.put((String) elementKey, (String) elementValue);
            }
            realMap.put(key, elementRealMap);
        }
        return realMap;
    }

    // ===============================================================================
    //                                                                         Mapping
    //                                                                         =======
    public Map<String, String> findMapping(String databaseType) {
        Map<String, String> map = _databaseNameMappingMap.get(databaseType);
        if (map == null) {
            map = _databaseNameMappingMap.get("default");
        }
        return map;
    }

    public String findGenerateName(String databaseType) {
        final Map<String, String> mapping = findMapping(databaseType);
        final String generateName = (String) mapping.get("generateName");
        if (generateName == null || generateName.trim().length() == 0) {
            String msg = "The database should have its generateName: " + mapping;
            throw new IllegalStateException(msg);
        }
        return generateName;
    }

    public DBDef findDBDef(String databaseType) {
        final Map<String, String> mapping = findMapping(databaseType);
        final String defName = (String) mapping.get("defName");
        if (defName == null || defName.trim().length() == 0) {
            String msg = "The database should have its defName: " + mapping;
            throw new IllegalStateException(msg);
        }
        final DBDef dbdef = DBDef.codeOf(defName);
        return dbdef != null ? dbdef : DBDef.Unknown;
    }
}
