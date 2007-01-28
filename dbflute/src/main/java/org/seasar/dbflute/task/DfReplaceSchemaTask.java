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
package org.seasar.dbflute.task;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.ant.BuildException;
import org.seasar.dbflute.DfBuildProperties;
import org.seasar.dbflute.helper.datahandler.DfSeparatedDataResultInfo;
import org.seasar.dbflute.helper.datahandler.DfSeparatedDataSeveralHandlingInfo;
import org.seasar.dbflute.helper.datahandler.DfXlsDataHandler;
import org.seasar.dbflute.helper.datahandler.impl.DfSeparatedDataHandlerImpl;
import org.seasar.dbflute.helper.datahandler.impl.DfXlsDataHandlerImpl;
import org.seasar.dbflute.helper.jdbc.DfRunnerInformation;
import org.seasar.dbflute.helper.jdbc.schemainitializer.DfSchemaInitializerMySQL;
import org.seasar.dbflute.helper.jdbc.sqlfile.DfSqlFileFireMan;
import org.seasar.dbflute.helper.jdbc.sqlfile.DfSqlFileRunner;
import org.seasar.dbflute.helper.jdbc.sqlfile.DfSqlFileRunnerExecute;
import org.seasar.dbflute.properties.DfReplaceSchemaProperties;
import org.seasar.dbflute.task.bs.DfAbstractTask;

public class DfReplaceSchemaTask extends DfAbstractTask {

    /** Log instance. */
    private static final Log _log = LogFactory.getLog(DfReplaceSchemaTask.class);

    // =========================================================================================
    //                                                                                DataSource
    //                                                                                ==========
    @Override
    protected boolean isUseDataSource() {
        return true;
    }

    // =========================================================================================
    //                                                                                   Execute
    //                                                                                   =======
    /**
     * Load the sql file and then execute it.
     *
     * @throws BuildException
     */
    @Override
    protected void doExecute() {
        initializeSchema();

        final DfRunnerInformation runInfo = createRunnerInformation();
        replaceSchema(runInfo);

        writeDbFromSeparatedFile("tsv", "\t");
        writeDbFromSeparatedFile("csv", ",");
        writeDbFromXls();
    }

    // --------------------------------------------
    //                            initialize schema
    //                            -----------------
    protected void initializeSchema() {
        if (DfBuildProperties.getInstance().getBasicProperties().isDatabaseMySQL()) {
            final DfSchemaInitializerMySQL initializer = createSchemaInitializerMySQL();
            initializer.initializeSchema();
        }

        // TODO: Make initializeSchema for Other DB.
    }

    protected DfSchemaInitializerMySQL createSchemaInitializerMySQL() {
        final DfSchemaInitializerMySQL initializer = new DfSchemaInitializerMySQL();
        initializer.setDataSource(getDataSource());
        return initializer;
    }

    // --------------------------------------------
    //                                       runner
    //                                       ------
    protected DfRunnerInformation createRunnerInformation() {
        final DfRunnerInformation runInfo = new DfRunnerInformation();
        runInfo.setDriver(_driver);
        runInfo.setUrl(_url);
        runInfo.setUser(_userId);
        runInfo.setPassword(_password);
        runInfo.setAutoCommit(getMyProperties().isReplaceSchemaAutoCommit());
        runInfo.setErrorContinue(getMyProperties().isReplaceSchemaErrorContinue());
        runInfo.setRollbackOnly(getMyProperties().isReplaceSchemaRollbackOnly());
        return runInfo;
    }

    protected void replaceSchema(DfRunnerInformation runInfo) {
        final DfSqlFileFireMan fireMan = new DfSqlFileFireMan();
        fireMan.execute(getSqlFileRunner(runInfo), getReplaceSchemaSqlFileList());
    }

    protected DfSqlFileRunner getSqlFileRunner(final DfRunnerInformation runInfo) {
        return new DfSqlFileRunnerExecute(runInfo, getDataSource());
    }

    // --------------------------------------------
    //                      replace schema sql file
    //                      -----------------------
    protected List<File> getReplaceSchemaSqlFileList() {
        final String sqlFile = getMyProperties().getReplaceSchemaSqlFile();
        final List<File> fileList = new ArrayList<File>();
        final File replaceSchemaSqlFile = new File(sqlFile);
        if (!replaceSchemaSqlFile.exists()) {
            String msg = "Not found replace schema sql file: " + replaceSchemaSqlFile.getPath();
            throw new IllegalStateException(msg);
        }
        fileList.add(replaceSchemaSqlFile);
        fileList.addAll(getReplaceSchemaNextSqlFileList());
        return fileList;
    }

    protected List<File> getReplaceSchemaNextSqlFileList() {
        final String replaceSchemaSqlFileDirectoryName = getReplaceSchemaSqlFileDirectoryName();
        final File baseDir = new File(replaceSchemaSqlFileDirectoryName);
        final FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                if (name.startsWith(getReplaceSchemaSqlFileNameWithoutExt())) {
                    if (name.endsWith("." + getReplaceSchemaSqlFileExt())) {
                        return true;
                    }
                }
                return false;
            }
        };
        final ArrayList<File> resultList = new ArrayList<File>();
        final String[] targetList = baseDir.list(filter);
        for (String targetFileName : targetList) {
            final String targetFilePath = replaceSchemaSqlFileDirectoryName + "/" + targetFileName;
            resultList.add(new File(targetFilePath));
        }
        return resultList;
    }

    protected String getReplaceSchemaSqlFileDirectoryName() {
        final String sqlFileName = getMyProperties().getReplaceSchemaSqlFile();
        return sqlFileName.substring(0, sqlFileName.lastIndexOf("/"));
    }

    protected String getReplaceSchemaSqlFileNameWithoutExt() {
        final String sqlFileName = getMyProperties().getReplaceSchemaSqlFile();
        final String tmp = sqlFileName.substring(sqlFileName.lastIndexOf("/") + 1);
        return tmp.substring(0, tmp.lastIndexOf("."));
    }

    protected String getReplaceSchemaSqlFileExt() {
        final String sqlFileName = getMyProperties().getReplaceSchemaSqlFile();
        return sqlFileName.substring(sqlFileName.lastIndexOf(".") + 1);
    }

    protected DfReplaceSchemaProperties getMyProperties() {
        return DfBuildProperties.getInstance().getInvokeReplaceSchemaProperties();
    }

    // --------------------------------------------
    //                                 data writing
    //                                 ------------
    protected void writeDbFromXls() {
        final DfXlsDataHandler xlsDataHandler = new DfXlsDataHandlerImpl();
        xlsDataHandler.writeSeveralData(getDataDirectoryPath("xls"), getDataSource());
    }

    protected void writeDbFromSeparatedFile(String typeName, String delimter) {
        final DfSeparatedDataHandlerImpl handler = new DfSeparatedDataHandlerImpl();
        handler.setDataSource(getDataSource());
        final DfSeparatedDataSeveralHandlingInfo handlingInfo = new DfSeparatedDataSeveralHandlingInfo();
        handlingInfo.setBasePath(getDataDirectoryPath("tsv"));
        handlingInfo.setTypeName(typeName);
        handlingInfo.setDelimter(delimter);
        handlingInfo.setErrorContinue(true);
        final DfSeparatedDataResultInfo resultInfo = handler.writeSeveralData(handlingInfo);
        showNotFoundColumn(typeName, resultInfo.getNotFoundColumnMap());
    }

    protected void showNotFoundColumn(String typeName, Map<String, Set<String>> notFoundColumnMap) {
        if (notFoundColumnMap.isEmpty()) {
            return;
        }
        _log.warn("* * * * * * * * * * * * *");
        _log.warn("Not Found Columns in " + typeName);
        _log.warn("* * * * * * * * * * * * *");
        final Set<String> notFoundColumnSet = notFoundColumnMap.keySet();
        for (String tableName : notFoundColumnSet) {
            _log.warn("[" + tableName + "]");
            final Set<String> columnNameList = notFoundColumnMap.get(tableName);
            for (String columnName : columnNameList) {
                _log.warn("    " + columnName);
            }
            _log.warn(" ");
        }
    }

    protected String getDataDirectoryPath(final String typeName) {
        return getReplaceSchemaSqlFileDirectoryName() + "/testdata/" + typeName;
    }

}
