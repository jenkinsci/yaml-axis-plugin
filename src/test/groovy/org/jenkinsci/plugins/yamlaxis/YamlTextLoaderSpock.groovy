package org.jenkinsci.plugins.yamlaxis

import spock.lang.Specification

class YamlTextLoaderSpock extends Specification {
    def "loadValues"(){
        setup:
        String yamlText = """
exclude:
  - a: 1
    b: 2
  - c: 3
"""

        def loader = new YamlTextLoader(yamlText: yamlText)

        expect:
        loader.loadValues(key) == expected

        where:
        key         || expected
        "exclude"   || [[a: "1", b: "2"], [c: "3"]]
        "not_found" || null
    }
}
