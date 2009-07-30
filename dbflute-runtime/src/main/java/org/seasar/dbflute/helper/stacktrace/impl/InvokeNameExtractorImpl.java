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
package org.seasar.dbflute.helper.stacktrace.impl;

import java.util.ArrayList;
import java.util.List;

import org.seasar.dbflute.helper.stacktrace.InvokeNameExtractingResource;
import org.seasar.dbflute.helper.stacktrace.InvokeNameExtractor;
import org.seasar.dbflute.helper.stacktrace.InvokeNameResult;

/**
 * @author jflute
 */
public class InvokeNameExtractorImpl implements InvokeNameExtractor {

    // ==========================================================================================
    //                                                                                  Attribute
    //                                                                                  =========
    protected StackTraceElement[] _stackTrace;

    // ==========================================================================================
    //                                                                                       Main
    //                                                                                       ====
    /**
     * @param resource the call-back resource for invoke-name-extracting. (NotNull)
     * @return Invoke name. (NotNull: If not found, returns empty string.)
     */
    public List<InvokeNameResult> extractInvokeName(InvokeNameExtractingResource resource) {
        if (_stackTrace == null) {
            String msg = "The attribute 'stackTrace' should not be null: resource=" + resource;
            throw new IllegalStateException(msg);
        }
        final List<InvokeNameResult> resultList = new ArrayList<InvokeNameResult>();
        String simpleClassName = null;
        String methodName = null;
        int lineNumber = 0;
        int foundIndex = -1; // The minus one means 'Not Found'.
        int foundFirstIndex = -1; // The minus one means 'Not Found'.
        boolean onTarget = false;
        boolean existsDuplicate = false;
        for (int i = resource.getStartIndex(); i < _stackTrace.length; i++) {
            final StackTraceElement element = _stackTrace[i];
            if (i > resource.getStartIndex() + resource.getLoopSize()) {
                break;
            }
            final String currentClassName = element.getClassName();
            if (currentClassName.startsWith("sun.") || currentClassName.startsWith("java.")) {
                if (onTarget) {
                    break;
                }
                continue;
            }
            final String currentMethodName = element.getMethodName();
            if (resource.isTargetElement(currentClassName, currentMethodName)) {
                if (currentMethodName.equals("invoke")) {
                    continue;
                }
                simpleClassName = currentClassName.substring(currentClassName.lastIndexOf(".") + 1);
                methodName = currentMethodName;
                if (resource.isUseAdditionalInfo()) {
                    lineNumber = element.getLineNumber();
                }
                foundIndex = i;
                if (foundFirstIndex == -1) {
                    foundFirstIndex = i;
                }
                onTarget = true;
                if (resultList.isEmpty()) { // first element
                    resultList.add(createResult(resource, simpleClassName, methodName, lineNumber, foundIndex,
                            foundFirstIndex));
                } else {
                    existsDuplicate = true;
                }
                continue;
            }
            if (onTarget) {
                break;
            }
        }
        if (simpleClassName == null) {
            return new ArrayList<InvokeNameResult>();
        }
        if (existsDuplicate) {
            resultList
                    .add(createResult(resource, simpleClassName, methodName, lineNumber, foundIndex, foundFirstIndex));
        }
        return resultList;
    }

    protected InvokeNameResult createResult(InvokeNameExtractingResource resource, String simpleClassName,
            String methodName, int lineNumber, int foundIndex, int foundFirstIndex) {
        final InvokeNameResult result = new InvokeNameResult();
        final String filteredSimpleClassName = resource.filterSimpleClassName(simpleClassName);
        result.setSimpleClassName(filteredSimpleClassName);
        result.setMethodName(methodName);
        final String invokeName;
        if (lineNumber > 0) {
            invokeName = filteredSimpleClassName + "." + methodName + "():" + lineNumber + " -> ";
        } else {
            invokeName = filteredSimpleClassName + "." + methodName + "() -> ";
        }
        result.setInvokeName(invokeName);
        result.setFoundIndex(foundIndex);
        result.setFoundFirstIndex(foundFirstIndex);
        return result;
    }

    // ==========================================================================================
    //                                                                                   Accessor
    //                                                                                   ========
    public void setStackTrace(StackTraceElement[] stackTrace) {
        _stackTrace = stackTrace;
    }
}