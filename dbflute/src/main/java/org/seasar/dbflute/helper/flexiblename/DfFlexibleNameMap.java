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
package org.seasar.dbflute.helper.flexiblename;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.seasar.dbflute.util.DfStringUtil;

/**
 * @author jflute
 */
public class DfFlexibleNameMap<KEY, VALUE> {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected LinkedHashMap<KEY, VALUE> map = new LinkedHashMap<KEY, VALUE>();

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfFlexibleNameMap() {
    }
    
    public DfFlexibleNameMap(Map<KEY, ? extends VALUE> map) {
        putAll(map);
    }
    
    public DfFlexibleNameMap(List<KEY> keyList, List<VALUE> valueList) {
        putAll(keyList, valueList);
    }

    // ===================================================================================
    //                                                                        Map Emulator
    //                                                                        ============
    public VALUE get(KEY key) {
        final KEY stringKey = convertStringKey(key);
        if (stringKey != null) {
            return (VALUE) map.get(stringKey);
        } else {
            return (VALUE) map.get(key);
        }
    }

    public VALUE put(KEY key, VALUE value) {
        final KEY stringKey = convertStringKey(key);
        if (stringKey != null) {
            return map.put(stringKey, value);
        } else {
            return map.put(key, value);
        }
    }

    public final void putAll(Map<KEY, ? extends VALUE> map) {
        final Set<KEY> keySet = map.keySet();
        for (KEY key : keySet) {
            put(key, map.get(key));
        }
    }
    
    public final void putAll(List<KEY> keyList, List<VALUE> valueList) {
        if (keyList.size() != valueList.size()) {
            String msg = "The keyList and valueList should have the same size:";
            msg = msg + " keyList.size()=" + keyList.size() + " valueList.size()=" + valueList.size();
            msg = msg + " keyList=" + keyList + " valueList=" + valueList;
            throw new IllegalStateException(msg);
        }
        int index = 0;
        for (KEY key : keyList) {
            put(key, valueList.get(index));
            ++index;
        }
    }

    public VALUE remove(KEY key) {
        final KEY stringKey = convertStringKey(key);
        if (stringKey != null) {
            return map.remove(stringKey);
        } else {
            return map.remove(key);
        }
    }

    public boolean containsKey(KEY key) {
        return get(key) != null;
    }
    
    public void clear() {
        map.clear();
    }
    
    public int size() {
        return map.size();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }
    
    public Set<KEY> keySet() {
        return map.keySet();
    }
    
    public Collection<VALUE> values() {
        return map.values();
    }

    // ===================================================================================
    //                                                                       Key Converter
    //                                                                       =============
    @SuppressWarnings("unchecked")
    protected KEY convertStringKey(KEY key) {
        if (!(key instanceof String)) {
            return null;
        }
        return (KEY) toLowerCaseKey(removeUnderscore((String) key));
    }

    protected String removeUnderscore(String key) {
        return DfStringUtil.replace((String) key, "_", "");
    }

    protected String toLowerCaseKey(String key) {
        return ((String) key).toLowerCase();
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return map.toString();
    }
}