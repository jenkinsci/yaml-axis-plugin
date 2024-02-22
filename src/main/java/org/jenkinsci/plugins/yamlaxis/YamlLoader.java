package org.jenkinsci.plugins.yamlaxis;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

public abstract class YamlLoader {

    public List<String> loadStrings(String key) {
        Map<String, Object> content = getContent();
        Object values = content.get(key);
        if (values == null) {
            return new ArrayList<>();
        }
        return ((List<?>) values).stream()
                .map(Object::toString)
                .collect(Collectors.toList());
    }

    /**
     *
     * @param key
     * @return if key is not found, return null
     */
    public List<Map<String, ?>> loadMaps(String key) {
        Map<String, Object> content = getContent();
        Object values = content.get(key);
        if (values == null) {
            return null;
        }
        return ((List<?>) values).stream()
                .map(value -> {
                    if (value instanceof Map) {
                        return ((Map<?, ?>) value).entrySet().stream()
                                .collect(Collectors.toMap(
                                        entry -> entry.getKey().toString(),
                                        entry -> {
                                            if (entry.getValue() instanceof List) {
                                                return ((List<?>) entry.getValue()).stream()
                                                        .map(Object::toString)
                                                        .collect(Collectors.toList());
                                            } else {
                                                return entry.getValue().toString();
                                            }
                                        }
                                ));
                    } else {
                        return new HashMap<String, Object>();
                    }
                })
                .collect(Collectors.toList());
    }

    public abstract Map<String, Object> getContent();
}
