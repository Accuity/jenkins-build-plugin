package com.accuity.jenkins.plugin.build.versioning

import com.accuity.jenkins.plugin.build.Logger
import groovy.json.JsonOutput
import groovy.json.JsonSlurper

class RevisionHelper {

    private RevisionFile revisionFile
    private Logger logger

    def RevisionHelper(RevisionFile revisionFile, Logger logger) {
        this.revisionFile = revisionFile
        this.logger = logger
    }

    /**
     * Increments the current revision for the supplied version and constructs the new version string
     * @param version
     *      The version string e.g. '1.2.0'
     * @param threeSectionLimit
     *      If true, the version string will be limited to 3 sections.
     *      The revision will be the 3rd section, and will replace anything after the first two sections.
     *      e.g. version 1.2.3.4 will become 1.2.r (where r is the revision)
     * @return
     *      The new version string with revision number
     */
    def incrementRevision(String version, boolean threeSectionLimit) {

        if (threeSectionLimit) {
            def sections = version.split('\\.')
            version = "${sections[0]}.${sections[1]}"
            logger.println "Version trimmed to ${version} for SemVer compatibility"
        }

        def revisions = parseRevisionFile()
        def currentRevision = revisions[version]

        // Set revision to 0 if it's a new version
        def newRevision = (currentRevision == null) ? 0 : ++currentRevision

        revisions[version] = newRevision
        def revisionsJson = JsonOutput.toJson revisions
        revisionFile.saveRevisions revisionsJson

        logger.println "Using revision ${newRevision} for version ${version}"

        version = "${version}.${newRevision}"
        return version
    }

    /**
     * Parses the JSON returned by the revisions file
     * @return
     *      A map of version strings to revisions
     */
    Map<String,Integer> parseRevisionFile() {
        def json = revisionFile.readRevisions()
        def revisionsObject = new JsonSlurper().parseText json
        return (Map<String,Integer>)revisionsObject
    }
}
