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
package org.seasar.dbflute.s2dao.beans.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Time;
import java.sql.Timestamp;

import org.seasar.dbflute.s2dao.beans.TnBeanDesc;
import org.seasar.dbflute.s2dao.beans.TnPropertyDesc;
import org.seasar.dbflute.s2dao.beans.exception.TnIllegalPropertyRuntimeException;
import org.seasar.dbflute.util.DfReflectionUtil;
import org.seasar.dbflute.util.DfTypeUtil;

/**
 * {Refers to S2Container's utility and Extends it}
 * @author jflute
 */
public class TnPropertyDescImpl implements TnPropertyDesc {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Object[] EMPTY_ARGS = new Object[0];

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    private String propertyName;
    private Class<?> propertyType;
    private Method readMethod;
    private Method writeMethod;
    private Field field;
    private TnBeanDesc beanDesc;
    private Constructor<?> stringConstructor;
    private Method valueOfMethod;
    private boolean readable = false;
    private boolean writable = false;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public TnPropertyDescImpl(String propertyName, Class<?> propertyType, Method readMethod, Method writeMethod,
            TnBeanDesc beanDesc) {

        this(propertyName, propertyType, readMethod, writeMethod, null, beanDesc);
    }

    public TnPropertyDescImpl(String propertyName, Class<?> propertyType, Method readMethod, Method writeMethod,
            Field field, TnBeanDesc beanDesc) {
        if (propertyName == null) {
            String msg = "The argument 'propertyName' should not be null!";
            throw new IllegalArgumentException(msg);
        }
        if (propertyType == null) {
            String msg = "The argument 'propertyType' should not be null!";
            throw new IllegalArgumentException(msg);
        }
        this.propertyName = propertyName;
        this.propertyType = propertyType;
        setReadMethod(readMethod);
        setWriteMethod(writeMethod);
        setField(field);
        this.beanDesc = beanDesc;
        setupStringConstructor();
        setupValueOfMethod();
    }

    private void setupStringConstructor() {
        Constructor<?>[] cons = propertyType.getConstructors();
        for (int i = 0; i < cons.length; ++i) {
            Constructor<?> con = cons[i];
            if (con.getParameterTypes().length == 1 && con.getParameterTypes()[0].equals(String.class)) {
                stringConstructor = con;
                break;
            }
        }
    }

    private void setupValueOfMethod() {
        Method[] methods = propertyType.getMethods();
        for (int i = 0; i < methods.length; ++i) {
            Method method = methods[i];
            if (DfReflectionUtil.isBridgeMethod(method) || DfReflectionUtil.isSyntheticMethod(method)) {
                continue;
            }
            if (DfReflectionUtil.isStatic(method.getModifiers()) && method.getName().equals("valueOf")
                    && method.getParameterTypes().length == 1 && method.getParameterTypes()[0].equals(String.class)) {
                valueOfMethod = method;
                break;
            }
        }
    }

    // ===================================================================================
    //                                                                                Bean
    //                                                                                ====
    public TnBeanDesc getBeanDesc() {
        return beanDesc;
    }

    // ===================================================================================
    //                                                                            Property
    //                                                                            ========
    public final String getPropertyName() {
        return propertyName;
    }

    public final Class<?> getPropertyType() {
        return propertyType;
    }

    // ===================================================================================
    //                                                                              Method
    //                                                                              ======
    public final Method getReadMethod() {
        return readMethod;
    }

    public final void setReadMethod(Method readMethod) {
        this.readMethod = readMethod;
        if (readMethod != null) {
            readable = true;
        }
    }

    public final boolean hasReadMethod() {
        return readMethod != null;
    }

    public final Method getWriteMethod() {
        return writeMethod;
    }

    public final void setWriteMethod(Method writeMethod) {
        this.writeMethod = writeMethod;
        if (writeMethod != null) {
            writable = true;
        }
    }

    public final boolean hasWriteMethod() {
        return writeMethod != null;
    }

    // ===================================================================================
    //                                                                               Field
    //                                                                               =====
    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
        if (field != null && DfReflectionUtil.isPublic(field.getModifiers())) {
            readable = true;
            writable = true;
        }
    }

    // ===================================================================================
    //                                                                       Determination
    //                                                                       =============
    public boolean isReadable() {
        return readable;
    }

    public boolean isWritable() {
        return writable;
    }

    // ===================================================================================
    //                                                                               Value
    //                                                                               =====
    public final Object getValue(Object target) {
        try {
            if (!readable) {
                throw new IllegalStateException(propertyName + " is not readable.");
            } else if (hasReadMethod()) {
                return DfReflectionUtil.invoke(readMethod, target, EMPTY_ARGS);
            } else {
                return DfReflectionUtil.getValue(field, target);
            }
        } catch (Throwable t) {
            throw new TnIllegalPropertyRuntimeException(beanDesc.getBeanClass(), propertyName, t);
        }
    }

    public final void setValue(Object target, Object value) {
        try {
            value = convertIfNeed(value);
            if (!writable) {
                throw new IllegalStateException(propertyName + " is not writable.");
            } else if (hasWriteMethod()) {
                DfReflectionUtil.invoke(writeMethod, target, new Object[] { value });
            } else {
                DfReflectionUtil.setValue(field, target, value);
            }
        } catch (Throwable t) {
            throw new TnIllegalPropertyRuntimeException(beanDesc.getBeanClass(), propertyName, t);
        }
    }

    public final String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("propertyName=");
        buf.append(propertyName);
        buf.append(",propertyType=");
        buf.append(propertyType.getName());
        buf.append(",readMethod=");
        buf.append(readMethod != null ? readMethod.getName() : "null");
        buf.append(",writeMethod=");
        buf.append(writeMethod != null ? writeMethod.getName() : "null");
        return buf.toString();
    }

    public Object convertIfNeed(Object arg) {
        if (propertyType.isPrimitive()) {
            return convertPrimitiveWrapper(arg);
        } else if (Number.class.isAssignableFrom(propertyType)) {
            return convertNumber(arg);
        } else if (java.util.Date.class.isAssignableFrom(propertyType)) {
            return convertDate(arg);
        } else if (Boolean.class.isAssignableFrom(propertyType)) {
            return DfTypeUtil.toBoolean(arg);
        } else if (arg != null && arg.getClass() != String.class && String.class == propertyType) {
            return arg.toString();
        } else if (arg instanceof String && !String.class.equals(propertyType)) {
            return convertWithString(arg);
        } else if (java.util.Calendar.class.isAssignableFrom(propertyType)) {
            return DfTypeUtil.toCalendar(arg);
        }
        return arg;
    }

    private Object convertPrimitiveWrapper(Object arg) {
        return DfTypeUtil.convertPrimitiveWrapper(propertyType, arg);
    }

    private Object convertNumber(Object arg) {
        return DfTypeUtil.toNumber(propertyType, arg);
    }

    private Object convertDate(Object arg) {
        if (propertyType == java.util.Date.class) {
            return DfTypeUtil.toDate(arg);
        } else if (propertyType == Timestamp.class) {
            return DfTypeUtil.toTimestamp(arg);
        } else if (propertyType == java.sql.Date.class) {
            return DfTypeUtil.toDate(arg);
        } else if (propertyType == Time.class) {
            return DfTypeUtil.toTime(arg);
        }
        return arg;
    }

    private Object convertWithString(Object arg) {
        if (stringConstructor != null) {
            return DfReflectionUtil.newInstance(stringConstructor, new Object[] { arg });
        }
        if (valueOfMethod != null) {
            return DfReflectionUtil.invoke(valueOfMethod, null, new Object[] { arg });
        }
        return arg;
    }
}
