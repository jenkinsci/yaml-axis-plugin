package org.jenkinsci.plugins.yamlaxis;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.FilePath;
import hudson.matrix.Combination;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixExecutionStrategyDescriptor;
import hudson.util.FormValidation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.yamlaxis.util.BuildUtils;
import org.jenkinsci.plugins.yamlaxis.util.DescriptorUtils;
import org.jenkinsci.plugins.yamlaxis.util.MatrixUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest2;

public class YamlMatrixExecutionStrategy extends BaseMES {
  private String yamlType = YamlFileLoader.RADIO_VALUE;
  private String yamlFile;
  private String yamlText;
  private String excludeKey;
  private volatile List<Combination> excludes = null;

  @DataBoundConstructor
  public YamlMatrixExecutionStrategy(
      String yamlType, String yamlText, String yamlFile, String excludeKey) {
    this.yamlType = yamlType;
    this.yamlText = yamlText;
    this.yamlFile = yamlFile;
    this.excludeKey = excludeKey;
  }

  public YamlMatrixExecutionStrategy(List<Combination> excludes) {
    this.excludes = excludes;
  }

  @Override
  public Map<String, List<Combination>> decideOrder(
      MatrixBuild.MatrixBuildExecution execution, List<Combination> comb) {
    List<Combination> excludeCombinations = loadExcludes(execution);
    List<Combination> combinations = MatrixUtils.reject(comb, excludeCombinations);
    BuildUtils.log(execution, "excludes=" + excludeCombinations);
    return new HashMap<>() {
	    {
		    put("YamlMatrixExecutionStrategy", combinations);
	    }
    };
  }

  public boolean isYamlTypeFile() {
    return yamlType.equals(YamlFileLoader.RADIO_VALUE);
  }

  public boolean isYamlTypeText() {
    return yamlType.equals(YamlTextLoader.RADIO_VALUE);
  }

  private List<Combination> loadExcludes(MatrixBuild.MatrixBuildExecution execution) {
    if (excludes != null) {
      return excludes;
    }
    try {
      List<Map<String, ?>> values = getYamlLoader(execution).loadMaps(excludeKey);
      if (values == null) {
        BuildUtils.log(execution, "[WARN] NotFound excludeKey " + excludeKey);
        return new ArrayList<>();
      }
      return collectExcludeCombinations(values);
    } catch (Exception e) {
      BuildUtils.log(execution, "[WARN] Can not read yamlFile: " + yamlFile, e);
      return new ArrayList<>();
    }
  }

  @SuppressWarnings("unchecked")
  public static List<Combination> collectExcludeCombinations(List<Map<String, ?>> excludes) {
    List<Map<String, String>> result = new ArrayList<>();
    for (Map<String, ?> value : excludes) {
      List<Map<String, String>> combos = new ArrayList<>();
      boolean isList = false;
      for (Map.Entry<String, ?> entry : value.entrySet()) {
        if (entry.getValue() instanceof List) {
          isList = true;
          List<Map<String, String>> newCombos = new ArrayList<>();
          for (Object v : (List) entry.getValue()) {
            if (!combos.isEmpty()) {
              for (Map<String, String> c : combos) {
                Map<String, String> clone = new HashMap<>(c);
                clone.put(entry.getKey(), (String) v);
                newCombos.add(clone);
              }
            } else {
              newCombos.add(
		              new HashMap<>() {
			              {
				              put(entry.getKey(), (String) v);
			              }
		              });
            }
          }
          combos = newCombos;
        }
      }
      if (isList) {
        for (Map.Entry<String, ?> entry : value.entrySet()) {
          if (entry.getValue() instanceof String) {
            for (Map<String, String> c : combos) {
              c.put(entry.getKey(), (String) entry.getValue());
            }
          }
        }
      } else if (value.values().stream().allMatch(v -> v instanceof String)) {
        combos.add((Map<String, String>) value);
      }
      result.addAll(combos);
    }
    List<Combination> combinations = new ArrayList<>();
    for (Map<String, String> map : result) {
      combinations.add(new Combination(map));
    }
    return combinations;
  }

  private YamlLoader getYamlLoader(MatrixBuild.MatrixBuildExecution execution) {
	  return switch (yamlType) {
		  case YamlFileLoader.RADIO_VALUE -> {
			  FilePath workspace = execution.getBuild().getModuleRoot();
			  yield new YamlFileLoader(yamlFile, workspace);
		  }
		  case YamlTextLoader.RADIO_VALUE -> new YamlTextLoader(yamlText);
		  default -> throw new IllegalArgumentException(yamlType + " is unknown");
	  };
  }

  @Extension
  public static class DescriptorImpl extends MatrixExecutionStrategyDescriptor {
    @NonNull
    @Override
    public String getDisplayName() {
      return "Yaml matrix execution strategy";
    }

    @Override
    public YamlMatrixExecutionStrategy newInstance(StaplerRequest2 req, @NonNull JSONObject formData) {
      String yamlType = formData.getString("yamlType");
      String yamlFile = formData.getString("yamlFile");
      String yamlText = formData.getString("yamlText");
      String excludeKey = formData.getString("excludeKey");
      return new YamlMatrixExecutionStrategy(yamlType, yamlText, yamlFile, excludeKey);
    }

    public FormValidation doCheckYamlFile(@QueryParameter String value) {
      return DescriptorUtils.checkFieldNotEmpty(value, "yamlFile");
    }

    public FormValidation doCheckYamlText(@QueryParameter String value) {
      return DescriptorUtils.checkFieldNotEmpty(value, "yamlText");
    }
  }
}
