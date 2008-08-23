package org.seasar.dbflute.logic.pathhandling;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.torque.engine.database.model.Database;
import org.seasar.dbflute.properties.DfLittleAdjustmentProperties;
import org.seasar.dbflute.util.DfStringUtil;

/**
 * @author jflute
 * @since 0.7.8 (2008/08/23 Saturday)
 */
public class DfPackagePathHandler {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Log instance. */
    private static final Log _log = LogFactory.getLog(Database.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected DfLittleAdjustmentProperties _littleAdjustmentProperties;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfPackagePathHandler(DfLittleAdjustmentProperties littleAdjustmentProperties) {
        _littleAdjustmentProperties = littleAdjustmentProperties;
    }

    // ===================================================================================
    //                                                                                Main
    //                                                                                ====
    public String getPackageAsPath(String pckge) {
        final String flatDirectoryPackage = _littleAdjustmentProperties.getFlatDirectoryPackage();
        if (flatDirectoryPackage == null || flatDirectoryPackage.trim().length() == 0) {
            return resolvePackageAsPath(pckge);
        }
        if (!pckge.contains(flatDirectoryPackage)) {
            return resolvePackageAsPath(pckge);
        }
        final String flatMark = "$$df:flatMark$$";
        pckge = DfStringUtil.replace(pckge, flatDirectoryPackage, flatMark);
        pckge = resolvePackageAsPath(pckge);
        pckge = DfStringUtil.replace(pckge, flatMark, flatDirectoryPackage);
        return pckge;
    }

    protected String resolvePackageAsPath(String pckge) {
        return pckge.replace('.', File.separator.charAt(0)) + File.separator;
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
}
