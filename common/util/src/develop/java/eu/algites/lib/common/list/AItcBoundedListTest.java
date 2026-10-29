package eu.algites.lib.common.list;

import eu.algites.lib.common.interval.AIcInterval;
import eu.algites.lib.common.interval.AInIntervalBoundary;
import java.util.List;
import java.util.Locale;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Cardinality, mutation, interval and rollback regression coverage. */
public class AItcBoundedListTest {
    @Test
    public void AIcMinimumAndMaximum() {
        AIcBoundedList<String> locList = new AIcBoundedList<>(2, 5);
        Assert.assertFalse(locList.isValid());
        Assert.assertTrue(locList.getInvalidStateMessage(Locale.forLanguageTag("cs")).contains("alespoň 2"));
        locList.addAll(List.of("a", "b"));
        Assert.assertTrue(locList.isValid());
        locList.addAll(List.of("c", "d", "e"));
        Assert.assertEquals(locList.size(), 5);
        Assert.expectThrows(IllegalStateException.class, () -> locList.add("f"));
        Assert.assertEquals(locList.size(), 5);
        locList.remove(0);
        locList.remove(0);
        locList.remove(0);
        Assert.assertEquals(locList.size(), 2);
        Assert.expectThrows(IllegalStateException.class, () -> locList.remove(0));
    }

    @Test
    public void AIcBulkChangesRollBack() {
        AIcBoundedList<Integer> locList = new AIcBoundedList<>(2, 5);
        locList.addAll(List.of(1, 2, 3, 4, 5));
        Assert.expectThrows(IllegalStateException.class, () -> locList.addAll(List.of(6, 7)));
        Assert.assertEquals(locList, List.of(1, 2, 3, 4, 5));
        locList.modifyAtomically(aValues -> {
            aValues.addAll(List.of(6, 7, 8));
            aValues.removeAll(List.of(2, 3, 4));
        });
        Assert.assertEquals(locList, List.of(1, 5, 6, 7, 8));
        Assert.expectThrows(IllegalStateException.class, () -> locList.modifyAtomically(aValues -> aValues.clear()));
        Assert.assertEquals(locList, List.of(1, 5, 6, 7, 8));
        Assert.expectThrows(IllegalArgumentException.class, () -> locList.modifyAtomically(aValues -> {
            aValues.add(9);
            throw new IllegalArgumentException("Rollback");
        }));
        Assert.assertEquals(locList.size(), 5);
    }

    @Test
    public void AIcAllowAnyAndRepair() {
        AIcBoundedList<Integer> locList = new AIcBoundedList<>(2, 5, AInBoundsMutationPolicy.ALLOW_ANY);
        locList.addAll(List.of(1, 2, 3, 4, 5, 6, 7));
        Assert.assertFalse(locList.isValid());
        Assert.assertEquals(locList.size(), 7);
        locList.remove(0);
        locList.remove(0);
        Assert.assertTrue(locList.isValid());
        AIcBoundedList<String> locUnbounded = new AIcBoundedList<>();
        Assert.assertTrue(locUnbounded.isValid());
    }

    @Test
    public void AIcIntervalConstructors() {
        AIcInterval<Integer> locInterval = new AIcInterval<>(
                AInIntervalBoundary.OPEN, 1, AInIntervalBoundary.CLOSED, 5);
        AIcBoundedList<String> locList = new AIcBoundedList<>(locInterval);
        Assert.assertEquals(locList.getMinSize(), Integer.valueOf(2));
        Assert.assertEquals(locList.getMaxSize(), Integer.valueOf(5));
        AIcInterval<Integer> locUnbounded = new AIcInterval<>(
                AInIntervalBoundary.UNBOUNDED, null, AInIntervalBoundary.UNBOUNDED, null);
        Assert.assertNull(new AIcBoundedList<>(locUnbounded).getMaxSize());
        AIcInterval<Integer> locNoInteger = new AIcInterval<>(
                AInIntervalBoundary.OPEN, 2, AInIntervalBoundary.OPEN, 3);
        Assert.expectThrows(IllegalArgumentException.class, () -> new AIcBoundedList<>(locNoInteger));
    }

    @Test
    public void AIcViewsCannotEvadeBounds() {
        AIcBoundedList<Integer> locList = new AIcBoundedList<>(2, 4);
        locList.addAll(List.of(1, 2, 3, 4));
        Assert.expectThrows(IllegalStateException.class, () -> locList.subList(0, 3).clear());
        Assert.assertEquals(locList.size(), 4);
        Assert.expectThrows(IllegalStateException.class, () -> locList.removeIf(aValue -> aValue > 1));
        Assert.assertEquals(locList.size(), 4);
    }
}
