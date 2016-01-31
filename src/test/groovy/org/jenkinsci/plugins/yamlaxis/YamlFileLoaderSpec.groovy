package org.jenkinsci.plugins.yamlaxis

import hudson.FilePath

class YamlFileLoaderSpec extends spock.lang.Specification {
    private static final String CURRENT_DIR = System.getProperty("user.dir")
    private static final String RELATIVE_YAML_FILE = "src/test/resources/axis.yml"
    private static final String ABSOLUTE_YAML_FILE = CURRENT_DIR + File.separator + RELATIVE_YAML_FILE

    def "loadStrings"(){
        setup:
        FilePath workspace = new FilePath(new File(CURRENT_DIR))
        def loader = new YamlFileLoader(yamlFile: yamlFile, workspace: workspace)

        expect:
        loader.loadStrings(key) == expected

        where:
        yamlFile           | key            || expected
        RELATIVE_YAML_FILE | "STRING_VALUE" || ["a", "b", "c"]
        RELATIVE_YAML_FILE | "INT_VALUE"    || ["1", "2", "3"]
        RELATIVE_YAML_FILE | "BOOL_VALUE"   || ["true", "false"]
        RELATIVE_YAML_FILE | "UNKNOWN"      || []
        ABSOLUTE_YAML_FILE | "STRING_VALUE" || ["a", "b", "c"]
    }

    def "loadMaps"(){
        setup:
        FilePath workspace = new FilePath(new File(CURRENT_DIR))
        def loader = new YamlFileLoader(yamlFile: yamlFile, workspace: workspace)

        expect:
        loader.loadMaps(key) == expected

        where:
        yamlFile           | key         || expected
        RELATIVE_YAML_FILE | "exclude"   || [[a: "1", b: "2"], [c: "3"]]
        RELATIVE_YAML_FILE | "not_found" || null
    }
}
