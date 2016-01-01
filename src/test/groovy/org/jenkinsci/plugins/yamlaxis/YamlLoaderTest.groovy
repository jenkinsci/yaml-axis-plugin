package org.jenkinsci.plugins.yamlaxis

class YamlLoaderTest extends spock.lang.Specification {
    private static final String YAML_FILE = "src/test/resources/matrix.yml"
    private static final String CURRENT_DIR = System.getProperty("user.dir");

    def "load"(){
        setup:
        def loader = new YamlLoader(yamlFile: yamlFile, currentDir: CURRENT_DIR)

        expect:
        loader.loadValues(key) == expected

        where:
        yamlFile                      | key            || expected
        YAML_FILE                     | "STRING_VALUE" || ["a", "b", "c"]
        YAML_FILE                     | "INT_VALUE"    || ["1", "2", "3"]
        YAML_FILE                     | "UNKNOWN"      || []
        CURRENT_DIR + "/" + YAML_FILE | "STRING_VALUE" || ["a", "b", "c"]
    }
}
