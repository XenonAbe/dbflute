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
package org.seasar.dbflute.logic.jdbc.metadata.sequence;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.seasar.dbflute.logic.jdbc.metadata.info.DfSequenceMetaInfo;

/**
 * @author jflute
 * @since 0.9.6.4 (2010/01/16 Saturday)
 */
public abstract class DfSequenceExtractorBase implements DfSequenceExtractor {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected DataSource _dataSource;
    protected List<String> _allSchemaList;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfSequenceExtractorBase(DataSource dataSource, List<String> allSchemaList) {
        _dataSource = dataSource;
        _allSchemaList = allSchemaList;
    }

    // ===================================================================================
    //                                                                        Sequence Map
    //                                                                        ============
    public Map<String, DfSequenceMetaInfo> getSequenceMap() {
        return doGetSequenceMap();
    }

    protected abstract Map<String, DfSequenceMetaInfo> doGetSequenceMap();

    protected String buildSequenceMapKey(String sequenceOwner, String sequenceName) {
        return (sequenceOwner != null ? sequenceOwner + "." : "") + sequenceName;
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String ln() {
        return "\n";
    }
}