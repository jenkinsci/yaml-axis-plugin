package org.jenkinsci.plugins.yamlaxis

import spock.lang.Specification

class YamlTextLoaderSpock extends Specification {
    def "loadStrings"(){
        setup:
        String yamlText = """
STRING_VALUE:
  - a
  - b
  - c
INT_VALUE:
  - 1
  - 2
  - 3
BOOL_VALUE:
  - true
  - false
"""

        def loader = new YamlTextLoader(yamlText)

        expect:
        loader.loadStrings(key) == expected

        where:
        key            || expected
        "STRING_VALUE" || ["a", "b", "c"]
        "INT_VALUE"    || ["1", "2", "3"]
        "BOOL_VALUE"   || ["true", "false"]
        "UNKNOWN"      || []
        "STRING_VALUE" || ["a", "b", "c"]
    }

    def "loadValues"(){
        setup:
        String yamlText = """
exclude:
  - a: 1
    b: 2
  - c: 3
"""

        def loader = new YamlTextLoader(yamlText)

        expect:
        loader.loadMaps(key) == expected

        where:
        key         || expected
        "exclude"   || [[a: "1", b: "2"], [c: "3"]]
        "not_found" || null
    }

    def "loadValuesList"(){
        setup:
        String yamlText = """
exclude:
  - a: 1
    b:
      - 2
      - 3
  - c: 4
"""

        def loader = new YamlTextLoader(yamlText)

        expect:
        loader.loadMaps(key) == expected

        where:
        key         || expected
        "exclude"   || [[a: "1", b: ["2", "3"]], [c: "4"]]
        "not_found" || null
    }
}
