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
package org.seasar.dbflute.config;

/**
 * @author jflute
 * @since 0.7.9 (2008/08/26 Tuesday)
 */
public class DfEnvironmentType {

    private static final DfEnvironmentType _instance = new DfEnvironmentType();
    private static final String DEFAULT_ENVIRONMENT_TYPE = "df:default";

    protected String _environmentType = DEFAULT_ENVIRONMENT_TYPE;

    private DfEnvironmentType() {
    }

    public static DfEnvironmentType getInstance() {
        return _instance;
    }

    public boolean isDefault() {
        return _environmentType != null && _environmentType.equalsIgnoreCase(DEFAULT_ENVIRONMENT_TYPE);
    }

    /**
     * @return The type of environment. (NotNull)
     */
    public String getEnvironmentType() {
        return _environmentType;
    }

    public void setEnvironmentType(String environmentType) {
        if (environmentType == null || environmentType.trim().length() == 0) {
            return;
        }
        if (environmentType.startsWith("${dfenv}")) {
            return;
        }
        _environmentType = environmentType;
    }
}
