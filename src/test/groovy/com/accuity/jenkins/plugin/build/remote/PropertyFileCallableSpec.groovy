package com.accuity.jenkins.plugin.build.remote

import hudson.FilePath.FileCallable
import hudson.remoting.VirtualChannel
import spock.lang.Specification

class PropertyFileCallableSpec extends Specification {

    def propertiesFile = GroovyMock(File)
    def mockVirtualChannel = Mock(VirtualChannel)
    def propertiesText = "existingProperty = foo"
    def properties = [:]

    FileCallable propertyFileCallable

    def setup() {
        propertiesFile.text >> { propertiesText }
    }

    def 'invoke should replace existing property value'() {
        given:
        properties = [ existingProperty: 'bar' ]
        propertyFileCallable = new PropertyFileCallable(properties)

        when:
        propertyFileCallable.invoke(propertiesFile, mockVirtualChannel)

        then:
        1 * propertiesFile.write("existingProperty = bar")
    }

    def 'invoke should add new property value'() {
        given:
        properties = [ newProperty: 'baz' ]
        propertyFileCallable = new PropertyFileCallable(properties)

        when:
        propertyFileCallable.invoke(propertiesFile, mockVirtualChannel)

        then:
        1 * propertiesFile.write("existingProperty = foo\nnewProperty = baz")
    }

    def 'invoke should update multiple properties'() {
        given:
        properties = [ newProperty: 'baz', anotherProperty: 'frank' ]
        propertyFileCallable = new PropertyFileCallable(properties)

        when:
        propertyFileCallable.invoke(propertiesFile, mockVirtualChannel)

        then:
        1 * propertiesFile.write("existingProperty = foo\nnewProperty = baz\nanotherProperty = frank")
    }
}
