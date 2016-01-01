package org.jenkinsci.plugins.yamlaxis

class YamlLoaderTest extends spock.lang.Specification {
    private static final String CURRENT_DIR = System.getProperty("user.dir")
    private static final String RELATIVE_YAML_FILE = "src/test/resources/matrix.yml"
    private static final String ABSOLUTE_YAML_FILE = CURRENT_DIR + File.separator + RELATIVE_YAML_FILE

    def "load"(){
        setup:
        def loader = new YamlLoader(yamlFile: yamlFile, currentDir: CURRENT_DIR)

        expect:
        loader.loadValues(key) == expected

        where:
        yamlFile           | key            || expected
        RELATIVE_YAML_FILE | "STRING_VALUE" || ["a", "b", "c"]
        RELATIVE_YAML_FILE | "INT_VALUE"    || ["1", "2", "3"]
        RELATIVE_YAML_FILE | "UNKNOWN"      || []
        ABSOLUTE_YAML_FILE | "STRING_VALUE" || ["a", "b", "c"]
    }
}
