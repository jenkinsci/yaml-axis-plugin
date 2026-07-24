package org.jenkinsci.plugins.yamlaxis;

import static org.junit.jupiter.api.Assertions.assertSame;

import hudson.FilePath;
import hudson.remoting.VirtualChannel;
import hudson.slaves.DumbSlave;
import java.io.File;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

/**
 * Regression tests for reading the yaml file on the node that owns the workspace (typically an
 * agent) instead of on the Jenkins controller.
 *
 * <p>Both cases fail against the previous implementation, which read an absolute path via {@code new
 * FilePath(new File(yamlFile))} — a null-channel {@link FilePath} that always resolves on the
 * controller JVM.
 */
@WithJenkins
@DisplayName("YamlFileLoader resolves on the workspace's node, not the controller")
class YamlFileLoaderChannelTest {

  @Test
  @DisplayName("an absolute path is resolved through the agent channel, not the controller")
  void absolutePathUsesWorkspaceChannel(JenkinsRule j) throws Exception {
    DumbSlave agent = j.createOnlineSlave();
    VirtualChannel agentChannel = agent.getChannel();
    FilePath workspace = new FilePath(agentChannel, agent.getRemoteFS());

    String absolute = new File(agent.getRemoteFS(), "axis.yml").getAbsolutePath();
    YamlFileLoader loader = new YamlFileLoader(absolute, workspace);

    assertSame(agentChannel, loader.createFilePath().getChannel());
  }

  @Test
  @DisplayName("a relative path stays resolved against the agent workspace channel")
  void relativePathUsesWorkspaceChannel(JenkinsRule j) throws Exception {
    DumbSlave agent = j.createOnlineSlave();
    VirtualChannel agentChannel = agent.getChannel();
    FilePath workspace = new FilePath(agentChannel, agent.getRemoteFS());

    YamlFileLoader loader = new YamlFileLoader("axis.yml", workspace);

    assertSame(agentChannel, loader.createFilePath().getChannel());
  }
}
