package org.jenkinsci.plugins.yamlaxis
import groovy.transform.TupleConstructor
import hudson.Util
import org.yaml.snakeyaml.Yaml

@TupleConstructor
class YamlLoader {
    String yamlFile
    String currentDir

    List<String> loadValues(String key){
        if(Util.fixEmpty(yamlFile) == null) {
            return []
        }

        Yaml yaml = new Yaml()
        InputStream input = new FileInputStream(createFile())

        try{
            def content = yaml.load(input)
            def values = content.get(key)
            values.collect { it.toString() }

        } finally {
            // NOTE: can not use withCloseable on groovy 1.8.9 (Jenkins included version)
            input.close()
        }
    }

    private File createFile() {
        if (!Util.isRelativePath(yamlFile)) {
            return new File(yamlFile)
        }

        if (Util.fixEmpty(currentDir) == null) {
            new File(yamlFile)
        } else {
            new File(currentDir + File.separator + yamlFile)
        }
    }
}
