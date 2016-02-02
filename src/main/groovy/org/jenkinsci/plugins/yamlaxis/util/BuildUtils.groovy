package org.jenkinsci.plugins.yamlaxis.util

import hudson.matrix.MatrixBuild
import org.apache.commons.lang.exception.ExceptionUtils

final class BuildUtils {
    private BuildUtils(){
    }

    static void log(MatrixBuild.MatrixBuildExecution execution, String message){
        execution.getListener().getLogger().println(message)
    }

    static void log(MatrixBuild.MatrixBuildExecution execution, String message, Throwable e){
        String errorMessage = ExceptionUtils.getFullStackTrace(e)
        log(execution, message + "\n" + errorMessage)
    }
}
