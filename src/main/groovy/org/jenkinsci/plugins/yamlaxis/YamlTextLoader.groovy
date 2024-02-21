package org.jenkinsci.plugins.yamlaxis
import groovy.transform.TupleConstructor
import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.SafeConstructor

@TupleConstructor
class YamlTextLoader extends YamlLoader {
    static final String RADIO_VALUE = "text"

    String yamlText

    @Override
    Map getContent() {
        Yaml yaml = new Yaml(new SafeConstructor(new LoaderOptions()))
        yaml.load(yamlText)
    }
}
