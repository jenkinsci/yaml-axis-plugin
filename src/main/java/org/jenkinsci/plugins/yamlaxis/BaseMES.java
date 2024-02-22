package org.jenkinsci.plugins.yamlaxis

import hudson.AbortException
import hudson.console.ModelHyperlinkNote
import hudson.matrix.*
import hudson.matrix.MatrixBuild.MatrixBuildExecution
import hudson.matrix.listeners.MatrixBuildListener
import hudson.model.*

import javax.annotation.Nullable

//seems like groovy wants the abstract class too
//import hudson.tasks.test.AggregatedTestResultAction
/**
 * @link https://github.com/jenkinsci/matrix-groovy-execution-strategy-plugin/blob/matrix-groovy-execution-strategy-plugin-1.0.4/src/main/groovy/org/jenkinsci/plugins/BaseMES.groovy
 */
abstract class BaseMES extends MatrixExecutionStrategy {

    @Override
    Result run(MatrixBuild.MatrixBuildExecution execution) throws InterruptedException, IOException {

        //final Collection<MatrixConfiguration> configurations = new HashSet<MatrixConfiguration>()
        List<Combination> combs = []
        Map<Combination, MatrixConfiguration> mc = [:]

        execution.activeConfigurations.each {
            def c = it.combination

            //only add it if the build listeners say so
            if (MatrixBuildListener.buildConfiguration(execution.build, it)) {
                combs << c
                mc[c] = it
            }
        }

        Result r = Result.SUCCESS

        def multiCombs = decideOrder(execution, combs)

        if (notifyStartBuild(execution.aggregators)) {
            return Result.FAILURE
        }

        multiCombs.any { k, v ->

            execution.listener.logger.println("Running ${k}")

            v.each { inner ->
                def mc2 = mc[inner]
                scheduleConfigurationBuild(execution, mc2)
            }

            v.each { inner ->
                def mc2 = mc[inner]

                MatrixRun run = waitForCompletion(execution, mc2)
                notifyEndBuild(run, execution.aggregators)
                execution.listener.logger.println(
                        'Completed ' + ModelHyperlinkNote.encodeTo(mc2) + ' ' + getResult(run))
                r = r.combine(getResult(run))
            }

            //choke if we have a failure
            r == Result.FAILURE
        }
        r
    }

    //override this and return a list of list of combinations
    //and the builds will be run each inner list in parallel then do the next list
    //and if anything fails it stops
    abstract Map decideOrder(MatrixBuild.MatrixBuildExecution execution, List<Combination> comb)

    void scheduleConfigurationBuild(MatrixBuildExecution exec, MatrixConfiguration c) {
        MatrixBuild build = exec.build
        exec.listener.logger.println('Triggering ' + ModelHyperlinkNote.encodeTo(c))

        // filter the parent actions for those that can be passed to the individual jobs.
        List<Action> childActions = new ArrayList<Action>(build.getActions(MatrixChildAction))
        childActions.addAll(build.getActions(ParametersAction)) // used to implement MatrixChildAction
        c.scheduleBuild(childActions, new Cause.UpstreamCause((Run) build))
    }

    MatrixRun waitForCompletion(MatrixBuildExecution exec, MatrixConfiguration c)
            throws InterruptedException, IOException {

        BuildListener listener = exec.listener
        String whyInQueue = ''
        long startTime = System.currentTimeMillis()

        // wait for the completion
        int appearsCancelledCount = 0
        while (true) {
            MatrixRun b = c.getBuildByNumber(exec.build.number)

            // two ways to get beyond this. one is that the build starts and gets done,
            // or the build gets cancelled before it even started.
            if (b != null && !b.isBuilding()) {
                Result buildResult = b.result
                if (buildResult != null) {
                    return b
                }
            }
            Queue.Item qi = c.queueItem
            if (b == null && qi == null) {
                appearsCancelledCount++
            } else {
                appearsCancelledCount = 0
            }

            if (appearsCancelledCount >= 5) {
                // there's conceivably a race condition in computating b and qi, as their computation
                // are not synchronized. There are indeed several reports of Hudson incorrectly assuming
                // builds being cancelled. See
                // http://www.nabble.com/Master-slave-problem-tt14710987.html and also
                // http://www.nabble.com/Anyone-using-AccuRev-plugin--tt21634577.html#a21671389
                // because of this, we really make sure that the build is cancelled by doing this 5
                // times over 5 seconds
                listener.logger.println(ModelHyperlinkNote.encodeTo(c) + ' appears to be cancelled')
                return null
            }

            if (qi != null) {
                // if the build seems to be stuck in the queue, display why
                String why = qi.why
                if (why != null && !why == whyInQueue && System.currentTimeMillis() - startTime > 5000) {
                    listener.logger.print(
                            'Configuration ' + ModelHyperlinkNote.encodeTo(c) + ' is still in the queue: ')
                    qi.causeOfBlockage.print(listener) //this is still shown on the same line
                    whyInQueue = why
                }
            }

            Thread.sleep(1000)
        }
    }

    Result getResult(@Nullable MatrixRun run) {
        // null indicates that the run was cancelled before it even gets going
        run != null ? run.result : Result.ABORTED
    }

    boolean notifyStartBuild(List<MatrixAggregator> aggregators) throws InterruptedException, IOException {
        aggregators.each { a ->
            if (!a.startBuild()) {
                return true
            }
        }
        false
    }

    void notifyEndBuild(MatrixRun b, List<MatrixAggregator> aggregators) throws InterruptedException, IOException {
        if (b == null) {
            return // can happen if the configuration run gets cancelled before it gets started.
        }

        aggregators.each { a ->
            if (!a.endRun(b)) {
                throw new AbortException()
            }
        }
    }

}
