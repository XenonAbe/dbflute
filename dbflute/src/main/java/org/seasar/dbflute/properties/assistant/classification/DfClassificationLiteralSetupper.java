package org.seasar.dbflute.properties.assistant.classification;

import java.util.List;
import java.util.Map;

/**
 * @author jflute
 * @since 0.9.5.1 (2009/07/03 Friday)
 */
public class DfClassificationLiteralSetupper {

    @SuppressWarnings("unchecked")
    public void setup(String classificationName, Map elementMap, List<Map<String, String>> elementList) {
        final String codeKey = DfClassificationInfo.KEY_CODE;
        final String nameKey = DfClassificationInfo.KEY_NAME;
        final String aliasKey = DfClassificationInfo.KEY_ALIAS;

        final String code = (String) elementMap.get(codeKey);
        if (code == null) {
            String msg = "The code of " + classificationName + " should not be null";
            throw new IllegalStateException(msg);
        }
        final String name = (String) elementMap.get(nameKey);
        if (name == null) {
            elementMap.put(nameKey, code);
        }
        final String alias = (String) elementMap.get(aliasKey);
        if (alias == null) {
            elementMap.put(aliasKey, name != null ? name : code);
        }
        elementList.add(elementMap);
    }
}
