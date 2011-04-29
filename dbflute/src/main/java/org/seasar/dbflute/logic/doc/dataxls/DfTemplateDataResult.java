package org.seasar.dbflute.logic.doc.dataxls;

import java.util.List;
import java.util.Map;

import org.seasar.dbflute.helper.jdbc.facade.DfJFadCursorCallback;

/**
 * @author jflute
 * @since 0.9.8.3 (2011/04/25 Monday)
 */
public class DfTemplateDataResult {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final List<Map<String, String>> _resultList;
    protected final DfJFadCursorCallback _cursorCallback;

    // ===================================================================================
    //                                                                         Easy-to-Use
    //                                                                         ===========
    public boolean isLargeData() {
        return _resultList == null && _cursorCallback != null;
    }

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfTemplateDataResult(List<Map<String, String>> resultList) {
        _resultList = resultList;
        _cursorCallback = null;
    }

    public DfTemplateDataResult(DfJFadCursorCallback cursorCallback) {
        _resultList = null;
        _cursorCallback = cursorCallback;
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public List<Map<String, String>> getResultList() {
        return _resultList;
    }

    public DfJFadCursorCallback getCursorCallback() {
        return _cursorCallback;
    }
}
