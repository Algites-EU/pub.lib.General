package eu.algites.lib.action.interaction;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Structural Java API regression tests, executed by the standard TestNG Gradle test task.
 * Behavioral tests are required when an implementation is introduced.
 */
public class AItcActionInteractionContractTest {

    @Test
    public void callerDoesNotInheritProviderAuthority() {
        Assert.assertFalse(AIiActionInteractionProviderToCaller.class
                .isAssignableFrom(AIiActionInteractionCallerToProvider.class));
        Assert.assertFalse(AIiActionInteractionCallerToProvider.class
                .isAssignableFrom(AIiActionInteractionProviderToCaller.class));
    }

    @Test
    public void interactionDirectionsPreserveSpecializedTypeFamily() {
        Assert.assertEquals(AIiActionInteractionProviderToCaller.class.getTypeParameters().length, 5);
        Assert.assertEquals(AIiActionInteractionCallerToProvider.class.getTypeParameters().length, 5);
    }
}
