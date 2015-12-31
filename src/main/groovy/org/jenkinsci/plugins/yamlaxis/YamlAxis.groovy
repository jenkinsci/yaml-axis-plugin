package org.jenkinsci.plugins.yamlaxis
import hudson.Extension
import hudson.matrix.Axis
import hudson.matrix.AxisDescriptor
import hudson.util.FormValidation
import net.sf.json.JSONObject
import org.kohsuke.stapler.DataBoundConstructor
import org.kohsuke.stapler.QueryParameter
import org.kohsuke.stapler.StaplerRequest

class YamlAxis extends Axis {
    @DataBoundConstructor
    YamlAxis(String name, String valueString) {
        super(name, valueString)
    }

    @Override
    List<String> getValues() {
        // TODO
        ["value1", "value2"]
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
            new YamlAxis(formData.getString("name"), formData.getString("valueString"))
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
        }
    }
}
