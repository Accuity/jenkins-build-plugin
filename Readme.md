# Accuity Jenkins Build Plugin

This plugin encapsulates common workflow steps in Accuity Jenkins builds:
  * Version revisions
  * Build categorisation
  * Updating properties files
  * Setting build name
  * Uploading artifacts

The aim is to keep job configurations simple and consistent

## Usage

This plugin provides two build steps 'Create Version String' and 'Publish Artifact to Nexus'. 

Both steps are configured to use a standard set of properties:
```
appGroup    = com.test
appName     = my-app
appVersion  = 1.2.0

# Variable expansion is done by Jenkins based on job variables
appArtifact = build/libs/${appName}.jar
appPom      = build/libs/pom.xml # Pom is optional
```

Usually these are imported from the project's `gradle.properties` file using the EnvInject plugin, 
however the plugin is not tied to gradle. The plugin reads these properties from job variables, 
not directly from `gradle.properties`, so these properties can be stored in any way 
as long as they are imported into the Jenkins job somehow.

Using both build steps from the plugin means that the core of any Jenkins job is the following:
  1. 'Inject environment variables' (Inject properties from `gradle.properties`)
  2. 'Create version string'
  3. Build the artifact (e.g. `gradle build`)
  4. 'Publish artifact to nexus'

#### Create Version String

This step does the following:
  1. Reads the `appVersion` variable
  2. Generates the next revision number for that version
  3. Adds the revision number to the version string
  4. Appends the git branch to the version string (unless it's a build from the main branch)
  5. Sets the version string as the build name
  6. Sets the version string as the value of the `FULL_VERSION_STRING` job variable
  7. Writes the version string back to the properties file (optional)
  
The current revision for each version is stored in the `revisions.json` file in the project's root directory. 
This directory is always on the master, so it can be included as part of a Jenkins backup, 
and also so that revisions are consistent when the job is being executed across a Jenkins cluster. 
This directory is also not effected if the workspace is cleared.

The build step will create the `revisions.json` file if it's missing on the first run

#### Publish Artifact to Nexus

This step uses the standard properties described above, 
as well as the `FULL_VERSION_STRING` variable from the 'Create Version String' step 
to upload the build artifact to a Nexus repository

The Nexus URL, username and password and configured globally on the Jenkins configuration page

The only parameter that each job is required to provide is the Nexus repository. 
This could have been included in the standard properties set, 
but that could couple each project too tightly to the publishing process, 
which was the motivation for Jenkins doing the upload in the first place

## Development

To run a local test Jenkins instance, execute the gradle `server` task, 
or run `./gradlew server` from the command line

To test the plugin in a master-slave environment:
  1. Run `vagrant up` to launch a slave VM
  2. Start Jenkins using the `server` task described above
  2. Add a new node using Jenkins UI
      * `jenkins-slave` is the username
      * Use `./keys/ssh.pem` as the key
      * The IP address is in the `Vagrantfile`

## ToDo

* Improved error feedback (don't just throw exceptions)
* Integration tests (for both master and slave implementations)
