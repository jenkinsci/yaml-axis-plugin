package org.jenkinsci.plugins.yamlaxis;

import hudson.FilePath;
import hudson.Util;
import java.io.File;
import java.io.InputStream;
import java.util.Map;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

public class YamlFileLoader extends YamlLoader {
  static final String RADIO_VALUE = "file";
  private String yamlFile;
  private FilePath workspace;

  public YamlFileLoader(String yamlFile, FilePath workspace) {
    this.yamlFile = yamlFile;
    this.workspace = workspace;
  }

  @Override
  public Map<String, Object> getContent() {
    if (Util.fixEmpty(yamlFile) == null) {
      return null;
    }
    Yaml yaml = new Yaml(new SafeConstructor(new LoaderOptions()));
    try (InputStream input = createFilePath().read()) {
      return yaml.load(input);
    } catch (Exception e) {
      return null;
    }
  }

  private FilePath createFilePath() {
    if (!Util.isRelativePath(yamlFile) || workspace == null) {
      return new FilePath(new File(yamlFile));
    }
    return new FilePath(workspace, yamlFile);
  }
}
