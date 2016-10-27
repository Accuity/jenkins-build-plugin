package com.accuity.jenkins.plugin.build.nexus

import com.sun.jersey.api.client.Client
import com.sun.jersey.api.client.ClientResponse
import com.sun.jersey.api.client.WebResource
import com.sun.jersey.api.client.config.DefaultClientConfig
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter
import com.sun.jersey.multipart.FormDataMultiPart
import com.sun.jersey.multipart.file.FileDataBodyPart

import javax.ws.rs.core.MediaType

/**
 * Class to encapsulate all the http stuff
 */
class NexusClient {

    String url
    String username
    String password

    def NexusClient(String url, String username, String password) {
        this.url = url
        this.username = username
        this.password = password
    }

    NexusResponse publishMavenArtifact(MavenArtifact artifact, File artifactFile, File pomFile) {

        // The ordering of the variables, see documentation before refactoring this section
        // https://support.sonatype.com/hc/en-us/articles/213465818-How-can-I-programatically-upload-an-artifact-into-Nexus

        executeRequest { service ->
            FormDataMultiPart formData = new FormDataMultiPart()
            formData.field('r', artifact.repository)
            formData.field('g', artifact.group)
            formData.field('a', artifact.name)
            formData.field('v', artifact.version)
            formData.field('p', getFileExtension(artifactFile))
            formData.field('e', getFileExtension(artifactFile))
            if (artifact.hasPom()) {
                // formData.field('hasPom', 'true') // for some reason this breaks everything!?
                formData.bodyPart(new FileDataBodyPart('file', pomFile))
            }
            formData.bodyPart(new FileDataBodyPart('file', artifactFile))

            service.path('service/local/artifact/maven/content')
                    .type(MediaType.MULTIPART_FORM_DATA)
                    .post(ClientResponse.class, formData)
        }
    }

    NexusResponse testRepository(String repository) {
        executeRequest { service ->
            service.path("service/local/repositories/${repository}/status")
                    .accept(MediaType.APPLICATION_JSON)
                    .get(ClientResponse.class)
        }
    }

    NexusResponse testConnection() {
        executeRequest { service ->
            service.path('service/local/status')
                    .accept(MediaType.APPLICATION_JSON)
                    .get(ClientResponse.class)
        }
    }

    private NexusResponse executeRequest(Closure request) {
        try {
            WebResource service = getService()
            ClientResponse response = request(service)
            def body = response.hasEntity() ? response.getEntity(String) : ''
            return new NexusResponse( status: response.status, body: body )
        } catch (Exception e) {
            return new NexusResponse( status: 0, body: "Error executing request: ${e.message}")
        }
    }

    private WebResource getService() {
        Client client = Client.create( new DefaultClientConfig() )
        client.addFilter( new HTTPBasicAuthFilter(username, password) )
        client.resource( url )
    }

    private static String getFileExtension(File file) {
        file.name.split('\\.').last()
    }
}
