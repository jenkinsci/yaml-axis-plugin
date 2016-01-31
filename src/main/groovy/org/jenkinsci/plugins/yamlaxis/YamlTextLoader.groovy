package org.jenkinsci.plugins.yamlaxis
import groovy.transform.TupleConstructor
import org.yaml.snakeyaml.Yaml

@TupleConstructor
class YamlTextLoader extends YamlLoader {
    static final String RADIO_VALUE = "text"

    String yamlText

    @Override
    Map getContent() {
        Yaml yaml = new Yaml()
        yaml.load(yamlText)
    }
}
