/*
 * Copyright 2004-2007 the Seasar Foundation and the Others.
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
package org.seasar.dbflute.task.bs;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import javax.sql.DataSource;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.ant.BuildException;
import org.apache.torque.engine.database.model.UnifiedSchema;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.texen.ant.TexenTask;
import org.seasar.dbflute.DfBuildProperties;
import org.seasar.dbflute.config.DfEnvironmentType;
import org.seasar.dbflute.config.DfSpecifiedSqlFile;
import org.seasar.dbflute.friends.velocity.DfGenerator;
import org.seasar.dbflute.friends.velocity.DfOriginalLog4JLogSystem;
import org.seasar.dbflute.helper.jdbc.connection.DfSimpleDataSourceCreator;
import org.seasar.dbflute.helper.jdbc.context.DfDataSourceContext;
import org.seasar.dbflute.logic.outsidesql.DfSqlFileCollector;
import org.seasar.dbflute.logic.scmconn.DfCurrentSchemaConnector;
import org.seasar.dbflute.logic.various.DfAntTaskUtil;
import org.seasar.dbflute.properties.DfBasicProperties;
import org.seasar.dbflute.properties.DfDatabaseProperties;
import org.seasar.dbflute.properties.DfRefreshProperties;
import org.seasar.dbflute.properties.DfReplaceSchemaProperties;

/**
 * The abstract class of texen task.
 * @author jflute
 */
public abstract class DfAbstractTexenTask extends TexenTask {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Log instance. */
    public static final Log _log = LogFactory.getLog(DfAbstractTexenTask.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** DB driver. */
    protected String _driver;

    /** DB URL. */
    protected String _url;

    /** Main schema. */
    protected UnifiedSchema _mainSchema;

    /** User name. */
    protected String _userId;

    /** Password */
    protected String _password;

    /** Connection properties. */
    protected Properties _connectionProperties;

    /** The simple creator of data source. (NotNull) */
    protected final DfSimpleDataSourceCreator _dataSourceCreator = new DfSimpleDataSourceCreator();

    // ===================================================================================
    //                                                                                Main
    //                                                                                ====
    // -----------------------------------------------------
    //                                               Execute
    //                                               -------
    @Override
    public final void execute() { // completely override
        boolean failure = false;
        long before = getTaskBeforeTimeMillis();
        try {
            initializeDatabaseInfo();
            if (isUseDataSource()) {
                setupDataSource();
            }
            doExecute();
        } catch (Exception e) {
            failure = true;
            try {
                logException(e);
            } catch (Throwable ignored) {
                _log.warn("*Ignored exception occured!", ignored);
                _log.error("*Failed to execute DBFlute Task!", e);
            }
        } catch (Error e) {
            failure = true;
            try {
                logError(e);
            } catch (Throwable ignored) {
                _log.warn("*Ignored exception occured!", ignored);
                _log.error("*Failed to execute DBFlute Task!", e);
            }
        } finally {
            if (isUseDataSource()) {
                try {
                    commitDataSource();
                } catch (Exception ignored) {
                } finally {
                    try {
                        destroyDataSource();
                    } catch (Exception ignored) {
                        _log.warn("*Failed to destroy data source: " + ignored.getMessage());
                    }
                }
            }
            if (isValidTaskEndInformation() || failure) {
                try {
                    long after = getTaskAfterTimeMillis();
                    showFinalMessage(before, after, failure);
                } catch (RuntimeException e) {
                    _log.info("*Failed to show final message!", e);
                }
            }
            if (failure) {
                throwTaskFailure();
            }
        }
    }

    protected long getTaskBeforeTimeMillis() {
        return System.currentTimeMillis();
    }

    protected long getTaskAfterTimeMillis() {
        return System.currentTimeMillis();
    }

    protected void logException(Exception e) {
        DfAntTaskUtil.logException(e, getDisplayTaskName());
    }

    protected void logError(Error e) {
        DfAntTaskUtil.logError(e, getDisplayTaskName());
    }

    protected boolean isValidTaskEndInformation() {
        return true;
    }

    protected void showFinalMessage(long before, long after, boolean failure) {
        final String environmentType = DfEnvironmentType.getInstance().getEnvironmentType();
        final StringBuilder sb = new StringBuilder();
        final String ln = ln();
        sb.append(ln);
        sb.append(ln).append("_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/");
        sb.append(ln).append("[Task End]: " + getPerformanceView(after - before));
        if (failure) {
            sb.append(ln).append("    * * * * * *");
            sb.append(ln).append("    * Failure *");
            sb.append(ln).append("    * * * * * *");
        }
        sb.append(ln);
        sb.append(ln).append("  DBFLUTE_CLIENT: {" + getBasicProperties().getProjectName() + "}");
        sb.append(ln).append("    database  = " + getBasicProperties().getDatabaseType());
        sb.append(ln).append("    language  = " + getBasicProperties().getTargetLanguage());
        sb.append(ln).append("    container = " + getBasicProperties().getTargetContainerName());
        sb.append(ln).append("    package   = " + getBasicProperties().getPackageBase());
        sb.append(ln);
        sb.append(ln).append("  DBFLUTE_ENVIRONMENT_TYPE: {" + environmentType + "}");
        sb.append(ln).append("    driver = " + _driver);
        sb.append(ln).append("    url    = " + _url);
        sb.append(ln).append("    schema = " + _mainSchema);
        sb.append(ln).append("    user   = " + _userId);
        sb.append(ln).append("    props  = " + _connectionProperties);

        final String additionalSchemaDisp = buildAdditionalSchemaDisp();
        sb.append(ln).append("    additionalSchema = " + additionalSchemaDisp);
        final DfReplaceSchemaProperties replaceSchemaProp = getProperties().getReplaceSchemaProperties();
        sb.append(ln).append("    dataLoadingType  = " + replaceSchemaProp.getDataLoadingType());
        final String refreshProjectDisp = buildRefreshProjectDisp();
        sb.append(ln).append("    refreshProject   = " + refreshProjectDisp);

        final String finalInformation = getFinalInformation();
        if (finalInformation != null) {
            sb.append(ln);
            sb.append(finalInformation);
        }
        sb.append(ln).append("_/_/_/_/_/_/_/_/_/_/" + " {" + getDisplayTaskName() + "}");
        _log.info(sb.toString());
    }

    private String buildAdditionalSchemaDisp() {
        final DfDatabaseProperties databaseProp = getDatabaseProperties();
        final List<UnifiedSchema> additionalSchemaList = databaseProp.getAdditionalSchemaList();
        String disp;
        if (additionalSchemaList.size() == 1) {
            final UnifiedSchema unifiedSchema = additionalSchemaList.get(0);
            final String identifiedSchema = unifiedSchema.getIdentifiedSchema();
            disp = identifiedSchema;
            if (unifiedSchema.isCatalogAdditionalSchema()) {
                disp = disp + "(catalog)";
            } else if (unifiedSchema.isMainSchema()) { // should NOT be true
                disp = disp + "(main)";
            } else if (unifiedSchema.isUnknownSchema()) { // should NOT be true
                disp = disp + "(unknown)";
            }
        } else {
            final StringBuilder sb = new StringBuilder();
            for (UnifiedSchema unifiedSchema : additionalSchemaList) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                final String identifiedSchema = unifiedSchema.getIdentifiedSchema();
                sb.append(identifiedSchema);
                if (unifiedSchema.isCatalogAdditionalSchema()) {
                    sb.append("(catalog)");
                } else if (unifiedSchema.isMainSchema()) { // should NOT be true
                    sb.append("(main)");
                } else if (unifiedSchema.isUnknownSchema()) { // should NOT be true
                    sb.append("(unknown)");
                }
            }
            disp = sb.toString();
        }
        return disp;
    }

    private String buildRefreshProjectDisp() {
        final DfRefreshProperties refreshProp = getProperties().getRefreshProperties();
        if (!refreshProp.hasRefreshDefinition()) {
            return "";
        }
        final List<String> refreshProjectList = refreshProp.getProjectNameList();
        final String disp;
        if (refreshProjectList.size() == 1) {
            disp = refreshProjectList.get(0);
        } else {
            final StringBuilder sb = new StringBuilder();
            for (String refreshProject : refreshProjectList) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(refreshProject);
            }
            disp = sb.toString();
        }
        return disp;
    }

    protected String getDisplayTaskName() {
        final String taskName = getTaskName();
        return DfAntTaskUtil.getDisplayTaskName(taskName);
    }

    protected String getFinalInformation() {
        return null; // as default
    }

    protected void throwTaskFailure() {
        DfAntTaskUtil.throwTaskFailure(getDisplayTaskName());
    }

    protected void initializeDatabaseInfo() {
        _driver = getDatabaseProperties().getDatabaseDriver();
        _url = getDatabaseProperties().getDatabaseUrl();
        _userId = getDatabaseProperties().getDatabaseUser();
        _mainSchema = getDatabaseProperties().getDatabaseSchema();
        _password = getDatabaseProperties().getDatabasePassword();
        _connectionProperties = getDatabaseProperties().getDatabaseConnectionProperties();
    }

    abstract protected void doExecute();

    /**
     * Get performance view.
     * @param mil The value of millisecond.
     * @return Performance view. (ex. 1m23s456ms) (NotNull)
     */
    protected String getPerformanceView(long mil) {
        if (mil < 0) {
            return String.valueOf(mil);
        }

        long sec = mil / 1000;
        long min = sec / 60;
        sec = sec % 60;
        mil = mil % 1000;

        StringBuffer sb = new StringBuffer();
        if (min >= 10) { // Minute
            sb.append(min).append("m");
        } else if (min < 10 && min >= 0) {
            sb.append("0").append(min).append("m");
        }
        if (sec >= 10) { // Second
            sb.append(sec).append("s");
        } else if (sec < 10 && sec >= 0) {
            sb.append("0").append(sec).append("s");
        }
        if (mil >= 100) { // Millisecond
            sb.append(mil).append("ms");
        } else if (mil < 100 && mil >= 10) {
            sb.append("0").append(mil).append("ms");
        } else if (mil < 10 && mil >= 0) {
            sb.append("00").append(mil).append("ms");
        }

        return sb.toString();
    }

    // -----------------------------------------------------
    //                                        Custom Execute
    //                                        --------------
    protected void fireVelocityProcess() {
        assertBasicAntParameter();

        // set up the encoding of templates from DBFlute property
        setInputEncoding(getBasicProperties().getTemplateFileEncoding());
        setOutputEncoding(getBasicProperties().getSourceFileEncoding());

        try {
            initializeVelocityInstance();
            final DfGenerator generator = setupGenerator();
            final Context ctx = setupControlContext();

            _log.info("generator.parse(\"" + controlTemplate + "\", c);");
            generator.parse(controlTemplate, ctx);
            generator.shutdown();
            cleanup();
        } catch (BuildException e) {
            throw e;
        } catch (MethodInvocationException e) {
            String msg = "Exception thrown by '" + e.getReferenceName() + "." + e.getMethodName() + "'.";
            throw new IllegalStateException(msg, e.getWrappedThrowable());
        } catch (ParseErrorException e) {
            throw new IllegalStateException("Velocity syntax error.", e);
        } catch (ResourceNotFoundException e) {
            throw new IllegalStateException("Resource not found.", e);
        } catch (Exception e) {
            throw new IllegalStateException("Generation failed.", e);
        }
    }

    private void assertBasicAntParameter() {
        if (templatePath == null && !useClasspath) {
            String msg = "The template path needs to be defined if you are not using the classpath for locating templates!";
            throw new IllegalStateException(msg);
        }
        if (controlTemplate == null) {
            throw new IllegalStateException("The control template needs to be defined!");
        }
        // *because of unused
        //if (outputDirectory == null) {
        //    throw new IllegalStateException("The output directory needs to be defined!");
        //}
        // *because of unused
        //if (outputFile == null) {
        //    throw new IllegalStateException("The output file needs to be defined!");
        //}
    }

    private void initializeVelocityInstance() {
        // /---------------------------
        // Initialize Velocity instance 
        // ----------/
        if (templatePath != null) {
            log("Using templatePath: " + templatePath, 3);
            setupVelocityTemplateProperty();
        }
        if (useClasspath) {
            log("Using classpath");
            setupVelocityClasspathProperty();
        }
        setupVelocityLogProperty();
        try {
            Velocity.init();
        } catch (Exception e) {
            String msg = "Failed to initialize Velocity:";
            msg = msg + " templatePath=" + templatePath + " useClasspath=" + useClasspath;
            throw new IllegalStateException(msg, e);
        }
    }

    private void setupVelocityTemplateProperty() {
        Velocity.setProperty("file.resource.loader.path", templatePath);
    }

    private void setupVelocityClasspathProperty() {
        final String resourceLoaderName = "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader";
        Velocity.addProperty("resource.loader", "classpath");
        Velocity.setProperty("classpath.resource.loader.class", resourceLoaderName);
        Velocity.setProperty("classpath.resource.loader.cache", "false");
        Velocity.setProperty("classpath.resource.loader.modificationCheckInterval", "2");
    }

    private void setupVelocityLogProperty() {
        Velocity.setProperty(VelocityEngine.RUNTIME_LOG_LOGSYSTEM_CLASS, DfOriginalLog4JLogSystem.class.getName());
    }

    private DfGenerator setupGenerator() {
        final DfGenerator generator = getGeneratorHandler();

        // *set up later using DBFlute property (dfprop)
        //generator.setOutputPath(outputDirectory);

        // actually from DBFlute property (dfprop)
        // because these variables could be set up before here
        generator.setInputEncoding(inputEncoding);
        generator.setOutputEncoding(outputEncoding);

        if (templatePath != null) {
            generator.setTemplatePath(templatePath);
        }
        return generator;
    }

    private Context setupControlContext() {
        final Context ctx;
        try {
            ctx = initControlContext();
        } catch (Exception e) {
            String msg = "Failed to initialize control context:";
            msg = msg + " templatePath=" + templatePath + " useClasspath=" + useClasspath;
            throw new IllegalStateException(msg, e);
        }
        try {
            populateInitialContext(ctx);
        } catch (Exception e) {
            String msg = "Failed to populate initial context:";
            msg = msg + " templatePath=" + templatePath + " useClasspath=" + useClasspath;
            throw new IllegalStateException(msg, e);
        }
        if (contextProperties != null) {
            for (Iterator<?> i = contextProperties.getKeys(); i.hasNext();) {
                String property = (String) i.next();
                String value = contextProperties.getString(property);
                try {
                    ctx.put(property, new Integer(value));
                } catch (NumberFormatException nfe) {
                    String booleanString = contextProperties.testBoolean(value);
                    if (booleanString != null) {
                        ctx.put(property, Boolean.valueOf(booleanString));
                    } else {
                        if (property.endsWith("file.contents")) {
                            final String canonicalPath;
                            try {
                                canonicalPath = getProject().resolveFile(value).getCanonicalPath();
                            } catch (IOException e) {
                                String msg = "Failed to get the canonical path:";
                                msg = msg + " property=" + property + " value=" + value;
                                throw new IllegalStateException(msg, e);
                            }
                            value = fileContentsToString(canonicalPath);
                            property = property.substring(0, property.indexOf("file.contents") - 1);
                        }
                        ctx.put(property, value);
                    }
                }
            }
        }
        return ctx;
    }

    // Copy from velocity.
    private static String fileContentsToString(String file) {
        String contents = "";
        File f = new File(file);
        if (f.exists()) {
            FileReader fr = null;
            try {
                fr = new FileReader(f);
                char template[] = new char[(int) f.length()];
                fr.read(template);
                contents = new String(template);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (fr != null) {
                    try {
                        fr.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        }
        return contents;
    }

    // -----------------------------------------------------
    //                                           Data Source
    //                                           -----------
    abstract protected boolean isUseDataSource();

    protected void setupDataSource() throws SQLException {
        _dataSourceCreator.setUserId(_userId);
        _dataSourceCreator.setPassword(_password);
        _dataSourceCreator.setDriver(_driver);
        _dataSourceCreator.setUrl(_url);
        _dataSourceCreator.setConnectionProperties(_connectionProperties);
        _dataSourceCreator.setAutoCommit(true);
        _dataSourceCreator.create();
        connectSchema();
    }

    protected void commitDataSource() throws SQLException {
        _dataSourceCreator.commit();
    }

    protected void destroyDataSource() throws SQLException {
        _dataSourceCreator.destroy();

        if (getBasicProperties().isDatabaseDerby()) {
            // Derby(Embedded) needs an original shutdown for destroying a connection
            DfAntTaskUtil.shutdownIfDerbyEmbedded(_driver);
        }
    }

    protected DataSource getDataSource() {
        return DfDataSourceContext.getDataSource();
    }

    protected void connectSchema() throws SQLException {
        final DfCurrentSchemaConnector connector = new DfCurrentSchemaConnector(_mainSchema, getBasicProperties());
        connector.connectSchema(getDataSource());
    }

    // -----------------------------------------------------
    //                                    Context Properties
    //                                    ------------------
    @Override
    public void setContextProperties(String file) { // called by ANT (and completely override)
        try {
            // /- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
            // Initialize torque properties as Properties and set up singleton class
            // that saves 'build.properties'.
            // - - - - - - - - - -/
            final Properties prop = DfAntTaskUtil.getBuildProperties(file, getProject());
            DfBuildProperties.getInstance().setProperties(prop);

            // /- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
            // Initialize context properties for Velocity.
            // - - - - - - - - - -/
            contextProperties = new ExtendedProperties();
            final Set<Entry<Object, Object>> entrySet = prop.entrySet();
            for (Entry<Object, Object> entry : entrySet) {
                contextProperties.setProperty((String) entry.getKey(), entry.getValue());
            }
        } catch (RuntimeException e) {
            String msg = "Failed to set context properties:";
            msg = msg + " file=" + file + " contextProperties=" + contextProperties;
            _log.warn(msg, e); // logging because it throws to ANT world
            throw e;
        }
    }

    // ===================================================================================
    //                                                                 SQL File Collecting
    //                                                                 ===================
    /**
     * Collect SQL files the list.
     * @return The list of SQL files. (NotNull)
     */
    protected List<File> collectSqlFileList() {
        final String sqlDirectory = getProperties().getOutsideSqlProperties().getSqlDirectory();
        final DfSqlFileCollector sqlFileCollector = new DfSqlFileCollector(sqlDirectory, getBasicProperties());
        return sqlFileCollector.collectSqlFileList();
    }

    // ===================================================================================
    //                                                                    Refresh Resource
    //                                                                    ================
    protected void refreshResources() {
        if (!isRefresh()) {
            return;
        }
        final List<String> projectNameList = getRefreshProjectNameList();
        for (String projectName : projectNameList) {
            doRefreshResources(projectName);
        }
    }

    protected void doRefreshResources(String projectName) {
        final StringBuilder sb = new StringBuilder().append("refresh?");
        sb.append(projectName).append("=INFINITE");

        final URL url = getRefreshRequestURL(sb.toString());
        if (url == null) {
            return;
        }

        InputStream is = null;
        try {
            _log.info("/- - - - - - - - - - - - - - - - - - - - - - - -");
            _log.info("...Refreshing the project: " + projectName);
            URLConnection connection = url.openConnection();
            connection.setReadTimeout(getRefreshRequestReadTimeout());
            connection.connect();
            is = connection.getInputStream();
            _log.info("");
            _log.info("    --> OK, Look the refreshed project!");
            _log.info("- - - - - - - - - -/");
            _log.info("");
        } catch (IOException ignored) {
            _log.info("");
            _log.info("    --> Oh, no! " + ignored.getMessage() + ": " + url);
            _log.info("- - - - - - - - - -/");
            _log.info("");
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    protected boolean isRefresh() {
        final DfRefreshProperties prop = getProperties().getRefreshProperties();
        return prop.hasRefreshDefinition();
    }

    protected int getRefreshRequestReadTimeout() {
        return 3 * 1000;
    }

    protected List<String> getRefreshProjectNameList() {
        final DfRefreshProperties prop = getProperties().getRefreshProperties();
        return prop.getProjectNameList();
    }

    protected URL getRefreshRequestURL(String path) {
        final DfRefreshProperties prop = getProperties().getRefreshProperties();
        String requestUrl = prop.getRequestUrl();
        if (requestUrl.length() > 0) {
            if (!requestUrl.endsWith("/")) {
                requestUrl = requestUrl + "/";
            }
            try {
                return new URL(requestUrl + path);
            } catch (MalformedURLException e) {
                _log.warn("The URL was invalid: " + requestUrl, e);
                return null;
            }
        } else {
            return null;
        }
    }

    // ===================================================================================
    //                                                                SQL File Information
    //                                                                ====================
    protected void showTargetSqlFileInformation(List<File> sqlFileList) {
        _log.info("/- - - - - - - - - - - - - - - - - - - - - - - -");
        _log.info("Target SQL files: " + sqlFileList.size());
        _log.info(" ");
        for (File sqlFile : sqlFileList) {
            _log.info("  " + sqlFile.getName());
        }
        _log.info("- - - - - - - - - -/");
        _log.info(" ");

        final String specifiedSqlFile = DfSpecifiedSqlFile.getInstance().getSpecifiedSqlFile();
        if (specifiedSqlFile != null) {
            _log.info("/- - - - - - - - - - - - - - - - - - - - - - - -");
            _log.info("Specified SQL file: " + specifiedSqlFile);
            _log.info("- - - - - - - - - -/");
            _log.info(" ");
        }
    }

    // ===================================================================================
    //                                                                    Skip Information
    //                                                                    ================
    protected void showSkippedFileInformation() {
        boolean skipGenerateIfSameFile = getProperties().getLittleAdjustmentProperties().isSkipGenerateIfSameFile();
        if (!skipGenerateIfSameFile) {
            _log.info("/- - - - - - - - - - - - - - - - - - - - - - - -");
            _log.info("All class files have been generated. (overrided)");
            _log.info("- - - - - - - - - -/");
            _log.info("");
            return;
        }
        List<String> parseFileNameList = DfGenerator.getInstance().getParseFileNameList();
        int parseSize = parseFileNameList.size();
        if (parseSize == 0) {
            _log.info("/- - - - - - - - - - - - - - - - - - - - - - - -");
            _log.info("No class file has been parsed.");
            _log.info("- - - - - - - - - -/");
            _log.info("");
            return;
        }
        List<String> skipFileNameList = DfGenerator.getInstance().getSkipFileNameList();
        int skipSize = skipFileNameList.size();
        if (skipSize == 0) {
            _log.info("/- - - - - - - - - - - - - - - - - - - - - - - -");
            _log.info("All class files have been generated. (overrided)");
            _log.info("- - - - - - - - - -/");
            _log.info("");
            return;
        }
        _log.info("/- - - - - - - - - - - - - - - - - - - - - - - -");
        if (skipSize == parseSize) {
            _log.info("All class files have been skipped generating");
            _log.info("                because they have no change.");
        } else {
            _log.info("Several class files have been skipped generating");
            _log.info("                    because they have no change.");
        }
        _log.info("");
        _log.info("    --> " + skipSize + " skipped (in " + parseSize + " files)");
        _log.info("- - - - - - - - - -/");
        _log.info("");
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected DfBuildProperties getProperties() {
        return DfBuildProperties.getInstance();
    }

    protected DfBasicProperties getBasicProperties() {
        return getProperties().getBasicProperties();
    }

    protected DfDatabaseProperties getDatabaseProperties() {
        return getProperties().getDatabaseProperties();
    }

    // ===================================================================================
    //                                                                       Assist Helper
    //                                                                       =============
    public DfGenerator getGeneratorHandler() {
        return DfGenerator.getInstance();
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String ln() {
        return "\n";
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getTargetDatabase() {
        return getBasicProperties().getDatabaseType();
    }

    public void setEnvironmentType(String environmentType) {
        DfEnvironmentType.getInstance().setEnvironmentType(environmentType);
    }
}