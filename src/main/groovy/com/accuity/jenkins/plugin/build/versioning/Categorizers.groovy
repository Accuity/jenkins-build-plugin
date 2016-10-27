package com.accuity.jenkins.plugin.build.versioning

import hudson.EnvVars

abstract class Categorizer {

    abstract String addCategory(EnvVars envVars, String version)

    protected static String addFormattedCategoryToVersion(String version, String category) {

        if (category?.trim()) {
            def formattedCategory = category.toLowerCase().replaceAll('[^\\w\\d]+', '.')
            return "${version}-${formattedCategory}"
        }
        return version
    }
}

class GitBranchCategorizer extends Categorizer {

    final static String branchVariable = 'GIT_BRANCH'
    final static String mainBranchVariable = 'MAIN__GIT__BRANCH' // weird name to avoid conflicts

    @Override
    String addCategory(EnvVars envVars, String version) {

        def gitBranch  = envVars[branchVariable]
        def mainBranch = envVars[mainBranchVariable]

        gitBranch  = gitBranch.replace('origin/', '')
        mainBranch = (mainBranch?.trim()) ? mainBranch : 'master'

        gitBranch = (gitBranch.toLowerCase() == mainBranch.toLowerCase()) ? null : gitBranch
        return addFormattedCategoryToVersion(version, gitBranch)
    }
}