package eu.algites.lib.action.execution.engine;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Structural Java API regression tests, executed by the standard TestNG Gradle test task.
 * Behavioral tests are required when an implementation is introduced.
 */
public class AItcActionExecutionContractTest {

    @Test
    public void actionExecutorHasTypedInputOutputAttemptAndContext() {
        Assert.assertEquals(AIiActionExecutor.class.getTypeParameters().length, 4);
    }

    @Test
    public void planAndServiceAreDistinctContracts() {
        Assert.assertFalse(AIiActionExecutionService.class.isAssignableFrom(AIiActionExecutionPlan.class));
        Assert.assertFalse(AIiActionExecutionPlan.class.isAssignableFrom(AIiActionExecutionService.class));
    }

    @Test
    public void executionPolicyNeverContainsEnableToggleOrEndpointCapacity() {
        for (java.lang.reflect.Method locMethod : AIiActionExecutionPolicy.class.getDeclaredMethods()) {
            Assert.assertFalse(locMethod.getName().toLowerCase().contains("enabled"));
            Assert.assertFalse(locMethod.getName().toLowerCase().contains("concurrent"));
        }
    }
}
