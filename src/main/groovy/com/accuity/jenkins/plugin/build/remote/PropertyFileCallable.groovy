package com.accuity.jenkins.plugin.build.remote

import hudson.remoting.VirtualChannel
import jenkins.MasterToSlaveFileCallable

// This will still work if the job is running on the Master
class PropertyFileCallable extends MasterToSlaveFileCallable<String> {

    Map<String,String> properties

    def PropertyFileCallable(Map<String,String> properties) {
        this.properties = properties
        super
    }

    @Override
    String invoke(File propertyFile, VirtualChannel channel) throws IOException, InterruptedException {

        processFileInplace(propertyFile) { String text ->
            properties.each { property ->
                if (text.contains(property.key)) {
                    text = text.replaceAll(/${property.key}.*=.*/, propertyLine(property))
                } else {
                    text = "${text}\n${propertyLine(property)}"
                }
            }
            text
        }
    }

    private static def processFileInplace(File file, Closure processText) {
        String text = processText(file.text)
        file.write(text)
    }

    private static String propertyLine(Map.Entry<String,String> property) {
        "${property.key} = ${property.value}"
    }
}
