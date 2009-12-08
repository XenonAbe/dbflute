package org.apache.torque.engine.database.model;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Turbine" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Turbine", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.torque.engine.EngineException;
import org.apache.velocity.texen.util.FileUtil;
import org.seasar.dbflute.DfBuildProperties;
import org.seasar.dbflute.config.DfDatabaseConfig;
import org.seasar.dbflute.friends.torque.DfAdditionalForeignKeyInitializer;
import org.seasar.dbflute.friends.torque.DfAdditionalPrimaryKeyInitializer;
import org.seasar.dbflute.friends.torque.DfAdditionalUniqueKeyInitializer;
import org.seasar.dbflute.friends.velocity.DfGenerator;
import org.seasar.dbflute.helper.StringKeyMap;
import org.seasar.dbflute.helper.jdbc.context.DfDataSourceContext;
import org.seasar.dbflute.logic.bqp.DfBehaviorQueryPathSetupper;
import org.seasar.dbflute.logic.deletefile.DfOldClassHandler;
import org.seasar.dbflute.logic.initializer.IncludeQueryInitializer;
import org.seasar.dbflute.logic.jdbc.handler.DfProcedureHandler;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfProcedureMetaInfo;
import org.seasar.dbflute.logic.outsidesql.DfSqlFileCollector;
import org.seasar.dbflute.logic.pathhandling.DfPackagePathHandler;
import org.seasar.dbflute.logic.pmb.DfParameterBeanBasicHandler;
import org.seasar.dbflute.logic.pmb.DfParameterBeanMetaData;
import org.seasar.dbflute.properties.DfBasicProperties;
import org.seasar.dbflute.properties.DfBuriProperties;
import org.seasar.dbflute.properties.DfClassificationProperties;
import org.seasar.dbflute.properties.DfSequenceIdentityProperties.SequenceDefinitionMapChecker;
import org.seasar.dbflute.properties.assistant.DfTableFinder;
import org.seasar.dbflute.properties.assistant.commoncolumn.CommonColumnSetupResource;
import org.xml.sax.Attributes;

/**
 * A class for holding application data structures.
 * @author Modified by jflute
 */
public class Database {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Log instance. */
    private static final Log _log = LogFactory.getLog(Database.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                                 Basic
    //                                                 -----
    protected Integer _version;
    protected String _name;

    // -----------------------------------------------------
    //                                               AppData
    //                                               -------
    protected AppData _appData;
    protected AppData _sql2entitySchemaData; // when sql2entity only

    // -----------------------------------------------------
    //                                                 Table
    //                                                 -----
    protected List<Table> _tableList = new ArrayList<Table>(100);
    protected StringKeyMap<Table> _tableMap = StringKeyMap.createAsFlexibleOrder();

    // -----------------------------------------------------
    //                                        for Sql2Entity
    //                                        --------------
    /** The meta data of parameter bean. */
    protected Map<String, DfParameterBeanMetaData> _pmbMetaDataMap;

    // -----------------------------------------------------
    //                                                 Other
    //                                                 -----
    protected String _databaseType;
    protected String _defaultJavaNamingMethod;
    protected boolean _skipDeleteOldClass;

    // [Unused on DBFlute]
    // protected String _pkg;
    // protected String _defaultIdMethod;
    // protected String _defaultJavaType;
    // protected boolean _isHeavyIndexing;

    // ===================================================================================
    //                                                                             Version
    //                                                                             =======
    public void initializeVersion(Integer version) {
        DfBuildProperties.getInstance().setVersion(version);
    }

    // ===================================================================================
    //                                                                             Loading
    //                                                                             =======
    /**
     * Load the database object from an XML tag.
     * @param attrib the XML attributes
     */
    public void loadFromXML(Attributes attrib) {
        setName(attrib.getValue("name"));
        _defaultJavaNamingMethod = attrib.getValue("defaultJavaNamingMethod"); // Basically Null
        if (_defaultJavaNamingMethod == null) {
            _defaultJavaNamingMethod = NameGenerator.CONV_METHOD_UNDERSCORE; // Basically Here!
        }

    }

    // ===================================================================================
    //                                                                               Table
    //                                                                               =====
    public Table[] getTables() {
        int size = _tableList.size();
        Table[] tbls = new Table[size];
        for (int i = 0; i < size; i++) {
            tbls[i] = (Table) _tableList.get(i);
        }
        return tbls;
    }

    public List<Table> getTableList() {
        final List<Table> ls = new ArrayList<Table>();
        final Table[] tables = getTables();
        for (Table table : tables) {
            ls.add(table);
        }
        return ls;
    }

    public List<Table> getTableDisplaySortedList() {
        final Comparator<Table> tableDisplayOrderBy = getProperties().getDocumentProperties().getTableDisplayOrderBy();
        final TreeSet<Table> tableSet = new TreeSet<Table>(tableDisplayOrderBy);
        tableSet.addAll(getTableList());
        return new ArrayList<Table>(tableSet);
    }

    public Table getTable(String name) {
        return _tableMap.get(name);
    }

    public Table addTable(Attributes attrib) {
        Table tbl = new Table();
        tbl.setDatabase(this);
        tbl.loadFromXML(attrib);
        addTable(tbl);
        return tbl;
    }

    public void addTable(Table tbl) {
        tbl.setDatabase(this);
        _tableList.add(tbl);
        _tableMap.put(tbl.getName(), tbl);
    }

    public void doFinalInitialization() throws EngineException { // Unused on DBFlute
        Table[] tables = getTables();
        for (int i = 0; i < tables.length; i++) {
            Table currTable = tables[i];

            // check schema integrity
            // if idMethod="autoincrement", make sure a column is
            // specified as autoIncrement="true"
            // F I X M E: Handle idMethod="native" via DB adapter.
            // [Unused on DBFlute]
            // if (currTable.getIdMethod().equals("autoincrement")) {
            //     Column[] columns = currTable.getColumns();
            //     boolean foundOne = false;
            //     for (int j = 0; j < columns.length && !foundOne; j++) {
            //         foundOne = columns[j].isAutoIncrement();
            //     }
            // 
            //     if (!foundOne) {
            //         String errorMessage = "Table '" + currTable.getName()
            //                 + "' is marked as autoincrement, but it does not "
            //                 + "have a column which declared as the one to "
            //                 + "auto increment (i.e. autoIncrement=\"true\")\n";
            //         throw new EngineException("Error in XML schema: " + errorMessage);
            //     }
            // }

            currTable.doFinalInitialization();

            // setup reverse fk relations
            ForeignKey[] fks = currTable.getForeignKeys();
            for (int j = 0; j < fks.length; j++) {
                ForeignKey currFK = fks[j];
                Table foreignTable = getTable(currFK.getForeignTableName());
                if (foreignTable == null) {
                    throw new EngineException("Attempt to set foreign" + " key to nonexistent table, "
                            + currFK.getForeignTableName());
                } else {
                    final List<ForeignKey> refererList = foreignTable.getRefererList();
                    if ((refererList == null || !refererList.contains(currFK))) {
                        foreignTable.addReferrer(currFK);
                    }

                    // local column references
                    Iterator<String> localColumnNames = currFK.getLocalColumns().iterator();
                    while (localColumnNames.hasNext()) {
                        Column local = currTable.getColumn((String) localColumnNames.next());
                        // give notice of a schema inconsistency.
                        // note we do not prevent the npe as there is nothing
                        // that we can do, if it is to occur.
                        if (local == null) {
                            throw new EngineException("Attempt to define foreign"
                                    + " key with nonexistent column in table, " + currTable.getName());
                        } else {
                            //check for foreign pk's
                            if (local.isPrimaryKey()) {
                                currTable.setContainsForeignPK(true);
                            }
                        }
                    }

                    // foreign column references
                    Iterator<String> foreignColumnNames = currFK.getForeignColumns().iterator();
                    while (foreignColumnNames.hasNext()) {
                        String foreignColumnName = (String) foreignColumnNames.next();
                        Column foreign = foreignTable.getColumn(foreignColumnName);
                        // if the foreign column does not exist, we may have an
                        // external reference or a misspelling
                        if (foreign == null) {
                            throw new EngineException("Attempt to set foreign" + " key to nonexistent column: table="
                                    + currTable.getName() + ", foreign column=" + foreignColumnName);
                        } else {
                            foreign.addReferrer(currFK);
                        }
                    }
                }
            }
        }
    }

    // ===================================================================================
    //                                                                      Parameter Bean
    //                                                                      ==============
    protected DfParameterBeanBasicHandler _pmbBasicHandler;

    protected DfParameterBeanBasicHandler getParameterBeanBasicHandler() {
        if (_pmbBasicHandler != null) {
            return _pmbBasicHandler;
        }
        final DfBasicProperties basicProp = getBasicProperties();
        final DfClassificationProperties classificationProp = getClassificationProperties();
        _pmbBasicHandler = new DfParameterBeanBasicHandler(_pmbMetaDataMap, basicProp, classificationProp);
        return _pmbBasicHandler;
    }

    // -----------------------------------------------------
    //                                              MetaData
    //                                              --------
    public boolean isExistPmbMetaData() {
        return getParameterBeanBasicHandler().isExistPmbMetaData();
    }

    public Collection<DfParameterBeanMetaData> getPmbMetaDataList() {
        return getParameterBeanBasicHandler().getPmbMetaDataList();
    }

    public String getPmbMetaDataSuperClassDefinition(String className) {
        return getParameterBeanBasicHandler().getPmbMetaDataSuperClassDefinition(className);
    }

    public boolean hasPmbMetaDataSafetyResultDefitinion(String className) {
        return getParameterBeanBasicHandler().hasPmbMetaDataSafetyResultDefitinion(className);
    }

    public Map<String, String> getPmbMetaDataPropertyNameTypeMap(String className) {
        return getParameterBeanBasicHandler().getPmbMetaDataPropertyNameTypeMap(className);
    }

    public Map<String, String> getPmbMetaDataPropertyNameColumnNameMap(String className) {
        return getParameterBeanBasicHandler().getPmbMetaDataPropertyNameColumnNameMap(className);
    }

    public Map<String, String> getPmbMetaDataPropertyNameOptionMap(String className) {
        return getParameterBeanBasicHandler().getPmbMetaDataPropertyNameOptionMap(className);
    }

    public Set<String> getPmbMetaDataPropertySet(String className) {
        return getParameterBeanBasicHandler().getPmbMetaDataPropertySet(className);
    }

    public String getPmbMetaDataPropertyType(String className, String propertyName) {
        return getParameterBeanBasicHandler().getPmbMetaDataPropertyType(className, propertyName);
    }

    public String getPmbMetaDataPropertyColumnName(String className, String propertyName) {
        return getParameterBeanBasicHandler().getPmbMetaDataPropertyColumnName(className, propertyName);
    }

    // -----------------------------------------------------
    //                                             Procedure
    //                                             ---------
    public boolean isPmbMetaDataForProcedure(String className) {
        return getParameterBeanBasicHandler().isPmbMetaDataForProcedure(className);
    }

    public String getPmbMetaDataProcedureName(String className) {
        return getParameterBeanBasicHandler().getPmbMetaDataProcedureName(className);
    }

    public boolean isPmbMetaDataPropertyOptionProcedureParameterIn(String className, String propertyName) {
        return getParameterBeanBasicHandler().isPmbMetaDataPropertyOptionProcedureParameterIn(className, propertyName);
    }

    public boolean isPmbMetaDataPropertyOptionProcedureParameterOut(String className, String propertyName) {
        return getParameterBeanBasicHandler().isPmbMetaDataPropertyOptionProcedureParameterOut(className, propertyName);
    }

    public boolean isPmbMetaDataPropertyOptionProcedureParameterInOut(String className, String propertyName) {
        return getParameterBeanBasicHandler().isPmbMetaDataPropertyOptionProcedureParameterInOut(className,
                propertyName);
    }

    public boolean isPmbMetaDataPropertyOptionProcedureParameterReturn(String className, String propertyName) {
        return getParameterBeanBasicHandler().isPmbMetaDataPropertyOptionProcedureParameterReturn(className,
                propertyName);
    }

    // -----------------------------------------------------
    //                                                Option
    //                                                ------
    public boolean hasPmbMetaDataPropertyOptionOriginalOnlyOneSetter(String className, String propertyName) {
        return getParameterBeanBasicHandler()
                .hasPmbMetaDataPropertyOptionOriginalOnlyOneSetter(className, propertyName);
    }

    // -----------------------------------------------------
    //                                    Option LikeSeasrch
    //                                    ------------------
    public boolean hasPmbMetaDataPropertyOptionAnyLikeSearch(String className) {
        return getParameterBeanBasicHandler().hasPmbMetaDataPropertyOptionAnyLikeSearch(className);
    }

    public boolean hasPmbMetaDataPropertyOptionAnyLikeSearch(String className, String propertyName) {
        return getParameterBeanBasicHandler().hasPmbMetaDataPropertyOptionAnyLikeSearch(className, propertyName);
    }

    public boolean isPmbMetaDataPropertyOptionLikeSearch(String className, String propertyName) {
        return getParameterBeanBasicHandler().isPmbMetaDataPropertyOptionLikeSearch(className, propertyName);
    }

    public boolean isPmbMetaDataPropertyOptionPrefixSearch(String className, String propertyName) {
        return getParameterBeanBasicHandler().isPmbMetaDataPropertyOptionPrefixSearch(className, propertyName);
    }

    public boolean isPmbMetaDataPropertyOptionContainSearch(String className, String propertyName) {
        return getParameterBeanBasicHandler().isPmbMetaDataPropertyOptionContainSearch(className, propertyName);
    }

    public boolean isPmbMetaDataPropertyOptionSuffixSearch(String className, String propertyName) {
        return getParameterBeanBasicHandler().isPmbMetaDataPropertyOptionSuffixSearch(className, propertyName);
    }

    // -----------------------------------------------------
    //                                         Option FromTo
    //                                         -------------
    public boolean hasPmbMetaDataPropertyOptionAnyFromTo(String className) {
        return getParameterBeanBasicHandler().hasPmbMetaDataPropertyOptionAnyFromTo(className);
    }

    public boolean hasPmbMetaDataPropertyOptionAnyFromTo(String className, String propertyName) {
        return getParameterBeanBasicHandler().hasPmbMetaDataPropertyOptionAnyFromTo(className, propertyName);
    }

    public boolean isPmbMetaDataPropertyOptionFromDate(String className, String propertyName) {
        return getParameterBeanBasicHandler().isPmbMetaDataPropertyOptionFromDate(className, propertyName);
    }

    public boolean isPmbMetaDataPropertyOptionToDate(String className, String propertyName) {
        return getParameterBeanBasicHandler().isPmbMetaDataPropertyOptionToDate(className, propertyName);
    }

    // -----------------------------------------------------
    //                                 Option Classification
    //                                 ---------------------
    public boolean isPmbMetaDataPropertyOptionClassification(String className, String propertyName) {
        return getParameterBeanBasicHandler().isPmbMetaDataPropertyOptionClassification(className, propertyName);
    }

    public String getPmbMetaDataPropertyOptionClassificationName(String className, String propertyName) {
        return getParameterBeanBasicHandler().getPmbMetaDataPropertyOptionClassificationName(className, propertyName);
    }

    public List<Map<String, String>> getPmbMetaDataPropertyOptionClassificationMapList(String className,
            String propertyName) {
        return getParameterBeanBasicHandler()
                .getPmbMetaDataPropertyOptionClassificationMapList(className, propertyName);
    }

    // -----------------------------------------------------
    //                                      Option Reference
    //                                      ----------------
    public String getPmbMetaDataPropertyRefColumnInfo(String className, String propertyName) {
        try {
            final DfParameterBeanBasicHandler handler = getParameterBeanBasicHandler();
            final AppData data = _sql2entitySchemaData;
            final String alias = handler.getPmbMetaDataPropertyRefAlias(className, propertyName, data);
            final String name = handler.getPmbMetaDataPropertyRefName(className, propertyName, data);
            final String lineDisp = handler.getPmbMetaDataPropertyRefLineDisp(className, propertyName, data);
            if (name != null && name.trim().length() > 0) {
                return " :: related to " + alias + name + ": " + lineDisp;
            } else {
                return "";
            }
        } catch (RuntimeException e) {
            String msg = "Failed to execute getPmbMetaDataPropertyRefColumnInfo(): ";
            msg = msg + " " + className + "." + propertyName;
            _log.debug(msg, e);
            throw new IllegalStateException(msg, e);
        }
    }

    public boolean isPmbMetaDataPropertyRefColumnChar(String className, String propertyName) {
        return getParameterBeanBasicHandler().isPmbMetaDataPropertyRefColumnChar(className, propertyName,
                _sql2entitySchemaData);
    }

    public String getPmbMetaDataPropertyRefSize(String className, String propertyName) {
        return getParameterBeanBasicHandler().getPmbMetaDataPropertyRefSize(className, propertyName,
                _sql2entitySchemaData);
    }

    // ===================================================================================
    //                                                                         Initializer
    //                                                                         ===========
    // -----------------------------------------------------
    //                                  AdditionalPrimaryKey
    //                                  --------------------
    public void initializeAdditionalPrimaryKey() {
        final DfAdditionalPrimaryKeyInitializer initializer = new DfAdditionalPrimaryKeyInitializer(this);
        initializer.initializeAdditionalPrimaryKey();
    }

    // -----------------------------------------------------
    //                                   AdditionalUniqueKey
    //                                   -------------------
    public void initializeAdditionalUniqueKey() {
        final DfAdditionalUniqueKeyInitializer initializer = new DfAdditionalUniqueKeyInitializer(this);
        initializer.initializeAdditionalUniqueKey();
    }

    // -----------------------------------------------------
    //                                  AdditionalForeignKey
    //                                  --------------------
    /**
     * Initialize additional foreign key. <br />
     * This is for Generate task. (Not Sql2Entity)
     */
    public void initializeAdditionalForeignKey() {
        // /- - - - - - - - - - - - - - - - - - - - - - - - - - - -
        // Set up implicit foreign key for Buri before initializing
        // - - - - - - - - - -/
        final DfBuriProperties buriProperties = getProperties().getBuriProperties();
        buriProperties.setupImplicitAdditionalForeignKey(new DfTableFinder() {
            public Table findTable(String tableName) {
                return getTable(tableName);
            }
        });
        // /- - - - - - - - - - - - - - - - -
        // Initialize additional foreign key
        // - - - - - - - - - -/
        final DfAdditionalForeignKeyInitializer initializer = new DfAdditionalForeignKeyInitializer(this);
        initializer.initializeAdditionalForeignKey();
    }

    // -----------------------------------------------------
    //                              ClassificationDeployment
    //                              ------------------------
    public void initializeClassificationDeployment() {
        final DfClassificationProperties clsProp = getClassificationProperties();

        // Initialize classification definition before initializing deployment.
        clsProp.initializeClassificationDefinition(); // Together!

        // Initialize current target database.
        clsProp.initializeClassificationDeployment(this);

        // If this is in sql2entity task, initialize schema database.
        if (_sql2entitySchemaData != null) {
            try {
                clsProp.initializeClassificationDeployment(_sql2entitySchemaData.getDatabase());
            } catch (EngineException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    // -----------------------------------------------------
    //                                          IncludeQuery
    //                                          ------------
    public void initializeIncludeQuery() {
        IncludeQueryInitializer initializer = new IncludeQueryInitializer();
        initializer.setIncludeQueryProperties(getProperties().getIncludeQueryProperties());
        initializer.setTableFinder(new DfTableFinder() {
            public Table findTable(String name) {
                return getTable(name);
            }
        });
        initializer.initializeIncludeQuery();
    }

    // ===================================================================================
    //                                                                    Check Properties
    //                                                                    ================
    /**
     * Check properties as mutually related validation.
     */
    public void checkProperties() {
        getProperties().getSequenceIdentityProperties().checkSequenceDefinitionMap(new SequenceDefinitionMapChecker() {
            public boolean hasTable(String tableName) {
                return getTable(tableName) != null;
            }
        });
        getProperties().getBasicProperties().checkDirectoryPackage();
    }

    // ===================================================================================
    //                                                              Delete Old Table Class
    //                                                              ======================
    public void deleteOldTableClass() {
        if (_skipDeleteOldClass) {
            return;
        }
        if (getProperties().getLittleAdjustmentProperties().isDeleteOldTableClass()) {
            final DfOldClassHandler handler = createOldClassHandler();
            handler.deleteOldTableClass();
        }
    }

    public void deleteOldCustomizeClass() {
        if (_skipDeleteOldClass) {
            return;
        }
        if (getProperties().getLittleAdjustmentProperties().isDeleteOldTableClass()) {
            final DfOldClassHandler handler = createOldClassHandler();
            handler.setPmbMetaDataMap(_pmbMetaDataMap);
            handler.deleteOldCustomizeClass();
        }
    }

    protected DfOldClassHandler createOldClassHandler() {
        return new DfOldClassHandler(getGeneratorInstance(), getBasicProperties(), getProperties()
                .getLittleAdjustmentProperties(), getTableList());
    }

    // ===================================================================================
    //                                                                    Output Directory
    //                                                                    ================
    public void enableGenerateOutputDirectory() {
        getGeneratorInstance().setOutputPath(getProperties().getBasicProperties().getGenerateOutputDirectory());
    }

    public void enableSql2EntityOutputDirectory() {
        getGeneratorInstance().setOutputPath(getProperties().getOutsideSqlProperties().getSql2EntityOutputDirectory());
    }

    public void enableFlexDtoOutputDirectory() {
        getGeneratorInstance().setOutputPath(getProperties().getFlexDtoProperties().getOutputDirectory());
    }

    // ===================================================================================
    //                                                                           Generator
    //                                                                           =========
    public DfGenerator getGeneratorInstance() {
        return DfGenerator.getInstance();
    }

    //====================================================================================
    //                                                                 Database Definition
    //                                                                 ===================
    protected Map<String, String> _databaseDefinitionMap;

    public Map<String, String> getDatabaseDefinitionMap() {
        if (_databaseDefinitionMap == null) {
            final DfDatabaseConfig config = new DfDatabaseConfig();
            final Map<String, Map<String, String>> databaseConfigMap = config.analyzeDatabaseBaseInfo();
            Map<String, String> databaseDefinitionMap = databaseConfigMap.get(getDatabaseType());
            if (databaseDefinitionMap == null) {
                databaseDefinitionMap = databaseConfigMap.get("default");
                if (databaseDefinitionMap == null) {
                    String msg = "The property[databaseDefinitionMap] doesn't have the database[";
                    throw new IllegalStateException(msg + getDatabaseType() + "] and default-database.");
                }
            }
            _databaseDefinitionMap = databaseDefinitionMap;
        }
        return _databaseDefinitionMap;
    }

    public String getDaoGenDbName() { // for SqlClause. It's Old Style method.
        return getGenerateDbName();
    }

    public String getDefaultDBDef() {
        String dbdef = getSpecialGuestDatabaseDBDef();
        if (dbdef != null) {
            return dbdef;
        }
        return getGenerateDbName();
    }

    protected String getSpecialGuestDatabaseDBDef() {
        if (getBasicProperties().isDatabaseMsAccess()) {
            return "msaccess";
        }
        return null;
    }

    public String getGenerateDbName() {
        final Map<String, String> databaseInfoMap = getDatabaseDefinitionMap();
        final String dbName = (String) databaseInfoMap.get("dbName");
        if (dbName == null || dbName.trim().length() == 0) {
            String msg = "The database doesn't have dbName in the property[databaseInfoMap]: ";
            throw new IllegalStateException(msg + databaseInfoMap);
        }
        return dbName;
    }

    public String getWildCard() {
        final Map<String, String> databaseInfoMap = getDatabaseDefinitionMap();
        final String wildCard = (String) databaseInfoMap.get("wildCard");
        if (wildCard == null || wildCard.trim().length() == 0) {
            String msg = "The database doesn't have wildCard in the property[databaseInfoMap]: ";
            throw new IllegalStateException(msg + databaseInfoMap);
        }
        return wildCard;
    }

    public String getSequenceNextSql() {
        final Map<String, String> databaseInfoMap = getDatabaseDefinitionMap();
        final String sequenceNextSql = (String) databaseInfoMap.get("sequenceNextSql");
        if (sequenceNextSql == null || sequenceNextSql.trim().length() == 0) {
            String msg = "The database doesn't have sequenceNextSql in the property[databaseInfoMap]: ";
            throw new IllegalStateException(msg + databaseInfoMap);
        }
        return sequenceNextSql;
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected DfBuildProperties getProperties() {
        return DfBuildProperties.getInstance();
    }

    // ===================================================================================
    //                                                                    Basic Properties
    //                                                                    ================
    protected DfBasicProperties getBasicProperties() {
        return getProperties().getBasicProperties();
    }

    // -----------------------------------------------------
    //                                               Project
    //                                               -------
    public String getProjectName() {
        return getBasicProperties().getProjectName();
    }

    // -----------------------------------------------------
    //                                              Database
    //                                              --------
    public String getDatabaseName() {
        return getBasicProperties().getDatabaseName();
    }

    public boolean isDatabaseMySQL() {
        return getBasicProperties().isDatabaseMySQL();
    }

    public boolean isDatabasePostgreSQL() {
        return getBasicProperties().isDatabasePostgreSQL();
    }

    public boolean isDatabaseOracle() {
        return getBasicProperties().isDatabaseOracle();
    }

    public boolean isDatabaseDB2() {
        return getBasicProperties().isDatabaseDB2();
    }

    public boolean isDatabaseSQLServer() {
        return getBasicProperties().isDatabaseSqlServer();
    }

    public boolean isDatabaseDerby() {
        return getBasicProperties().isDatabaseDerby();
    }

    public boolean isDatabaseH2() {
        return getBasicProperties().isDatabaseH2();
    }

    public boolean isDatabaseMSAccess() {
        return getBasicProperties().isDatabaseMsAccess();
    }

    // -----------------------------------------------------
    //                                              Language
    //                                              --------
    public String getTargetLanguage() {
        return getBasicProperties().getTargetLanguage();
    }

    public String getResourceDirectory() {
        return getBasicProperties().getResourceDirectory();
    }

    public String getTargetLanguageInitCap() {
        final String targetLanguage = getBasicProperties().getTargetLanguage();
        return targetLanguage.substring(0, 1).toUpperCase() + targetLanguage.substring(1);
    }

    public boolean isTargetLanguageJava() {
        return getBasicProperties().isTargetLanguageJava();
    }

    public boolean isTargetLanguageCSharp() {
        return getBasicProperties().isTargetLanguageCSharp();
    }

    public boolean isTargetLanguagePhp() {
        return getBasicProperties().isTargetLanguagePhp();
    }

    public boolean isJavaVersionGreaterEqualTiger() {
        return getBasicProperties().isJavaVersionGreaterEqualTiger();
    }

    public boolean isJavaVersionGreaterEqualMustang() {
        return getBasicProperties().isJavaVersionGreaterEqualMustang();
    }

    // -----------------------------------------------------
    //                                             Container
    //                                             ---------
    public String getTargetContainerName() {
        return getBasicProperties().getTargetContainerName();
    }

    public boolean isTargetContainerSeasar() {
        return getBasicProperties().isTargetContainerSeasar();
    }

    public boolean isTargetContainerSpring() {
        return getBasicProperties().isTargetContainerSpring();
    }

    public boolean isTargetContainerLucy() {
        return getBasicProperties().isTargetContainerLucy();
    }

    public boolean isTargetContainerGuice() {
        return getBasicProperties().isTargetContainerGuice();
    }

    public boolean isTargetContainerSlim3() {
        return getBasicProperties().isTargetContainerSlim3();
    }

    // -----------------------------------------------------
    //                                             Extension
    //                                             ---------
    public String getTemplateFileExtension() {
        return getBasicProperties().getTemplateFileExtension();
    }

    public String getClassFileExtension() {
        return getBasicProperties().getClassFileExtension();
    }

    // -----------------------------------------------------
    //                                              Encoding
    //                                              --------
    public String getTemplateFileEncoding() {
        return getBasicProperties().getTemplateFileEncoding();
    }

    // -----------------------------------------------------
    //                                               JavaDir
    //                                               -------
    public String getJavaDir() {
        return getBasicProperties().getGenerateOutputDirectory();
    }

    // -----------------------------------------------------
    //                                                Author
    //                                                ------
    public String getClassAuthor() {
        return getBasicProperties().getClassAuthor();
    }

    // -----------------------------------------------------
    //                                             Copyright
    //                                             ---------
    public String getAllClassCopyright() {
        return getProperties().getAllClassCopyrightProperties().getAllClassCopyright();
    }

    public void reflectAllExCopyright(String path) {
        final String outputPath = DfGenerator.getInstance().getOutputPath();
        final String absolutePath = outputPath + "/" + path;
        getProperties().getAllClassCopyrightProperties().reflectAllExCopyright(absolutePath);
    }

    // -----------------------------------------------------
    //                                         Prefix/Suffix
    //                                         -------------
    public String getProjectPrefix() {
        return getBasicProperties().getProjectPrefix();
    }

    public String getBasePrefix() {
        return getBasicProperties().getBasePrefix();
    }

    public String getBaseSuffixForEntity() {
        return "";
    }

    // -----------------------------------------------------
    //                                      Generate Package
    //                                      ----------------
    public String getPackageBase() {
        return getProperties().getBasicProperties().getPackageBase();
    }

    public String getBaseCommonPackage() {
        return getProperties().getBasicProperties().getBaseCommonPackage();
    }

    public String getBaseBehaviorPackage() {
        return getProperties().getBasicProperties().getBaseBehaviorPackage();
    }

    public String getBaseDaoPackage() {
        return getProperties().getBasicProperties().getBaseDaoPackage();
    }

    public String getBaseEntityPackage() {
        return getProperties().getBasicProperties().getBaseEntityPackage();
    }

    public String getDBMetaPackage() {
        return getProperties().getBasicProperties().getDBMetaPackage();
    }

    public String getConditionBeanPackage() {
        return getProperties().getBasicProperties().getConditionBeanPackage();
    }

    public String getExtendedConditionBeanPackage() {
        return getProperties().getBasicProperties().getExtendedConditionBeanPackage();
    }

    public String getExtendedBehaviorPackage() {
        return getProperties().getBasicProperties().getExtendedBehaviorPackage();
    }

    public String getExtendedDaoPackage() {
        return getProperties().getBasicProperties().getExtendedDaoPackage();
    }

    public String getExtendedEntityPackage() {
        return getProperties().getBasicProperties().getExtendedEntityPackage();
    }

    // -----------------------------------------------------
    //                                             Flat/Omit
    //                                             ---------
    // CSharp Only
    public boolean isFlatDirectoryPackageValid() {
        return getProperties().getBasicProperties().isFlatDirectoryPackageValid();
    }

    public String getFlatDirectoryPackage() {
        return getProperties().getBasicProperties().getFlatDirectoryPackage();
    }

    public boolean isOmitDirectoryPackageValid() {
        return getProperties().getBasicProperties().isOmitDirectoryPackageValid();
    }

    public String getOmitDirectoryPackage() {
        return getProperties().getBasicProperties().getOmitDirectoryPackage();
    }

    // -----------------------------------------------------
    //                                            Hot Deploy
    //                                            ----------
    public boolean isAvailableHotDeploy() {
        return getBasicProperties().isAvailableHotDeploy();
    }

    // -----------------------------------------------------
    //                                   Behavior Query Path
    //                                   -------------------
    public String getBehaviorQueryPathBeginMark() {
        return getBasicProperties().getBehaviorQueryPathBeginMark();
    }

    public String getBehaviorQueryPathEndMark() {
        return getBasicProperties().getBehaviorQueryPathEndMark();
    }

    // ===================================================================================
    //                                                                 Database Properties
    //                                                                 ===================
    public String getDatabaseSchema() {
        return getProperties().getDatabaseProperties().getDatabaseSchema();
    }

    public boolean hasDatabaseSchema() {
        String databaseSchema = getDatabaseSchema();
        return databaseSchema != null && databaseSchema.trim().length() > 0;
    }

    public boolean hasAdditionalSchema() {
        return getProperties().getDatabaseProperties().hasAdditionalSchema();
    }

    // ===================================================================================
    //                                                                Dependency Injection
    //                                                                ====================
    // -----------------------------------------------------
    //                                                Seasar
    //                                                ------
    public String getDBFluteDiconNamespace() {
        return getProperties().getDependencyInjectionProperties().getDBFluteDiconNamespace();
    }

    public List<String> getDBFluteDiconPackageNameList() {
        final String resourceOutputDirectory = getBasicProperties().getResourceOutputDirectory();
        if (resourceOutputDirectory != null) {
            final List<String> resulList = new ArrayList<String>();
            resulList.add(resourceOutputDirectory);
            return resulList;
        }

        // for compatible and default value
        final List<String> diconPackageNameList = getProperties().getDependencyInjectionProperties()
                .getDBFluteDiconPackageNameList();
        if (diconPackageNameList != null && !diconPackageNameList.isEmpty()) {
            return diconPackageNameList;
        } else {
            final List<String> resulList = new ArrayList<String>();
            resulList.add(getBasicProperties().getDefaultResourceOutputDirectory());
            return resulList;
        }
    }

    public String getDBFluteCreatorDiconFileName() {
        return getProperties().getDependencyInjectionProperties().getDBFluteCreatorDiconFileName();
    }

    public String getDBFluteCustomizerDiconFileName() {
        return getProperties().getDependencyInjectionProperties().getDBFluteCustomizerDiconFileName();
    }

    public String getDBFluteDiconFileName() {
        return getProperties().getDependencyInjectionProperties().getDBFluteDiconFileName();
    }

    public String getJ2eeDiconResourceName() {
        return getProperties().getDependencyInjectionProperties().getJ2eeDiconResourceName();
    }

    public List<String> getDBFluteDiconBeforeJ2eeIncludePathList() {
        return getProperties().getDependencyInjectionProperties().getDBFluteDiconBeforeJ2eeIncludePathList();
    }

    public List<String> getDBFluteDiconOtherIncludePathList() {
        return getProperties().getDependencyInjectionProperties().getDBFluteDiconOtherIncludePathList();
    }

    // -----------------------------------------------------
    //                                         Spring & Lucy
    //                                         -------------
    public List<String> getDBFluteBeansPackageNameList() {
        final String resourceOutputDirectory = getBasicProperties().getResourceOutputDirectory();
        if (resourceOutputDirectory != null) {
            final List<String> resulList = new ArrayList<String>();
            resulList.add(resourceOutputDirectory);
            return resulList;
        }

        // for compatible and default value
        final List<String> diconPackageNameList = getProperties().getDependencyInjectionProperties()
                .getDBFluteBeansPackageNameList();
        if (diconPackageNameList != null && !diconPackageNameList.isEmpty()) {
            return diconPackageNameList;
        } else {
            final List<String> resulList = new ArrayList<String>();
            resulList.add(getBasicProperties().getDefaultResourceOutputDirectory());
            return resulList;
        }
    }

    public String getDBFluteBeansFileName() {
        return getProperties().getDependencyInjectionProperties().getDBFluteBeansFileName();
    }

    public String getDBFluteBeansDataSourceName() {
        return getProperties().getDependencyInjectionProperties().getDBFluteBeansDataSourceName();
    }

    // -----------------------------------------------------
    //                                                 Quill
    //                                                 -----
    public boolean isQuillDataSourceNameValid() {
        return getProperties().getDependencyInjectionProperties().isQuillDataSourceNameValid();
    }

    public String getQuillDataSourceName() {
        return getProperties().getDependencyInjectionProperties().getQuillDataSourceName();
    }

    // ===================================================================================
    //                                                        Sequence/Identity Properties
    //                                                        ============================
    public String getSequenceDefinitionMapSequence(String flexibleTableName) {
        return getProperties().getSequenceIdentityProperties().getSequenceDefinitionMapSequence(flexibleTableName);
    }

    public String getSequenceReturnType() {
        return getProperties().getSequenceIdentityProperties().getSequenceReturnType();
    }

    public String getIdentityDefinitionMapColumnName(String flexibleTableName) {
        return getProperties().getSequenceIdentityProperties().getIdentityDefinitionMapColumnName(flexibleTableName);
    }

    // ===================================================================================
    //                                                            Common Column Properties
    //                                                            ========================
    public Map<String, Object> getCommonColumnMap() {
        return getProperties().getCommonColumnProperties().getCommonColumnMap();
    }

    public List<String> getCommonColumnNameList() {
        return getProperties().getCommonColumnProperties().getCommonColumnNameList();
    }

    public List<String> getCommonColumnNameConversionList() {
        return getProperties().getCommonColumnProperties().getCommonColumnNameConversionList();
    }

    public String filterCommonColumn(String commonColumnName) {
        return getProperties().getCommonColumnProperties().filterCommonColumn(commonColumnName);
    }

    public boolean hasCommonColumn() {
        return !getProperties().getCommonColumnProperties().getCommonColumnNameList().isEmpty();
    }

    public boolean isExistCommonColumnSetupElement() {
        return getProperties().getCommonColumnProperties().isExistCommonColumnSetupElement();
    }

    public boolean hasCommonColumnConvertion(String commonColumnName) {
        return getProperties().getCommonColumnProperties().isCommonColumnConversion(commonColumnName);
    }

    // --------------------------------------
    //                                 insert
    //                                 ------
    public Map<String, Object> getCommonColumnSetupBeforeInsertInterceptorLogicMap() {
        return getProperties().getCommonColumnProperties().getBeforeInsertMap();
    }

    public boolean containsValidColumnNameKeyCommonColumnSetupBeforeInsertInterceptorLogicMap(String columnName) {
        return getProperties().getCommonColumnProperties()
                .containsValidColumnNameKeyCommonColumnSetupBeforeInsertInterceptorLogicMap(columnName);
    }

    public String getCommonColumnSetupBeforeInsertInterceptorLogicByColumnName(String columnName) {
        return getProperties().getCommonColumnProperties()
                .getCommonColumnSetupBeforeInsertInterceptorLogicByColumnName(columnName);
    }

    // --------------------------------------
    //                                 update
    //                                 ------
    public Map<String, Object> getCommonColumnSetupBeforeUpdateInterceptorLogicMap() {
        return getProperties().getCommonColumnProperties().getBeforeUpdateMap();
    }

    public boolean containsValidColumnNameKeyCommonColumnSetupBeforeUpdateInterceptorLogicMap(String columnName) {
        return getProperties().getCommonColumnProperties()
                .containsValidColumnNameKeyCommonColumnSetupBeforeUpdateInterceptorLogicMap(columnName);
    }

    public String getCommonColumnSetupBeforeUpdateInterceptorLogicByColumnName(String columnName) {
        return getProperties().getCommonColumnProperties()
                .getCommonColumnSetupBeforeUpdateInterceptorLogicByColumnName(columnName);
    }

    // --------------------------------------
    //                               resource
    //                               --------
    public boolean hasCommonColumnSetupResource() {
        return getProperties().getCommonColumnProperties().hasCommonColumnSetupResource();
    }

    public List<CommonColumnSetupResource> getCommonColumnSetupResourceList() {
        return getProperties().getCommonColumnProperties().getCommonColumnSetupResourceList();
    }

    // --------------------------------------
    //                         logic handling
    //                         --------------
    public boolean isCommonColumnSetupInvokingLogic(String logic) {
        return getProperties().getCommonColumnProperties().isCommonColumnSetupInvokingLogic(logic);
    }

    public String removeCommonColumnSetupInvokingMark(String logic) {
        return getProperties().getCommonColumnProperties().removeCommonColumnSetupInvokingMark(logic);
    }

    // ===================================================================================
    //                                                           Classification Properties
    //                                                           =========================
    public DfClassificationProperties getClassificationProperties() {
        return getProperties().getClassificationProperties();
    }

    // --------------------------------------
    //                             Definition
    //                             ----------
    public Map<String, Map<String, String>> getClassificationTopDefinitionMap() {
        return getClassificationProperties().getClassificationTopDefinitionMap();
    }

    public boolean hasClassificationDefinitionMap() {
        return getClassificationProperties().hasClassificationDefinitionMap();
    }

    public Map<String, List<Map<String, String>>> getClassificationDefinitionMap() {
        return getClassificationProperties().getClassificationDefinitionMap();
    }

    public List<String> getClassificationNameList() {
        return getClassificationProperties().getClassificationNameList();
    }

    public List<String> getClassificationNameListValidNameOnly() {
        return getClassificationProperties().getClassificationNameListValidNameOnly();
    }

    public List<String> getClassificationNameListValidAliasOnly() {
        return getClassificationProperties().getClassificationNameListValidAliasOnly();
    }

    public String getClassificationDefinitionMapAsStringRemovedLineSeparatorFilteredQuotation() {
        return getClassificationProperties()
                .getClassificationDefinitionMapAsStringRemovedLineSeparatorFilteredQuotation();
    }

    public List<java.util.Map<String, String>> getClassificationMapList(String classificationName) {
        return getClassificationProperties().getClassificationMapList(classificationName);
    }

    public String buildClassificationApplicationComment(Map<String, String> classificationMap) {
        return getClassificationProperties().buildClassificationApplicationComment(classificationMap);
    }

    public String buildClassificationCodeAliasVariables(Map<String, String> classificationMap) {
        return getClassificationProperties().buildClassificationCodeAliasVariables(classificationMap);
    }

    public String buildClassificationCodeNameAliasVariables(Map<String, String> classificationMap) {
        return getClassificationProperties().buildClassificationCodeNameAliasVariables(classificationMap);
    }

    public boolean isTableClassification(String classificationName) {
        return getClassificationProperties().isTableClassification(classificationName);
    }

    // --------------------------------------
    //                             Deployment
    //                             ----------
    public Map<String, Map<String, String>> getClassificationDeploymentMap() {
        return getClassificationProperties().getClassificationDeploymentMap();
    }

    public String getClassificationDeploymentMapAsStringRemovedLineSeparatorFilteredQuotation() {
        return getClassificationProperties()
                .getClassificationDeploymentMapAsStringRemovedLineSeparatorFilteredQuotation();
    }

    public boolean hasClassification(String tableName, String columnName) {
        return getClassificationProperties().hasClassification(tableName, columnName);
    }

    public String getClassificationName(String tableName, String columnName) {
        return getClassificationProperties().getClassificationName(tableName, columnName);
    }

    public boolean hasClassificationName(String tableName, String columnName) {
        return getClassificationProperties().hasClassificationName(tableName, columnName);
    }

    public boolean hasClassificationAlias(String tableName, String columnName) {
        return getClassificationProperties().hasClassificationAlias(tableName, columnName);
    }

    public Map<String, String> getAllColumnClassificationMap() { // for EntityDefinedCommonColumn
        return getClassificationProperties().getAllColumnClassificationMap();
    }

    public boolean isAllClassificationColumn(String columnName) { // for EntityDefinedCommonColumn
        return getClassificationProperties().isAllClassificationColumn(columnName);
    }

    public String getAllClassificationName(String columnName) { // for EntityDefinedCommonColumn
        return getClassificationProperties().getAllClassificationName(columnName);
    }

    // ===================================================================================
    //                                                        Little Adjustment Properties
    //                                                        ============================
    public boolean isAvailableDatabaseDependency() {
        return getProperties().getLittleAdjustmentProperties().isAvailableDatabaseDependency();
    }

    public boolean isAvailableNonPrimaryKeyWritable() {
        return getProperties().getLittleAdjustmentProperties().isAvailableNonPrimaryKeyWritable();
    }

    public boolean isAvailableToLowerInGeneratorUnderscoreMethod() {
        return getProperties().getLittleAdjustmentProperties().isAvailableToLowerInGeneratorUnderscoreMethod();
    }

    public boolean isMakeDeprecated() {
        return getProperties().getLittleAdjustmentProperties().isMakeDeprecated();
    }

    public boolean isMakeRecentlyDeprecated() {
        return getProperties().getLittleAdjustmentProperties().isMakeRecentlyDeprecated();
    }

    public boolean isMakeConditionQueryEqualEmptyString() {
        return getProperties().getLittleAdjustmentProperties().isMakeConditionQueryEqualEmptyString();
    }

    public boolean isMakeEntityChaseRelation() {
        return getProperties().getLittleAdjustmentProperties().isMakeEntityChaseRelation();
    }

    public boolean hasExtendedImplementedInvokerAssistantClass() {
        return getProperties().getLittleAdjustmentProperties().hasExtendedImplementedInvokerAssistantClass();
    }

    public String getExtendedImplementedInvokerAssistantClass() {
        return getProperties().getLittleAdjustmentProperties().getExtendedImplementedInvokerAssistantClass();
    }

    public boolean hasExtendedImplementedCommonColumnAutoSetupperClass() {
        return getProperties().getLittleAdjustmentProperties().hasExtendedImplementedCommonColumnAutoSetupperClass();
    }

    public String getExtendedImplementedCommonColumnAutoSetupperClass() {
        return getProperties().getLittleAdjustmentProperties().getExtendedImplementedCommonColumnAutoSetupperClass();
    }

    public boolean hasExtendedS2DaoSettingClassValid() {
        return getProperties().getLittleAdjustmentProperties().hasExtendedS2DaoSettingClassValid();
    }

    public String getExtendedS2DaoSettingClass() {
        return getProperties().getLittleAdjustmentProperties().getExtendedS2DaoSettingClass();
    }

    public boolean isShortCharHandlingValid() {
        return getProperties().getLittleAdjustmentProperties().isShortCharHandlingValid();
    }

    public String getShortCharHandlingMode() {
        return getProperties().getLittleAdjustmentProperties().getShortCharHandlingMode();
    }

    public String getShortCharHandlingModeCode() {
        return getProperties().getLittleAdjustmentProperties().getShortCharHandlingModeCode();
    }

    public boolean isCDefToStringReturnsName() {
        return getProperties().getLittleAdjustmentProperties().isCDefToStringReturnsName();
    }

    public boolean isStopGenerateExtendedBhv() {
        return getProperties().getLittleAdjustmentProperties().isStopGenerateExtendedBhv();
    }

    public boolean isStopGenerateExtendedDao() {
        return getProperties().getLittleAdjustmentProperties().isStopGenerateExtendedDao();
    }

    public boolean isStopGenerateExtendedEntity() {
        return getProperties().getLittleAdjustmentProperties().isStopGenerateExtendedEntity();
    }

    public boolean isMakeFlatExpansion() {
        return getProperties().getLittleAdjustmentProperties().isMakeFlatExpansion();
    }

    public boolean isMakeDaoInterface() {
        return getProperties().getLittleAdjustmentProperties().isMakeDaoInterface();
    }

    // ===================================================================================
    //                                                                     Buri Properties
    //                                                                     ===============
    public boolean isUseBuri() {
        return getProperties().getBuriProperties().isUseBuri();
    }

    public List<String> getBuriPackageList() {
        return new ArrayList<String>(getProperties().getBuriProperties().getActivityDefinitionMap().keySet());
    }

    public List<String> getBuriProcessList(String packageName) {
        return new ArrayList<String>(getProperties().getBuriProperties().getProcessMap(packageName).keySet());
    }

    public List<String> getBuriStatusList(String packageName, String processName) {
        return getProperties().getBuriProperties().getStatusList(packageName, processName);
    }

    public List<String> getBuriActionList(String packageName, String processName) {
        return getProperties().getBuriProperties().getActionList(packageName, processName);
    }

    public boolean hasBuriAllRoundStateHistory() {
        return getProperties().getBuriProperties().hasBuriAllRoundStateHistory(new DfTableFinder() {
            public Table findTable(String name) {
                return getTable(name);
            }
        });
    }

    // ===================================================================================
    //                                                         SQL Log Registry Properties
    //                                                         ===========================
    public boolean isSqlLogRegistryValid() {
        return getProperties().getSqlLogRegistryProperties().isValid();
    }

    public int getSqlLogRegistryLimitSize() {
        return getProperties().getSqlLogRegistryProperties().getLimitSize();
    }

    // ===================================================================================
    //                                                             Type Mapping Properties
    //                                                             =======================
    public List<Object> getJavaNativeStringList() {
        return getProperties().getTypeMappingProperties().getJavaNativeStringList();
    }

    public List<Object> getJavaNativeBooleanList() {
        return getProperties().getTypeMappingProperties().getJavaNativeBooleanList();
    }

    public List<Object> getJavaNativeNumberList() {
        return getProperties().getTypeMappingProperties().getJavaNativeNumberList();
    }

    public List<Object> getJavaNativeDateList() {
        return getProperties().getTypeMappingProperties().getJavaNativeDateList();
    }

    public List<Object> getJavaNativeBinaryList() {
        return getProperties().getTypeMappingProperties().getJavaNativeBinaryList();
    }

    // ===================================================================================
    //                                                               OutsideSql Properties
    //                                                               =====================
    public boolean isGenerateProcedureParameterBean() {
        return getProperties().getOutsideSqlProperties().isGenerateProcedureParameterBean();
    }

    public boolean hasSqlFileEncoding() {
        return getProperties().getOutsideSqlProperties().hasSqlFileEncoding();
    }

    public String getSqlFileEncoding() {
        return getProperties().getOutsideSqlProperties().getSqlFileEncoding();
    }

    public boolean isOutsideSqlPackageValid() {
        return getProperties().getOutsideSqlProperties().isSqlPackageValid();
    }

    public String getOutsideSqlPackage() {
        return getProperties().getOutsideSqlProperties().getSqlPackage();
    }

    public boolean isDefaultPackageValid() {
        return getProperties().getOutsideSqlProperties().isDefaultPackageValid();
    }

    public String getDefaultPackage() {
        return getProperties().getOutsideSqlProperties().getDefaultPackage();
    }

    public boolean isOmitResourcePathPackageValid() {
        return getProperties().getOutsideSqlProperties().isOmitResourcePathPackageValid();
    }

    public String getOmitResourcePathPackage() {
        return getProperties().getOutsideSqlProperties().getOmitResourcePathPackage();
    }

    public boolean isOmitFileSystemPathPackageValid() {
        return getProperties().getOutsideSqlProperties().isOmitFileSystemPathPackageValid();
    }

    public String getOmitFileSystemPathPackage() {
        return getProperties().getOutsideSqlProperties().getOmitFileSystemPathPackage();
    }

    public String getSql2EntityBaseEntityPackage() {
        return getProperties().getOutsideSqlProperties().getBaseEntityPackage();
    }

    public String getSql2EntityDBMetaPackage() {
        return getProperties().getOutsideSqlProperties().getDBMetaPackage();
    }

    public String getSql2EntityExtendedEntityPackage() {
        return getProperties().getOutsideSqlProperties().getExtendedEntityPackage();
    }

    public String getSql2EntityBaseCursorPackage() {
        return getProperties().getOutsideSqlProperties().getBaseCursorPackage();
    }

    public String getSql2EntityExtendedCursorPackage() {
        return getProperties().getOutsideSqlProperties().getExtendedCursorPackage();
    }

    public String getSql2EntityBaseParameterBeanPackage() {
        return getProperties().getOutsideSqlProperties().getBaseParameterBeanPackage();
    }

    public String getSql2EntityExtendedParameterBeanPackage() {
        return getProperties().getOutsideSqlProperties().getExtendedParameterBeanPackage();
    }

    // ===================================================================================
    //                                                                 Document Properties
    //                                                                 ===================
    public boolean isAliasDelimiterInDbCommentValid() {
        return getProperties().getDocumentProperties().isAliasDelimiterInDbCommentValid();
    }

    public boolean isEntityJavaDocDbCommentValid() {
        return getProperties().getDocumentProperties().isEntityJavaDocDbCommentValid();
    }

    // ===================================================================================
    //                                                               Simple DTO Properties
    //                                                               =====================
    public boolean hasSimpleDtoDefinition() {
        return getProperties().getSimpleDtoProperties().hasSimpleDtoDefinition();
    }

    public String getSimpleDtoBaseDtoPackage() {
        return getProperties().getSimpleDtoProperties().getBaseDtoPackage();
    }

    public String getSimpleDtoExtendedDtoPackage() {
        return getProperties().getSimpleDtoProperties().getExtendedDtoPackage();
    }

    public String getSimpleDtoBaseDtoPrefix() {
        return getProperties().getSimpleDtoProperties().getBaseDtoPrefix();
    }

    public String getSimpleDtoBaseDtoSuffix() {
        return getProperties().getSimpleDtoProperties().getBaseDtoSuffix();
    }

    public String getSimpleDtoExtendedDtoPrefix() {
        return getProperties().getSimpleDtoProperties().getExtendedDtoPrefix();
    }

    public String getSimpleDtoExtendedDtoSuffix() {
        return getProperties().getSimpleDtoProperties().getExtendedDtoSuffix();
    }

    public String getSimpleDtoDtoMapperPackage() {
        return getProperties().getSimpleDtoProperties().getMapperPackage();
    }

    public boolean isSimpleDtoUseDtoMapper() {
        return getProperties().getSimpleDtoProperties().isUseDtoMapper();
    }

    // ===================================================================================
    //                                                                 Flex DTO Properties
    //                                                                 ===================
    public boolean hasFlexDtoDefinition() {
        return getProperties().getFlexDtoProperties().hasFlexDtoDefinition();
    }

    public boolean isFlexDtoOverrideExtended() {
        return getProperties().getFlexDtoProperties().isOverrideExtended();
    }

    public String getFlexDtoBaseDtoPackage() {
        return getProperties().getFlexDtoProperties().getBaseDtoPackage();
    }

    public String getFlexDtoExtendedDtoPackage() {
        return getProperties().getFlexDtoProperties().getExtendedDtoPackage();
    }

    public String getFlexDtoBaseDtoPrefix() {
        return getProperties().getFlexDtoProperties().getBaseDtoPrefix();
    }

    public String getFlexDtoBaseDtoSuffix() {
        return getProperties().getFlexDtoProperties().getBaseDtoSuffix();
    }

    public String getFlexDtoExtendedDtoPrefix() {
        return getProperties().getFlexDtoProperties().getExtendedDtoPrefix();
    }

    public String getFlexDtoExtendedDtoSuffix() {
        return getProperties().getFlexDtoProperties().getExtendedDtoSuffix();
    }

    // ===================================================================================
    //                                                                Hibernate Properties
    //                                                                ====================
    public boolean hasHibernateDefinition() {
        return getProperties().getHibernateProperties().hasHibernateDefinition();
    }

    public String getHibernateBaseEntityPackage() {
        return getBaseEntityPackage();
    }

    public String getHibernateExtendedEntityPackage() {
        return getExtendedEntityPackage();
    }

    public String getHibernateBaseEntityPrefix() {
        return getBasePrefix();
    }

    public String getHibernateManyToOneFetch() {
        return getProperties().getHibernateProperties().getManyToOneFetch();
    }

    public String getHibernateOneToOneFetch() {
        return getProperties().getHibernateProperties().getOneToOneFetch();
    }

    public String getHibernateOneToManyFetch() {
        return getProperties().getHibernateProperties().getOneToManyFetch();
    }

    // ===================================================================================
    //                                                                   S2JDBC Properties
    //                                                                   =================
    public boolean hasS2jdbcDefinition() {
        return getProperties().getS2jdbcProperties().hasS2jdbcDefinition();
    }

    public String getS2jdbcBaseEntityPackage() {
        return getProperties().getS2jdbcProperties().getBaseEntityPackage();
    }

    public String getS2jdbcExtendedEntityPackage() {
        return getProperties().getS2jdbcProperties().getExtendedEntityPackage();
    }

    public String getS2jdbcBaseEntityPrefix() {
        return getProperties().getS2jdbcProperties().getBaseEntityPrefix();
    }

    // ===================================================================================
    //                                                  Component Name Helper for Template
    //                                                  ==================================
    // -----------------------------------------------------
    //                                   AllCommon Component
    //                                   -------------------
    // /= = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = 
    // These methods for all-common components are used when it needs to identity their components.
    // For example when the DI container is Seasar, These methods are not used
    // because S2Container has name-space in the DI architecture.
    // = = = = = = = = = =/

    // Comment out because the dbfluteInitializer does not need ID or Name basically.
    //public String getDBFluteInitializerComponentName() {
    //    return filterComponentNameWithProjectPrefix("dbfluteInitializer");
    //}

    public String getInvokerAssistantComponentName() {
        return filterComponentNameWithProjectPrefix("invokerAssistant");
    }

    public String getCommonColumnAutoSetupperComponentName() {
        return filterComponentNameWithProjectPrefix("commonColumnAutoSetupper");
    }

    public String getBehaviorSelectorComponentName() {
        return filterComponentNameWithProjectPrefix("behaviorSelector");
    }

    public String getBehaviorCommandInvokerComponentName() {
        return filterComponentNameWithProjectPrefix("behaviorCommandInvoker");
    }

    // -----------------------------------------------------
    //                                     Filtering Utility
    //                                     -----------------
    /**
     * Filter a component name with a project prefix.
     * @param componentName The name of component. (NotNull)
     * @return A filtered component name with project prefix. (NotNull)
     */
    public String filterComponentNameWithProjectPrefix(String componentName) {
        final String prefix = getBasicProperties().getProjectPrefix();
        if (prefix == null || prefix.trim().length() == 0) {
            return componentName;
        }
        final String filteredPrefix = prefix.substring(0, 1).toLowerCase() + prefix.substring(1);
        return filteredPrefix + componentName.substring(0, 1).toUpperCase() + componentName.substring(1);
    }

    // ===================================================================================
    //                                                        Type Map Helper for Template
    //                                                        ============================
    public String convertJavaNativeByJdbcType(String jdbcType) {
        try {
            return TypeMap.findJavaNativeByJdbcType(jdbcType, null, null);
        } catch (RuntimeException e) {
            _log.warn("TypeMap.findJavaNativeTypeString(jdbcType, null, null) threw the exception: jdbcType="
                    + jdbcType, e);
            throw e;
        }
    }

    public String convertJavaNameByJdbcNameAsTable(String jdbcName) {
        if (getBasicProperties().isTableNameCamelCase()) {
            return jdbcName;
        }
        final List<String> inputs = new ArrayList<String>(2);
        inputs.add(jdbcName);
        inputs.add(getDefaultJavaNamingMethod());
        return StringUtils.capitalise(generateName(NameFactory.JAVA_GENERATOR, inputs));
    }

    public String convertUncapitalisedJavaNameByJdbcNameAsTable(String jdbcName) {
        return StringUtils.uncapitalise(convertJavaNameByJdbcNameAsTable(jdbcName));
    }

    public String convertJavaNameByJdbcNameAsColumn(String jdbcName) {
        if (getBasicProperties().isColumnNameCamelCase()) {
            return jdbcName;
        }
        final List<String> inputs = new ArrayList<String>(2);
        inputs.add(jdbcName);
        inputs.add(getDefaultJavaNamingMethod());
        return StringUtils.capitalise(generateName(NameFactory.JAVA_GENERATOR, inputs));
    }

    public String convertUncapitalisedJavaNameByJdbcNameAsColumn(String jdbcName) {
        return StringUtils.uncapitalise(convertJavaNameByJdbcNameAsColumn(jdbcName));
    }

    /**
     * Generate name.
     * @param algorithmName Algorithm name.
     * @param inputs Inputs.
     * @return Generated name.
     */
    protected String generateName(String algorithmName, List<?> inputs) {
        String javaName = null;
        try {
            javaName = NameFactory.generateName(NameFactory.JAVA_GENERATOR, inputs);
        } catch (EngineException e) {
            String msg = "NameFactory.generateName() threw the exception: inputs=" + inputs;
            _log.warn(msg, e);
            throw new RuntimeException(msg, e);
        } catch (RuntimeException e) {
            String msg = "NameFactory.generateName() threw the exception: inputs=" + inputs;
            _log.warn(msg, e);
            throw new RuntimeException(msg, e);
        }
        if (javaName == null) {
            String msg = "NameFactory.generateName() returned null: inputs=" + inputs;
            _log.warn(msg);
            throw new IllegalStateException(msg);
        }
        return javaName;
    }

    // ===================================================================================
    //                                                         General Helper for Template
    //                                                         ===========================
    // -----------------------------------------------------
    //                                             Character
    //                                             ---------
    public String getSharp() {
        return "#";
    }

    public String getDollar() {
        return "$";
    }

    // -----------------------------------------------------
    //                                               Comment
    //                                               -------
    public String getOverrideComment() {
        return "{@inheritDoc}";
    }

    public String getImplementComment() {
        return "{@inheritDoc}";
    }

    // -----------------------------------------------------
    //                                               Logging
    //                                               -------
    public void info(String msg) {
        _log.info(msg);
    }

    public void debug(String msg) {
        _log.debug(msg);
    }

    // -----------------------------------------------------
    //                                             Timestamp
    //                                             ---------
    public String getTimestampExpression() {
        final Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        return new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(timestamp);
    }

    // -----------------------------------------------------
    //                                                String
    //                                                ------
    public String initCap(String str) {
        if (str == null) {
            String msg = "Argument[str] must not be null.";
            throw new IllegalArgumentException(msg);
        }
        if (str.length() == 0) {
            return "";
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public boolean isInitNumber(String str) {
        if (str == null) {
            String msg = "Argument[str] must not be null.";
            throw new IllegalArgumentException(msg);
        }
        if (str.length() == 0) {
            return false;
        }
        try {
            Integer.valueOf(str.substring(0, 1));
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // -----------------------------------------------------
    //                                                   Map
    //                                                   ---
    public String getMapValue(Map<?, ?> map, String key) {
        final Object value = map.get(key);
        return value != null ? (String) value : "";
    }

    // -----------------------------------------------------
    //                                                   I/O
    //                                                   ---
    public void makeDirectory(String packagePath) {
        FileUtil.mkdir(getGeneratorInstance().getOutputPath() + "/" + packagePath);
    }

    public String getPackageAsPath(String pckge) {
        final DfPackagePathHandler handler = new DfPackagePathHandler(getBasicProperties());
        return handler.getPackageAsPath(pckge);
    }

    // ===================================================================================
    //                                                                 Behavior Query Path
    //                                                                 ===================
    protected Map<String, Map<String, Map<String, String>>> _tableBqpMap;

    public boolean hasTableBqpMap() {
        return !getTableBqpMap().isEmpty();
    }

    protected Map<String, Map<String, Map<String, String>>> getTableBqpMap() {
        if (_tableBqpMap != null) {
            return _tableBqpMap;
        }
        final DfBehaviorQueryPathSetupper setupper = new DfBehaviorQueryPathSetupper(getProperties());
        try {
            _tableBqpMap = setupper.extractTableBqpMap(collectSqlFileList());
        } catch (RuntimeException e) {
            _log.warn("Failed to extract the map of table behavior query path!", e);
            _tableBqpMap = new HashMap<String, Map<String, Map<String, String>>>();
        }
        return _tableBqpMap;
    }

    protected List<File> collectSqlFileList() {
        final String sqlDirectory = getProperties().getOutsideSqlProperties().getSqlDirectory();
        final DfSqlFileCollector sqlFileCollector = new DfSqlFileCollector(sqlDirectory, getBasicProperties());
        sqlFileCollector.suppressDirectoryCheck();
        return sqlFileCollector.collectSqlFileList();
    }

    // ===================================================================================
    //                                                                  Procedure Document
    //                                                                  ==================
    public List<DfProcedureMetaInfo> getAvailableProcedureList() {
        final DfProcedureHandler handler = new DfProcedureHandler();
        handler.includeProcedureSynonym(getDataSource());
        final String schemaName = getDatabaseSchema();
        final DataSource dataSource = getDataSource();
        try {
            final Connection conn = dataSource.getConnection();
            final DatabaseMetaData metaData = conn.getMetaData();
            final Map<String, DfProcedureMetaInfo> procedureMap = handler.getAvailableProcedureMap(metaData);
            return new ArrayList<DfProcedureMetaInfo>(procedureMap.values());
        } catch (SQLException e) {
            String msg = "Failed to get the list of available procedure:";
            msg = msg + " schemaName=" + schemaName;
            throw new IllegalStateException(msg);
        }
    }

    // ===================================================================================
    //                                                                          DataSource
    //                                                                          ==========
    protected DataSource getDataSource() {
        return DfDataSourceContext.getDataSource();
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    /**
     * Creates a string representation of this Database.
     * The representation is given in xml format.
     * @return String representation in XML. (NotNull)
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("<database name=\"").append(getName()).append('"').append(">\n");
        for (Iterator<Table> i = _tableList.iterator(); i.hasNext();) {
            sb.append(i.next());
        }
        sb.append("</database>");
        return sb.toString();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getName() {
        return _name;
    }

    public void setName(String name) {
        this._name = (name == null ? "default" : name);
    }

    /**
     * Get the value of defaultJavaNamingMethod which specifies the
     * method for converting schema names for table and column to Java names.
     * @return The default naming conversion used by this database.
     */
    public String getDefaultJavaNamingMethod() {
        return _defaultJavaNamingMethod;
    }

    /**
     * Set the value of defaultJavaNamingMethod.
     * @param v The default naming conversion for this database to use.
     */
    public void setDefaultJavaNamingMethod(String v) {
        this._defaultJavaNamingMethod = v;
    }

    public void setAppData(AppData appData) {
        _appData = appData;
    }

    public void setSql2EntitySchemaData(AppData sql2entitySchemaData) {
        _sql2entitySchemaData = sql2entitySchemaData;
    }

    public AppData getAppData() {
        return _appData;
    }

    public String getDatabaseType() {
        return _databaseType;
    }

    public void setDatabaseType(String databaseType) {
        this._databaseType = databaseType;
    }

    public Map<String, DfParameterBeanMetaData> getPmbMetaDataMap() {
        return _pmbMetaDataMap;
    }

    public void setPmbMetaDataMap(Map<String, DfParameterBeanMetaData> pmbMetaDataMap) {
        _pmbMetaDataMap = pmbMetaDataMap;
    }

    public void setSkipDeleteOldClass(boolean skipDeleteOldClass) {
        _skipDeleteOldClass = skipDeleteOldClass;
    }
}