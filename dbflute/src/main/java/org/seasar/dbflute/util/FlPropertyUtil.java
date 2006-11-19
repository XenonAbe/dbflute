package org.seasar.dbflute.util;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.seasar.dbflute.helper.mapstring.FlMapListString;
import org.seasar.dbflute.helper.mapstring.FlMapListStringImpl;

/**
 * Property Utility.
 * <p>
 * <pre>
 * # �v���p�e�B�I�u�W�F�N�g����l���擾����ۂ�Null�`�F�b�N��^�ϊ��Ȃǂ̔ς킵����
 * # �㗝����P���ȃv���p�e�B���[�e�B���e�B�B
 * </pre>
 * 
 * @author mkubo
 */
public class FlPropertyUtil {

    //========================================================================================
    //																				PropGetter
    //																				==========
    /**
     * �v���p�e�B���oString�^�p�Ŏ擾����B
     * 
     * @param prop �v���p�e�B�I�u�W�F�N�g (NotNull)
     * @param key �L�[�l (NotNull)
     * @return �v���p�e�B�oString�^�p (NotNull) (Trim)
     */
    public static String stringProp(Properties prop, String key) {
        if (prop == null) {
            String msg = "Argument[prop] must not be null: " + getLogStrKey(key);
            throw new NullPointerException(msg);
        }
        if (key == null) {
            throw new NullPointerException("Argument[key] must not be null.");
        }
        final String value = (String) prop.get(key);
        if (value == null) {
            String msg = "Not found property for the key: " + getLogStrKey(key);
            throw new PropertyNotFoundException(msg + " properties=" + prop);
        }
        if ("null".equalsIgnoreCase(value.trim())) {
            return "";
        } else {
            return value.trim();
        }
    }

    /**
     * �v���p�e�B���oboolean�^�p�Ŏ擾����B
     * 
     * @param prop �v���p�e�B�I�u�W�F�N�g (NotNull)
     * @param key �L�[�l (NotNull)
     * @return �v���p�e�B�oboolean�^�p
     */
    public static boolean booleanProp(Properties prop, String key) {
        String value = stringProp(prop, key);
        if ("true".equalsIgnoreCase(value)) {
            return true;
        } else if ("false".equalsIgnoreCase(value)) {
            return false;
        } else {
            String msg = "The property is not boolean: " + getLogStrKeyValue(key, value);
            throw new PropertyBooleanFormatException(msg);
        }
    }

    /**
     * �v���p�e�B���oint�^�p�Ŏ擾����B
     * 
     * @param prop �v���p�e�B�I�u�W�F�N�g (NotNull)
     * @param key �L�[�l (NotNull)
     * @return �v���p�e�B�oint�^�p
     */
    public static int intProp(Properties prop, String key) {
        String value = stringProp(prop, key);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            String msg = "NumberFormatException: " + getLogStrKeyValue(key, value);
            throw new PropertyIntegerFormatException(msg);
        }
    }

    /**
     * �v���p�e�B���oBigDecimal�^�p�Ŏ擾����B
     * 
     * @param prop �v���p�e�B�I�u�W�F�N�g (NotNull)
     * @param key �L�[�l (NotNull)
     * @return �v���p�e�B�oint�^�p
     */
    public static BigDecimal bigDecimalProp(Properties prop, String key) {
        String value = stringProp(prop, key);
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            String msg = "NumberFormatException: " + getLogStrKeyValue(key, value);
            throw new IllegalStateException(msg);
        }
    }

    public static List<Object> listProp(Properties prop, String key) {
        final FlMapListString mapListString = new FlMapListStringImpl();
        return mapListString.generateList(stringProp(prop, key));
    }

    public static List<Object> listProp(Properties prop, String key, String delimiter) {
        final FlMapListString mapListString = new FlMapListStringImpl();
        mapListString.setDelimiter(delimiter);
        return mapListString.generateList(stringProp(prop, key));
    }

    public static Map<String, Object> mapProp(Properties prop, String key) {
        final FlMapListString mapListString = new FlMapListStringImpl();
        return mapListString.generateMap(stringProp(prop, key));
    }

    public static Map<String, Object> mapProp(Properties prop, String key, String delimiter) {
        final FlMapListString mapListString = new FlMapListStringImpl();
        mapListString.setDelimiter(delimiter);
        return mapListString.generateMap(stringProp(prop, key));
    }

    public static String getLogStrKey(String key) {
        return "key==[" + key + "]";
    }

    public static String getLogStrKeyValue(String key, String value) {
        return "key==[" + key + "] value==[" + value + "]";
    }

    //========================================================================================
    //																				TagStrUtil
    //																				==========
    /**
     * �^�O���R���o�[�g���܂��D
     * <p>
     * 
     * <pre>
     *	��D
     *	    targetStr="Select[aa]And[bb]Fdefs"
     *	    key="aa" value="FZ"
     *	    key="bb" value="SUI-"
     *	    result="SelectFZAndSUI-Fdefs"
     *	<br>
     *	@param		targetStr		�Ώە�����
     *	@param		convertMap		�R���o�[�g�}�b�v
     *	@return	�Ώە�����
     *	<p>
     */
    public static String convertTag(String targetStr, Map convertMap) {
        Set keySet = convertMap.keySet();
        Collection valCol = convertMap.values();
        Iterator keyIte = keySet.iterator();
        Iterator valIte = valCol.iterator();
        Map<String, String> wk = new LinkedHashMap<String, String>(convertMap.size());
        while (keyIte.hasNext()) {
            String key = (String) keyIte.next();
            String val = (String) valIte.next();
            wk.put("[" + key + "]", val);
        }
        return convertAll(targetStr, wk);
    }

    /**
     * �^�O�������[�u���܂��D <br>
     * @param targetStr
     * @return �Ώە�����
     *         <p>
     */
    public static String removeTag(String targetStr) {
        return removeAll(targetStr, "[", "]");
    }

    /**
     * �^�O���܂܂�Ă��邩�ۂ����肵�܂��D <br>
     * @param targetStr
     * @return �^�O���܂܂�Ă��邩�ۂ�
     *         <p>
     */
    public static boolean containsTag(String targetStr) {
        int start = targetStr.indexOf("[");
        int end = targetStr.indexOf("]");
        if (start != -1 && end != -1 && start < end) {
            return true;
        } else {
            return false;
        }
    }

    //========================================================================================
    //																				StringUtil
    //																				==========
    /**
     * targetStr�Ɋ܂܂�Ă���S�Ă�oldStr��newStr�ɃR���o�[�g���܂��D <br>
     * @param targetStr
     * @param oldStr
     * @param newStr
     * @return �R���o�[�g���ꂽ������
     */
    public static String convertAll(String targetStr, String oldStr, String newStr) {
        if (targetStr == null) {
            throw new IllegalArgumentException("'targetStr' is null");
        }
        if (oldStr == null) {
            throw new IllegalArgumentException("'oldStr' is null");
        }
        if (newStr == null) {
            throw new IllegalArgumentException("'newStr' is null");
        }

        String result = "";
        int index = 0;
        StringBuffer sb = new StringBuffer(targetStr);
        while (true) {
            index = sb.toString().indexOf(oldStr);
            if (index == -1) {
                result = result + sb.toString();
                break;
            }
            sb.delete(index, index + oldStr.length());
            sb.insert(index, newStr);
            result = result + sb.substring(0, index + newStr.length());
            sb.delete(0, index + newStr.length());
        }

        return result;
    }

    /**
     * targetStr�Ɋ܂܂�Ă���S�Ă�key��value�ɃR���o�[�g���܂��D <br>
     * @param targetStr
     * @param convertMap
     * @return �R���o�[�g���ꂽ������
     *         <p>
     */
    public static String convertAll(String targetStr, Map convertMap) {
        if (targetStr == null) {
            throw new IllegalArgumentException("'targetStr' is null");
        }
        if (convertMap == null) {
            throw new IllegalArgumentException("'replaceMap' is null");
        }
        Set keySet = convertMap.keySet();
        Collection valCol = convertMap.values();
        Iterator keyIte = keySet.iterator();
        Iterator valIte = valCol.iterator();
        while (keyIte.hasNext()) {
            String oldStr = (String) keyIte.next();
            String newStr = (String) valIte.next();
            if (oldStr == null) {
                throw new IllegalArgumentException("'replaceMap' has null key!");
            }
            if (newStr == null) {
                newStr = "";
            }
            targetStr = convertAll(targetStr, oldStr, newStr);
        }
        return targetStr;
    }

    /**
     * targetStr�Ɋ܂܂�Ă���S�Ă�removeStr�������[�u���܂��D <br>
     * @param targetStr
     * @param removeStr
     * @return �����[�u���ꂽ������
     */
    public static String removeAll(String targetStr, String removeStr) {
        if (targetStr == null) {
            throw new IllegalArgumentException("'targetStr' is null");
        }
        if (removeStr == null) {
            throw new IllegalArgumentException("'start' is null");
        }
        return removeAll(targetStr, new String[] { removeStr });
    }

    /**
     * targetStr�Ɋ܂܂�Ă���S�Ă�removeStrs�������[�u���܂��D <br>
     * @param targetStr
     * @param removeStrs
     * @return �����[�u���ꂽ������
     */
    public static String removeAll(String targetStr, String[] removeStrs) {
        if (targetStr == null) {
            throw new IllegalArgumentException("'targetStr' is null");
        }
        if (removeStrs == null) {
            throw new IllegalArgumentException("'removeStrs' is null");
        }
        for (int i = 0; i < removeStrs.length; i++) {
            String removeStr = removeStrs[i];
            if (removeStrs == null) {
                throw new IllegalArgumentException("'removeStrs' has null: index==[" + i + "]");
            }
            targetStr = convertAll(targetStr, removeStr, "");
        }
        return targetStr;
    }

    /**
     * targetStr�Ɋ܂܂�Ă���S�Ă�start��end�Ɉ͂܂ꂽ������������[�u���܂��D <br>
     * @param targetStr
     * @param start
     * @param end
     * @return �����[�u���ꂽ������
     */
    public static String removeAll(String targetStr, String start, String end) {
        if (targetStr == null) {
            throw new IllegalArgumentException("'targetStr' is null");
        }
        if (start == null) {
            throw new IllegalArgumentException("'start' is null");
        }
        if (end == null) {
            throw new IllegalArgumentException("'end' is null");
        }
        int startIndex = 0;
        int endIndex = 0;
        StringBuffer sb = new StringBuffer(targetStr);
        while (true) {
            startIndex = sb.toString().indexOf(start);
            if (startIndex == -1) {
                break;
            }
            endIndex = sb.toString().indexOf(end, startIndex + 1);
            if (endIndex == -1) {
                break;
            }
            sb.delete(startIndex, endIndex + 1);
        }
        return sb.toString();
    }

    /**
     * ������Ɏw��̃f���~�^�����݂��鐔���擾���܂��D
     * <p>
     * ���݂��Ȃ��ꍇ�́AString#indexOf()�Ɠ����l��߂��܂��D <br>
     * @param str ������
     * @param delim �f���~�^
     * @return ��
     *         <p>
     */
    public static int countDelim(String str, String delim) {
        int result = 0;
        for (int i = 0;;) {
            if (str.indexOf(delim, i) != -1) {
                result++;
                i = str.indexOf(delim, i) + 1;
            } else {
                break;
            }
        }
        if (result == 0) {
            result = -1;
        }
        return result;
    }

    public static class PropertyNotFoundException extends RuntimeException {

        /** Serial version UID. (Default) */
        private static final long serialVersionUID = 1L;

        /**
         * Constructor.
         * 
         * @param msg Exception message.
         */
        public PropertyNotFoundException(String msg) {
            super(msg);
        }
    }

    public static class PropertyBooleanFormatException extends RuntimeException {

        /** Serial version UID. (Default) */
        private static final long serialVersionUID = 1L;

        /**
         * Constructor.
         * 
         * @param msg Exception message.
         */
        public PropertyBooleanFormatException(String msg) {
            super(msg);
        }
    }

    public static class PropertyIntegerFormatException extends RuntimeException {

        /** Serial version UID. (Default) */
        private static final long serialVersionUID = 1L;

        /**
         * Constructor.
         * 
         * @param msg Exception message.
         */
        public PropertyIntegerFormatException(String msg) {
            super(msg);
        }
    }
}