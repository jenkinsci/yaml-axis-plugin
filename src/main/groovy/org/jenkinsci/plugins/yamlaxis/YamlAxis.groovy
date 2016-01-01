package org.jenkinsci.plugins.yamlaxis
import groovy.transform.InheritConstructors
import hudson.Extension
import hudson.matrix.Axis
import hudson.matrix.AxisDescriptor
import hudson.matrix.MatrixBuild
import hudson.model.OneOffExecutor
import hudson.util.FormValidation
import net.sf.json.JSONObject
import org.kohsuke.stapler.QueryParameter
import org.kohsuke.stapler.StaplerRequest

@InheritConstructors
class YamlAxis extends Axis {
    private transient List<String> computedValues = null

    @Override
    List<String> getValues() {
        if(computedValues != null){
            return computedValues
        }

        YamlLoader loader = new YamlLoader(yamlFile: yamlFile(), currentDir: getCurrentWorkspace())

        try {
            // TODO: debug
            println("getValues")
            computedValues = loader.loadValues(name)
            println("getValues=${computedValues}")
            return computedValues
        } catch (IOException){
            println("getValues: IOException")
            return Collections.emptyList()
        }
    }

    @Override
    public List<String> rebuild(MatrixBuild.MatrixBuildExecution context) {
        String workspace = context.getBuild().getWorkspace().getRemote()
        YamlLoader loader = new YamlLoader(yamlFile: yamlFile(), currentDir: workspace)

        try {
            // TODO: debug
            println("rebuild")
            computedValues = loader.loadValues(name)
            println("rebuild=${computedValues}")
            return computedValues;
        } catch (IOException e){
            println("rebuild: IOException")
            e.printStackTrace()
            return Collections.emptyList();
        }
    }

    String yamlFile(){
        valueString
    }

    private String getCurrentWorkspace(){
        try {
            OneOffExecutor thr = (OneOffExecutor) Thread.currentThread();
            return thr.getCurrentWorkspace().getRemote();
        } catch (ClassCastException){
            return "";
        }
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
