package com.accuity.jenkins.plugin.build.versioning

import com.accuity.jenkins.plugin.build.Logger
import spock.lang.Specification

class RevisionHelperSpec extends Specification {

    String sampleJson = '{ "1.0.0": 3, "1.1.0": 4, "1.2.0": 5, "1.3": 6 }'

    def mockLogger       = Mock(Logger)
    def mockRevisionFile = Mock(RevisionFile)
    def revisionHelper   = new RevisionHelper(mockRevisionFile, mockLogger)

    def "incrementRevision appends the next revision number"() {
        setup:
        mockRevisionFile.readRevisions() >> { sampleJson }
        when:
        def version = revisionHelper.incrementRevision('1.0.0', false)
        then:
        version == '1.0.0.4'
    }

    def "incrementRevision sets revision to 0 if new version"() {
        setup:
        mockRevisionFile.readRevisions() >> { sampleJson }
        when:
        def version = revisionHelper.incrementRevision('2.0.0', false)
        then:
        version == '2.0.0.0'
    }

    def "incrementRevision can limit version to 3 sections"() {
        setup:
        mockRevisionFile.readRevisions() >> { sampleJson }
        when:
        def version = revisionHelper.incrementRevision('1.3.0', true)
        then:
        version == '1.3.7'
    }

    def "incrementRevision uses shortened version when finding current revision"() {
        setup:
        mockRevisionFile.readRevisions() >> { '{ "1.2": 7, "1.2.0": 5 }' }
        when:
        def version = revisionHelper.incrementRevision('1.2.0', true)
        then:
        version == '1.2.8'
    }

    def "incrementRevision does not treat 0 as null"() {
        setup:
        mockRevisionFile.readRevisions() >> { '{ "1.0.0": 0 }' }
        when:
        def version = revisionHelper.incrementRevision('1.0.0', false)
        then:
        version == '1.0.0.1'
    }

    def "incrementRevision saves the new revision"() {
        setup:
        mockRevisionFile.readRevisions() >> { '{ "1.0.0": 3 }' }
        when:
        revisionHelper.incrementRevision('1.0.0', false)
        then:
        1 * mockRevisionFile.saveRevisions('{"1.0.0":4}')
    }

    def "parseRevisionFile de-serializes revisions correctly"() {
        setup:
        mockRevisionFile.readRevisions() >> { sampleJson }
        when:
        def revisions = revisionHelper.parseRevisionFile()
        then:
        revisions.size() == 4
        revisions.'1.0.0' == 3
    }

    def "parseRevisionFile returns empty map if json is empty"() {
        setup:
        mockRevisionFile.readRevisions() >> { '{}' }
        when:
        def revisions = revisionHelper.parseRevisionFile()
        then:
        revisions.size() == 0
    }
}
