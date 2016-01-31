package org.jenkinsci.plugins.yamlaxis

import hudson.FilePath

class YamlFileLoaderSpec extends spock.lang.Specification {
    private static final String CURRENT_DIR = System.getProperty("user.dir")
    private static final String RELATIVE_YAML_FILE = "src/test/resources/axis.yml"
    // private static final String ABSOLUTE_YAML_FILE = CURRENT_DIR + File.separator + RELATIVE_YAML_FILE

    def "loadValues"(){
        setup:
        FilePath workspace = new FilePath(new File(CURRENT_DIR))
        def loader = new YamlFileLoader(yamlFile: yamlFile, workspace: workspace)

        expect:
        loader.loadValues(key) == expected

        where:
        yamlFile           | key         || expected
        RELATIVE_YAML_FILE | "exclude"   || [[a: "1", b: "2"], [c: "3"]]
        RELATIVE_YAML_FILE | "not_found" || null
    }
}
