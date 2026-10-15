package eu.algites.lib.action.execution.transaction;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Structural Java API regression tests, executed by the standard TestNG Gradle test task.
 * Behavioral tests are required when an implementation is introduced.
 */
public class AItcTransactionContractTest {

    @Test
    public void afterTransactionActionPreservesAllThreeGenericContracts() {
        Assert.assertEquals(AIiAfterTransactionAction.class.getTypeParameters().length, 3);
    }

    @Test
    public void finalizationExposesThreeDistinctFailureAndCommitPhases() throws Exception {
        Assert.assertEquals(AIiTransactionFinalizationAction.class.getTypeParameters().length, 5);
        Assert.assertNotNull(AIiTransactionFinalizationAction.class.getMethod(
                "commit", eu.algites.lib.action.execution.engine.AIiActionInput.class,
                AIiTransactionFinalizationContext.class));
        Assert.assertNotNull(AIiTransactionFinalizationAction.class.getMethod(
                "onActionFailure", AIiTransactionActionFailureContext.class));
        Assert.assertNotNull(AIiTransactionFinalizationAction.class.getMethod(
                "onCommitFailure", AIiTransactionCommitFailureContext.class));
    }

    @Test
    public void subtransactionIsNotTopLevelTransaction() {
        Assert.assertFalse(AIiExecutionTransaction.class.isAssignableFrom(AIiExecutionSubtransaction.class));
    }
}
