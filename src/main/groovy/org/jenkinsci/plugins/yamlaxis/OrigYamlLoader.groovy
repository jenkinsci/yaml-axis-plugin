package org.jenkinsci.plugins.yamlaxis
import groovy.transform.TupleConstructor
import hudson.FilePath
import hudson.Util
import org.yaml.snakeyaml.Yaml

@TupleConstructor
class OrigYamlLoader {
    String yamlFile
    FilePath workspace

    List<String> loadValues(String key){
        if(Util.fixEmpty(yamlFile) == null) {
            return []
        }

        Yaml yaml = new Yaml()
        InputStream input = createFilePath().read()

        try{
            def content = yaml.load(input)
            def values = content.get(key)
            values.collect { it.toString() }

        } finally {
            // NOTE: can not use withCloseable on groovy 1.8.9 (Jenkins included version)
            input.close()
        }
    }

    private FilePath createFilePath() {
        if (!Util.isRelativePath(yamlFile) || workspace == null) {
            return new FilePath(new File(yamlFile))
        }

        new FilePath(workspace, yamlFile)
    }
}
