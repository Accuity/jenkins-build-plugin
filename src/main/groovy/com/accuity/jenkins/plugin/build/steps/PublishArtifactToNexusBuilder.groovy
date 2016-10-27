package com.accuity.jenkins.plugin.build.steps

import com.accuity.jenkins.plugin.build.Logger
import com.accuity.jenkins.plugin.build.Parameters
import com.accuity.jenkins.plugin.build.nexus.MavenArtifact
import com.accuity.jenkins.plugin.build.nexus.NexusClient
import com.accuity.jenkins.plugin.build.nexus.NexusResponse
import com.accuity.jenkins.plugin.build.remote.NexusPublishCallable
import hudson.EnvVars
import hudson.Extension
import hudson.Launcher
import hudson.model.AbstractBuild
import hudson.model.AbstractProject
import hudson.model.BuildListener
import hudson.model.Descriptor.FormException
import hudson.tasks.BuildStepDescriptor
import hudson.tasks.Builder
import hudson.util.FormValidation
import hudson.util.Secret
import net.sf.json.JSONObject
import org.kohsuke.stapler.DataBoundConstructor
import org.kohsuke.stapler.QueryParameter
import org.kohsuke.stapler.StaplerRequest

class PublishArtifactToNexusBuilder extends Builder {

    boolean overrideDefaults // Used for UI <f:optionalBlock>
    MavenArtifact artifactDefinition

    private static final String logPrefix = 'PublishArtifact'

    def getRepository() {
        artifactDefinition.repository
    }
    def getArtifactGroup() {
        artifactDefinition.group
    }
    def getArtifactName() {
        artifactDefinition.name
    }
    def getArtifactVersion() {
        artifactDefinition.version
    }
    def getArtifactPath() {
        artifactDefinition.path
    }
    def getPomFilePath() {
        artifactDefinition.pomPath
    }

    // Tells Jenkins to hydrate object using configuration (UI) data
    @DataBoundConstructor
    def PublishArtifactToNexusBuilder(String repository, String artifactGroup, String artifactName, String artifactVersion,
                                      String artifactPath, String pomFilePath, boolean overrideDefaults) {
        this.overrideDefaults = overrideDefaults
        if (overrideDefaults) {
            this.artifactDefinition = new MavenArtifact(repository, artifactGroup, artifactName, artifactVersion,
                    artifactPath, pomFilePath)
        } else {
            this.artifactDefinition = new MavenArtifact(repository, null, null, null, null, null)
        }
    }

    @Override
    boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        def logger = new Logger(listener.getLogger(), logPrefix)
        def envVars = build.getEnvironment(listener)

        // ToDo: null check all these
        def url      = descriptor.nexusUrl
        def username = descriptor.nexusUser
        def password = descriptor.nexusPassword?.plainText

        if (url == null || username == null || password == null) {
            logger.println 'Nexus server is not configured, please check Jenkins configuration page'
            return false
        }

        def artifact = createArtifact(envVars, artifactDefinition)

        def missingParameters = artifact.findMissingParameters()
        if (missingParameters.size() > 0) {
            logger.println "The following required parameters are missing: ${missingParameters.join(', ')}"
            return false
        }

        // workspace could be on master or slave
        def responseJson = build.workspace.act(new NexusPublishCallable(url, username, password, artifact.toJson()))

        def response = NexusResponse.fromJson(responseJson)

        if (response.status != 201) {
            logger.println "Failed to publish artifact ${response.toString()}"
            return false
        }

        logger.println "Successfully published artifact ${artifact.name}:${artifact.version}"
        true  // Returning true signals no errors in build step
    }

    /**
     * Merges Job variable values and Job config values into into one artifact definition.
     * All parameters will use job config values (i.e. UI overrides) first,
     * then default to job variables values (i.e. EnvVars)
     * @return
     *      Merged artifact definition
     */
    private static MavenArtifact createArtifact(EnvVars envVars, MavenArtifact artifact) {

        // Repository should be configured in job config
        def repository      = artifact.repository
        def artifactGroup   = expandVariableOrParameter(envVars, artifact.group,   Parameters.group)
        def artifactName    = expandVariableOrParameter(envVars, artifact.name,    Parameters.name)
        def artifactVersion = expandVariableOrParameter(envVars, artifact.version, VersionStringBuilder.outputVariable)
        def artifactPath    = expandVariableOrParameter(envVars, artifact.path,    Parameters.path)
        def pomFilePath     = expandVariableOrParameter(envVars, artifact.pomPath, Parameters.pomPath)

        new MavenArtifact(repository, artifactGroup, artifactName, artifactVersion, artifactPath, pomFilePath)
    }

    private static String expandVariableOrParameter(EnvVars envVars, String variable, String parameter) {
        def value = (variable?.trim()) ? variable : envVars[parameter]
        envVars.expand(value)
    }

    @Extension
    static final class PublishNexusArtifactDescriptor extends BuildStepDescriptor<Builder> {

        String nexusUrl
        String nexusUser
        Secret nexusPassword

        @SuppressWarnings("unused")
        PublishNexusArtifactDescriptor() {
            load() // loads config from xml file
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {

            nexusUrl = formData.getString("nexusUrl")
            nexusUser = formData.getString("nexusUser")
            nexusPassword = Secret.fromString(formData.getString("nexusPassword"))

            save()
            super.configure(req, formData)
        }

        FormValidation doTestConnection(@QueryParameter("nexusUrl") final String nexusUrl,
                                        @QueryParameter("nexusUser") final String nexusUser,
                                        @QueryParameter("nexusPassword") final String nexusPassword) {

            testConnection(nexusUrl, nexusUser, nexusPassword, 'Did you forget "/nexus" ?') { client ->
                client.testConnection()
            }
        }

        FormValidation doTestRepository(@QueryParameter("repository") final String repository) {

            testConnection(nexusUrl, nexusUser, nexusPassword.plainText, 'Repositories are case sensitive') { client ->
                client.testRepository(repository)
            }
        }

        private static FormValidation testConnection(String url, String user, String password, String notFoundMessage, Closure request) {

            def client = new NexusClient(url, user, password)

            def response = request(client)

            switch(response.status) {
                case 200:
                    return FormValidation.ok('Success')
                    break
                case 404:
                    return FormValidation.error("404: URL Not Found. ${notFoundMessage}")
                    break
                case 401:
                    return FormValidation.error('401: Not Authorized')
                    break
                case 0:
                    return FormValidation.error('Error with http request, no status code')
                    break
                default:
                    return FormValidation.error("Unexpected response: ${response.toString()}")
                    break
            }
        }

        @Override
        String getDisplayName() { "Publish Artifact to Nexus" }

        @Override
        boolean isApplicable(Class<? extends AbstractProject> jobType) { true }
    }

    @Override
    PublishNexusArtifactDescriptor getDescriptor() {
        (PublishNexusArtifactDescriptor)super.getDescriptor()
    }
}
