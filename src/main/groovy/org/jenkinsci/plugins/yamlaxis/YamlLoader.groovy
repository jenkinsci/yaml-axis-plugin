package org.jenkinsci.plugins.yamlaxis

import org.yaml.snakeyaml.Yaml

class YamlLoader {
    static List<String> loadValues(String file, String key){
        Yaml yaml = new Yaml()
        InputStream input = new FileInputStream(new File(file))
        input.withCloseable {
            def content = yaml.load(input)
            def values = content.get(key)
            values.collect { it.toString() }
        }
    }
}
