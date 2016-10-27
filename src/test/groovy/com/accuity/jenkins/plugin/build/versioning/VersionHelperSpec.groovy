package com.accuity.jenkins.plugin.build.versioning

import hudson.EnvVars
import spock.lang.Specification

class VersionHelperSpec extends Specification {

    def versionVariable = VersionHelper.versionVariable
    def envVars = Mock(EnvVars)

    def 'returns the appVersion if not null'() {
        given:
        envVars.get(versionVariable) >> { '2.3.0' }
        def version = VersionHelper.appVersionOrDefault(envVars, null)
        expect:
        version == '2.3.0'
    }

    def 'returns the default version if appVersion is null'() {
        given:
        envVars.get(versionVariable) >> { null }
        def version = VersionHelper.appVersionOrDefault(envVars, '1.3.0')
        expect:
        version == '1.3.0'
    }

    def 'returns the default version if appVersion is empty'() {
        given:
        envVars.get(versionVariable) >> { '  ' }
        def version = VersionHelper.appVersionOrDefault(envVars, '1.4.0')
        expect:
        version == '1.4.0'
    }

    def 'returns the fallback version if appVersion and default version are null'() {
        given:
        envVars.get(versionVariable) >> { null }
        def version = VersionHelper.appVersionOrDefault(envVars, null)
        expect:
        version == VersionHelper.fallbackVersion
    }

    def 'returns the fallback version if appVersion and default version are empty'() {
        given:
        envVars.get(versionVariable) >> { ' ' }
        def version = VersionHelper.appVersionOrDefault(envVars, '  ')
        expect:
        version == VersionHelper.fallbackVersion
    }
}
