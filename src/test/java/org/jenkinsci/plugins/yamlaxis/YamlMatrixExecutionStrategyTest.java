package org.jenkinsci.plugins.yamlaxis;

import hudson.matrix.AxisList;
import hudson.matrix.Combination;
import hudson.matrix.MatrixProject;
import hudson.matrix.TextAxis;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.assertj.core.api.Assertions.assertThat;

@WithJenkins
@DisplayName("YamlMatrixExecutionStrategyTest")
class YamlMatrixExecutionStrategyTest {
  private MatrixProject configure(JenkinsRule rule) throws Exception {
    MatrixProject matrixProject = rule.createProject(MatrixProject.class);

    TextAxis axis1 = new TextAxis("axis1", List.of("a", "b", "c"));
    TextAxis axis2 = new TextAxis("axis2", List.of("x", "y", "z"));
    AxisList axl = new AxisList();

    axl.add(axis1);
    axl.add(axis2);

    matrixProject.setAxes(axl);
    return matrixProject;
  }

  @Nested
  @DisplayName("RunTest")
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  class RunTest {
    private MatrixProject matrixProject;

    @BeforeEach
    void setUp(JenkinsRule rule) throws Exception {
      this.matrixProject = configure(rule);
    }

    @ParameterizedTest(name = "[{index}] excludes={0}, runsCount={1}")
    @MethodSource("provideRunData")
    void testRun(List<Map<String, ?>> excludes, int runsCount) throws Exception {
      List<Combination> excludeCombinations = YamlMatrixExecutionStrategy.collectExcludeCombinations(excludes);
      matrixProject.setExecutionStrategy(new YamlMatrixExecutionStrategy(excludeCombinations));

      var build = matrixProject.scheduleBuild2(0).get();

      String logText = JenkinsRule.getLog(build);
      assertThat(logText).contains("SUCCESS");

      build.getRuns().forEach(run -> {
        try {
          String runLog = JenkinsRule.getLog(run);
          assertThat(runLog).contains("SUCCESS");
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      });

      assertEquals(runsCount, build.getRuns().size());
    }

    Stream<Arguments> provideRunData() {
      return Stream.of(
        arguments(List.of(), 9),
        arguments(List.of(Map.of("axis1", "c", "axis2", "z")), 8),
        arguments(List.of(Map.of("axis1", "c", "axis2", List.of("z"))), 8),
        arguments(List.of(Map.of("axis1", List.of("c"), "axis2", "z")), 8),
        arguments(List.of(Map.of("axis1", List.of("c"), "axis2", List.of("z"))), 8),
        arguments(List.of(Map.of("axis1", "c")), 6),
        arguments(List.of(Map.of("axis1", List.of("b", "c"))), 3),
        arguments(List.of(Map.of("axis1", "b"), Map.of("axis1", "c")), 3),
        arguments(List.of(Map.of("axis1", "b", "axis2", List.of("x", "y"))), 7),
        arguments(List.of(Map.of("axis1", List.of("a", "b"), "axis2", List.of("x", "y"))), 5)
      );
    }
  }
}
