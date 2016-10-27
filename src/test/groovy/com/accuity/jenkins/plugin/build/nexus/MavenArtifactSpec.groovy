package com.accuity.jenkins.plugin.build.nexus

import spock.lang.Specification

class MavenArtifactSpec extends Specification {

    def 'toJson returns serialized json string'() {
        given:
        def artifact = new MavenArtifact('builds', 'com.ex', 'foo', '0.1.0', 'path/to/foo.jar', null)

        expect:
        artifact.toJson() == '{"group":"com.ex","pomPath":null,"version":"0.1.0","path":"path/to/foo.jar","repository":"builds","name":"foo"}'
    }

    def 'fromJson returns de-serialized artifact object'() {
        given:
        def artifactJson = '{"group":"com.ex","pomPath":null,"version":"0.1.0","path":"path/to/bar.jar","repository":"builds","name":"bar"}'

        when:
        def artifact = MavenArtifact.fromJson artifactJson

        then:
        artifact.name == 'bar'
        artifact.path == 'path/to/bar.jar'
        artifact.pomPath == null
    }
}
