package org.jenkinsci.plugins.yamlaxis.util;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class MatrixUtils {
  private MatrixUtils() {}

  // Whether parent map contains all entries of child map
  public static <T extends Map<?, ?>> boolean contains(T parent, T child) {
    return child.entrySet().stream()
        .allMatch(
            entry ->
                parent.containsKey(entry.getKey())
                    && parent.get(entry.getKey()).equals(entry.getValue()));
  }

  // reject elements in variables if they match any element of excludes
  public static <T extends Map<?, ?>> List<T> reject(List<T> variables, List<T> excludes) {
    if (excludes.isEmpty()) {
      return variables;
    }
    return variables.stream()
        .filter(variable -> excludes.stream().noneMatch(exclude -> contains(variable, exclude)))
        .collect(Collectors.toList());
  }
}
