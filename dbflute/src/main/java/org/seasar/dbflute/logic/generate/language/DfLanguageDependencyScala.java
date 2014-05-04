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
package org.seasar.dbflute.logic.generate.language;

import org.seasar.dbflute.logic.generate.language.grammar.DfLanguageGrammar;
import org.seasar.dbflute.logic.generate.language.grammar.DfLanguageGrammarScala;
import org.seasar.dbflute.logic.generate.language.implstyle.DfLanguageImplStyle;
import org.seasar.dbflute.logic.generate.language.implstyle.DfLanguageImplStyleScala;
import org.seasar.dbflute.logic.generate.language.pkgstyle.DfLanguagePropertyPackageResolver;
import org.seasar.dbflute.logic.generate.language.pkgstyle.DfLanguagePropertyPackageResolverScala;

/**
 * @author jflute
 */
public class DfLanguageDependencyScala extends DfLanguageDependencyJava {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static final String PATH_MAVEN_SRC_MAIN_SCALA = "src/main/scala";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfLanguageGrammar _grammarScala = new DfLanguageGrammarScala();
    protected final DfLanguageImplStyle _implStyleScala = new DfLanguageImplStyleScala();
    protected final DfLanguagePropertyPackageResolver _packageResolverScala = new DfLanguagePropertyPackageResolverScala();

    // ===================================================================================
    //                                                                               Basic
    //                                                                               =====
    @Override
    public String getLanguageTitle() {
        return "Scala";
    }

    // ===================================================================================
    //                                                                    Program Handling
    //                                                                    ================
    @Override
    public DfLanguageGrammar getLanguageGrammar() {
        return _grammarScala;
    }

    @Override
    public DfLanguageImplStyle getLanguageImplStyle() {
        return _implStyleScala;
    }

    // ===================================================================================
    //                                                                 Compile Environment
    //                                                                 ===================
    @Override
    public String getMainProgramDirectory() {
        return PATH_MAVEN_SRC_MAIN_SCALA;
    }

    // ===================================================================================
    //                                                                Generate Environment
    //                                                                ====================
    @Override
    public String getGenerateControl() {
        return "om/ControlGenerateScala.vm";
    }

    @Override
    public String getGenerateControlBhvAp() {
        throw new UnsupportedOperationException("Unsupported language Scala");
    }

    @Override
    public String getSql2EntityControl() {
        return "om/ControlSql2EntityScala.vm";
    }

    @Override
    public String getTemplateFileExtension() {
        return "vmsca";
    }

    @Override
    public DfLanguagePropertyPackageResolver getLanguagePropertyPackageResolver() {
        return _packageResolverScala;
    }
}
