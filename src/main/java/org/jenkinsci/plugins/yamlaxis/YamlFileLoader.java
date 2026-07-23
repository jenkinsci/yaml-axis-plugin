package org.jenkinsci.plugins.yamlaxis;

import hudson.FilePath;
import hudson.Util;
import hudson.model.Executor;
import hudson.remoting.VirtualChannel;
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

  // Package-private for testing (YamlFileLoaderChannelTest asserts the channel selection).
  FilePath createFilePath() {
    // A relative path is resolved against the workspace, which is bound to the node that ran the
    // checkout (the agent). This case already read from the correct node.
    if (workspace != null && Util.isRelativePath(yamlFile)) {
      return new FilePath(workspace, yamlFile);
    }
    // An absolute path (or one with no workspace) must NOT be read via new FilePath(new File(...)),
    // which has a null channel and therefore always reads on the Jenkins controller JVM. Read it
    // through the channel of the node that holds the workspace / runs the current build instead, so
    // an absolute path points at the agent's filesystem rather than the controller's.
    VirtualChannel channel = resolveChannel();
    if (channel != null) {
      return new FilePath(channel, yamlFile);
    }
    return new FilePath(new File(yamlFile));
  }

  /**
   * Best-effort channel of the node that should serve the file: the workspace's channel when a
   * workspace is available (agent channel for an agent-bound workspace, otherwise the controller's
   * local channel), falling back to the channel of the current build's executor. Returns {@code
   * null} when neither is available so the caller can default to the controller-local filesystem.
   */
  private VirtualChannel resolveChannel() {
    if (workspace != null) {
      return workspace.getChannel();
    }
    Executor executor = Executor.currentExecutor();
    return executor != null ? executor.getOwner().getChannel() : null;
  }
}
