package org.jenkinsci.plugins.yamlaxis

import org.yaml.snakeyaml.Yaml

class YamlLoader {
    static List<String> loadValues(String file, String key){
        if(file == null || file == "") {
            return []
        }

        Yaml yaml = new Yaml()

        try {
            InputStream input = new FileInputStream(new File(file))
            input.withCloseable {
                def content = yaml.load(input)
                def values = content.get(key)
                values.collect { it.toString() }
            }

        } catch (FileNotFoundException e){
            // workspace is not cloned
            []
        }
    }
}
