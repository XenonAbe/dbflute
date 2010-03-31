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
package org.seasar.dbflute.s2dao.metadata;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.seasar.dbflute.DBDef;
import org.seasar.dbflute.helper.beans.DfBeanDesc;
import org.seasar.dbflute.helper.beans.DfPropertyDesc;
import org.seasar.dbflute.helper.beans.factory.DfBeanDescFactory;
import org.seasar.dbflute.jdbc.ValueType;
import org.seasar.dbflute.resource.ResourceContext;
import org.seasar.dbflute.s2dao.metadata.TnProcedureParameterType.TnProcedureParameterAccessor;
import org.seasar.dbflute.util.DfReflectionUtil;
import org.seasar.dbflute.util.DfStringUtil;
import org.seasar.dbflute.util.DfTypeUtil;

/**
 * {Refers to Seasar and Extends its class}
 * @author jflute
 */
public class TnProcedureMetaDataFactory {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected TnFieldProcedureAnnotationReader _annotationReader = new TnFieldProcedureAnnotationReader();
    protected TnProcedureValueTypeProvider _valueTypeProvider = new TnProcedureValueTypeProvider();

    // ===================================================================================
    //                                                                                Main
    //                                                                                ====
    public TnProcedureMetaData createProcedureMetaData(final String procedureName, final Class<?> pmbType) {
        final TnProcedureMetaData procedureMetaData = new TnProcedureMetaData(procedureName);
        if (pmbType == null) {
            return procedureMetaData;
        } else {
            if (!isDtoType(pmbType)) {
                throw new IllegalStateException("The pmb type was Not DTO type: " + pmbType.getName());
            }
        }
        final DfBeanDesc pmbDesc = DfBeanDescFactory.getBeanDesc(pmbType);
        final List<String> proppertyNameList = pmbDesc.getProppertyNameList();
        for (String propertyName : proppertyNameList) {
            final DfPropertyDesc parameterDesc = pmbDesc.getPropertyDesc(propertyName);
            if (parameterDesc.isReadable() && parameterDesc.isWritable()) {
                registerParameterType(procedureMetaData, parameterDesc);
            }
        }
        procedureMetaData.fix();
        return procedureMetaData;
    }

    protected void registerParameterType(TnProcedureMetaData procedureMetaData, DfPropertyDesc parameterDesc) {
        final TnProcedureParameterType ppt = getProcedureParameterType(parameterDesc);
        if (ppt == null) {
            return;
        }
        procedureMetaData.addParameterType(ppt);
    }

    protected TnProcedureParameterType getProcedureParameterType(DfPropertyDesc parameterDesc) {
        final String specificationExp = _annotationReader.getParameterSpecification(parameterDesc);
        if (specificationExp == null) {
            return null;
        }
        final TnProcedureParameterSpec spec = parseParameterSpec(specificationExp, parameterDesc);
        final String type = spec.getParameterType();
        final TnProcedureParameterType ppt = createProcedureParameterType(parameterDesc);
        if (type.equalsIgnoreCase("in")) {
            ppt.setInType(true);
        } else if (type.equalsIgnoreCase("out")) {
            ppt.setOutType(true);
        } else if (type.equalsIgnoreCase("inout")) {
            ppt.setInType(true);
            ppt.setOutType(true);
        } else if (type.equalsIgnoreCase("return")) {
            ppt.setOutType(true);
            ppt.setReturnType(true);
        } else if (type.equalsIgnoreCase("notParamResult")) {
            ppt.setNotParamResultType(true);
        } else {
            String msg = "The parameter type should be 'in, out, inout, return, notParamResult':";
            msg = msg + " type=" + type;
            msg = msg + " class=" + DfTypeUtil.toClassTitle(parameterDesc.getBeanDesc().getBeanClass());
            msg = msg + " property=" + parameterDesc.getPropertyName();
            throw new IllegalStateException(msg);
        }
        ppt.setParameterOrder(spec.getParameterOrder());
        final ValueType valueType = findValueType(parameterDesc);
        ppt.setValueType(valueType);
        return ppt;
    }

    protected TnProcedureParameterType createProcedureParameterType(final DfPropertyDesc parameterDesc) {
        final Type genericReturnType = parameterDesc.getReadMethod().getGenericReturnType();
        final Class<?> elementType = DfReflectionUtil.getGenericType(genericReturnType);
        return new TnProcedureParameterType(new TnProcedureParameterAccessor() {
            public Object getValue(Object target) {
                return parameterDesc.getValue(target);
            }

            public void setValue(Object target, Object value) {
                parameterDesc.setValue(target, value);
            }
        }, parameterDesc.getPropertyName(), parameterDesc.getPropertyType(), elementType);
    }

    // ===================================================================================
    //                                                             Parameter Specification
    //                                                             =======================
    protected TnProcedureParameterSpec parseParameterSpec(String specExp, DfPropertyDesc parameterDesc) {
        final List<String> list = DfStringUtil.splitListTrimmed(specExp, ",");
        if (list.size() != 2) {
            String msg = "The size of parameterInfo elements was illegal:";
            msg = msg + " elements=" + list + " spec=" + specExp;
            msg = msg + " parameter=" + parameterDesc.getPropertyName();
            msg = msg + " pmb=" + DfTypeUtil.toClassTitle(parameterDesc.getBeanDesc().getBeanClass());
            throw new IllegalStateException(msg);
        }
        final TnProcedureParameterSpec spec = new TnProcedureParameterSpec();
        spec.setParameterType(list.get(0));
        final String order = list.get(1);
        try {
            spec.setParameterOrder(DfTypeUtil.toInteger(list.get(1)));
        } catch (NumberFormatException e) {
            String msg = "Failed to parse the parameter index as Integer:";
            msg = msg + " order=" + order + " spec=" + specExp;
            msg = msg + " parameter=" + parameterDesc.getPropertyName();
            msg = msg + " pmb=" + DfTypeUtil.toClassTitle(parameterDesc.getBeanDesc().getBeanClass());

            throw new IllegalStateException(msg);
        }
        return spec;
    }

    protected static class TnProcedureParameterSpec {
        protected String _parameterType;
        protected Integer _parameterOrder;

        public String getParameterType() {
            return _parameterType;
        }

        public void setParameterType(String parameterType) {
            this._parameterType = parameterType;
        }

        public Integer getParameterOrder() {
            return _parameterOrder;
        }

        public void setParameterOrder(Integer parameterOrder) {
            this._parameterOrder = parameterOrder;
        }
    }

    // ===================================================================================
    //                                                                          Value Type
    //                                                                          ==========
    protected ValueType findValueType(DfPropertyDesc parameterDesc) {
        final String name = _annotationReader.getValueType(parameterDesc);
        final Class<?> type = parameterDesc.getPropertyType();
        final DBDef currentDBDef = ResourceContext.currentDBDef();
        return _valueTypeProvider.provideValueType(name, type, currentDBDef);
    }

    // ===================================================================================
    //                                                                Determination Helper
    //                                                                ====================
    protected boolean isCurrentDBDef(DBDef currentDBDef) {
        return ResourceContext.isCurrentDBDef(currentDBDef);
    }

    protected boolean isInstanceField(Field field) {
        final int mod = field.getModifiers();
        return !Modifier.isStatic(mod) && !Modifier.isFinal(mod);
    }

    protected boolean isDtoType(Class<?> clazz) {
        return !isSimpleType(clazz) && !isContainerType(clazz);
    }

    protected boolean isSimpleType(Class<?> clazz) {
        if (clazz == null) {
            throw new NullPointerException("clazz");
        }
        return clazz == String.class || clazz.isPrimitive() || clazz == Boolean.class || clazz == Character.class
                || Number.class.isAssignableFrom(clazz) || Date.class.isAssignableFrom(clazz)
                || Calendar.class.isAssignableFrom(clazz) || clazz == byte[].class;
    }

    protected boolean isContainerType(Class<?> clazz) {
        if (clazz == null) {
            throw new NullPointerException("clazz");
        }
        return Collection.class.isAssignableFrom(clazz) || Map.class.isAssignableFrom(clazz) || clazz.isArray();
    }

    // ===================================================================================
    //                                                                   Annotation Reader
    //                                                                   =================
    protected static class TnFieldProcedureAnnotationReader {
        protected static final String PARAMETER_SUFFIX = "_PROCEDURE_PARAMETER";
        protected static final String VALUE_TYPE_SUFFIX = "_VALUE_TYPE";

        public String getParameterSpecification(DfPropertyDesc propertyDesc) {
            final String propertyName = propertyDesc.getPropertyName();
            final String annotationName = propertyName + PARAMETER_SUFFIX;
            final DfBeanDesc pmbDesc = propertyDesc.getBeanDesc();
            if (pmbDesc.hasField(annotationName)) {
                final Field f = pmbDesc.getField(annotationName);
                return (String) getValue(f, null);
            } else {
                return null;
            }
        }

        public String getValueType(DfPropertyDesc propertyDesc) {
            final String propertyName = propertyDesc.getPropertyName();
            final String annotationName = propertyName + VALUE_TYPE_SUFFIX;
            final DfBeanDesc pmbDesc = propertyDesc.getBeanDesc();
            if (pmbDesc.hasField(annotationName)) {
                final Field f = pmbDesc.getField(annotationName);
                return (String) getValue(f, null);
            } else {
                return null;
            }
        }

        protected Object getValue(Field field, Object target) {
            try {
                return field.get(target);
            } catch (IllegalAccessException e) {
                String msg = "The getting of the field threw the exception:";
                msg = msg + " class=" + DfTypeUtil.toClassTitle(field.getDeclaringClass());
                msg = msg + " field=" + field.getName();
                throw new IllegalStateException(msg, e);
            }
        }
    }
}