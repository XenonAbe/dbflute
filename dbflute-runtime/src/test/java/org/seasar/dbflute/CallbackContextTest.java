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
package org.seasar.dbflute;

import org.seasar.dbflute.CallbackContext.CallbackContextThreadLocalProvider;
import org.seasar.dbflute.bhv.core.BehaviorCommandMeta;
import org.seasar.dbflute.bhv.core.SqlFireHook;
import org.seasar.dbflute.bhv.core.SqlFireReadyInfo;
import org.seasar.dbflute.bhv.core.SqlFireResultInfo;
import org.seasar.dbflute.unit.core.PlainTestCase;

/**
 * @author jflute
 * @since 1.0.5F (2014/05/05 Monday)
 */
public class CallbackContextTest extends PlainTestCase {

    // ===================================================================================
    //                                                                          Management
    //                                                                          ==========
    public void test_useThreadLocalProvider_basic() throws Exception {
        // ## Arrange ##
        final ThreadLocal<CallbackContext> threadLocal = new ThreadLocal<CallbackContext>() {
            @Override
            public CallbackContext get() {
                markHere("get()");
                return super.get();
            }

            @Override
            public void set(CallbackContext value) {
                if (value != null) { // because of also called by clearing
                    markHere("set()");
                }
                super.set(value);
            }
        };

        // ## Act ##
        assertTrue(CallbackContext.isLocked());
        CallbackContext.unlock();
        assertFalse(CallbackContext.isLocked());
        CallbackContext.useThreadLocalProvider(new CallbackContextThreadLocalProvider() {
            public ThreadLocal<CallbackContext> provide() {
                return threadLocal;
            }
        });
        assertTrue(CallbackContext.isLocked());

        // ## Assert ##
        CallbackContext context = new CallbackContext();
        context.setSqlFireHook(new SqlFireHook() {
            public void hookFinally(BehaviorCommandMeta meta, SqlFireResultInfo fireResultInfo) {
            }

            public void hookBefore(BehaviorCommandMeta meta, SqlFireReadyInfo fireReadyInfo) {
            }
        });
        CallbackContext.setCallbackContextOnThread(context);
        CallbackContext actual = CallbackContext.getCallbackContextOnThread();
        assertEquals(context, actual);
        assertMarked("get()");
        assertMarked("set()");
    }

    public void test_useThreadLocalProvider_locked() throws Exception {
        try {
            assertTrue(CallbackContext.isLocked());
            CallbackContext.useThreadLocalProvider(new CallbackContextThreadLocalProvider() {
                public ThreadLocal<CallbackContext> provide() {
                    return new ThreadLocal<CallbackContext>();
                }
            });
            fail();
        } catch (IllegalStateException e) {
            log(e.getMessage());
        } finally {
            assertTrue(CallbackContext.isLocked());
        }
    }

    public void test_useThreadLocalProvider_nullProvided() throws Exception {
        assertTrue(CallbackContext.isLocked());
        CallbackContext.unlock();
        assertFalse(CallbackContext.isLocked());
        try {
            CallbackContext.useThreadLocalProvider(new CallbackContextThreadLocalProvider() {
                public ThreadLocal<CallbackContext> provide() {
                    return null;
                }
            });
            assertTrue(CallbackContext.isLocked());
            CallbackContext.getCallbackContextOnThread();
            fail();
        } catch (IllegalStateException e) {
            log(e.getMessage());
        } finally {
            CallbackContext.unlock();
            CallbackContext.useThreadLocalProvider(null);
            CallbackContext.lock();
            assertTrue(CallbackContext.isLocked());
        }
    }
}
