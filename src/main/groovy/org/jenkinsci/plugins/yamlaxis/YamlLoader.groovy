package org.jenkinsci.plugins.yamlaxis

import groovy.transform.TupleConstructor
import hudson.Util
import org.yaml.snakeyaml.Yaml

@TupleConstructor
class YamlLoader {
    String yamlFile
    String currentDir

    List<String> loadValues(String key){
        if(yamlFile == null || yamlFile == "") {
            return []
        }

        File file;
        if(Util.isRelativePath(yamlFile)){
            file = new File(currentDir + File.separator + yamlFile);
        } else {
            file = new File(yamlFile);
        }

        // TODO: debug
        println("groovy version=${GroovySystem.version}")
        printf("currentDir=%s, yamlFile=%s, file=%s\n", currentDir, yamlFile, file.toString())

        Yaml yaml = new Yaml()
        InputStream input = new FileInputStream(file)

        try{
            def content = yaml.load(input)
            def values = content.get(key)
            values.collect { it.toString() }

        } finally {
            input.close()
        }
    }
}
