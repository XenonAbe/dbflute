/*
 * Copyright 2004-2010 the Seasar Foundation and the Others.
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
package org.seasar.dbflute.dbmeta.name;

/**
 * The filter call-back for SQL name.
 * @author jflute
 * @since 0.9.7.6 (2010/11/23 Tuesday)
 */
public interface SqlNameFilter {

    /**
     * Filter the SQL name of a object, for example, table or column.
     * @param sqlName The original SQL name. (NotNull)
     * @return The filtered name. (NotNull)
     */
    String filter(String sqlName);
}
