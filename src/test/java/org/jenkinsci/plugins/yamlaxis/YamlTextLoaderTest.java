package org.jenkinsci.plugins.yamlaxis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@DisplayName("YamlTextLoaderTest")
class YamlTextLoaderTest {
  @Nested
  @DisplayName("LoadStringsTest")
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  class LoadStringsTest {
    private YamlTextLoader loader;

    @BeforeEach
    void setUp() {
      String yamlText = """
                STRING_VALUE:
                  - a
                  - b
                  - c
                INT_VALUE:
                  - 1
                  - 2
                  - 3
                BOOL_VALUE:
                  - true
                  - false
                """;
      loader = new YamlTextLoader(yamlText);
    }

    @ParameterizedTest(name = "[{index}] key={0}, expected={1}")
    @MethodSource("provideLoadStringsData")
    void testLoadStrings(String key, List<String> expected) {
      assertEquals(expected, loader.loadStrings(key));
    }

    Stream<Arguments> provideLoadStringsData() {
      return Stream.of(
        arguments("STRING_VALUE", List.of("a", "b", "c")),
        arguments("INT_VALUE",    List.of("1", "2", "3")),
        arguments("BOOL_VALUE",   List.of("true", "false")),
        arguments("UNKNOWN",      List.of()),
        arguments("STRING_VALUE", List.of("a", "b", "c"))
      );
    }
  }

  @Nested
  @DisplayName("LoadValuesTest")
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  class LoadValuesTest {
    private YamlTextLoader loader;

    @BeforeEach
    void setUp() {
      String yamlText = """
                exclude:
                  - a: 1
                    b: 2
                  - c: 3
                """;
      loader = new YamlTextLoader(yamlText);
    }

    @ParameterizedTest(name = "[index] key={0}")
    @MethodSource("provideLoadValuesData")
    void testLoadValues(String key, List<Map<String, String>> expected) {
      assertEquals(expected, loader.loadMaps(key));
    }

    Stream<Arguments> provideLoadValuesData() {
      return Stream.of(
        arguments("exclude", List.of(
          Map.of("a", "1", "b", "2"),
          Map.of("c", "3")
        )),
        arguments("not_found", null)
      );
    }
  }

  @Nested
  @DisplayName("LoadValuesListTest")
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  class LoadValuesListTest {
    private YamlTextLoader loader;

    @BeforeEach
    void setUp() {
      String yamlText = """
                exclude:
                  - a: 1
                    b:
                      - 2
                      - 3
                  - c: 4
                """;
      loader = new YamlTextLoader(yamlText);
    }

    @ParameterizedTest(name = "{index} => key={0}")
    @MethodSource("provideLoadValuesListData")
    void testLoadValuesList(String key, List<Map<String, Object>> expected) {
      assertEquals(expected, loader.loadMaps(key));
    }

    Stream<Arguments> provideLoadValuesListData() {
      return Stream.of(
        arguments("exclude",
          List.of(
            Map.of("a", "1", "b", List.of("2", "3")),
            Map.of("c", "4")
          )
        ),
        arguments("not_found", null)
      );
    }
  }
}
