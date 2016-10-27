package com.accuity.jenkins.plugin.build.nexus

import spock.lang.Specification

class NexusResponseSpec extends Specification {

    def 'toJson returns serialized json string'() {
        given:
        def response = new NexusResponse( status: 200, body: 'message' )

        expect:
        response.toJson() == '{"status":200,"body":"message"}'
    }

    def 'fromJson returns de-serialized response object'() {
        given:
        def responseJson = '{ "status": 201, "body": "created" }'

        when:
        def response = NexusResponse.fromJson(responseJson)

        then:
        response.status == 201
        response.body == 'created'
    }

    def 'toString returns message with status and body'() {
        given:
        def response = new NexusResponse( status: 500, body: 'oops' )

        expect:
        response.toString() == '500: oops'
    }
}
