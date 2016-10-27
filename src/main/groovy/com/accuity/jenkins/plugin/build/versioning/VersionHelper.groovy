package com.accuity.jenkins.plugin.build.versioning

import com.accuity.jenkins.plugin.build.Parameters
import hudson.EnvVars

class VersionHelper {

    // The version number to use if neither a version or a default is supplied by the job
    final static String fallbackVersion = '1.0.0'

    // By convention the build job will read the version from the code
    // and inject it as the "appVersion" variable
    final static String versionVariable = Parameters.version

    /**
     * Returns the version to use based on the following order:
     *   1. appVersion variable
     *   2. User supplied default
     *   3. Hard-coded default
     * @param envVars
     * @param defaultVersion
     * @return
     *      version string
     */
    static String appVersionOrDefault(EnvVars envVars, String defaultVersion) {
        def appVersion = envVars[versionVariable]
        return [appVersion, defaultVersion, fallbackVersion].find() { it?.trim() } // not null or whitespace
    }

    /**
     * Determines the version category (e.g. branch name) and appends it to the version
     * @param envVars
     * @param version
     * @return
     *      Categorised version string
     */
    static String addVersionCategory(EnvVars envVars, String version) {
        // Hard code it to use git branch categories for now
        Categorizer categorizer = new GitBranchCategorizer()
        return categorizer.addCategory(envVars, version)
    }
}
