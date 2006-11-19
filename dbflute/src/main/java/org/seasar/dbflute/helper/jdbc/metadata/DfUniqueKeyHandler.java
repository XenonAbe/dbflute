/*
 * Copyright 2004-2006 the Seasar Foundation and the Others.
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
package org.seasar.dbflute.helper.jdbc.metadata;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class generates an XML schema of an existing database from JDBC metadata..
 * <p>
 * @author mkubo
 * @version $Revision$ $Date$
 */
public class DfUniqueKeyHandler extends DfAbstractMetaDataHandler {

    public static final Log _log = LogFactory.getLog(DfUniqueKeyHandler.class);

    /**
     * Retrieves a list of the columns composing the primary key for a given table.
     * <p>
     * @param dbMeta JDBC metadata.
     * @param tableName Table from which to retrieve PK information.
     * @return A list of the primary key parts for <code>tableName</code>.
     * @throws SQLException
     */
    public List<String> getPrimaryColumnNameList(DatabaseMetaData dbMeta, String schemaName, String tableName)
            throws SQLException {
        final List<String> primaryKeyColumnNameList = new ArrayList<String>();
        ResultSet parts = null;
        try {
            parts = getPrimaryKeyResultSetFromDBMeta(dbMeta, schemaName, tableName);
            while (parts.next()) {
                primaryKeyColumnNameList.add(getPrimaryKeyColumnNameFromDBMeta(parts));
            }
        } finally {
            if (parts != null) {
                parts.close();
            }
        }
        return primaryKeyColumnNameList;
    }

    protected ResultSet getPrimaryKeyResultSetFromDBMeta(DatabaseMetaData dbMeta, String schemaName, String tableName)
            throws SQLException {
        return dbMeta.getPrimaryKeys(null, schemaName, tableName);
    }

    protected String getPrimaryKeyColumnNameFromDBMeta(ResultSet resultSet) throws SQLException {
        return resultSet.getString(4);
    }

    // {WEB���甲��}
    // 
    //�e�[�u���̃C���f�b�N�X�Ɠ��v���̋L�q���擾���܂��B NON_UNIQUE�ATYPE�AINDEX_NAME�AORDINAL_POSITION �̏��ɕ��ׂ܂��B
    //�C���f�b�N�X��̋L�q�ɂ͈ȉ��̃J����������܂��B
    //
    //   1. TABLE_CAT String => �e�[�u�� �J�^���O (null �̏ꍇ������܂�)�B
    //   2. TABLE_SCHEM String => �e�[�u�� �X�L�[�} (null �̏ꍇ������܂�)�B
    //   3. TABLE_NAME String => �e�[�u�����B
    //   4. NON_UNIQUE boolean => ��ӂłȂ��C���f�b�N�X�������邩�ǂ����BTYPE �� tableIndexStatistic �̏ꍇ�� false�B
    //   5. INDEX_QUALIFIER String => �C���f�b�N�X �J�^���O (null �̏ꍇ������܂�)�BTYPE �� tableIndexStatistic �̏ꍇ�� null�B
    //   6. INDEX_NAME String => �C���f�b�N�X���BTYPE �� tableIndexStatistic �̏ꍇ�� null�B
    //   7. TYPE short => �C���f�b�N�X �^�C�v�B
    //          * tableIndexStatistic - �e�[�u���̃C���f�b�N�X�L�q�Ƌ��ɕԂ����e�[�u���̓��v�������ʁB
    //          * tableIndexClustered - �N���X�^�����ꂽ�C���f�b�N�X�B
    //          * tableIndexHashed - �n�b�V�������ꂽ�C���f�b�N�X�B
    //          * tableIndexOther - �ق��̌`���̃C���f�b�N�X�B 
    //   8. ORDINAL_POSITION short => �C���f�b�N�X���̗�̘A�ԁBTYPE �� tableIndexStatistic �̏ꍇ�� 0�B
    //   9. COLUMN_NAME String => �񖼁BTYPE �� tableIndexStatistic �̏ꍇ�� null�B
    //  10. ASC_OR_DESC String => ��̃\�[�g���B"A" => �����B"D" => �~���B�\�[�g�����T�|�[�g���Ă��Ȃ��ꍇ�� null�BTYPE �� tableIndexStatistic �̏ꍇ�� null�B
    //  11. CARDINALITY int => TYPE �� tableIndexStatistic �̏ꍇ�́A�e�[�u�����̍s���B���̂ق��̏ꍇ�́A�C���f�b�N�X���̈�ӂ̒l�̐��B
    //  12. PAGES int => TYPE �� tableIndexStatistic �̏ꍇ�́A�e�[�u���̃y�[�W���B���̂ق��̏ꍇ�́A���݂̃C���f�b�N�X�̃y�[�W���B
    //  13. FILTER_CONDITION String => �t�B���^������ꍇ�́A���̃t�B���^�̏�� (null �̏ꍇ������܂�)�B 
    //
    public Map<String, Map<Integer, String>> getUniqueColumnNameList(DatabaseMetaData dbMeta, String schemaName,
            String tableName) throws SQLException {
        final List<String> primaryColumnNameList = getPrimaryColumnNameList(dbMeta, schemaName, tableName);
        final Map<String, Map<Integer, String>> uniqueMap = new LinkedHashMap<String, Map<Integer, String>>();

        ResultSet parts = null;
        try {
            parts = dbMeta.getIndexInfo(null, schemaName, tableName, true, true);
            while (parts.next()) {
                final boolean isNonUnique;
                {
                    final String nonUnique = parts.getString(4);
                    isNonUnique = (nonUnique != null && nonUnique.equals("true") ? true : false);
                }
                if (isNonUnique) {
                    continue;
                }

                final String indexType;
                {
                    indexType = parts.getString(7);
                }

                final String columnName = parts.getString(9);
                if (columnName == null || columnName.trim().length() == 0) {
                    continue;
                }

                if (primaryColumnNameList.contains(columnName)) {
                    continue;
                }

                final String indexName = parts.getString(6);
                final Integer ordinalPosition;
                {
                    final String ordinalPositionString = parts.getString(8);
                    if (ordinalPositionString == null) {
                        String msg = "The unique columnName should have ordinal-position but null: ";
                        msg = msg + " columnName=" + columnName + " indexType=" + indexType;
                        _log.warn(msg);
                        continue;
                    }
                    try {
                        ordinalPosition = Integer.parseInt(ordinalPositionString);
                    } catch (NumberFormatException e) {
                        String msg = "The unique column should have ordinal-position as number but: ";
                        msg = msg + ordinalPositionString + " columnName=" + columnName + " indexType=" + indexType;
                        _log.warn(msg);
                        continue;
                    }
                }

                if (uniqueMap.containsKey(indexName)) {
                    final Map<Integer, String> uniqueElementMap = uniqueMap.get(indexName);
                    uniqueElementMap.put(ordinalPosition, columnName);
                } else {
                    final Map<Integer, String> uniqueElementMap = new LinkedHashMap<Integer, String>();
                    uniqueElementMap.put(ordinalPosition, columnName);
                    uniqueMap.put(indexName, uniqueElementMap);
                }
            }
        } finally {
            if (parts != null) {
                parts.close();
            }
        }
        return uniqueMap;
    }
}