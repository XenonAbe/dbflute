/*
 * Copyright 2004-2009 the Seasar Foundation and the Others.
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
package org.seasar.dbflute.cbean;

/**
 * The bean of paging.
 * @author jflute
 */
public interface PagingBean extends FetchNarrowingBean, OrderByBean {

    // ===================================================================================
    //                                                                Paging Determination
    //                                                                ====================
    /**
     * Is the execution for paging(NOT count)? {for parameter comment}
     * @return Determination.
     */
    boolean isPaging();

    /**
     * Can the paging execute count later? {for framework}
     * @return Determination.
     */
    boolean canPagingCountLater();

    /**
     * Can the paging re-select? {for framework}
     * @return Determination.
     */
    boolean canPagingReSelect();

    // ===================================================================================
    //                                                                      Paging Setting
    //                                                                      ==============
    /**
     * Set up paging resources.
     * @param pageSize The page size per one page. (NotMinus & NotZero)
     * @param pageNumber The number of page. It's ONE origin. (NotMinus & NotZero: If it's minus or zero, it treats as one.)
     * @throws org.seasar.dbflute.exception.PagingPageSizeNotPlusException When the page size for paging is minus or zero. 
     */
    void paging(int pageSize, int pageNumber);

    /**
     * Set whether the execution for paging(NOT count). {INTERNAL METHOD}
     * @param paging Determination.
     */
    void xsetPaging(boolean paging);

    /**
     * Enable paging count-later that means counting after selecting.
     */
    void enablePagingCountLater();

    /**
     * Disable paging re-select that is executed when the page number is over page count.
     */
    void disablePagingReSelect();

    // ===================================================================================
    //                                                                       Fetch Setting
    //                                                                       =============
    /**
     * Fetch first. <br />
     * Your SQL returns [fetch-size] records from first.
     * @param fetchSize The size of fetch. (NotMinus & NotZero)
     * @return this. (NotNull)
     */
    PagingBean fetchFirst(int fetchSize);

    /**
     * Fetch scope. <br />
     * Your SQL returns [fetch-size] records from [fetch-start-index].
     * @param fetchStartIndex The start index of fetch. 0 origin. (NotMinus)
     * @param fetchSize The size of fetch. (NotMinus & NotZero)
     * @return this. (NotNull)
     */
    PagingBean fetchScope(int fetchStartIndex, int fetchSize);

    /**
     * Fetch page. This method is an old style, so you should use paging() instead of this. <br />
     * When you call this, it is normally necessary to invoke 'fetchFirst()' or 'fetchScope()' ahead of that. <br />
     * But you also can use default-fetch-size without invoking 'fetchFirst()' or 'fetchScope()'. <br />
     * If you invoke this, your SQL returns [fetch-size] records from [fetch-start-index] calculated by [fetch-page-number].
     * @param fetchPageNumber The page number of fetch. 1 origin. (NotMinus & NotZero: If minus or zero, set one.)
     * @return this. (NotNull)
     */
    PagingBean fetchPage(int fetchPageNumber);

    // ===================================================================================
    //                                                                      Fetch Property
    //                                                                      ==============
    /**
     * Get fetch-start-index.
     * @return Fetch-start-index.
     */
    int getFetchStartIndex();

    /**
     * Get fetch-size.
     * @return Fetch-size.
     */
    int getFetchSize();

    /**
     * Get fetch-page-number.
     * @return Fetch-page-number.
     */
    int getFetchPageNumber();

    /**
     * Get page start index.
     * @return Page start index. 0 origin. (NotMinus)
     */
    int getPageStartIndex();

    /**
     * Get page end index.
     * @return Page end index. 0 origin. (NotMinus)
     */
    int getPageEndIndex();

    /**
     * Is fetch scope effective?
     * @return Determination.
     */
    boolean isFetchScopeEffective();
}
