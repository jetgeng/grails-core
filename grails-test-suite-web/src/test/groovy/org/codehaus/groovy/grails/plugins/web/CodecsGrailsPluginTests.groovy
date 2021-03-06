package org.codehaus.groovy.grails.plugins.web

import org.codehaus.groovy.grails.commons.test.*
import org.codehaus.groovy.grails.commons.spring.*
import org.codehaus.groovy.grails.plugins.*
import org.codehaus.groovy.runtime.*

class CodecsGrailsPluginTests extends AbstractGrailsPluginTests {

    protected void onSetUp() {
        gcl.parseClass """
                class FirstCodec {
                   static def encode = { str -> \"found first encode method for string: \${str}\" }
                   static def decode = { str -> \"found first decode method for string: \${str}\" }
                }
                """
        gcl.parseClass """
                class SecondCodec {
                   static def encode = { str -> \"found second encode method for string: \${str}\" }
                }
                """
        gcl.parseClass """
                class ThirdCodec {
                   static def decode = { str -> \"found third decode method for string: \${str}\" }
                }
                """

        pluginsToLoad << gcl.loadClass("org.codehaus.groovy.grails.plugins.CoreGrailsPlugin")
        pluginsToLoad << gcl.loadClass("org.codehaus.groovy.grails.plugins.CodecsGrailsPlugin")

        def registry = GroovySystem.metaClassRegistry

        registry.removeMetaClass(String)
        registry.removeMetaClass(byte[])
        registry.removeMetaClass(Byte[])
    }

    void testCodecsPlugin() {
        def someString = 'some string'

        assert someString.encodeAsFirst() == 'found first encode method for string: some string'
        assert someString.decodeFirst() == 'found first decode method for string: some string'
        assert someString.encodeAsSecond() == 'found second encode method for string: some string'
        assert someString.decodeThird() == 'found third decode method for string: some string'

        def message = shouldFail(MissingMethodException) {
            42.decodeSecond()
        }
        assertTrue message.startsWith("No signature of method: java.lang.Integer.decodeSecond() is applicable for")

        shouldFail(MissingMethodException) {
            someString.encodeAsThird()
        }
    }

    void testEncodeDecodeAsBase64() {
        assertEquals "dGVzdA==", "test".bytes.encodeAsBase64()
        assertEquals "dGVzdA==", "test".encodeAsBase64()
        assertEquals "test",new String("dGVzdA==".decodeBase64())
    }
}
