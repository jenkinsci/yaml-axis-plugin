package org.jenkinsci.plugins.yamlaxis

import groovy.transform.TupleConstructor
import org.yaml.snakeyaml.Yaml

@TupleConstructor
class YamlLoader {
    String yamlFile;

    List<String> loadValues(String key){
        if(yamlFile == null || yamlFile == "") {
            return []
        }

        Yaml yaml = new Yaml()
        InputStream input = new FileInputStream(new File(yamlFile))
        input.withCloseable {
            def content = yaml.load(input)
            def values = content.get(key)
            values.collect { it.toString() }
        }
    }
}
