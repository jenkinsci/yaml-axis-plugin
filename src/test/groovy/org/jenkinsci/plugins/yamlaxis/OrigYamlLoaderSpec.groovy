package org.jenkinsci.plugins.yamlaxis

import hudson.FilePath

class OrigYamlLoaderSpec extends spock.lang.Specification {
    private static final String CURRENT_DIR = System.getProperty("user.dir")
    private static final String RELATIVE_YAML_FILE = "src/test/resources/matrix.yml"
    private static final String ABSOLUTE_YAML_FILE = CURRENT_DIR + File.separator + RELATIVE_YAML_FILE

    def "load"(){
        setup:
        FilePath workspace = new FilePath(new File(CURRENT_DIR))
        def loader = new OrigYamlLoader(yamlFile: yamlFile, workspace: workspace)

        expect:
        loader.loadValues(key) == expected

        where:
        yamlFile           | key            || expected
        RELATIVE_YAML_FILE | "STRING_VALUE" || ["a", "b", "c"]
        RELATIVE_YAML_FILE | "INT_VALUE"    || ["1", "2", "3"]
        RELATIVE_YAML_FILE | "BOOL_VALUE"   || ["true", "false"]
        RELATIVE_YAML_FILE | "UNKNOWN"      || []
        ABSOLUTE_YAML_FILE | "STRING_VALUE" || ["a", "b", "c"]
    }
}
