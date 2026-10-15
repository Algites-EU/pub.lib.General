package eu.algites.lib.action.execution.resource;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Structural Java API regression tests, executed by the standard TestNG Gradle test task.
 * Behavioral tests are required when an implementation is introduced.
 */
public class AItcExecutionResourceContractTest {

    @Test
    public void capacityAcquisitionIsNonBlockingAndGrouped() throws Exception {
        java.lang.reflect.Method locMethod = AIiExecutionResourceCoordinator.class.getMethod(
                "tryReserve", java.util.List.class);
        Assert.assertEquals(locMethod.getReturnType(), AIiExecutionResourceLease.class);
    }

    @Test
    public void endpointOwnsResourceDefinitions() throws Exception {
        Assert.assertNotNull(AIiExecutionResourceEndpoint.class.getMethod("getExecutionResources"));
        Assert.assertNotNull(AIiExecutionResourceEndpoint.class.getMethod(
                "getRequirementsForOperation", String.class));
    }
}
