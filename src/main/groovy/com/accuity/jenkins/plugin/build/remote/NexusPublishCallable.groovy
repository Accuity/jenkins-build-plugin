package com.accuity.jenkins.plugin.build.remote

import com.accuity.jenkins.plugin.build.nexus.MavenArtifact
import com.accuity.jenkins.plugin.build.nexus.NexusClient
import hudson.remoting.VirtualChannel
import jenkins.MasterToSlaveFileCallable

// This will still work if the job is running on the Master
class NexusPublishCallable extends MasterToSlaveFileCallable<String> {

    String url
    String username
    String password
    String artifactAsJson // need to store as string to avoid Hudson serialization errors

    NexusPublishCallable(String url, String username, String password, String artifactJson) {
        this.url = url
        this.username = username
        this.password = password
        this.artifactAsJson = artifactJson
    }

    @Override
    String invoke(File file, VirtualChannel channel) throws IOException, InterruptedException {
        def workspace = file
        def artifact = MavenArtifact.fromJson artifactAsJson
        def artifactFile = new File("${workspace}/${artifact.path}")
        def pomFile = null

        if (!artifactFile.exists()) {
            throw new Exception("Could not publish ${artifact.name}: Could not find ${artifact.path}")
        }
        if (artifact.hasPom()) {
            pomFile = new File("${workspace}/${artifact.pomPath}")
            if (!pomFile.exists()) {
                throw new Exception("Could not publish ${artifact.name}: Could not find ${artifact.pomPath}")
            }
        }

        new NexusClient(url, username, password)
                .publishMavenArtifact(artifact, artifactFile, pomFile)
                .toJson()
    }
}
