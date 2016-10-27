package com.accuity.jenkins.plugin.build.actions

import hudson.EnvVars
import hudson.model.AbstractBuild
import hudson.model.EnvironmentContributingAction

/**
 * Injects a variable into the list of job variables
 */
class VariableInjectionAction implements EnvironmentContributingAction {

    private String key
    private String value

    /**
     * @param key
     *      The name of the variable to be injected.
     * @param value
     *      The value of the variable to be injected.
     */
    public VariableInjectionAction(String key, String value) {
        this.key = key
        this.value = value
    }

    public void buildEnvVars(AbstractBuild build, EnvVars envVars) {

        if (envVars != null && key != null && value != null) {
            envVars.put(key, value)
        }
    }

    public String getDisplayName() {
        return "Variable Injection Action"
    }

    public String getIconFileName() {
        return null;
    }

    public String getUrlName() {
        return null;
    }
}
