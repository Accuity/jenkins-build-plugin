package com.accuity.jenkins.plugin.build.versioning

import hudson.EnvVars
import spock.lang.Specification

class CategorizersSpec extends Specification {

    class FormatCheckCategorizer extends Categorizer{
        @Override
        String addCategory(EnvVars envVars, String version) {
            def category = version
            return addFormattedCategoryToVersion('x', category)
        }
    }

    def mockEnvVars = Mock(EnvVars)
    def formatCheckCategorizer = new FormatCheckCategorizer()
    def gitBranchCategorizer   = new GitBranchCategorizer()

    def 'formatCategory should return a clean version string'() {
        expect:
        formatCheckCategorizer.addCategory(null, category) == version

        where:
        category                          |  version
        null                              | 'x'
        '  '                              | 'x'
        'UPPER.CamelCase.Lower'           | 'x-upper.camelcase.lower'
        'string with  multiple    spaces' | 'x-string.with.multiple.spaces'
        'feature/with-punctuation'        | 'x-feature.with.punctuation'
    }

    def 'GitBranchCategorizer should remove "origin/" from git branch'() {
        given:
        mockEnvVars.get(GitBranchCategorizer.branchVariable) >> { 'origin/foo' }
        mockEnvVars.get(GitBranchCategorizer.mainBranchVariable) >> { null }

        expect:
        gitBranchCategorizer.addCategory(mockEnvVars, '1.2.0') == '1.2.0-foo'
    }

    def 'GitBranchCategorizer should use the formatted git branch'() {
        given:
        mockEnvVars.get(GitBranchCategorizer.branchVariable) >> { 'feature/a-git-branch' }
        mockEnvVars.get(GitBranchCategorizer.mainBranchVariable) >> { null }

        expect:
        gitBranchCategorizer.addCategory(mockEnvVars, '1.2.0') == '1.2.0-feature.a.git.branch'
    }

    def 'GitBranchCategorizer should not append category for "main" branch'() {
        given:
        mockEnvVars.get(GitBranchCategorizer.branchVariable) >> { 'trunk' }
        mockEnvVars.get(GitBranchCategorizer.mainBranchVariable) >> { 'trunk' }

        expect:
        gitBranchCategorizer.addCategory(mockEnvVars, '1.2.0') == '1.2.0'
    }

    def 'GitBranchCategorizer should use "master" as main branch default'() {
        given:
        mockEnvVars.get(GitBranchCategorizer.branchVariable) >> { 'master' }
        mockEnvVars.get(GitBranchCategorizer.mainBranchVariable) >> { null }

        expect:
        gitBranchCategorizer.addCategory(mockEnvVars, '1.2.0') == '1.2.0'
    }

    def 'GitBranchCategorizer should be case insensitive with branch names'() {
        given:
        mockEnvVars.get(GitBranchCategorizer.branchVariable) >> { branch }
        mockEnvVars.get(GitBranchCategorizer.mainBranchVariable) >> { main }

        expect:
        gitBranchCategorizer.addCategory(mockEnvVars, '1.2.0') == '1.2.0'

        where:
        branch  | main
        'Trunk' | 'trunk'
        'trunk' | 'Trunk'
    }
}
