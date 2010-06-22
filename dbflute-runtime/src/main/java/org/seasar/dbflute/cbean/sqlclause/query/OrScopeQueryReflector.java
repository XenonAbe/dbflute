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
package org.seasar.dbflute.cbean.sqlclause.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.seasar.dbflute.cbean.sqlclause.join.LeftOuterJoinInfo;

/**
 * @author jflute
 */
public class OrScopeQueryReflector {

    protected final List<QueryClause> _whereList;
    protected final List<QueryClause> _baseTableInlineWhereList;
    protected final Map<String, LeftOuterJoinInfo> _outerJoinMap;
    protected final OrScopeQuerySetupper _setupper = new OrScopeQuerySetupper();

    public OrScopeQueryReflector(List<QueryClause> whereList, List<QueryClause> baseTableInlineWhereList,
            Map<String, LeftOuterJoinInfo> outerJoinMap) {
        _whereList = whereList;
        _baseTableInlineWhereList = baseTableInlineWhereList;
        _outerJoinMap = outerJoinMap;
    }

    public void reflectTmpOrClauseToRealObject(OrScopeQueryInfo localInfo) {
        {
            final List<OrScopeQueryClauseGroup> groupList = setupTmpOrListList(localInfo,
                    new OrScopeQueryClauseListProvider() {
                        public List<QueryClause> provide(OrScopeQueryInfo tmpOrScopeQueryInfo) {
                            return tmpOrScopeQueryInfo.getTmpOrWhereList();
                        }
                    });
            setupOrScopeQuery(groupList, _whereList, true);
        }
        {
            final List<OrScopeQueryClauseGroup> groupList = setupTmpOrListList(localInfo,
                    new OrScopeQueryClauseListProvider() {
                        public List<QueryClause> provide(OrScopeQueryInfo tmpOrScopeQueryInfo) {
                            return tmpOrScopeQueryInfo.getTmpOrBaseTableInlineWhereList();
                        }
                    });
            setupOrScopeQuery(groupList, _baseTableInlineWhereList, false);
        }
        {
            final Set<Entry<String, LeftOuterJoinInfo>> entrySet = _outerJoinMap.entrySet();
            for (Entry<String, LeftOuterJoinInfo> entry : entrySet) {
                final String aliasName = entry.getKey();
                final LeftOuterJoinInfo joinInfo = entry.getValue();
                final List<OrScopeQueryClauseGroup> groupList = new ArrayList<OrScopeQueryClauseGroup>();
                groupList.addAll(setupTmpOrListList(localInfo, new OrScopeQueryClauseListProvider() {
                    public List<QueryClause> provide(OrScopeQueryInfo tmpOrScopeQueryInfo) {
                        return tmpOrScopeQueryInfo.getTmpOrAdditionalOnClauseList(aliasName);
                    }
                }));
                setupOrScopeQuery(groupList, joinInfo.getAdditionalOnClauseList(), false);
            }
        }
        {
            final Set<Entry<String, LeftOuterJoinInfo>> entrySet = _outerJoinMap.entrySet();
            for (Entry<String, LeftOuterJoinInfo> entry : entrySet) {
                final String aliasName = entry.getKey();
                final LeftOuterJoinInfo joinInfo = entry.getValue();
                final List<OrScopeQueryClauseGroup> groupList = new ArrayList<OrScopeQueryClauseGroup>();
                groupList.addAll(setupTmpOrListList(localInfo, new OrScopeQueryClauseListProvider() {
                    public List<QueryClause> provide(OrScopeQueryInfo tmpOrScopeQueryInfo) {
                        return tmpOrScopeQueryInfo.getTmpOrOuterJoinInlineClauseList(aliasName);
                    }
                }));
                setupOrScopeQuery(groupList, joinInfo.getInlineWhereClauseList(), false);
            }
        }
    }

    protected List<OrScopeQueryClauseGroup> setupTmpOrListList(OrScopeQueryInfo parentInfo,
            OrScopeQueryClauseListProvider provider) {
        final List<OrScopeQueryClauseGroup> resultList = new ArrayList<OrScopeQueryClauseGroup>();
        final OrScopeQueryClauseGroup groupInfo = new OrScopeQueryClauseGroup();
        groupInfo.setOrClauseList(provider.provide(parentInfo));
        resultList.add(groupInfo);
        if (parentInfo.hasChildInfo()) {
            for (OrScopeQueryInfo childInfo : parentInfo.getChildInfoList()) {
                resultList.addAll(setupTmpOrListList(childInfo, provider)); // recursive call
            }
        }
        return resultList;
    }

    protected void setupOrScopeQuery(List<OrScopeQueryClauseGroup> clauseGroupList, List<QueryClause> realList,
            boolean line) {
        _setupper.setupOrScopeQuery(clauseGroupList, realList, line);
    }
}
