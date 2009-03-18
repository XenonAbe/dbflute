package org.seasar.dbflute.cbean.cvalue;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

/**
 * 
 * @author jflute
 * @since 0.9.4 (2009/03/18 Wednesday)
 */
public class ConditionValueTest extends TestCase {

    // ===================================================================================
    //                                                                            In Scope
    //                                                                            ========
    public void test_inScope_SeveralRegistered() {
        // ## Arrange ##
        ConditionValue cv = new ConditionValue();
        List<String> value1 = new ArrayList<String>();
        value1.add("a");
        value1.add("b");
        value1.add("c");
        List<String> value2 = new ArrayList<String>();
        value2.add("d");
        value2.add("e");
        value2.add("f");
        List<String> value3 = new ArrayList<String>();
        value3.add("g");
        value3.add("h");
        value3.add("i");

        // ## Act ##
        cv.setInScope(value1);
        cv.setInScope(value2);
        cv.setInScope(value3);

        // ## Assert ##
        assertEquals(value1, cv.getInScope());
        assertEquals(value2, cv.getInScope());
        assertEquals(value3, cv.getInScope());
        assertEquals(value1, cv.getInScope());
        assertEquals(value2, cv.getInScope());
        assertEquals(value3, cv.getInScope());
        assertEquals(value1, cv.getInScope());
        assertEquals(value2, cv.getInScope());
        assertEquals(value3, cv.getInScope());
    }

    // ===================================================================================
    //                                                                        Not In Scope
    //                                                                        ============
    public void test_notInScope_SeveralRegistered() {
        // ## Arrange ##
        ConditionValue cv = new ConditionValue();
        List<String> value1 = new ArrayList<String>();
        value1.add("a");
        value1.add("b");
        value1.add("c");
        List<String> value2 = new ArrayList<String>();
        value2.add("d");
        value2.add("e");
        value2.add("f");

        // ## Act ##
        cv.setNotInScope(value1);
        cv.setNotInScope(value2);

        // ## Assert ##
        assertEquals(value1, cv.getNotInScope());
        assertEquals(value2, cv.getNotInScope());
        assertEquals(value1, cv.getNotInScope());
        assertEquals(value2, cv.getNotInScope());
        assertEquals(value1, cv.getNotInScope());
        assertEquals(value2, cv.getNotInScope());
    }
}
