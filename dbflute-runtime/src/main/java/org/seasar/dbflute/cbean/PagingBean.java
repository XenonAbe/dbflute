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
    // * * * * * * * *
    // For SQL Comment
    // * * * * * * * *
    /**
     * Is the execution for paging(NOT count)?
     * @return Determination.
     */
    public boolean isPaging();

    // * * * * * * * *
    // For Framework
    // * * * * * * * *
    /**
     * Is the count executed later?
     * @return Determination.
     */
    public boolean isCountLater();

    // ===================================================================================
    //                                                                      Paging Setting
    //                                                                      ==============
    /**
     * Set up paging resources.
	 * @param pageSize The page size per one page. (NotMinus & NotZero)
	 * @param pageNumber The number of page. It's ONE origin. (NotMinus & NotZero: If it's minus or zero, it treats as one.)
     */
    public void paging(int pageSize, int pageNumber);

    /**
     * Set whether the execution for paging(NOT count). {INTERNAL METHOD}
     * @param paging Determination.
     */
    public void xsetPaging(boolean paging);

    /**
     * Disable paging re-select that is executed when the page number is over page count.
     */
    public void disablePagingReSelect();

    /**
     * Can the paging re-select?
     * @return Can the paging re-select?
     */
    public boolean canPagingReSelect();

    // ===================================================================================
    //                                                                       Fetch Setting
    //                                                                       =============
    /**
     * Fetch first. <br />
     * If you invoke this, your SQL returns [fetch-size] records from first.
     * @param fetchSize The size of fetch. (NotMinus & NotZero)
     * @return this. (NotNull)
     */
    public PagingBean fetchFirst(int fetchSize);

    /**
     * Fetch scope. <br />
     * If you invoke this, your SQL returns [fetch-size] records from [fetch-start-index].
     * @param fetchStartIndex The start index of fetch. 0 origin. (NotMinus)
     * @param fetchSize The size of fetch. (NotMinus & NotZero)
     * @return this. (NotNull)
     */
    public PagingBean fetchScope(int fetchStartIndex, int fetchSize);

    /**
     * Fetch page. <br />
     * When you invoke this, it is normally necessary to invoke 'fetchFirst()' or 'fetchScope()' ahead of that. <br />
     * But you also can use default-fetch-size without invoking 'fetchFirst()' or 'fetchScope()'. <br />
     * If you invoke this, your SQL returns [fetch-size] records from [fetch-start-index] calculated by [fetch-page-number].
     * @param fetchPageNumber The page number of fetch. 1 origin. (NotMinus & NotZero: If minus or zero, set one.)
     * @return this. (NotNull)
     */
    public PagingBean fetchPage(int fetchPageNumber);

    // ===================================================================================
    //                                                                      Fetch Property
    //                                                                      ==============
    /**
     * Get fetch-start-index.
     * @return Fetch-start-index.
     */
    public int getFetchStartIndex();

    /**
     * Get fetch-size.
     * @return Fetch-size.
     */
    public int getFetchSize();

    /**
     * Get fetch-page-number.
     * @return Fetch-page-number.
     */
    public int getFetchPageNumber();

    /**
     * Get page start index.
     * @return Page start index. 0 origin. (NotMinus)
     */
    public int getPageStartIndex();

    /**
     * Get page end index.
     * @return Page end index. 0 origin. (NotMinus)
     */
    public int getPageEndIndex();

    /**
     * Is fetch scope effective?
     * @return Determination.
     */
    public boolean isFetchScopeEffective();
}
