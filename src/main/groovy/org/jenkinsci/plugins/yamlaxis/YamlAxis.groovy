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
    public YamlAxis(String name, String valueString, List<String> computedValues) {
        super(name, valueString)
        this.computedValues = computedValues
    }

    @Override
    List<String> getValues() {
        if(computedValues != null){
            return computedValues
        }

        // NOTE: Plugin can not get workspace location in this method
        YamlLoader loader = new YamlLoader(yamlFile: yamlFile)

        try {
            computedValues = loader.loadValues(name)
            computedValues
        } catch (IOException){
            LOGGER.log(Level.SEVERE, "Can not read yamlFile: ${yamlFile}")
            []
        }
    }

    @Override
    public List<String> rebuild(MatrixBuild.MatrixBuildExecution context) {
        FilePath workspace = context.getBuild().getModuleRoot()
        YamlLoader loader = new YamlLoader(yamlFile: yamlFile, workspace: workspace)

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
        /**
         * Overridden to create a new instance of our Axis extension from UI
         * values.
         * @see hudson.model.Descriptor#newInstance(org.kohsuke.stapler.StaplerRequest,
         * net.sf.json.JSONObject )
         */
        @Override
        public Axis newInstance(StaplerRequest req, JSONObject formData) {
            String name = formData.getString("name")
            String yamlFile = formData.getString("valueString")
            new YamlAxis(name, yamlFile, null)
        }

        /**
         * Overridden to provide our own display name.
         * @see hudson.model.Descriptor#getDisplayName()
         */
        @Override
        public String getDisplayName() {
            "Yaml Axis"
        }

        public FormValidation doCheckValueString(@QueryParameter String value) {
            if(Util.fixEmpty(value) == null) {
                return FormValidation.error("Axis yaml file can not be empty")
            }
            FormValidation.ok()
        }
    }
}
