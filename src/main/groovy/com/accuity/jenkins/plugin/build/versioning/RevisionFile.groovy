package com.accuity.jenkins.plugin.build.versioning

class RevisionFile {

    static final String fileName = 'revisions.json'

    private File revisionsFile

    def RevisionFile(File projectRoot) {
        this.revisionsFile = new File("${projectRoot}/${fileName}")
    }

    String readRevisions() {
        revisionsFile.exists() ? revisionsFile.text : '{}'
    }

    void saveRevisions(String revisionsJson) {
        revisionsFile.write revisionsJson
    }
}
