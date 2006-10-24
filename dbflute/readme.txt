#
# **************
# DBFlute-readme
# **************
# Written by jflute (Last updated at 2006/09/08 Tue.)
#
# �����̃e�L�X�g��4�^�u�ŎQ�Ƃ��ĉ������B
# �����[���[�̍ő��120�ȏ�ɐݒ肵�ĉ������B

# /============================================================================
#                                                               Support Version
#                                                               ===============
Java
	JDK-1.4.X or JDK-5.0
	S2Dao = 1.0.36 (35-OK)

CSharp
	.Net-2.0
	S2Dao = 1.0.0



# /============================================================================
#                                                             Setup Environment
#                                                             =================

# -------------------------------------------
#                                  Java & Ant
#                                  ----------
1. Setup JRE

    Sun��Page���JRE-5.0(�ȏ�)��Download���AInstall���ĉ������B�o�K�{�p


2. Setup Ant

    Ant-1.5(�ȏ�)���ȉ���Site���Download���AInstall���ĉ������B�o�K�{�p

        Apache Ant

    Download���ĉ𓀂��A���ϐ��Ƃ���{ANT_HOME}��ǉ����܂��B
    ����ɁA���ϐ�{Path}��%ANT_HOME%\bin��ǉ����܂��B


# -------------------------------------------
#                        Environment-Variable
#                        --------------------
3. Setup environment-variable

    ���ϐ��Ƃ���{DBFLUTE_HOME}��ǉ����܂��B�o�K�{�p

        ex) DBFLUTE_HOME = C:\java\dbflute-1.0


# -------------------------------------------
#                            Client-Directory
#                            ----------------
4. Locate client-directory(containing build-properties and batch-files)

    4-1. %DBFLUTE_HOME%/etc/client_directory-template��Directory�ɁA���sDirectory(Client-Directory)��
         Template������܂��B�C�ӂ�Template��Copy���ALocalPC�̔C�ӂ̏ꏊ�ɔz�u���ĉ������B

        ����ʂ�build-properties��batch-files�͊eProject��Version�Ǘ��Ɋ܂߂����̂ŁA������l�����Ĕz�u���܂��B
        ��Template�́A�ȉ��̂悤�ɂȂ��Ă��܂��B

            - ldb_fullProperties:
                �S�Ă�Property���L�q����Ă��܂��B
                DBFlute���g������Ă��āA�ŏ�����FULL�@�\����������l���邽�߂�
                ����Property�𐸍����Ă����ꍇ�́A����������߂��܂��B

            - ldb_minimumProperties:
                �Œ����Property���L�q����Ă��܂��B
                �Ƃ肠�����������������Ă݂Ă��̌㏙�X�ɋ@�\��ǉ����Ă����ꍇ�́A
                ����������߂��܂��B�oDBFlute���S�҂̕��͂���p
                (��������Ă���Ant-Task��full�Ɠ��l�ł�)

            - ldb_schemaHTMLOnly:
                HtmlDocument-Task�����s���邽�߂̍Œ����Property�������L�q����Ă��܂��B
                DBFlute���S�҂łƂ肠����SchemaHTML�������p���Ă݂����Ƃ������A
                �܂��A����O/R-Mapper���g���Ă��邪SchemaHTML�����𗘗p���Ă݂����Ƃ������͂���𗘗p���ĉ������B
                (DB�̎�ނ�DB�̐ڑ���Ȃǂ�ݒ肷�邾���ł����ɗ��p�\)

    4-2. _project.bat�́uMY_PROJECT_NAME�v�ϐ��̒l��C�ӂ�Project���ɕύX���ĉ������B

    4-3. build-ldb.properties��File����build-[xxx].properties(xxx�͔C�ӂ�Project��)�ɕύX���ĉ������B

    ��schemaHTMLOnly�𗘗p����ꍇ�́A����Project���ɂ������K�v�͂���܂���B(���̂܂܂ł�OK)


# -------------------------------------------
#                     Properties Modification
#                     -----------------------
5. Modify build-properties

    �eProperty�̏ڍׂ�%DBFLUTE_HOME%/etc/client_directory-template/ldb_fullProperties��
    build-ldb.properties���ɋL�q����Ă���eProperty��Comment���Q�l�ɂ��ĉ������B
    (NotRequired��Property��Default�l�Ŏ������Ȃ�Ώ����Ă��\���܂���)

    _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
    j2ee.dicon�̐ݒ�ɂāAComponent��`���ȉ��̂悤�ɍ����ւ��Ă��������B
    ����ɂ��APaging��������̋@�\���L���ɂȂ�܂��B
    (CSharp�ł�'FetchNarrowingResultSetFactory'�̂�)

    component class="org.seasar.extension.jdbc.impl.BasicStatementFactory"
    component class="org.seasar.extension.jdbc.impl.BasicResultSetFactory"

        ������

    component class="xxx.allcommon.s2dao.S2DaoStatementFactory"
    component class="xxx.allcommon.s2dao.FetchNarrowingResultSetFactory"

      �����ӁFj2ee.dicon�͎��������ΏۊO�ł��B(dao.dicon�͎�������)
    _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/


# -------------------------------------------
#                              Task Executing
#                              --------------
6. Execute task of JDBC and Generate

    6-1. jdbc.bat�����s���܂��B./schema�ȉ���project-schema-xxx.xml���쐬����Ă����琬���ł��B
            ������DB��Schema���XML�`���ŋL�q����Ă��܂��B

    6-2. generate.bat�����s���܂��BProperty�ɂĎw�肵���o��Directory��Source���쐬����Ă����琬���ł��B

    �����s����Java��Version�ɒ��ӂ��Ă��������B
      �Ⴆ�΁ADBFlute���gJava-5.0�h��Compile����Ă��āA���s��JDK-1.4�̏ꍇ��Exception�ɂȂ�܂��B
      Install���Ă���Java��Version��JAVA_HOME�Ɏw�肵�Ă���Directory�Ȃǂ����m�F�������B


# -------------------------------------------
#                                Confirmation
#                                ------------
7. Confirm the behavior

    7-1. �m�F�̂��߂ɐ������ꂽSource��Compile���Ă݂ĉ������B

    7-2. �m�F�̂��߂�Compile���ꂽDao��Select���Ă݂ĉ������B

        ex) BOOK�Ƃ���Table�����݂��Ă���ꍇ
        �oPrimaryKey�ɂ���ӌ����p
        /- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        final BookDao dao = (BookDao)container.getComponent(BookDao.class);
        Book entity = dao.getEntity(new BigDecimal(4));
        - - - - - - /

        �oConditionBean�ɂ���ӌ����p
        /- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        final BookCB cb = new BookCB();
        cb.query().setBookId_Equal(4);
        final BookDao dao = (BookDao)container.getComponent(BookDao.class);
        Book entity = dao.selectEntity(cb);
        - - - - - - /

# -------------------------------------------
#                          Modification again
#                          ------------------

8. Modify build-properties again

    Project�̗v���ɍ��킹�āA�œK��Property��ݒ肵�Ă����܂��B






# /============================================================================
#                                                                   Torque Task
#                                                                   ===========

A. JDBC-Task
    �T�v�F
        Database����JDBC�o�R��Schema�����擾���܂��B

    �ڍׁF
        ���s�����'./schema'�ȉ��Ɏ擾����Schema���XML�Ƃ��ĕۑ�����܂��B
        �܂���������s���Ȃ���Ύn�܂�܂���B



B. OM-Task
    �T�v�F
        Schema����Velocity-Template�𗘗p���āAS2Dao��Class�𐶐����܂��B

    �ڍׁF
        JDBC-Task�ɂĎ擾����Schema����build.properties�����ɁAVelocity-Template��
        ���p����Class�������������܂��BDBFlute��Main�̋@�\�ł��B
        Velocity-Template��'%DBFLUTE_HOME%/templates/om'�ȉ��ɑ��݂��܂��B



C. SchemaHTML-Task
    �T�v�F
        Schema���𗘗p���āASchema����HTML-Document�𐶐����܂��B(SchemaHTML)

    �ڍׁF
        Batch�����s����CurrentDirectory��'./output/doc'�ȉ��ɐ�������܂��B




D. Invoke_ReplaceSchema-Task
    �T�v�F
        �w�肳�ꂽSqlFile(DropTable��CreateTable��DDL��)�����s����DB��Schema����蒼���B

    �ڍׁF
        ������SQL������؂�Delimiter��';'�ł��B

        �P��SQL�������s���Ă��邾���Ȃ̂ŁA�ʂ�DDL�łȂ��Ă����s����܂��B
        �ꉞ�A�����I��'DB��Replace�p'�Ɩ��ł��Ă��邾���ł��B
        DB��Replace����Ɠ����ɁAMaster-Table��Record���i�[������A
        Test�p��Record��o�^�����肷��̂ɂ����p�ł��܂��B

        build.properties�ł́AJDBC-Task�ŗ��p����DB�ڑ������
        'invokeReplaceSchemaDefinitionMap'���K�v�ƂȂ�܂��B



E. Invoke_SqlDirectory-Task
    �T�v�F
        �w�肳�ꂽDirectory�ȉ�(�ċA�I)��SqlFile��S�Ď��s����B

    �ڍׁF
        �g���q��'.sql'�ł�(�啶����������ʂȂ�)�B
        ������SQL������؂�Delimiter��';'�ł��B

        - - - - - - - - - - - - - - - - - - - - - - - - - - 
        S2Dao�́gTest�l�𗘗p�����O����SQL�h�̈�Ď��s���\
        - - - - - - - - - - - - - - - - - - - - - - - - - - 

        build.properties�ł́AJDBC-Task�ŗ��p����DB�ڑ������
        'invokeSqlDirectoryDefinitionMap'���K�v�ƂȂ�܂��B





# /============================================================================
#                                                                  Dependencies
#                                                                  ============
Java
	Seasar-2.3.10
	S2Dao-1.0.35
	Commons-Logging-1.0.3 over
	log4j-1.2.8.jar over
	Aopalliance-1.0
	Javassist-3.0
	Ognl-2.6.5

CSharp
	Seasar-1.2.4
	S2Dao-1.0.0


# /============================================================================
#                                                                  Supported DB
#                                                                  ============

- DBFlute supports all things Torque supports.
	/-----------------------------------------------------------
	axion, cloudscape, db2, db2400, hypersonic, interbase, mssql
	, mysql, oracle, postgresql, sapdb, sybase, firebird, derby
	--------------------/

# /============================================================================
#                                                                   Restriction
#                                                                   ===========

- DBFlute does not support that Changing xxxPrefixes(ex.insertPrefixes). Sorry!
  Please use default name. {update, insert, modify, remove...}

