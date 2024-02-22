package org.jenkinsci.plugins.yamlaxis;

import hudson.Extension;
import hudson.FilePath;
import hudson.matrix.Axis;
import hudson.matrix.AxisDescriptor;
import hudson.matrix.MatrixBuild;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.yamlaxis.util.BuildUtils;
import org.jenkinsci.plugins.yamlaxis.util.DescriptorUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class YamlAxis extends Axis {
    private List<String> computedValues = null;

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
            computedValues = loader.loadStrings(name);
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
            computedValues = loader.loadStrings(name);
            return computedValues;
        } catch (Exception e) {
            BuildUtils.log(context, "[WARN] Cannot read yamlFile: " + getYamlFile(), e);
            return List.of();
        }
    }

    public String getYamlFile() {
        return getValueString();
    }

    /**
     * Descriptor for this plugin.
     */
    @Extension
    public static class DescriptorImpl extends AxisDescriptor {
        @Override
        public String getDisplayName() {
            return "Yaml Axis";
        }

        /**
         * Overridden to create a new instance of our Axis extension from UI
         * values.
         */
        @Override
        public Axis newInstance(StaplerRequest req, JSONObject formData) {
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
        if (!(o instanceof YamlAxis)) return false;
        YamlAxis yamlAxis = (YamlAxis) o;
        return computedValues != null ? computedValues.equals(yamlAxis.computedValues) : yamlAxis.computedValues == null;
    }

    @Override
    public int hashCode() {
        return computedValues != null ? computedValues.hashCode() : 0;
    }
}
