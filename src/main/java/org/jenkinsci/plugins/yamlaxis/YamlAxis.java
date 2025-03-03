package org.jenkinsci.plugins.yamlaxis;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.FilePath;
import hudson.matrix.Axis;
import hudson.matrix.AxisDescriptor;
import hudson.matrix.MatrixBuild;
import hudson.util.FormValidation;
import java.util.List;
import java.util.Objects;

import net.sf.json.JSONObject;
import org.jenkinsci.plugins.yamlaxis.util.BuildUtils;
import org.jenkinsci.plugins.yamlaxis.util.DescriptorUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest2;

public class YamlAxis extends Axis {
  private List<String> computedValues;

  @DataBoundConstructor
  public YamlAxis(String name, String valueString, List<String> computedValues) {
    super(name, valueString);
    this.computedValues = computedValues;
  }

  @Override
  public List<String> getValues() {
    if (computedValues != null) {
      return computedValues;
    }
    // NOTE: Plugin cannot get workspace location in this method
    YamlLoader loader = new YamlFileLoader(getYamlFile(), null);
    try {
      computedValues = loader.loadStrings(getName());
      return computedValues;
    } catch (Exception e) {
      return List.of();
    }
  }

  @Override
  public List<String> rebuild(MatrixBuild.MatrixBuildExecution context) {
    FilePath workspace = context.getBuild().getModuleRoot();
    YamlLoader loader = new YamlFileLoader(getYamlFile(), workspace);
    try {
      computedValues = loader.loadStrings(getName());
      return computedValues;
    } catch (Exception e) {
      BuildUtils.log(context, "[WARN] Cannot read yamlFile: " + getYamlFile(), e);
      return List.of();
    }
  }

  public String getYamlFile() {
    return getValueString();
  }

  /** Descriptor for this plugin. */
  @Extension
  public static class DescriptorImpl extends AxisDescriptor {
    @NonNull
    @Override
    public String getDisplayName() {
      return "Yaml Axis";
    }

    /** Overridden to create a new instance of our Axis extension from UI values. */
    @Override
    public Axis newInstance(StaplerRequest2 req, @NonNull JSONObject formData) {
      String name = formData.getString("name");
      String yamlFile = formData.getString("valueString");
      return new YamlAxis(name, yamlFile, null);
    }

    public FormValidation doCheckValueString(@QueryParameter String value) {
      return DescriptorUtils.checkFieldNotEmpty(value, "valueString");
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof YamlAxis yamlAxis)) return false;
    return Objects.equals(computedValues, yamlAxis.computedValues);
  }

  @Override
  public int hashCode() {
    return computedValues != null ? computedValues.hashCode() : 0;
  }
}
