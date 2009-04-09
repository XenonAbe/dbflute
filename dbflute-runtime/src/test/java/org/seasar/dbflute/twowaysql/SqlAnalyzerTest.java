package org.seasar.dbflute.twowaysql;

import org.seasar.dbflute.twowaysql.context.CommandContext;
import org.seasar.dbflute.twowaysql.context.CommandContextCreator;
import org.seasar.dbflute.twowaysql.node.Node;
import org.seasar.dbflute.unit.PlainTestCase;

/**
 * @author jflute
 * @since 0.9.5 (2009/04/08)
 */
public class SqlAnalyzerTest extends PlainTestCase {

    // ===================================================================================
    //                                                                          IF comment
    //                                                                          ==========
    public void test_parse_IF_true() {
        // ## Arrange ##
        String sql = "/*IF pmb.memberName != null*/and member.MEMBER_NAME = 'TEST'/*END*/";
        SqlAnalyzer analyzer = new SqlAnalyzer(sql, false);

        // ## Act ##
        Node rootNode = analyzer.parse();

        // ## Assert ##
        SimpleMemberPmb pmb = new SimpleMemberPmb();
        pmb.setMemberName("foo");
        CommandContext ctx = createCtx(pmb);
        rootNode.accept(ctx);
        log("ctx:" + ctx);
        assertEquals("and member.MEMBER_NAME = 'TEST'", ctx.getSql());
    }

    public void test_parse_IF_false() {
        // ## Arrange ##
        String sql = "/*IF pmb.memberName != null*/and member.MEMBER_NAME = 'TEST'/*END*/";
        SqlAnalyzer analyzer = new SqlAnalyzer(sql, false);

        // ## Act ##
        Node rootNode = analyzer.parse();

        // ## Assert ##
        SimpleMemberPmb pmb = new SimpleMemberPmb();
        pmb.setMemberName(null);
        CommandContext ctx = createCtx(pmb);
        rootNode.accept(ctx);
        log("ctx:" + ctx);
        assertEquals("", ctx.getSql().trim());
    }

    // ===================================================================================
    //                                                                       BEGIN comment
    //                                                                       =============
    // -----------------------------------------------------
    //                                                 Basic
    //                                                 -----
    public void test_parse_BEGIN_for_where_all_true() {
        // ## Arrange ##
        String sql = "/*BEGIN*/where";
        sql = sql + " /*IF pmb.memberId != null*/member.MEMBER_ID = 3/*END*/";
        sql = sql + " /*IF pmb.memberName != null*/and member.MEMBER_NAME = 'TEST'/*END*/";
        sql = sql + "/*END*/";
        SqlAnalyzer analyzer = new SqlAnalyzer(sql, false);

        // ## Act ##
        Node rootNode = analyzer.parse();

        // ## Assert ##
        SimpleMemberPmb pmb = new SimpleMemberPmb();
        pmb.setMemberId(3);
        pmb.setMemberName("foo");
        CommandContext ctx = createCtx(pmb);
        rootNode.accept(ctx);
        log("ctx:" + ctx);
        String expected = "where member.MEMBER_ID = 3 and member.MEMBER_NAME = 'TEST'";
        assertEquals(expected, ctx.getSql());
    }

    public void test_parse_BEGIN_for_where_all_false() {
        // ## Arrange ##
        String sql = "/*BEGIN*/where";
        sql = sql + " /*IF pmb.memberId != null*/member.MEMBER_ID = 3/*END*/";
        sql = sql + " /*IF pmb.memberName != null*/and member.MEMBER_NAME = 'TEST'/*END*/";
        sql = sql + "/*END*/";
        SqlAnalyzer analyzer = new SqlAnalyzer(sql, false);

        // ## Act ##
        Node rootNode = analyzer.parse();

        // ## Assert ##
        SimpleMemberPmb pmb = new SimpleMemberPmb();
        CommandContext ctx = createCtx(pmb);
        rootNode.accept(ctx);
        log("ctx:" + ctx);
        String expected = "";
        assertEquals(expected, ctx.getSql());
    }

    public void test_parse_BEGIN_for_select_all_true() {
        // ## Arrange ##
        String sql = "select /*BEGIN*/";
        sql = sql + "/*IF pmb.memberId != null*/member.MEMBER_ID as c1/*END*/";
        sql = sql + "/*IF pmb.memberName != null*/, member.MEMBER_NAME as c2/*END*/";
        sql = sql + "/*END*/";
        SqlAnalyzer analyzer = new SqlAnalyzer(sql, false);

        // ## Act ##
        Node rootNode = analyzer.parse();

        // ## Assert ##
        SimpleMemberPmb pmb = new SimpleMemberPmb();
        pmb.setMemberId(3);
        pmb.setMemberName("foo");
        CommandContext ctx = createCtx(pmb);
        rootNode.accept(ctx);
        log("ctx:" + ctx);
        String expected = "select member.MEMBER_ID as c1, member.MEMBER_NAME as c2";
        assertEquals(expected, ctx.getSql());
    }

    public void test_parse_BEGIN_for_select_all_false() {
        // ## Arrange ##
        String sql = "select /*BEGIN*/";
        sql = sql + "/*IF pmb.memberId != null*/member.MEMBER_ID as c1/*END*/";
        sql = sql + "/*IF pmb.memberName != null*/, member.MEMBER_NAME as c2/*END*/";
        sql = sql + "/*END*/";
        SqlAnalyzer analyzer = new SqlAnalyzer(sql, false);

        // ## Act ##
        Node rootNode = analyzer.parse();

        // ## Assert ##
        SimpleMemberPmb pmb = new SimpleMemberPmb();
        CommandContext ctx = createCtx(pmb);
        rootNode.accept(ctx);
        log("ctx:" + ctx);
        String expected = "select ";
        assertEquals(expected, ctx.getSql());
    }

    // -----------------------------------------------------
    //                                                Nested
    //                                                ------
    public void test_parse_BEGIN_that_has_nested_IFIF_root_has_and() {
        // ## Arrange ##
        String sql = "/*BEGIN*/where";
        sql = sql + " ";
        sql = sql + "/*IF pmb.memberId != null*/";
        sql = sql + "FIXED";
        sql = sql + "/*END*/";
        sql = sql + " ";
        sql = sql + "/*IF pmb.memberName != null*/";
        sql = sql + "and AAA /*IF true*/and BBB /*IF true*/and CCC/*END*//*END*/ /*IF true*/and DDD/*END*/";
        sql = sql + "/*END*/";
        sql = sql + "/*END*/";
        SqlAnalyzer analyzer = new SqlAnalyzer(sql, false);

        // ## Act ##
        Node rootNode = analyzer.parse();

        // ## Assert ##
        SimpleMemberPmb pmb = new SimpleMemberPmb();
        pmb.setMemberName("foo");
        CommandContext ctx = createCtx(pmb);
        rootNode.accept(ctx);
        log("ctx:" + ctx);
        String expected = "where  AAA and BBB and CCC and DDD";
        assertEquals(expected, ctx.getSql());
    }

    public void test_parse_BEGIN_that_has_nested_IFIF_root_has_no_and() {
        // ## Arrange ##
        String sql = "/*BEGIN*/where";
        sql = sql + " ";
        sql = sql + "/*IF pmb.memberName != null*/";
        sql = sql + "AAA /*IF true*/and BBB /*IF true*/and CCC/*END*//*END*/ /*IF true*/and DDD/*END*/";
        sql = sql + "/*END*/";
        sql = sql + "/*END*/";
        SqlAnalyzer analyzer = new SqlAnalyzer(sql, false);

        // ## Act ##
        Node rootNode = analyzer.parse();

        // ## Assert ##
        SimpleMemberPmb pmb = new SimpleMemberPmb();
        pmb.setMemberName("foo");
        CommandContext ctx = createCtx(pmb);
        rootNode.accept(ctx);
        log("ctx:" + ctx);
        String expected = "where AAA and BBB and CCC and DDD";
        assertEquals(expected, ctx.getSql());
    }

    public void test_parse_BEGIN_that_has_nested_IFIF_root_has_both() {
        // ## Arrange ##
        String sql = "/*BEGIN*/where";
        sql = sql + " ";
        sql = sql + "/*IF pmb.memberId != null*/";
        sql = sql + "FIXED";
        sql = sql + "/*END*/";
        sql = sql + " ";
        sql = sql + "/*IF pmb.memberName != null*/";
        sql = sql + "and AAA /*IF true*/and BBB /*IF true*/and CCC/*END*//*END*/ /*IF true*/and DDD/*END*/";
        sql = sql + "/*END*/";
        sql = sql + "/*END*/";
        sql = sql + " ";
        sql = sql + "/*BEGIN*/where";
        sql = sql + " ";
        sql = sql + "/*IF pmb.memberId != null*/";
        sql = sql + "FIXED";
        sql = sql + "/*END*/";
        sql = sql + " ";
        sql = sql + "/*IF pmb.memberName != null*/";
        sql = sql + "and AAA /*IF true*/and BBB /*IF true*/and CCC/*END*//*END*/ /*IF true*/and DDD/*END*/";
        sql = sql + "/*END*/";
        sql = sql + "/*END*/";
        SqlAnalyzer analyzer = new SqlAnalyzer(sql, false);

        // ## Act ##
        Node rootNode = analyzer.parse();

        // ## Assert ##
        SimpleMemberPmb pmb = new SimpleMemberPmb();
        pmb.setMemberName("foo");
        CommandContext ctx = createCtx(pmb);
        rootNode.accept(ctx);
        log("ctx:" + ctx);
        String expected = "where  AAA and BBB and CCC and DDD where  AAA and BBB and CCC and DDD";
        assertEquals(expected, ctx.getSql());
    }

    public void test_parse_BEGIN_that_has_nested_IFIF_fixed_condition_() {
        // ## Arrange ##
        String sql = "/*BEGIN*/where";
        sql = sql + " 1 = 1";
        sql = sql + "/*IF pmb.memberId != null*/";
        sql = sql + "and FIXED";
        sql = sql + "/*END*/";
        sql = sql + " ";
        sql = sql + "/*IF pmb.memberName != null*/";
        sql = sql + "and AAA /*IF true*/and BBB /*IF true*/and CCC/*END*//*END*/ /*IF true*/and DDD/*END*/";
        sql = sql + "/*END*/";
        sql = sql + "/*END*/";
        SqlAnalyzer analyzer = new SqlAnalyzer(sql, false);

        // ## Act ##
        Node rootNode = analyzer.parse();

        // ## Assert ##
        SimpleMemberPmb pmb = new SimpleMemberPmb();
        pmb.setMemberName("foo");
        CommandContext ctx = createCtx(pmb);
        rootNode.accept(ctx);
        log("ctx:" + ctx);
        String expected = "where 1 = 1 AAA and BBB and CCC and DDD";
        assertEquals(expected, ctx.getSql());
    }

    public void test_parse_BEGIN_that_has_nested_IFIF_all_false() {
        // ## Arrange ##
        String sql = "/*BEGIN*/where";
        sql = sql + " ";
        sql = sql + "/*IF pmb.memberId != null*/";
        sql = sql + "AAA /*IF false*/and BBB /*IF false*/and CCC/*END*//*END*/ /*IF false*/and DDD/*END*/";
        sql = sql + "/*END*/";
        sql = sql + "/*END*/";
        SqlAnalyzer analyzer = new SqlAnalyzer(sql, false);

        // ## Act ##
        Node rootNode = analyzer.parse();

        // ## Assert ##
        SimpleMemberPmb pmb = new SimpleMemberPmb();
        CommandContext ctx = createCtx(pmb);
        rootNode.accept(ctx);
        log("ctx:" + ctx);
        assertEquals("", ctx.getSql());
    }

    public void test_parse_BEGIN_that_has_nested_IFIF_formal_use_but_basically_nonsense() {
        // ## Arrange ##
        String sql = "/*BEGIN*/where";
        sql = sql + " ";
        sql = sql + "/*IF pmb.memberId != null*//*IF true*/and AAA/*END*//*END*/";
        sql = sql + " ";
        sql = sql + "/*IF pmb.memberName != null*/and BBB/*END*/";
        sql = sql + "/*END*/";
        SqlAnalyzer analyzer = new SqlAnalyzer(sql, false);

        // ## Act ##
        Node rootNode = analyzer.parse();

        // ## Assert ##
        SimpleMemberPmb pmb = new SimpleMemberPmb();
        pmb.setMemberId(3);
        pmb.setMemberName("foo");
        CommandContext ctx = createCtx(pmb);
        rootNode.accept(ctx);
        log("ctx:" + ctx);
        assertEquals("where AAA and BBB", ctx.getSql());
    }

    // -----------------------------------------------------
    //                                       Where UpperCase
    //                                       ---------------
    public void test_parse_BEGIN_where_upperCase_that_has_nested_IFIF_root_has_and() {
        // ## Arrange ##
        String sql = "/*BEGIN*/WHERE";
        sql = sql + " ";
        sql = sql + "/*IF pmb.memberId != null*/";
        sql = sql + "FIXED";
        sql = sql + "/*END*/";
        sql = sql + " ";
        sql = sql + "/*IF pmb.memberName != null*/";
        sql = sql + "and AAA /*IF true*/and BBB /*IF true*/and CCC/*END*//*END*/ /*IF true*/and DDD/*END*/";
        sql = sql + "/*END*/";
        sql = sql + "/*END*/";
        SqlAnalyzer analyzer = new SqlAnalyzer(sql, false);

        // ## Act ##
        Node rootNode = analyzer.parse();

        // ## Assert ##
        SimpleMemberPmb pmb = new SimpleMemberPmb();
        pmb.setMemberName("foo");
        CommandContext ctx = createCtx(pmb);
        rootNode.accept(ctx);
        log("ctx:" + ctx);
        String expected = "WHERE  AAA and BBB and CCC and DDD";
        assertEquals(expected, ctx.getSql());
    }

    // ===================================================================================
    //                                                                         Test Helper
    //                                                                         ===========
    private static class SimpleMemberPmb {
        protected Integer memberId;
        protected String memberName;

        public Integer getMemberId() {
            return memberId;
        }

        public void setMemberId(Integer memberId) {
            this.memberId = memberId;
        }

        public String getMemberName() {
            return memberName;
        }

        public void setMemberName(String memberName) {
            this.memberName = memberName;
        }
    }

    private CommandContext createCtx(SimpleMemberPmb pmb) {
        return xcreateCommandContext(new Object[] { pmb }, new String[] { "pmb" }, new Class<?>[] { pmb.getClass() });
    }

    private CommandContext xcreateCommandContext(Object[] args, String[] argNames, Class<?>[] argTypes) {
        return xcreateCommandContextCreator(argNames, argTypes).createCommandContext(args);
    }

    private CommandContextCreator xcreateCommandContextCreator(String[] argNames, Class<?>[] argTypes) {
        return new CommandContextCreator(argNames, argTypes);
    }
}
