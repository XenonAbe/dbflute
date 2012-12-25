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
package org.seasar.dbflute.logic.doc.prophtml;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.seasar.dbflute.util.DfCollectionUtil;

/**
 * @author jflute
 * @since 1.0.1 (2012/12/21 Friday)
 */
public class DfPropHtmlFileAttribute {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final File _propertiesFile;
    protected final String _envType;
    protected final String _langType;
    protected Integer _keyCount;
    protected final Set<String> _propertyKeySet = DfCollectionUtil.newLinkedHashSet();
    protected boolean _rootFile;
    protected boolean _defaultEnv;
    protected boolean _lonely;
    protected DfPropHtmlFileAttribute _standardAttribute;
    protected final List<String> _duplicateKeyList = DfCollectionUtil.newArrayList();
    protected final List<String> _overKeyList = DfCollectionUtil.newArrayList();
    protected final List<String> _shortKeyList = DfCollectionUtil.newArrayList();

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfPropHtmlFileAttribute(File propertiesFile, String envType, String langType) {
        _propertiesFile = propertiesFile;
        _envType = envType;
        _langType = langType;
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public File getPropertiesFile() {
        return _propertiesFile;
    }

    public String getEnvType() {
        return _envType;
    }

    public String getLangType() {
        return _langType;
    }

    public Integer getKeyCount() {
        return _keyCount;
    }

    public void setKeyCount(Integer keyCount) {
        _keyCount = keyCount;
    }

    public boolean isRootFile() {
        return _rootFile;
    }

    public void toBeRootFile() {
        _rootFile = true;
    }

    public boolean isDefaultEnv() {
        return _defaultEnv;
    }

    public void toBeDefaultEnv() {
        _defaultEnv = true;
    }

    public boolean isLonely() {
        return _lonely;
    }

    public void toBeLonely() {
        _lonely = true;
    }

    public DfPropHtmlFileAttribute getStandardAttribute() {
        return _standardAttribute;
    }

    public void setStandardAttribute(DfPropHtmlFileAttribute standardAttribute) {
        _standardAttribute = standardAttribute;
    }

    public Set<String> getPropertyKeySet() {
        return _propertyKeySet;
    }

    public void addPropertyKeyAll(Set<String> propertyKeySet) {
        _propertyKeySet.addAll(propertyKeySet);
    }

    public boolean hasDuplicateKey() {
        return !_duplicateKeyList.isEmpty();
    }

    public List<String> getDuplicateKeyList() {
        return _duplicateKeyList;
    }

    public void addDuplicateKeyAll(List<String> duplicateKeyList) {
        _duplicateKeyList.addAll(duplicateKeyList);
    }

    public boolean hasOverKey() {
        return !_overKeyList.isEmpty();
    }

    public List<String> getOverKeyList() {
        return _overKeyList;
    }

    public void addOverKey(String propertyKey) {
        _overKeyList.add(propertyKey);
    }

    public boolean hasShortKey() {
        return !_shortKeyList.isEmpty();
    }

    public List<String> getShortKeyList() {
        return _shortKeyList;
    }

    public void addShortKey(String propertyKey) {
        _shortKeyList.add(propertyKey);
    }
}