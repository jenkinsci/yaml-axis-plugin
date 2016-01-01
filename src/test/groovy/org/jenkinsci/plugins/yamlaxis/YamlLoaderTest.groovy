package org.jenkinsci.plugins.yamlaxis

class YamlLoaderTest extends spock.lang.Specification {
    private static final String YAML_FILE = "src/test/resources/matrix.yml"

    def "load"(){
        expect:
        YamlLoader.loadValues(YAML_FILE, key) == expected

        where:
        key            || expected
        "STRING_VALUE" || ["a", "b", "c"]
        "INT_VALUE"    || ["1", "2", "3"]
        "UNKNOWN"      || []
    }
}
