package org.jenkinsci.plugins.yamlaxis;

import hudson.FilePath;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@DisplayName("YamlFileLoaderTest")
class YamlFileLoaderTest {
  private static final String CURRENT_DIR = System.getProperty("user.dir");
  private static final String RELATIVE_YAML_FILE = "src/test/resources/axis.yml";
  private static final String ABSOLUTE_YAML_FILE = CURRENT_DIR + File.separator + RELATIVE_YAML_FILE;

  private FilePath workspace;

  @BeforeEach
  void setUpCommon() {
    this.workspace = new FilePath(new File(CURRENT_DIR));
  }

  @Nested
  @DisplayName("LoadStringsTest")
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  class LoadStringsTest {
    Stream<Arguments> provideLoadStringsData() {
      return Stream.of(
        arguments(RELATIVE_YAML_FILE, "STRING_VALUE", List.of("a", "b", "c")),
        arguments(RELATIVE_YAML_FILE, "INT_VALUE",    List.of("1", "2", "3")),
        arguments(RELATIVE_YAML_FILE, "BOOL_VALUE",   List.of("true", "false")),
        arguments(RELATIVE_YAML_FILE, "UNKNOWN",      List.of()),
        arguments(ABSOLUTE_YAML_FILE, "STRING_VALUE", List.of("a", "b", "c"))
      );
    }

    @ParameterizedTest(name = "yaml={0}, key={1}, expected={2}")
    @MethodSource("provideLoadStringsData")
    void testLoadStrings(String yamlFile, String key, List<String> expected) throws Exception {
      YamlFileLoader loader = new YamlFileLoader(yamlFile, workspace);

      assertEquals(expected, loader.loadStrings(key));
    }
  }

  @Nested
  @DisplayName("LoadMapsTest")
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  class LoadMapsTest {
    Stream<Arguments> provideLoadMapsData() {
      return Stream.of(
        arguments(
          RELATIVE_YAML_FILE,
          "exclude",
          List.of(
            Map.of("a", "1", "b", "2"),
            Map.of("c", "3")
          )
        ),
        arguments(RELATIVE_YAML_FILE, "not_found", null)
      );
    }

    @ParameterizedTest(name = "yaml={0}, key={1}, expected={2}")
    @MethodSource("provideLoadMapsData")
    void testLoadMaps(String yamlFile, String key, List<Map<String, String>> expected) throws Exception {
      YamlFileLoader loader = new YamlFileLoader(yamlFile, workspace);

      assertEquals(expected, loader.loadMaps(key));
    }
  }
}
