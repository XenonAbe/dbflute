package org.seasar.dbflute.cbean.sqlclause.subquery;

import org.seasar.dbflute.cbean.sqlclause.SqlClause;
import org.seasar.dbflute.dbmeta.DBMeta;
import org.seasar.dbflute.dbmeta.name.ColumnRealName;
import org.seasar.dbflute.dbmeta.name.ColumnRealNameProvider;
import org.seasar.dbflute.dbmeta.name.ColumnSqlName;
import org.seasar.dbflute.dbmeta.name.ColumnSqlNameProvider;

/**
 * @author jflute
 * @since 0.9.7.2 (2010/06/20 Sunday)
 */
public class QueryDerivedReferrer extends DerivedReferrer {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String _operand;
    protected final Object _value;
    protected final String _parameterPath;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public QueryDerivedReferrer(SqlClause sqlClause, SubQueryPath subQueryPath,
            ColumnRealNameProvider localRealNameProvider, ColumnSqlNameProvider subQuerySqlNameProvider,
            int subQueryLevel, SqlClause subQueryClause, SubQueryLevelReflector reflector, String subQueryIdentity,
            DBMeta subQueryDBMeta, String mainSubQueryIdentity, String operand, Object value, String parameterPath) {
        super(sqlClause, subQueryPath, localRealNameProvider, subQuerySqlNameProvider, subQueryLevel, subQueryClause,
                reflector, subQueryIdentity, subQueryDBMeta, mainSubQueryIdentity);
        _operand = operand;
        _value = value;
        _parameterPath = parameterPath;
    }

    // ===================================================================================
    //                                                                        Build Clause
    //                                                                        ============
    @Override
    protected String doBuildDerivedReferrer(String function, ColumnRealName columnRealName,
            ColumnSqlName relatedColumnSqlName, String subQueryClause, String beginMark, String endMark,
            String endIndent) {
        final String parameter = "/*pmb." + _parameterPath + "*/null";
        return "(" + beginMark + subQueryClause + ln() + endIndent + ") " + _operand + " " + parameter + " " + endMark;
    }

    @Override
    protected void throwDerivedReferrerInvalidColumnSpecificationException(String function) {
        createCBExThrower().throwQueryDerivedReferrerInvalidColumnSpecificationException(function);
    }

    @Override
    protected void doAssertDerivedReferrerColumnType(String function, String derivedColumnDbName,
            Class<?> derivedColumnType) {
        final Object value = _value;
        if ("sum".equalsIgnoreCase(function) || "avg".equalsIgnoreCase(function)) {
            if (!Number.class.isAssignableFrom(derivedColumnType)) {
                throwQueryDerivedReferrerUnmatchedColumnTypeException(function, derivedColumnDbName, derivedColumnType);
            }
        }
        if (value != null) {
            final Class<?> parameterType = value.getClass();
            if (String.class.isAssignableFrom(derivedColumnType)) {
                if (!String.class.isAssignableFrom(parameterType)) {
                    throwQueryDerivedReferrerUnmatchedColumnTypeException(function, derivedColumnDbName,
                            derivedColumnType);
                }
            }
            if (Number.class.isAssignableFrom(derivedColumnType)) {
                if (!Number.class.isAssignableFrom(parameterType)) {
                    throwQueryDerivedReferrerUnmatchedColumnTypeException(function, derivedColumnDbName,
                            derivedColumnType);
                }
            }
            if (java.util.Date.class.isAssignableFrom(derivedColumnType)) {
                if (!java.util.Date.class.isAssignableFrom(parameterType)) {
                    throwQueryDerivedReferrerUnmatchedColumnTypeException(function, derivedColumnDbName,
                            derivedColumnType);
                }
            }
        }
    }

    protected void throwQueryDerivedReferrerUnmatchedColumnTypeException(String function, String derivedColumnDbName,
            Class<?> derivedColumnType) {
        createCBExThrower().throwQueryDerivedReferrerUnmatchedColumnTypeException(function, derivedColumnDbName,
                derivedColumnType, _value);
    }
}
