package org.jenkinsci.plugins.yamlaxis
import groovy.transform.TupleConstructor
import hudson.FilePath
import hudson.Util
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.SafeConstructor

@TupleConstructor
class YamlFileLoader extends YamlLoader {
    static final String RADIO_VALUE = "file"

    String yamlFile
    FilePath workspace

    @Override
    Map getContent() {
        if(Util.fixEmpty(yamlFile) == null) {
            return null
        }

        Yaml yaml = new Yaml(new SafeConstructor())
        InputStream input = createFilePath().read()

        try{
            yaml.load(input)

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
