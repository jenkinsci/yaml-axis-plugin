package org.jenkinsci.plugins.yamlaxis

import hudson.Extension
import hudson.FilePath
import hudson.matrix.Combination
import hudson.matrix.MatrixBuild
import hudson.matrix.MatrixExecutionStrategyDescriptor
import hudson.util.FormValidation
import net.sf.json.JSONObject
import org.jenkinsci.plugins.yamlaxis.util.BuildUtils
import org.jenkinsci.plugins.yamlaxis.util.DescriptorUtils
import org.jenkinsci.plugins.yamlaxis.util.MatrixUtils
import org.kohsuke.stapler.DataBoundConstructor
import org.kohsuke.stapler.QueryParameter
import org.kohsuke.stapler.StaplerRequest

class YamlMatrixExecutionStrategy extends BaseMES {
    String yamlType = YamlFileLoader.RADIO_VALUE
    String yamlFile
    String yamlText
    String excludeKey

    private volatile List<Combination> excludes = null

    @DataBoundConstructor
    YamlMatrixExecutionStrategy(String yamlType, String yamlText, String yamlFile, String excludeKey){
        this.yamlType = yamlType
        this.yamlText = yamlText
        this.yamlFile = yamlFile
        this.excludeKey = excludeKey
    }

    YamlMatrixExecutionStrategy(List<Combination> excludes){
        this.excludes = excludes
    }

    @Override
    Map decideOrder(MatrixBuild.MatrixBuildExecution execution, List<Combination> comb) {
        List<Combination> excludeCombinations = loadExcludes(execution)
        List<Combination> combinations = MatrixUtils.reject(comb, excludeCombinations)

        BuildUtils.log(execution, "excludes=${excludeCombinations}")
        ["YamlMatrixExecutionStrategy": combinations]
    }

    boolean isYamlTypeFile(){
        yamlType == YamlFileLoader.RADIO_VALUE
    }

    boolean isYamlTypeText(){
        yamlType == YamlTextLoader.RADIO_VALUE
    }

    private List<Combination> loadExcludes(MatrixBuild.MatrixBuildExecution execution){
        if(excludes != null){
            return excludes
        }

        try{
            List<Map<String, String>> values = getYamlLoader(execution).loadMaps(excludeKey)
            if(values == null){
                BuildUtils.log(execution, "[WARN] NotFound excludeKey ${excludeKey}")
                return []
            }
            collectExcludeCombinations(values)

        } catch (IOException e) {
            BuildUtils.log(execution, "[WARN] Can not read yamlFile: ${yamlFile}", e)
            []
        }
    }

    public static List<Combination> collectExcludeCombinations(List<Map<String, ?>> excludes) {
        List<Map<String, String>> result = []
        for (value in excludes) {
            List<Map<String, String>> combos = []
            boolean isList = false
            for (Map.Entry<String, ?> entry in value) {
                if (entry.value instanceof List) {
                    isList = true
                    List<Map<String, String>> newCombos = []
                    for (def v in entry.value) {
                        if (combos) {
                            for (def c in combos) {
                                Map<String, String> clone = new HashMap<>(c)
                                clone.put(entry.key, v)
                                newCombos.add(clone)
                            }
                        } else {
                            newCombos.add([(entry.key): v])
                        }
                    }
                    combos = newCombos
                }
            }
            if (isList) {
                for (Map.Entry<String, ?> entry in value) {
                    if (entry.value instanceof String) {
                        for (def c in combos) {
                            c.put(entry.key, entry.value)
                        }
                    }
                }
            } else {
                combos.add(value)
            }
            result.addAll(combos)
        }
        result.collect { new Combination(it) }
    }

    private YamlLoader getYamlLoader(MatrixBuild.MatrixBuildExecution execution){
        switch(yamlType){
        case YamlFileLoader.RADIO_VALUE:
            FilePath workspace = execution.getBuild().getModuleRoot()
            return new YamlFileLoader(yamlFile: yamlFile, workspace: workspace)
        case YamlTextLoader.RADIO_VALUE:
            return new YamlTextLoader(yamlText: yamlText)
        default:
            throw new IllegalArgumentException("${yamlType} is unknown")
        }
    }

    @Extension
    static class DescriptorImpl extends MatrixExecutionStrategyDescriptor {
        final String displayName = 'Yaml matrix execution strategy'

        @Override
        YamlMatrixExecutionStrategy newInstance(StaplerRequest req, JSONObject formData) {
            String yamlType = formData.getString("yamlType")
            String yamlFile = formData.getString("yamlFile")
            String yamlText = formData.getString("yamlText")
            String excludeKey = formData.getString("excludeKey")
            new YamlMatrixExecutionStrategy(yamlType, yamlText, yamlFile, excludeKey)
        }

        FormValidation doCheckYamlFile(@QueryParameter String value) {
            DescriptorUtils.checkFieldNotEmpty(value, "yamlFile")
        }

        FormValidation doCheckYamlText(@QueryParameter String value) {
            DescriptorUtils.checkFieldNotEmpty(value, "yamlText")
        }
    }
}
