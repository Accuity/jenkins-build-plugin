package com.accuity.jenkins.plugin.build

import com.accuity.jenkins.plugin.build.actions.VariableInjectionAction
import hudson.model.AbstractBuild

/**
 * Provides helper methods to modify a build
 */
class BuildModifier {

    /**
     * Injects a new variable into the list of build variables
     */
    static addVariable(AbstractBuild build, String key, String value) {
        def action = new VariableInjectionAction(key, value)
        build.addAction(action)
    }

    static setBuildName(AbstractBuild build, String buildName) {
        build.setDisplayName(buildName);
        def action = new VariableInjectionAction("BUILD_DISPLAY_NAME", buildName)
        build.addAction(action)
    }
}
