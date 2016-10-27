package com.accuity.jenkins.plugin.build.nexus

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

class MavenArtifact {

    String repository
    String group
    String name
    String version
    String path
    String pomPath

    MavenArtifact(String repository, String group, String name, String version,
                  String path, String pomPath) {

        this.repository = repository
        this.group = group
        this.name = name
        this.version = version
        this.path = path
        this.pomPath = pomPath
    }

    // Private constructor to support fromJson method
    private MavenArtifact() { }

    def boolean hasPom() {
        (pomPath?.trim()) ? true : false
    }

    List<String> findMissingParameters() {

        def missingParameters = []
        def params = [
                'Repository': this.repository,
                'Group':      this.group,
                'Name':       this.name,
                'Version':    this.version,
                'Path':       this.path,
                // Pom Path is optional
        ]

        params.each { param ->
            if (!param.value?.trim()) {
                missingParameters.add param.key
            }
        }

        missingParameters
    }

    String toJson() {
        JsonOutput.toJson(this)
    }

    static MavenArtifact fromJson(String artifactJson) {
        def artifactObject = new JsonSlurper().parseText(artifactJson)
        (MavenArtifact)artifactObject
    }
}
