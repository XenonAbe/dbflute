package org.seasar.dbflute.logic.jdbc.diff;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.torque.engine.EngineException;
import org.apache.torque.engine.database.model.Column;
import org.apache.torque.engine.database.model.Database;
import org.apache.torque.engine.database.model.Table;
import org.seasar.dbflute.DfBuildProperties;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.infra.schemadiff.SchemaDiffFile;
import org.seasar.dbflute.logic.jdbc.schemaxml.DfSchemaXmlReader;
import org.seasar.dbflute.properties.DfBasicProperties;
import org.seasar.dbflute.util.DfCollectionUtil;
import org.seasar.dbflute.util.DfSystemUtil;
import org.seasar.dbflute.util.DfTypeUtil;

/**
 * @author jflute
 * @since 0.9.7.1 (2010/06/06 Sunday)
 */
public class DfSchemaDiff {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static final String DIFF_DATE_KEY = "$$DiffDate$$";
    protected static final String DIFF_DATE_PATTERN = "yyyy/MM/dd HH:mm:ss";
    protected static final String TABLE_COUNT_KEY = "$$TableCountDiff$$";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected Database _nextDb; // not null after loading
    protected Database _previousDb; // not null after loading
    protected Date _diffDate; // not null after loading next schema
    protected boolean _firstTime; // judged when loading previous schema
    protected boolean _loadingFailure; // judged when loading previous schema
    protected final List<DfTableDiff> _addedTableList = DfCollectionUtil.newArrayList();
    protected final List<DfTableDiff> _changedTableList = DfCollectionUtil.newArrayList();
    protected final List<DfTableDiff> _deletedTableList = DfCollectionUtil.newArrayList();

    // ===================================================================================
    //                                                                         Load Schema
    //                                                                         ===========
    public void loadNextSchema() { // after loading previous schema
        if (isFirstTime()) {
            String msg = "You should not call this because of first time.";
            throw new IllegalStateException(msg);
        }
        if (_previousDb == null) {
            String msg = "You should not call this because of previous not loaded.";
            throw new IllegalStateException(msg);
        }
        final DfSchemaXmlReader reader = createSchemaXmlReader();
        try {
            reader.read();
        } catch (IOException e) {
            handleException(e);
        }
        try {
            _nextDb = reader.getSchemaData().getDatabase();
        } catch (EngineException e) {
            handleException(e);
        }
        _diffDate = new Date(DfSystemUtil.currentTimeMillis());
    }

    public void loadPreviousSchema() { // before loading next schema
        final DfSchemaXmlReader reader = createSchemaXmlReader();
        try {
            reader.read();
        } catch (FileNotFoundException normal) {
            _firstTime = true;
            return;
        } catch (IOException e) {
            _loadingFailure = true;
            handleException(e);
        }
        try {
            _previousDb = reader.getSchemaData().getDatabase();
        } catch (EngineException e) {
            _loadingFailure = true;
            handleException(e);
        }
    }

    protected void handleException(Exception e) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to load schema XML.");
        br.addItem("SchemaXML");
        br.addElement(getSchemaXmlFilePath());
        br.addItem("DatabaseType");
        br.addElement(getDatabaseType());
        br.addItem("Exception");
        br.addElement(e.getClass().getName());
        br.addElement(e.getMessage());
        final String msg = br.buildExceptionMessage();
        throw new IllegalStateException(msg, e);
    }

    // ===================================================================================
    //                                                                             Analyze
    //                                                                             =======
    public void analyzeDiff() {
        processAddedTable();
        processChangedTable();
        processDeletedTable();
    }

    // ===================================================================================
    //                                                                       Table Process
    //                                                                       =============
    protected void processAddedTable() {
        final List<Table> tableList = _nextDb.getTableList();
        for (Table table : tableList) {
            final Table found = findPreviousTable(table);
            if (found == null) { // added
                _addedTableList.add(DfTableDiff.createAdded(table.getName()));
            }
        }
    }

    protected void processChangedTable() {
        final List<Table> tableList = _nextDb.getTableList();
        for (Table next : tableList) {
            final Table previous = findPreviousTable(next);
            if (previous == null) {
                continue;
            }
            final DfTableDiff diff = DfTableDiff.createChanged(next.getName());
            if (!isSameSchema(next, previous)) {
                final String nextSchema = next.getUnifiedSchema().getCatalogSchema();
                final String previousSchema = previous.getUnifiedSchema().getCatalogSchema();
                diff.setSchemaDiff(createNextPreviousBean(nextSchema, previousSchema));
            }
            if (!isSameObjectType(next, previous)) {
                diff.setObjectTypeDiff(createNextPreviousBean(next.getType(), previous.getType()));
            }
            processAddedColumn(diff, next, previous);
            processChangedColumn(diff, next, previous);
            processDeletedColumn(diff, next, previous);
            if (diff.hasDiff()) {
                _changedTableList.add(diff);
            }
        }
    }

    protected void processDeletedTable() {
        final List<Table> tableList = _previousDb.getTableList();
        for (Table table : tableList) {
            final Table found = findNextTable(table);
            if (found == null) { // deleted
                _deletedTableList.add(DfTableDiff.createDeleted(table.getName()));
            }
        }
    }

    protected boolean isSameSchema(Table next, Table previous) {
        return isSame(next.getUnifiedSchema(), previous.getUnifiedSchema());
    }

    protected boolean isSameObjectType(Table next, Table previous) {
        return isSame(next.getType(), previous.getType());
    }

    // ===================================================================================
    //                                                                      Column Process
    //                                                                      ==============
    protected void processAddedColumn(DfTableDiff tableDiff, Table nextTable, Table previousTable) {
        final List<Column> columnList = nextTable.getColumnList();
        for (Column column : columnList) {
            final Column found = previousTable.getColumn(column.getName());
            if (found == null) { // added
                tableDiff.addAddedColumn(DfColumnDiff.createAdded(column.getName()));
            }
        }
    }

    protected void processChangedColumn(DfTableDiff tableDiff, Table nextTable, Table previousTable) {
        final List<Column> columnList = nextTable.getColumnList();
        for (Column next : columnList) {
            final Column previous = previousTable.getColumn(next.getName());
            if (previous == null) {
                continue;
            }
            final DfColumnDiff diff = DfColumnDiff.createChanged(next.getName());
            if (!isSameDbType(next, previous)) {
                diff.setDbTypeDiff(createNextPreviousBean(next.getDbType(), previous.getDbType()));
            }
            if (!isSameColumnSize(next, previous)) {
                diff.setColumnSizeDiff(createNextPreviousBean(next.getColumnSize(), previous.getColumnSize()));
            }
            tableDiff.addChangedColumn(diff);
        }
    }

    protected void processDeletedColumn(DfTableDiff tableDiff, Table nextTable, Table previousTable) {
        final List<Column> columnList = previousTable.getColumnList();
        for (Column column : columnList) {
            final Column found = nextTable.getColumn(column.getName());
            if (found == null) { // deleted
                tableDiff.addAddedColumn(DfColumnDiff.createDeleted(column.getName()));
            }
        }
    }

    protected boolean isSameDbType(Column next, Column previous) {
        return isSame(next.getDbType(), previous.getDbType());
    }

    protected boolean isSameColumnSize(Column next, Column previous) {
        return isSame(next.getColumnSize(), previous.getColumnSize());
    }

    // ===================================================================================
    //                                                                         Find Object
    //                                                                         ===========
    protected Table findNextTable(Table table) {
        return _nextDb.getTable(table.getName());
    }

    protected Table findPreviousTable(Table table) {
        return _previousDb.getTable(table.getName());
    }

    // ===================================================================================
    //                                                                       Assist Helper
    //                                                                       =============
    protected DfNextPreviousBean createNextPreviousBean(String next, String previous) {
        return new DfNextPreviousBean(next, previous);
    }

    protected boolean isSame(Object next, Object previous) {
        if (next == null && previous == null) {
            return true;
        }
        if (next == null || previous == null) {
            return false;
        }
        return next.equals(previous);
    }

    // ===================================================================================
    //                                                                              Status
    //                                                                              ======
    public boolean hasDiff() {
        for (DfTableDiff diff : _addedTableList) {
            if (diff.hasDiff()) {
                return true;
            }
        }
        for (DfTableDiff diff : _changedTableList) {
            if (diff.hasDiff()) {
                return true;
            }
        }
        for (DfTableDiff diff : _deletedTableList) {
            if (diff.hasDiff()) {
                return true;
            }
        }
        return false;
    }

    public boolean isFirstTime() {
        return _firstTime;
    }

    public boolean isLoadingFailure() {
        return _loadingFailure;
    }

    // ===================================================================================
    //                                                                             DiffMap
    //                                                                             =======
    public Map<String, Object> createDiffMap() {
        final Map<String, Object> tableDiffMap = DfCollectionUtil.newLinkedHashMap();
        tableDiffMap.put(DIFF_DATE_KEY, DfTypeUtil.toString(_diffDate, DIFF_DATE_PATTERN));
        final Map<String, Object> tableCountMap = DfCollectionUtil.newLinkedHashMap();
        tableCountMap.put("next", _nextDb.getTableList().size());
        tableCountMap.put("previous", _previousDb.getTableList().size());
        tableCountMap.put("added", _addedTableList.size());
        tableCountMap.put("changed", _changedTableList.size());
        tableCountMap.put("deleted", _deletedTableList.size());
        tableDiffMap.put(TABLE_COUNT_KEY, tableCountMap);
        for (DfTableDiff diff : _addedTableList) {
            if (diff.hasDiff()) {
                tableDiffMap.put(diff.getTableName(), diff.createDiffMap());
            }
        }
        for (DfTableDiff diff : _changedTableList) {
            if (diff.hasDiff()) {
                tableDiffMap.put(diff.getTableName(), diff.createDiffMap());
            }
        }
        for (DfTableDiff diff : _deletedTableList) {
            if (diff.hasDiff()) {
                tableDiffMap.put(diff.getTableName(), diff.createDiffMap());
            }
        }
        return tableDiffMap;
    }

    public void serializeSchemaDiff() throws IOException {
        final String path = getDiffMapFilePath();
        final SchemaDiffFile diffFile = new SchemaDiffFile();
        final File file = new File(path);

        // ordered by DIFF_DATE desc
        final Map<String, Object> serializedMap = DfCollectionUtil.newLinkedHashMap();
        final Map<String, Object> diffMap = createDiffMap();
        serializedMap.put((String) diffMap.get(DIFF_DATE_KEY), diffMap);

        if (file.exists()) {
            FileInputStream ins = null;
            try {
                ins = new FileInputStream(file);
                final Map<String, Object> existingMap = diffFile.readMap(ins);
                final Set<Entry<String, Object>> entrySet = existingMap.entrySet();
                int count = 0;
                final int historyLimit = getHistoryLimit();
                final boolean historyLimitValid = historyLimit >= 0;
                for (Entry<String, Object> entry : entrySet) {
                    if (historyLimitValid && count >= historyLimit) {
                        break;
                    }
                    serializedMap.put(entry.getKey(), entry.getValue());
                    ++count;
                }
            } finally {
                if (ins != null) {
                    ins.close();
                }
            }
        } else {
            file.createNewFile();
        }

        FileOutputStream ous = null;
        try {
            ous = new FileOutputStream(path);
            diffFile.writeMap(ous, serializedMap);
        } finally {
            if (ous != null) {
                ous.close();
            }
        }
    }

    protected int getHistoryLimit() {
        return -1; // as default (no limit)
    }

    // ===================================================================================
    //                                                                       Schema Reader
    //                                                                       =============
    protected DfSchemaXmlReader createSchemaXmlReader() {
        return new DfSchemaXmlReader(getSchemaXmlFilePath(), getDatabaseType());
    }

    protected String getSchemaXmlFilePath() {
        return getBasicProperties().getProejctSchemaXMLFilePath();
    }

    protected String getDatabaseType() {
        return getBasicProperties().getDatabaseType();
    }

    public String getDiffMapFilePath() {
        return getBasicProperties().getProejctSchemaDiffMapFilePath();
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected DfBasicProperties getBasicProperties() {
        return DfBuildProperties.getInstance().getBasicProperties();
    }
}