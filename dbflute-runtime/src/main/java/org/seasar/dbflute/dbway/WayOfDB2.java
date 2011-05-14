/*
 * Copyright 2004-2011 the Seasar Foundation and the Others.
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
package org.seasar.dbflute.dbway;

/**
 * The DB way of DB2.
 * @author jflute
 */
public class WayOfDB2 implements DBWay {

    // ===================================================================================
    //                                                                        Sequence Way
    //                                                                        ============
    public String buildSequenceNextValSql(String sequenceName) {
        return "values nextval for " + sequenceName;
    }

    // ===================================================================================
    //                                                                       Identity Info
    //                                                                       =============
    public String getIdentitySelectSql() {
        return "values IDENTITY_VAL_LOCAL()";
    }

    // ===================================================================================
    //                                                                         SQL Support
    //                                                                         ===========
    public boolean isBlockCommentSupported() {
        return true;
    }

    public boolean isLineCommentSupported() {
        return true;
    }

    // ===================================================================================
    //                                                                        JDBC Support
    //                                                                        ============
    public boolean isScrollableCursorSupported() {
        return true;
    }

    // ===================================================================================
    //                                                                   SQLException Info
    //                                                                   =================
    public boolean isUniqueConstraintException(String sqlState, Integer errorCode) {
        return "23505".equals(sqlState);
    }
}
