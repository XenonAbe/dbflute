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
package org.seasar.dbflute.jdbc;

import java.util.HashMap;
import java.util.Map;

import org.seasar.dbflute.exception.CharParameterShortSizeException;
import org.seasar.dbflute.util.DfStringUtil;

/**
 * @author jflute
 */
public class ParameterUtil {

    /**
     * @param value Query value. (Nullable)
     * @return Converted value. (Nullable)
     */
    public static String convertEmptyToNull(String value) {
        return ((value != null && !"".equals(value)) ? value : null);
    }

    /**
     * @param parameterName The name of the parameter. (NotNull)
     * @param value The value of the parameter. (Nullable)
     * @param size The size of parameter type. (Nullable)
     * @param mode The handling mode. (NotNull)
     * @return The filtered value. (Nullable)
     */
    public static String handleShortChar(String parameterName, String value, Integer size, ShortCharHandlingMode mode) {
        if (parameterName == null || parameterName.trim().length() == 0) {
            String msg = "The argument 'parameterName' should not be null or empty:";
            msg = msg + " value=" + value + " size=" + size + " mode=" + mode;
            throw new IllegalArgumentException(msg);
        }
        if (mode == null) {
            String msg = "The argument 'mode' should not be null:";
            msg = msg + " parameterName=" + parameterName + " value=" + value + " size=" + size;
            throw new IllegalArgumentException(msg);
        }
        if (value == null) {
            return null;
        }
        if (size == null) {
            return value;
        }
        if (value.length() >= size) {
            return value;
        }
        if (mode.equals(ShortCharHandlingMode.RFILL)) {
            return DfStringUtil.rfill(value, size);
        } else if (mode.equals(ShortCharHandlingMode.LFILL)) {
            return DfStringUtil.lfill(value, size);
        } else if (mode.equals(ShortCharHandlingMode.EXCEPTION)) {
            String msg = "The size of the parameter '" + parameterName + "' should be " + size + ":";
            msg = msg + " value=[" + value + "] size=" + value.length();
            throw new CharParameterShortSizeException(msg);
        } else {
            return value;
        }
    }

    public static enum ShortCharHandlingMode {
        RFILL("R"), LFILL("L"), EXCEPTION("E"), NONE("N");
        private static final Map<String, ShortCharHandlingMode> _codeValueMap = new HashMap<String, ShortCharHandlingMode>();
        static {
            for (ShortCharHandlingMode value : values()) {
                _codeValueMap.put(value.code().toLowerCase(), value);
            }
        }
        protected final String _code;

        private ShortCharHandlingMode(String code) {
            _code = code;
        }

        public static ShortCharHandlingMode codeOf(Object code) {
            if (code == null) {
                return null;
            }
            if (code instanceof ShortCharHandlingMode) {
                return (ShortCharHandlingMode) code;
            }
            return _codeValueMap.get(code.toString().toLowerCase());
        }

        public String code() {
            return _code;
        }
    }
}
