package org.jenkinsci.plugins.yamlaxis

abstract class YamlLoader {
    List<String> loadStrings(String key){
        Map content = getContent()
        def values = content.get(key)
        if(values == null){
            return []
        }
        values.collect { it.toString() }
    }

    /**
     *
     * @param key
     * @return if key is not found, return null
     */
    List<Map<String, String>> loadMaps(String key){
        Map content = getContent()
        def values = content.get(key)
        if(values == null){
            return null
        }
        values.collect {
            it.collectEntries { k, v -> [k, v.toString()] }
        }
    }

    abstract Map getContent()
}
