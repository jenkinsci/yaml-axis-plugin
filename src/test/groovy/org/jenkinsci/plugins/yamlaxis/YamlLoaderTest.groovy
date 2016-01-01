package org.jenkinsci.plugins.yamlaxis

class YamlLoaderTest {
    static class loadValues extends spock.lang.Specification {
        private static final String YAML_FILE = "src/test/resources/matrix.yml"

        def "When String values"() {
            expect:
            YamlLoader.loadValues(YAML_FILE, "STRING_VALUE") == ["a", "b", "c"]
        }

        def "When int values"() {
            expect:
            YamlLoader.loadValues(YAML_FILE, "INT_VALUE") == ["1", "2", "3"]
        }

        def "When not fount key"() {
            expect:
            YamlLoader.loadValues(YAML_FILE, "UNKNOWN") == []
        }
    }
}
