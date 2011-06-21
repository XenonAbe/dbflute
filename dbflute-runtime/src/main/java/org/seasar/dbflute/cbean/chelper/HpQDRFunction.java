package org.seasar.dbflute.cbean.chelper;

import org.seasar.dbflute.cbean.ConditionBean;
import org.seasar.dbflute.cbean.SubQuery;
import org.seasar.dbflute.cbean.coption.DerivedReferrerOption;

/**
 * @author jflute
 * @param <CB> The type of condition-bean.
 */
public class HpQDRFunction<CB extends ConditionBean> {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final HpQDRSetupper<CB> _setupper;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public HpQDRFunction(HpQDRSetupper<CB> setupper) {
        _setupper = setupper;
    }

    // ===================================================================================
    //                                                                            Function
    //                                                                            ========
    /**
     * Set up the sub query of referrer for the scalar 'count'.
     * <pre>
     * cb.query().derivedPurchaseList().count(new SubQuery&lt;PurchaseCB&gt;() {
     *     public void query(PurchaseCB subCB) {
     *         subCB.specify().columnPurchaseId(); // *Point
     *         subCB.query().setPaymentCompleteFlg_Equal_True();
     *     }
     * }).greaterEqual(123); // *Don't forget the parameter
     * </pre> 
     * @param subQuery The sub query of referrer. (NotNull) 
     * @return The parameter for comparing with scalar. (NotNull)
     */
    public HpQDRParameter<CB, Integer> count(SubQuery<CB> subQuery) {
        return doCount(subQuery, null);
    }

    /**
     * An overload method for count(). So refer to the method's java-doc about basic info.
     * <pre>
     * cb.query().derivedPurchaseList().count(new SubQuery&lt;PurchaseCB&gt;() {
     *     public void query(PurchaseCB subCB) {
     *         ...
     *     }
     * }).greaterEqual(123, new DerivedReferrerOption().coalesce(0));
     * </pre> 
     * @param subQuery The sub query of referrer. (NotNull)
     * @param option The option for DerivedReferrer. For example, you can use a coalesce function. (NotNull)
     * @return The parameter for comparing with scalar. (NotNull)
     */
    public HpQDRParameter<CB, Integer> count(SubQuery<CB> subQuery, DerivedReferrerOption option) {
        assertDerivedReferrerOption(option);
        return doCount(subQuery, option);
    }

    protected HpQDRParameter<CB, Integer> doCount(SubQuery<CB> subQuery, DerivedReferrerOption option) {
        assertSubQuery(subQuery);
        return new HpQDRParameter<CB, Integer>("count", subQuery, option, _setupper);
    }

    /**
     * Set up the sub query of referrer for the scalar 'count(with distinct)'.
     * <pre>
     * cb.query().derivedPurchaseList().countDistinct(new SubQuery&lt;PurchaseCB&gt;() {
     *     public void query(PurchaseCB subCB) {
     *         subCB.specify().columnPurchasePrice(); // *Point
     *         subCB.query().setPaymentCompleteFlg_Equal_True();
     *     }
     * }).greaterEqual(123); // *Don't forget the parameter
     * </pre> 
     * @param subQuery The sub query of referrer. (NotNull) 
     * @return The parameter for comparing with scalar. (NotNull)
     */
    public HpQDRParameter<CB, Integer> countDistinct(SubQuery<CB> subQuery) {
        return doCountDistinct(subQuery, null);
    }

    /**
     * An overload method for countDistinct(). So refer to the method's java-doc about basic info.
     * <pre>
     * cb.query().derivedPurchaseList().countDistinct(new SubQuery&lt;PurchaseCB&gt;() {
     *     public void query(PurchaseCB subCB) {
     *         ...
     *     }
     * }).greaterEqual(123, new DerivedReferrerOption().coalesce(0));
     * </pre> 
     * @param subQuery The sub query of referrer. (NotNull)
     * @param option The option for DerivedReferrer. For example, you can use a coalesce function. (NotNull)
     * @return The parameter for comparing with scalar. (NotNull)
     */
    public HpQDRParameter<CB, Integer> countDistinct(SubQuery<CB> subQuery, DerivedReferrerOption option) {
        assertDerivedReferrerOption(option);
        return doCountDistinct(subQuery, option);
    }

    protected HpQDRParameter<CB, Integer> doCountDistinct(SubQuery<CB> subQuery, DerivedReferrerOption option) {
        assertSubQuery(subQuery);
        return new HpQDRParameter<CB, Integer>("count(distinct", subQuery, option, _setupper);
    }

    /**
     * Set up the sub query of referrer for the scalar 'max'.
     * <pre>
     * cb.query().derivedPurchaseList().max(new SubQuery&lt;PurchaseCB&gt;() {
     *     public void query(PurchaseCB subCB) {
     *         subCB.specify().columnPurchasePrice(); // *Point
     *         subCB.query().setPaymentCompleteFlg_Equal_True();
     *     }
     * }).greaterEqual(123); // *Don't forget the parameter
     * </pre> 
     * @param subQuery The sub query of referrer. (NotNull) 
     * @return The parameter for comparing with scalar. (NotNull)
     */
    public HpQDRParameter<CB, Object> max(SubQuery<CB> subQuery) {
        return doMax(subQuery, null);
    }

    /**
     * An overload method for max(). So refer to the method's java-doc about basic info.
     * <pre>
     * cb.query().derivedPurchaseList().max(new SubQuery&lt;PurchaseCB&gt;() {
     *     public void query(PurchaseCB subCB) {
     *         ...
     *     }
     * }).greaterEqual(123, new DerivedReferrerOption().coalesce(0));
     * </pre> 
     * @param subQuery The sub query of referrer. (NotNull)
     * @param option The option for DerivedReferrer. For example, you can use a coalesce function. (NotNull)
     * @return The parameter for comparing with scalar. (NotNull)
     */
    public HpQDRParameter<CB, Object> max(SubQuery<CB> subQuery, DerivedReferrerOption option) {
        assertDerivedReferrerOption(option);
        return doMax(subQuery, option);
    }

    protected HpQDRParameter<CB, Object> doMax(SubQuery<CB> subQuery, DerivedReferrerOption option) {
        assertSubQuery(subQuery);
        return new HpQDRParameter<CB, Object>("max", subQuery, option, _setupper);
    }

    /**
     * Set up the sub query of referrer for the scalar 'min'.
     * <pre>
     * cb.query().derivedPurchaseList().min(new SubQuery&lt;PurchaseCB&gt;() {
     *     public void query(PurchaseCB subCB) {
     *         subCB.specify().columnPurchasePrice(); // *Point
     *         subCB.query().setPaymentCompleteFlg_Equal_True();
     *     }
     * }).greaterEqual(123); // *Don't forget the parameter
     * </pre> 
     * @param subQuery The sub query of referrer. (NotNull) 
     * @return The parameter for comparing with scalar. (NotNull)
     */
    public HpQDRParameter<CB, Object> min(SubQuery<CB> subQuery) {
        return doMin(subQuery, null);
    }

    /**
     * An overload method for min(). So refer to the method's java-doc about basic info.
     * <pre>
     * cb.query().derivedPurchaseList().min(new SubQuery&lt;PurchaseCB&gt;() {
     *     public void query(PurchaseCB subCB) {
     *         ...
     *     }
     * }).greaterEqual(123, new DerivedReferrerOption().coalesce(0));
     * </pre> 
     * @param subQuery The sub query of referrer. (NotNull)
     * @param option The option for DerivedReferrer. For example, you can use a coalesce function. (NotNull)
     * @return The parameter for comparing with scalar. (NotNull)
     */
    public HpQDRParameter<CB, Object> min(SubQuery<CB> subQuery, DerivedReferrerOption option) {
        return doMin(subQuery, option);
    }

    protected HpQDRParameter<CB, Object> doMin(SubQuery<CB> subQuery, DerivedReferrerOption option) {
        assertSubQuery(subQuery);
        return new HpQDRParameter<CB, Object>("min", subQuery, option, _setupper);
    }

    /**
     * Set up the sub query of referrer for the scalar 'sum'.
     * <pre>
     * cb.query().derivedPurchaseList().sum(new SubQuery&lt;PurchaseCB&gt;() {
     *     public void query(PurchaseCB subCB) {
     *         subCB.specify().columnPurchasePrice(); // *Point
     *         subCB.query().setPaymentCompleteFlg_Equal_True();
     *     }
     * }).greaterEqual(123); // *Don't forget the parameter
     * </pre> 
     * @param subQuery The sub query of referrer. (NotNull) 
     * @return The parameter for comparing with scalar. (NotNull)
     */
    public HpQDRParameter<CB, Number> sum(SubQuery<CB> subQuery) {
        return doSum(subQuery, null);
    }

    /**
     * An overload method for sum(). So refer to the method's java-doc about basic info.
     * <pre>
     * cb.query().derivedPurchaseList().sum(new SubQuery&lt;PurchaseCB&gt;() {
     *     public void query(PurchaseCB subCB) {
     *         ...
     *     }
     * }).greaterEqual(123, new DerivedReferrerOption().coalesce(0));
     * </pre> 
     * @param subQuery The sub query of referrer. (NotNull)
     * @param option The option for DerivedReferrer. For example, you can use a coalesce function. (NotNull)
     * @return The parameter for comparing with scalar. (NotNull)
     */
    public HpQDRParameter<CB, Number> sum(SubQuery<CB> subQuery, DerivedReferrerOption option) {
        assertDerivedReferrerOption(option);
        return doSum(subQuery, option);
    }

    protected HpQDRParameter<CB, Number> doSum(SubQuery<CB> subQuery, DerivedReferrerOption option) {
        assertSubQuery(subQuery);
        return new HpQDRParameter<CB, Number>("sum", subQuery, option, _setupper);
    }

    /**
     * Set up the sub query of referrer for the scalar 'avg'.
     * <pre>
     * cb.query().derivedPurchaseList().avg(new SubQuery&lt;PurchaseCB&gt;() {
     *     public void query(PurchaseCB subCB) {
     *         subCB.specify().columnPurchasePrice(); // *Point
     *         subCB.query().setPaymentCompleteFlg_Equal_True();
     *     }
     * }).greaterEqual(123); // *Don't forget the parameter
     * </pre> 
     * @param subQuery The sub query of referrer. (NotNull) 
     * @return The parameter for comparing with scalar. (NotNull)
     */
    public HpQDRParameter<CB, Number> avg(SubQuery<CB> subQuery) {
        return doAvg(subQuery, null);
    }

    /**
     * An overload method for avg(). So refer to the method's java-doc about basic info.
     * <pre>
     * cb.query().derivedPurchaseList().avg(new SubQuery&lt;PurchaseCB&gt;() {
     *     public void query(PurchaseCB subCB) {
     *         ...
     *     }
     * }).greaterEqual(123, new DerivedReferrerOption().coalesce(0));
     * </pre> 
     * @param subQuery The sub query of referrer. (NotNull)
     * @param option The option for DerivedReferrer. For example, you can use a coalesce function. (NotNull)
     * @return The parameter for comparing with scalar. (NotNull)
     */
    public HpQDRParameter<CB, Number> avg(SubQuery<CB> subQuery, DerivedReferrerOption option) {
        return doAvg(subQuery, option);
    }

    protected HpQDRParameter<CB, Number> doAvg(SubQuery<CB> subQuery, DerivedReferrerOption option) {
        assertSubQuery(subQuery);
        return new HpQDRParameter<CB, Number>("avg", subQuery, option, _setupper);
    }

    // ===================================================================================
    //                                                                       Assist Helper
    //                                                                       =============
    protected void assertSubQuery(SubQuery<?> subQuery) {
        if (subQuery == null) {
            String msg = "The argument 'subQuery' for DerivedReferrer should not be null.";
            throw new IllegalArgumentException(msg);
        }
    }

    protected void assertDerivedReferrerOption(DerivedReferrerOption option) {
        if (option == null) {
            String msg = "The argument 'option' for DerivedReferrer should not be null.";
            throw new IllegalArgumentException(msg);
        }
    }
}
