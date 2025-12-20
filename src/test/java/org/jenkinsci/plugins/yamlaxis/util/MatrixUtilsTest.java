package org.jenkinsci.plugins.yamlaxis.util;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@DisplayName("MatrixUtilsTest")
class MatrixUtilsTest {
  @Nested
  @DisplayName("ContainsTest")
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  class ContainsTest {
    Stream<Arguments> provideContainData() {
      return Stream.of(
        arguments(
          Map.of("a", 1, "b", 2, "c", 3),
          Map.of("a", 1, "b", 2, "c", 3),
          true
        ),
        arguments(
          Map.of("a", 1, "b", 2, "c", 3),
          Map.of("a", 1, "c", 3),
          true
        ),
        arguments(
          Map.of("a", 1, "b", 2, "c", 3),
          Map.of("a", 1, "c", 4),
          false
        ),
        arguments(
          Map.of("a", 1, "b", 2, "c", 3),
          Map.of("a", 1, "c", 3, "d", 4),
          false
        )
      );
    }

    @ParameterizedTest(name = "[{index}] parent={0}, child={1}, expected={2}")
    @MethodSource("provideContainData")
    void testContains(Map<String, Integer> parent, Map<String, Integer> child, boolean expected) {
      assertEquals(expected, MatrixUtils.contains(parent, child));
    }
  }

  @Nested
  @DisplayName("RejectTest")
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  class RejectTest {
    private List<Map<String, Integer>> variables;

    @BeforeEach
    void setUp() {
      variables = List.of(
        Map.of("a", 1,  "b", 2, "c", 3),
        Map.of("a", 11, "b", 2, "c", 3),
        Map.of("a", 21, "b", 2, "c", 3)
      );
    }

    Stream<Arguments> provideRejectData() {
      return Stream.of(
        arguments(
          List.of(Map.of("b", 2, "a", 11, "c", 3)),
          List.of(
            Map.of("a", 1, "b", 2, "c", 3),
            Map.of("a", 21, "b", 2, "c", 3)
          )
        ),
        arguments(
          List.of(Map.of("a", 1)),
          List.of(
            Map.of("a", 11, "b", 2, "c", 3),
            Map.of("a", 21, "b", 2, "c", 3)
          )
        ),
        arguments(
          List.of(Map.of("b", 2)),
          List.of()
        )
      );
    }

    @ParameterizedTest(name = "[{index}] excludes={0}, expected={1}")
    @MethodSource("provideRejectData")
    void testReject(List<Map<String, Integer>> excludes, List<Map<String, Integer>> expected) {
      assertEquals(expected, MatrixUtils.reject(variables, excludes));
    }
  }
}
