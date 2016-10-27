package com.accuity.jenkins.plugin.build.steps

import com.accuity.jenkins.plugin.build.BuildModifier
import com.accuity.jenkins.plugin.build.Logger
import com.accuity.jenkins.plugin.build.remote.PropertyFileCallable
import com.accuity.jenkins.plugin.build.versioning.GitBranchCategorizer
import com.accuity.jenkins.plugin.build.versioning.RevisionFile
import com.accuity.jenkins.plugin.build.versioning.RevisionHelper
import com.accuity.jenkins.plugin.build.versioning.VersionHelper
import hudson.Extension
import hudson.FilePath
import hudson.Launcher
import hudson.model.AbstractBuild
import hudson.model.AbstractProject
import hudson.model.BuildListener
import hudson.tasks.BuildStepDescriptor
import hudson.tasks.Builder
import org.kohsuke.stapler.DataBoundConstructor

class VersionStringBuilder extends Builder {

    // Properties are referenced by config.jelly
    String mainBranch
    String defaultVersion
    boolean threeSectionLimit
    boolean writeToPropsFile
    boolean overridePropDefaults // just used for UI
    String propertiesFilePath
    String versionPropertyName

    static final String outputVariable = 'FULL_VERSION_STRING'
    private static final String logPrefix = 'VersionString'
    private static final String defaultPropertiesFile = 'gradle.properties'

    // Tells Jenkins to hydrate object using configuration (UI) data
    @DataBoundConstructor
    def VersionStringBuilder(String mainBranch, String defaultVersion, boolean threeSectionLimit,
                             boolean writeToPropsFile, boolean overridePropDefaults,
                             String propertiesFilePath, String versionPropertyName) {
        this.mainBranch = mainBranch
        this.defaultVersion = defaultVersion
        this.threeSectionLimit = threeSectionLimit
        this.writeToPropsFile = writeToPropsFile
        this.overridePropDefaults = overridePropDefaults
        this.propertiesFilePath = overridePropDefaults ? propertiesFilePath : null
        this.versionPropertyName = overridePropDefaults ? versionPropertyName : null
    }

    @Override
    boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        def logger = new Logger(listener.getLogger(), logPrefix)
        def envVars = build.getEnvironment(listener)

        def version = VersionHelper.appVersionOrDefault(envVars, defaultVersion)

        // project rootDir is parent directory of workspace (always on Master)
        def revisionFile = new RevisionFile(build.project.rootDir)
        version = new RevisionHelper(revisionFile, logger).incrementRevision(version, threeSectionLimit)

        // envVars is just a clone so inserting vars won't end up in job variables
        envVars.put(GitBranchCategorizer.mainBranchVariable, mainBranch)
        version = VersionHelper.addVersionCategory(envVars, version)

        BuildModifier.addVariable(build, outputVariable, version)
        BuildModifier.setBuildName(build, version)

        if(writeToPropsFile) {
            writeVersionToPropertiesFile(build.workspace, version, logger)
        }

        true // Returning true signals no errors in build step
    }

    private def writeVersionToPropertiesFile(FilePath workspace, String version, Logger logger) {

        def propertyName = (versionPropertyName?.trim()) ? versionPropertyName : VersionHelper.versionVariable

        def propertiesFilePath = (propertiesFilePath?.trim()) ? propertiesFilePath : defaultPropertiesFile

        logger.println "Setting ${propertyName} to ${version} in ${propertiesFilePath}"

        def properties = [ "${propertyName}" : version ]
        workspace.child(propertiesFilePath).act(new PropertyFileCallable(properties))
    }

    @Extension
    static final class VersionStringDescriptor extends BuildStepDescriptor<Builder> {

        @Override
        String getDisplayName() { "Create Version String" }

        @Override
        boolean isApplicable(Class<? extends AbstractProject> jobType) { true }
    }

    @Override
    VersionStringDescriptor getDescriptor() {
        (VersionStringDescriptor)super.descriptor
    }
}
