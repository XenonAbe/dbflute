package org.seasar.dbflute.logic.deletefile;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.torque.engine.database.model.Database;
import org.apache.torque.engine.database.model.Table;
import org.seasar.dbflute.friends.velocity.DfGenerator;
import org.seasar.dbflute.helper.language.properties.DfGeneratedClassPackageDefault;
import org.seasar.dbflute.logic.pathhandling.DfPackagePathHandler;
import org.seasar.dbflute.logic.pmb.DfParameterBeanMetaData;
import org.seasar.dbflute.properties.DfBasicProperties;
import org.seasar.dbflute.properties.DfLittleAdjustmentProperties;

/**
 * @author jflute
 * @since 0.7.8 (2008/08/23 Saturday)
 */
public class DfOldClassHandler {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Log instance. */
    private static final Log _log = LogFactory.getLog(Database.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected DfGenerator _generator;
    protected DfBasicProperties _basicProperties;
    protected DfLittleAdjustmentProperties _littleAdjustmentProperties;
    protected DfGeneratedClassPackageDefault _generatedClassPackageDefault;
    protected List<Table> _tableList;
    protected Map<String, DfParameterBeanMetaData> _pmbMetaDataMap;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfOldClassHandler(DfGenerator generator, DfBasicProperties basicProperties,
            DfLittleAdjustmentProperties littleAdjustmentProperties, List<Table> tableList) {
        _generator = generator;
        _basicProperties = basicProperties;
        _littleAdjustmentProperties = littleAdjustmentProperties;
        _generatedClassPackageDefault = basicProperties.getLanguageDependencyInfo().getGeneratedClassPackageInfo();
        _tableList = tableList;
    }

    // ===================================================================================
    //                                                                     Old Table Class
    //                                                                     ===============
    public void deleteOldTableClass() {
        info("// /- - - - - - - - - - - - -");
        info("// Delete old table classes!");
        info("// - - - - - - - - - -/");
        deleteOldTableClass_for_BaseBehavior();
        deleteOldTableClass_for_BaseDao();
        deleteOldTableClass_for_BaseEntity();
        deleteOldTableClass_for_DBMeta();
        deleteOldTableClass_for_BaseConditionBean();
        deleteOldTableClass_for_AbstractBaseConditionQuery();
        deleteOldTableClass_for_BaseConditionQuery();
        deleteOldTableClass_for_NestSelectSetupper();
        deleteOldTableClass_for_ExtendedConditionBean();
        deleteOldTableClass_for_ExtendedConditionQuery();
        deleteOldTableClass_for_ExtendedConditionInlineQuery();
        deleteOldTableClass_for_ExtendedBehavior();
        deleteOldTableClass_for_ExtendedDao();
        deleteOldTableClass_for_ExtendedEntity();
        info("");
    }

    protected List<String> _deletedOldTableBaseBehaviorList;

    public void deleteOldTableClass_for_BaseBehavior() {
        final NotDeleteTCNSetupper setupper = new NotDeleteTCNSetupper() {
            public String setup(Table table) {
                if (_basicProperties.isApplicationBehavior()) {
                    return table.getBaseBehaviorApClassName();
                } else {
                    return table.getBaseBehaviorClassName();
                }
            }
        };
        final String packagePath = getBaseBehaviorPackage();
        final String classPrefix = getProjectPrefix() + getBasePrefix();
        final String classSuffix;
        if (_basicProperties.isApplicationBehavior()) {
            classSuffix = "Bhv" + _basicProperties.getApplicationBehaviorSuffix();
        } else {
            classSuffix = "Bhv";
        }
        final DfOldTableClassDeletor deletor = createTCD(packagePath, classPrefix, classSuffix, setupper);
        _deletedOldTableBaseBehaviorList = deletor.deleteOldTableClass();
        showDeleteOldTableFile(_deletedOldTableBaseBehaviorList);
    }

    protected String getBaseBehaviorPackage() {
        return _basicProperties.getBaseBehaviorPackage();
    }

    protected List<String> _deletedOldTableBaseDaoList;

    public void deleteOldTableClass_for_BaseDao() {
        final NotDeleteTCNSetupper setupper = new NotDeleteTCNSetupper() {
            public String setup(Table table) {
                return table.getBaseDaoClassName();
            }
        };
        final String packagePath = getBaseDaoPackage();
        final String classPrefix = getProjectPrefix() + getBasePrefix();
        final DfOldTableClassDeletor deletor = createTCD(packagePath, classPrefix, "Dao", setupper);
        _deletedOldTableBaseDaoList = deletor.deleteOldTableClass();
        showDeleteOldTableFile(_deletedOldTableBaseDaoList);
    }

    protected String getBaseDaoPackage() {
        return _basicProperties.getBaseDaoPackage();
    }

    protected List<String> _deletedOldTableBaseEntityList;

    public void deleteOldTableClass_for_BaseEntity() {
        final NotDeleteTCNSetupper setupper = new NotDeleteTCNSetupper() {
            public String setup(Table table) {
                return table.getBaseEntityClassName();
            }
        };
        final String packagePath = getBaseEntityPackage();
        final String classPrefix = getProjectPrefix() + getBasePrefix();
        final DfOldTableClassDeletor deletor = createTCD(packagePath, classPrefix, null, setupper);
        _deletedOldTableBaseEntityList = deletor.deleteOldTableClass();
        showDeleteOldTableFile(_deletedOldTableBaseEntityList);
    }

    protected String getBaseEntityPackage() {
        return _basicProperties.getBaseEntityPackage();
    }

    public void deleteOldTableClass_for_DBMeta() {
        final NotDeleteTCNSetupper setupper = new NotDeleteTCNSetupper() {
            public String setup(Table table) {
                return table.getDBMetaClassName();
            }
        };
        final String packagePath = getDBMetaPackage();
        final String classPrefix = getProjectPrefix();
        final DfOldTableClassDeletor deletor = createTCD(packagePath, classPrefix, "Dbm", setupper);
        showDeleteOldTableFile(deletor.deleteOldTableClass());
    }

    protected String getDBMetaPackage() {
        return _basicProperties.getDBMetaPackage();
    }

    public void deleteOldTableClass_for_BaseConditionBean() {
        final NotDeleteTCNSetupper setupper = new NotDeleteTCNSetupper() {
            public String setup(Table table) {
                return table.getBaseConditionBeanClassName();
            }
        };
        final String packagePath = getConditionBeanPackage() + ".bs";
        final String classPrefix = getProjectPrefix() + getBasePrefix();
        final DfOldTableClassDeletor deletor = createTCD(packagePath, classPrefix, "CB", setupper);
        showDeleteOldTableFile(deletor.deleteOldTableClass());
    }

    protected String getConditionBeanPackage() {
        return _basicProperties.getConditionBeanPackage();
    }

    public void deleteOldTableClass_for_AbstractBaseConditionQuery() {
        final NotDeleteTCNSetupper setupper = new NotDeleteTCNSetupper() {
            public String setup(Table table) {
                return table.getAbstractBaseConditionQueryClassName();
            }
        };
        final String packagePath = getConditionBeanPackage() + ".cq.bs";
        final String classPrefix = getProjectPrefix() + "Abstract" + getBasePrefix();
        final DfOldTableClassDeletor deletor = createTCD(packagePath, classPrefix, "CQ", setupper);
        showDeleteOldTableFile(deletor.deleteOldTableClass());
    }

    public void deleteOldTableClass_for_BaseConditionQuery() {
        final NotDeleteTCNSetupper setupper = new NotDeleteTCNSetupper() {
            public String setup(Table table) {
                return table.getBaseConditionQueryClassName();
            }
        };
        final String packagePath = getConditionBeanPackage() + ".cq.bs";
        final String classPrefix = getProjectPrefix() + getBasePrefix();
        final DfOldTableClassDeletor deletor = createTCD(packagePath, classPrefix, "CQ", setupper);
        showDeleteOldTableFile(deletor.deleteOldTableClass());
    }

    public void deleteOldTableClass_for_NestSelectSetupper() {
        final NotDeleteTCNSetupper setupper = new NotDeleteTCNSetupper() {
            public String setup(Table table) {
                return table.getNestSelectSetupperClassName();
            }
        };
        final String packagePath = getConditionBeanPackage() + ".nss";
        final String classPrefix = getProjectPrefix();
        final DfOldTableClassDeletor deletor = createTCD(packagePath, classPrefix, "Nss", setupper);
        showDeleteOldTableFile(deletor.deleteOldTableClass());
    }

    public void deleteOldTableClass_for_ExtendedConditionBean() {
        final NotDeleteTCNSetupper setupper = new NotDeleteTCNSetupper() {
            public String setup(Table table) {
                return table.getExtendedConditionBeanClassName();
            }
        };
        final String packagePath = getConditionBeanPackage();
        final String classPrefix = getProjectPrefix();
        final DfOldTableClassDeletor deletor = createTCD(packagePath, classPrefix, "CB", setupper);
        showDeleteOldTableFile(deletor.deleteOldTableClass());
    }

    public void deleteOldTableClass_for_ExtendedConditionQuery() {
        final NotDeleteTCNSetupper setupper = new NotDeleteTCNSetupper() {
            public String setup(Table table) {
                return table.getExtendedConditionQueryClassName();
            }
        };
        final String packagePath = getConditionBeanPackage() + ".cq";
        final String classPrefix = getProjectPrefix();
        final DfOldTableClassDeletor deletor = createTCD(packagePath, classPrefix, "CQ", setupper);
        showDeleteOldTableFile(deletor.deleteOldTableClass());
    }

    public void deleteOldTableClass_for_ExtendedConditionInlineQuery() {
        final NotDeleteTCNSetupper setupper = new NotDeleteTCNSetupper() {
            public String setup(Table table) {
                return table.getExtendedConditionInlineQueryClassName();
            }
        };
        final String packagePath = getConditionBeanPackage() + ".cq.ciq";
        final String classPrefix = getProjectPrefix();
        final DfOldTableClassDeletor deletor = createTCD(packagePath, classPrefix, "CIQ", setupper);
        showDeleteOldTableFile(deletor.deleteOldTableClass());
    }

    public void deleteOldTableClass_for_ExtendedBehavior() {
        if (_deletedOldTableBaseBehaviorList == null || _deletedOldTableBaseBehaviorList.isEmpty()) {
            return;
        }
        final String outputPath = _generator.getOutputPath();
        final String packagePath = getExtendedBehaviorPackage();
        final DfPackagePathHandler packagePathHandler = createPackagePathHandler();
        final String dirPath = outputPath + "/" + packagePathHandler.getPackageAsPath(packagePath);
        for (String baseClassName : _deletedOldTableBaseBehaviorList) {
            final int prefixLength = getProjectPrefix().length() + getBasePrefix().length();
            final String extendedClassName = getProjectPrefix() + baseClassName.substring(prefixLength);
            final File file = new File(dirPath + "/" + extendedClassName + "." + getClassFileExtension());
            if (file.exists()) {
                file.delete();
                _log.info("deleteOldTableClass('" + extendedClassName + "');");
            }
        }
    }

    protected String getExtendedBehaviorPackage() {
        return _basicProperties.getExtendedBehaviorPackage();
    }

    public void deleteOldTableClass_for_ExtendedDao() {
        if (_deletedOldTableBaseDaoList == null || _deletedOldTableBaseDaoList.isEmpty()) {
            return;
        }
        final String outputPath = _generator.getOutputPath();
        final String packagePath = getExtendedDaoPackage();
        final DfPackagePathHandler packagePathHandler = createPackagePathHandler();
        final String dirPath = outputPath + "/" + packagePathHandler.getPackageAsPath(packagePath);
        for (String baseClassName : _deletedOldTableBaseDaoList) {
            final int prefixLength = getProjectPrefix().length() + getBasePrefix().length();
            final String extendedClassName = getProjectPrefix() + baseClassName.substring(prefixLength);
            final File file = new File(dirPath + "/" + extendedClassName + "." + getClassFileExtension());
            if (file.exists()) {
                file.delete();
                _log.info("deleteOldTableClass('" + extendedClassName + "');");
            }
        }
    }

    protected String getExtendedDaoPackage() {
        return _basicProperties.getExtendedDaoPackage();
    }

    public void deleteOldTableClass_for_ExtendedEntity() {
        if (_deletedOldTableBaseEntityList == null || _deletedOldTableBaseEntityList.isEmpty()) {
            return;
        }
        final String outputPath = _generator.getOutputPath();
        final String packagePath = getExtendedEntityPackage();
        final DfPackagePathHandler packagePathHandler = createPackagePathHandler();
        final String dirPath = outputPath + "/" + packagePathHandler.getPackageAsPath(packagePath);
        for (String baseClassName : _deletedOldTableBaseEntityList) {
            final int prefixLength = getProjectPrefix().length() + getBasePrefix().length();
            final String extendedClassName = getProjectPrefix() + baseClassName.substring(prefixLength);
            final File file = new File(dirPath + "/" + extendedClassName + "." + getClassFileExtension());
            if (file.exists()) {
                file.delete();
                _log.info("deleteOldTableClass('" + extendedClassName + "');");
            }
        }
    }

    protected String getExtendedEntityPackage() {
        return _basicProperties.getExtendedEntityPackage();
    }

    protected void showDeleteOldTableFile(List<String> deletedClassNameList) {
        for (String className : deletedClassNameList) {
            _log.info("deleteOldTableClass('" + className + "');");
        }
    }

    protected DfOldTableClassDeletor createTCD(String packagePath, String classPrefix, String classSuffix,
            NotDeleteTCNSetupper setupper) { // createOldTableClassDeletor()
        final DfOldTableClassDeletor deletor = new DfOldTableClassDeletor(_generator, createPackagePathHandler());
        deletor.addPackagePath(packagePath);
        deletor.setClassPrefix(classPrefix);
        deletor.setClassSuffix(classSuffix);
        deletor.setClassExtension(getClassFileExtension());
        deletor.setNotDeleteClassNameSet(createNotDeleteTCNSet(setupper));
        return deletor;
    }

    protected static interface NotDeleteTCNSetupper { // NotDeleteTableClassNameSetupper
        public String setup(Table table);
    }

    protected Set<String> createNotDeleteTCNSet(NotDeleteTCNSetupper setupper) {
        final Set<String> notDeleteClassNameSet = new HashSet<String>();
        final List<Table> tableList = getTableList();
        for (Table table : tableList) {
            notDeleteClassNameSet.add(setupper.setup(table));
        }
        return notDeleteClassNameSet;
    }

    // ===================================================================================
    //                                                                 Old Customize Class
    //                                                                 ===================
    public void deleteOldCustomizeClass() {
        info("// /- - - - - - - - - - - - - - -");
        info("// Delete old customize classes!");
        info("// - - - - - - - - - -/");
        deleteOldCustomizeClass_for_BaseCustomizeEntity();
        deleteOldCustomizeClass_for_DBMeta();
        deleteOldCustomizeClass_for_BaseCursor();
        deleteOldCustomizeClass_for_BaseCursorHandler();
        deleteOldCustomizeClass_for_BaseParameterBean();
        deleteOldCustomizeClass_for_ExtendedCustomizeEntity();
        deleteOldCustomizeClass_for_ExtendedCursor();
        deleteOldCustomizeClass_for_ExtendedCursorHandler();
        deleteOldCustomizeClass_for_ExtendedParameterBean();
        info("");
    }

    protected List<String> _deletedOldCustomizeBaseEntityList;

    public void deleteOldCustomizeClass_for_BaseCustomizeEntity() {
        final String customizePackageName = _generatedClassPackageDefault.getCustomizeEntitySimplePackageName();
        final String packagePath = getBaseEntityPackage() + "." + customizePackageName;
        final String classPrefix = getProjectPrefix() + getBasePrefix();
        final DfOldTableClassDeletor deletor = createCCD(packagePath, classPrefix, null, new NotDeleteTCNSetupper() {
            public String setup(Table table) {
                return table.getBaseEntityClassName();
            }
        });
        _deletedOldCustomizeBaseEntityList = deletor.deleteOldTableClass();
        showDeleteOldTableFile(_deletedOldCustomizeBaseEntityList);
    }

    public void deleteOldCustomizeClass_for_DBMeta() {
        final String customizePackageName = _generatedClassPackageDefault.getCustomizeEntitySimplePackageName();
        final String dbmetaSimplePackageName = _generatedClassPackageDefault.getDBMetaSimplePackageName();
        final String packagePath = getBaseEntityPackage() + "." + customizePackageName + "." + dbmetaSimplePackageName;
        final String classPrefix = getProjectPrefix();
        final DfOldTableClassDeletor deletor = createCCD(packagePath, classPrefix, "Dbm", new NotDeleteTCNSetupper() {
            public String setup(Table table) {
                return table.getDBMetaClassName();
            }
        });
        showDeleteOldTableFile(deletor.deleteOldTableClass());
    }

    protected List<String> _deletedOldCustomizeBaseCursorList;

    public void deleteOldCustomizeClass_for_BaseCursor() {
        final String cursorPackageName = _generatedClassPackageDefault.getCursorSimplePackageName();
        final String packagePath = getBaseBehaviorPackage() + "." + cursorPackageName;
        final String oldStylePackagePath = getBaseDaoPackage() + "." + cursorPackageName;
        final String classPrefix = getProjectPrefix() + getBasePrefix();
        final String classSuffix = "Cursor";
        final DfOldTableClassDeletor deletor = createCCD(packagePath, classPrefix, classSuffix,
                new NotDeleteTCNSetupper() {
                    public String setup(Table table) {
                        return table.getBaseEntityClassName() + classSuffix;
                    }
                });
        deletor.addPackagePath(oldStylePackagePath); // for Old Style Package
        _deletedOldCustomizeBaseCursorList = deletor.deleteOldTableClass();
        showDeleteOldTableFile(_deletedOldCustomizeBaseCursorList);
    }

    protected List<String> _deletedOldCustomizeBaseCursorHandlerList;

    public void deleteOldCustomizeClass_for_BaseCursorHandler() {
        final String cursorPackageName = _generatedClassPackageDefault.getCursorSimplePackageName();
        final String packagePath = getBaseBehaviorPackage() + "." + cursorPackageName;
        final String oldStylePackagePath = getBaseDaoPackage() + "." + cursorPackageName;
        final String classPrefix = getProjectPrefix() + getBasePrefix();
        final String classSuffix = "CursorHandler";
        final DfOldTableClassDeletor deletor = createCCD(packagePath, classPrefix, classSuffix,
                new NotDeleteTCNSetupper() {
                    public String setup(Table table) {
                        return table.getBaseEntityClassName() + classSuffix;
                    }
                });
        deletor.addPackagePath(oldStylePackagePath); // for Old Style Package
        _deletedOldCustomizeBaseCursorHandlerList = deletor.deleteOldTableClass();
        showDeleteOldTableFile(_deletedOldCustomizeBaseCursorHandlerList);
    }

    protected List<String> _deletedOldCustomizeBaseParameterBeanList;

    public void deleteOldCustomizeClass_for_BaseParameterBean() {
        final String parameterBeanPackageName = _generatedClassPackageDefault.getParameterBeanSimplePackageName();
        final String packagePath = getBaseBehaviorPackage() + "." + parameterBeanPackageName;
        final String oldStylePackagePath = getBaseDaoPackage() + "." + parameterBeanPackageName;
        final String classPrefix = getProjectPrefix() + getBasePrefix();
        final Set<String> notDeleteClassNameSet = new HashSet<String>();
        final Map<String, DfParameterBeanMetaData> pmbMetaDataMap = getPmbMetaDataMap();
        if (pmbMetaDataMap != null && !pmbMetaDataMap.isEmpty()) {
            final Set<String> pmbNameSet = pmbMetaDataMap.keySet();
            for (String pmbName : pmbNameSet) {
                notDeleteClassNameSet.add(getProjectPrefix() + getBasePrefix() + pmbName);
            }
        }
        final DfOldTableClassDeletor deletor = createCCD(packagePath, classPrefix, null, notDeleteClassNameSet);
        deletor.addPackagePath(oldStylePackagePath); // for Old Style Package
        _deletedOldCustomizeBaseParameterBeanList = deletor.deleteOldTableClass();
        showDeleteOldTableFile(_deletedOldCustomizeBaseParameterBeanList);
    }

    public void deleteOldCustomizeClass_for_ExtendedCustomizeEntity() {
        if (_deletedOldCustomizeBaseEntityList == null || _deletedOldCustomizeBaseEntityList.isEmpty()) {
            return;
        }
        final String customizePackageName = _generatedClassPackageDefault.getCustomizeEntitySimplePackageName();
        final String packagePath = getExtendedEntityPackage() + "." + customizePackageName;
        deleteCustomizeExtendedClass(_deletedOldCustomizeBaseEntityList, packagePath);
    }

    public void deleteOldCustomizeClass_for_ExtendedCursor() {
        if (_deletedOldCustomizeBaseCursorList == null || _deletedOldCustomizeBaseCursorList.isEmpty()) {
            return;
        }
        final String cursorPackageName = _generatedClassPackageDefault.getCursorSimplePackageName();
        final String packagePath = getExtendedBehaviorPackage() + "." + cursorPackageName;
        final String oldStylePackagePath = getExtendedDaoPackage() + "." + cursorPackageName;
        deleteCustomizeExtendedClass(_deletedOldCustomizeBaseCursorList, packagePath, oldStylePackagePath);
    }

    public void deleteOldCustomizeClass_for_ExtendedCursorHandler() {
        if (_deletedOldCustomizeBaseCursorHandlerList == null || _deletedOldCustomizeBaseCursorHandlerList.isEmpty()) {
            return;
        }
        final String cursorPackageName = _generatedClassPackageDefault.getCursorSimplePackageName();
        final String packagePath = getExtendedBehaviorPackage() + "." + cursorPackageName;
        final String oldStylePackagePath = getExtendedDaoPackage() + "." + cursorPackageName;
        deleteCustomizeExtendedClass(_deletedOldCustomizeBaseCursorHandlerList, packagePath, oldStylePackagePath);
    }

    public void deleteOldCustomizeClass_for_ExtendedParameterBean() {
        if (_deletedOldCustomizeBaseParameterBeanList == null || _deletedOldCustomizeBaseParameterBeanList.isEmpty()) {
            return;
        }
        final String parameterBeanPackageName = _generatedClassPackageDefault.getParameterBeanSimplePackageName();
        final String packagePath = getExtendedBehaviorPackage() + "." + parameterBeanPackageName;
        final String oldStylePackagePath = getExtendedDaoPackage() + "." + parameterBeanPackageName;
        deleteCustomizeExtendedClass(_deletedOldCustomizeBaseParameterBeanList, packagePath, oldStylePackagePath);
    }

    protected DfOldTableClassDeletor createCCD(String packagePath, String classPrefix, String classSuffix,
            NotDeleteTCNSetupper setupper) { // createOldCustomizeClassDeletor()
        return createCCD(packagePath, classPrefix, classSuffix, createNotDeleteTCNSet(setupper));
    }

    protected DfOldTableClassDeletor createCCD(String packagePath, String classPrefix, String classSuffix,
            Set<String> notDeleteClassNameSet) { // createOldCustomizeClassDeletor()
        final DfPackagePathHandler packagePathHandler = new DfPackagePathHandler(_basicProperties);
        final DfOldTableClassDeletor deletor = new DfOldTableClassDeletor(_generator, packagePathHandler);
        deletor.addPackagePath(packagePath);
        deletor.setClassPrefix(classPrefix);
        deletor.setClassSuffix(classSuffix);
        deletor.setClassExtension(getClassFileExtension());
        deletor.setNotDeleteClassNameSet(notDeleteClassNameSet);
        return deletor;
    }

    protected void deleteCustomizeExtendedClass(List<String> baseClassList, String... packagePathList) {
        final String outputPath = _generator.getOutputPath();
        final DfPackagePathHandler packagePathHandler = createPackagePathHandler();
        for (String packagePath : packagePathList) {
            final String dirPath = outputPath + "/" + packagePathHandler.getPackageAsPath(packagePath);
            for (String baseClassName : baseClassList) {
                final int prefixLength = getProjectPrefix().length() + getBasePrefix().length();
                final String extendedClassName = getProjectPrefix() + baseClassName.substring(prefixLength);
                final File file = new File(dirPath + "/" + extendedClassName + "." + getClassFileExtension());
                if (file.exists()) {
                    file.delete();
                    _log.info("deleteOldCustoimzeClass('" + extendedClassName + "');");
                }
            }
        }
    }

    // ===================================================================================
    //                                                                       Assist Helper
    //                                                                       =============
    protected DfPackagePathHandler createPackagePathHandler() {
        return new DfPackagePathHandler(_basicProperties);
    }

    protected String getProjectPrefix() {
        return _basicProperties.getProjectPrefix();
    }

    protected String getBasePrefix() {
        return _basicProperties.getBasePrefix();
    }

    protected String getClassFileExtension() {
        return _basicProperties.getClassFileExtension();
    }

    protected List<Table> getTableList() {
        return _tableList;
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

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public Map<String, DfParameterBeanMetaData> getPmbMetaDataMap() {
        return _pmbMetaDataMap;
    }

    public void setPmbMetaDataMap(Map<String, DfParameterBeanMetaData> pmbMetaDataMap) {
        this._pmbMetaDataMap = pmbMetaDataMap;
    }
}
