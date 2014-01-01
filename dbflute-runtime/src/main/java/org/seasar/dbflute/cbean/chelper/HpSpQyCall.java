/*
 * Copyright 2004-2014 the Seasar Foundation and the Others.
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
package org.seasar.dbflute.cbean.chelper;

import org.seasar.dbflute.cbean.ConditionQuery;

/**
 * The callback of query for specification.
 * @author jflute
 * @param <CQ> The type of condition-query.
 */
public interface HpSpQyCall<CQ extends ConditionQuery> {

    /**
     * Does it have its own query?
     * @return The determination, true or false.
     */
    boolean has();

    /**
     * Delegate query method.
     * @return The condition-query. (NotNull)
     */
    CQ qy();
}
