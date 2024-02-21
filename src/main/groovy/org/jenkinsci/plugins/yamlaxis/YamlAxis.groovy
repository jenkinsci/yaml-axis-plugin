package org.jenkinsci.plugins.yamlaxis

import hudson.Extension
import hudson.FilePath
import hudson.matrix.Axis
import hudson.matrix.AxisDescriptor
import hudson.matrix.MatrixBuild
import hudson.util.FormValidation
import net.sf.json.JSONObject
import org.jenkinsci.plugins.yamlaxis.util.BuildUtils
import org.jenkinsci.plugins.yamlaxis.util.DescriptorUtils
import org.kohsuke.stapler.DataBoundConstructor
import org.kohsuke.stapler.QueryParameter
import org.kohsuke.stapler.StaplerRequest

class YamlAxis extends Axis {
    private List<String> computedValues = null

    @DataBoundConstructor
    YamlAxis(String name, String valueString, List<String> computedValues) {
        super(name, valueString)
        this.computedValues = computedValues
    }

    @Override
    List<String> getValues() {
        if(computedValues != null){
            return computedValues
        }

        // NOTE: Plugin can not get workspace location in this method
        YamlLoader loader = new YamlFileLoader(yamlFile: yamlFile)

        try {
            computedValues = loader.loadStrings(name)
            computedValues
        } catch (IOException){
            []
        }
    }

    @Override
    List<String> rebuild(MatrixBuild.MatrixBuildExecution context) {
        FilePath workspace = context.getBuild().getModuleRoot()
        YamlLoader loader = new YamlFileLoader(yamlFile: yamlFile, workspace: workspace)

        try {
            computedValues = loader.loadStrings(name)
            computedValues
        } catch (IOException e){
            BuildUtils.log(context, "[WARN] Can not read yamlFile: ${yamlFile}", e)
            []
        }
    }

    String getYamlFile(){
        valueString
    }

    /**
     * Descriptor for this plugin.
     */
    @Extension
    static class DescriptorImpl extends AxisDescriptor {
        final String displayName = "Yaml Axis"

        /**
         * Overridden to create a new instance of our Axis extension from UI
         * values.
         * @see hudson.model.Descriptor#newInstance(org.kohsuke.stapler.StaplerRequest,
         * net.sf.json.JSONObject )
         */
        @Override
        Axis newInstance(StaplerRequest req, JSONObject formData) {
            String name = formData.getString("name")
            String yamlFile = formData.getString("valueString")
            new YamlAxis(name, yamlFile, null)
        }

        FormValidation doCheckValueString(@QueryParameter String value) {
            DescriptorUtils.checkFieldNotEmpty(value, "valueStrng")
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true
        if (!(o instanceof YamlAxis)) return false

        YamlAxis yamlAxis = (YamlAxis) o
        if (computedValues != null ? !computedValues.equals(yamlAxis.computedValues) : yamlAxis.computedValues != null)
            return false

        true
    }

    @Override public int hashCode() {
        return computedValues ? computedValues.hashCode() : 0;
    }
}
