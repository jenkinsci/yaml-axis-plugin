package org.jenkinsci.plugins.yamlaxis.util;

import hudson.matrix.MatrixBuild;
import org.apache.commons.lang.exception.ExceptionUtils;

public final class BuildUtils {
    private BuildUtils() {
    }

    public static void log(MatrixBuild.MatrixBuildExecution execution, String message) {
        execution.getListener().getLogger().println(message);
    }

    public static void log(MatrixBuild.MatrixBuildExecution execution, String message, Throwable e) {
        String errorMessage = ExceptionUtils.getStackTrace(e);
        log(execution, message + "\n" + errorMessage);
    }
}
