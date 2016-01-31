package org.jenkinsci.plugins.yamlaxis

import hudson.Extension
import hudson.FilePath
import hudson.Util
import hudson.matrix.Axis
import hudson.matrix.AxisDescriptor
import hudson.matrix.MatrixBuild
import hudson.util.FormValidation
import net.sf.json.JSONObject
import org.kohsuke.stapler.DataBoundConstructor
import org.kohsuke.stapler.QueryParameter
import org.kohsuke.stapler.StaplerRequest

import java.util.logging.Level
import java.util.logging.Logger

class YamlAxis extends Axis {
    private static final Logger LOGGER = Logger.getLogger(YamlAxis.class.getName())

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
        OrigYamlLoader loader = new OrigYamlLoader(yamlFile: yamlFile)

        try {
            computedValues = loader.loadValues(name)
            computedValues
        } catch (IOException){
            LOGGER.log(Level.SEVERE, "Can not read yamlFile: ${yamlFile}")
            []
        }
    }

    @Override
    List<String> rebuild(MatrixBuild.MatrixBuildExecution context) {
        FilePath workspace = context.getBuild().getModuleRoot()
        OrigYamlLoader loader = new OrigYamlLoader(yamlFile: yamlFile, workspace: workspace)

        try {
            computedValues = loader.loadValues(name)
            computedValues
        } catch (IOException){
            LOGGER.log(Level.SEVERE, "Can not read yamlFile: ${yamlFile}")
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
            if(Util.fixEmpty(value) == null) {
                return FormValidation.error("Axis yaml file can not be empty")
            }
            FormValidation.ok()
        }
    }
}
