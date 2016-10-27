package com.accuity.jenkins.plugin.build.nexus

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

class NexusResponse {

    int status
    String body

    String toJson() {
        JsonOutput.toJson(this)
    }

    static NexusResponse fromJson(String responseJson) {
        def responseObject = new JsonSlurper().parseText(responseJson)
        (NexusResponse)responseObject
    }

    @Override
    String toString() {
        "${status}: ${body}"
    }
}
