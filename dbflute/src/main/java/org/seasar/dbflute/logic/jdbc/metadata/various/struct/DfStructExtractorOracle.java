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
package org.seasar.dbflute.logic.jdbc.metadata.various.struct;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.torque.engine.database.model.UnifiedSchema;
import org.seasar.dbflute.helper.StringKeyMap;
import org.seasar.dbflute.helper.jdbc.facade.DfJdbcFacade;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfColumnMetaInfo;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfTypeStructInfo;
import org.seasar.dbflute.util.DfCollectionUtil;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 * @since 0.9.7.6 (2010/11/18 Thursday)
 */
public class DfStructExtractorOracle {

    private static final Log _log = LogFactory.getLog(DfStructExtractorOracle.class);

    protected final DataSource _dataSource;

    public DfStructExtractorOracle(DataSource dataSource) {
        _dataSource = dataSource;
    }

    /**
     * 
     * @param unifiedSchema The unified schema. (NotNull)
     * @return The map of struct info. {key=struct type name} (NotNull)
     */
    public Map<String, DfTypeStructInfo> assistStructInfoMap(UnifiedSchema unifiedSchema) {
        final List<Map<String, String>> resultList = selectStructAttribute(unifiedSchema);
        final Map<String, DfTypeStructInfo> structInfoMap = StringKeyMap.createAsFlexibleOrdered();
        for (Map<String, String> map : resultList) {
            final String typeName = map.get("TYPE_NAME");
            DfTypeStructInfo info = structInfoMap.get(typeName);
            if (info == null) {
                info = new DfTypeStructInfo();
                structInfoMap.put(typeName, info);
            }
            info.setTypeName(typeName);
            final DfColumnMetaInfo attributeInfo = new DfColumnMetaInfo();
            final String attrName = map.get("ATTR_NAME");
            if (Srl.is_Null_or_TrimmedEmpty(attrName)) {
                continue;
            }
            attributeInfo.setColumnName(attrName);

            // in review
            //final String typeOwner = map.get("ATTR_TYPE_OWNER");

            final String dbTypeName = map.get("ATTR_TYPE_NAME");
            attributeInfo.setDbTypeName(dbTypeName);
            final String length = map.get("LENGTH");
            if (Srl.is_NotNull_and_NotTrimmedEmpty(length)) {
                attributeInfo.setColumnSize(Integer.valueOf(length));
            } else {
                final String precision = map.get("PRECISION");
                if (Srl.is_NotNull_and_NotTrimmedEmpty(precision)) {
                    attributeInfo.setColumnSize(Integer.valueOf(precision));
                }
            }
            final String scale = map.get("SCALE");
            if (Srl.is_NotNull_and_NotTrimmedEmpty(scale)) {
                attributeInfo.setDecimalDigits(Integer.valueOf(scale));
            }
            info.putAttributeInfo(attributeInfo);
        }
        return structInfoMap;
    }

    protected List<Map<String, String>> selectStructAttribute(UnifiedSchema unifiedSchema) {
        final DfJdbcFacade facade = new DfJdbcFacade(_dataSource);
        final List<String> columnList = new ArrayList<String>();
        columnList.add("TYPE_NAME");
        columnList.add("ATTR_NAME");
        columnList.add("ATTR_TYPE_OWNER");
        columnList.add("ATTR_TYPE_NAME");
        columnList.add("LENGTH");
        columnList.add("PRECISION");
        columnList.add("SCALE");
        columnList.add("ATTR_NO");
        final String sql = buildStructAttributeSql(unifiedSchema);
        final List<Map<String, String>> resultList;
        try {
            _log.info(sql);
            resultList = facade.selectStringList(sql, columnList);
        } catch (Exception continued) {
            // because of assist info
            _log.info("Failed to select supplement info: " + continued.getMessage());
            return DfCollectionUtil.emptyList();
        }
        return resultList;
    }

    protected String buildStructAttributeSql(UnifiedSchema unifiedSchema) {
        final StringBuilder sb = new StringBuilder();
        sb.append("select *");
        sb.append(" from ALL_TYPE_ATTRS");
        sb.append(" where OWNER = '" + unifiedSchema.getPureSchema() + "'");
        sb.append(" and TYPE_NAME in (");
        sb.append("select TYPE_NAME from ALL_TYPES");
        sb.append(" where OWNER = '" + unifiedSchema.getPureSchema() + "' and TYPECODE = 'OBJECT'");
        sb.append(")");
        sb.append(" order by TYPE_NAME, ATTR_NO");
        return sb.toString();
    }
}
