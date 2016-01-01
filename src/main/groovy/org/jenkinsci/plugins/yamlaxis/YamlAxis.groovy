package org.jenkinsci.plugins.yamlaxis
import groovy.transform.InheritConstructors
import hudson.Extension
import hudson.matrix.Axis
import hudson.matrix.AxisDescriptor
import hudson.util.FormValidation
import net.sf.json.JSONObject
import org.kohsuke.stapler.QueryParameter
import org.kohsuke.stapler.StaplerRequest

@InheritConstructors
class YamlAxis extends Axis {
    @Override
    List<String> getValues() {
        YamlLoader.loadValues(yamlFile(), name)
    }

    String yamlFile(){
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
            new YamlAxis(name, yamlFile)
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
            if(value == null || value == "") {
                return FormValidation.error("Axis yaml file can not be empty")
            }
            FormValidation.ok()
        }
    }
}
