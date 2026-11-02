package eu.algites.lib.common.list;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.testng.annotations.Test;

/**
 * Contract and regression tests for multi-key grouping, unique constraints,
 * mutation propagation, snapshots, weakly registered membership views and
 * positional list behavior.
 *
 * @author linhart1
 */
public class AItcMultiMappedListTest {
    private record Employee(long id, String email, String city) {}
    private record LocationKey(String city, String email) {}

    private final AIiKeyDefinition<Employee, Long> byId =
        new AIcKeyDefinition<>("BY_ID", Employee::id, true);
    private final AIiKeyDefinition<Employee, String> byEmail =
        new AIcKeyDefinition<>("BY_EMAIL", Employee::email, true);
    private final AIiKeyDefinition<Employee, String> byCity =
        new AIcKeyDefinition<>("BY_CITY", Employee::city, false);
    private final AIiKeyDefinition<Employee, LocationKey> byLocation =
        new AIcKeyDefinition<>("BY_LOCATION", v -> new LocationKey(v.city(), v.email()), true);

    private AIcMultiMappedList<Employee> list() {
        return new AIcMultiMappedList<>(byId, byEmail, byCity, byLocation);
    }

    private static final Employee A = new Employee(1, "a@example.org", "Prague");
    private static final Employee B = new Employee(2, "b@example.org", "Brno");
    private static final Employee C = new Employee(3, "c@example.org", "Prague");
    private static final Employee D = new Employee(4, "d@example.org", "Prague");

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void fails(Class<? extends Throwable> expected, Runnable operation) {
        try {
            operation.run();
        } catch (Throwable actual) {
            if (expected.isInstance(actual)) return;
            throw new AssertionError("Expected " + expected + ", got " + actual, actual);
        }
        throw new AssertionError("Expected " + expected + " but operation succeeded");
    }

    /** Verifies xs:unique-style optional keys and ordinary required keys. */
    @Test public void optionalKeysAndNullGroups() {
        var optionalCode = new AIcKeyDefinition<Employee, String>(
                "OPTIONAL", Employee::email, true, false);
        var requiredId = new AIcKeyDefinition<Employee, Long>("REQUIRED", Employee::id, true);
        var employees = new AIcMultiMappedList<Employee>(optionalCode, requiredId);
        Employee missing1 = new Employee(50, null, "Prague");
        Employee missing2 = new Employee(51, null, "Brno");
        employees.addAll(List.of(missing1, missing2, A));
        Map<String, List<Employee>> mapped = employees.mapByKeyType(optionalCode);
        check(mapped.get(null).equals(List.of(missing1, missing2)), "missing keys form null group");
        fails(IllegalArgumentException.class, () -> employees.add(
                new Employee(52, A.email(), "Plzen")));
        check(employees.size() == 3, "failed unique insertion must be atomic");
        mapped.remove(null);
        check(employees.equals(List.of(A)), "null group deletion updates root list");
        var strict = new AIcKeyDefinition<Employee, String>("STRICT", Employee::email, true);
        var strictList = new AIcMultiMappedList<Employee>(strict);
        fails(IllegalArgumentException.class, () -> strictList.add(missing1));
        check(strictList.isEmpty(), "missing required keys must be rejected");
    }

    /** Ensures indexed map views are cached and group snapshots never mutate. */
    @Test public void cachedViewsAndSnapshots() {
        AIcMultiMappedList<Employee> employees = list();
        Map<String, List<Employee>> cities = employees.mapByKeyType(byCity);
        check(cities == employees.mapByKeyType(byCity), "map must be cached");
        employees.addAll(List.of(A, B, C));
        List<Employee> prague = cities.get("Prague");
        check(prague.equals(List.of(A, C)), "stable list ordering within a group");
        fails(UnsupportedOperationException.class, () -> prague.add(D));
        employees.add(D);
        check(prague.equals(List.of(A, C)), "snapshot must not change");
        check(cities.get("Prague").equals(List.of(A, C, D)), "live map must change");
        fails(UnsupportedOperationException.class, () -> cities.put("Prague", List.of(B)));
        fails(UnsupportedOperationException.class, () -> cities.get("Prague").clear());
        check(cities.get("missing") == null, "missing groups must be null");
    }

    /** Confirms removing a group updates the root, other maps and tracked sublists. */
    @Test public void groupRemovalPropagates() {
        AIcMultiMappedList<Employee> employees = list();
        employees.addAll(List.of(A, B, C, D));
        List<Employee> part = employees.subList(0, 3);
        Map<String, List<Employee>> cities = employees.mapByKeyType(byCity);
        Map<Long, List<Employee>> ids = employees.mapByKeyType(byId);
        check(cities.remove("Prague").equals(List.of(A, C, D)), "remove returns removed snapshot");
        check(employees.equals(List.of(B)), "parent synchronized");
        check(part.equals(List.of(B)), "sublist synchronized");
        check(ids.size() == 1 && ids.get(2L).equals(List.of(B)), "other indexes synchronized");
        check(cities.remove("Prague") == null, "absent group removal");
        employees.add(A);
        check(ids.get(1L).equals(List.of(A)), "cached view updated after group removal");
        check(part.equals(List.of(B)), "parent add does not extend member set");
    }

    /** Verifies atomic rollback on any unique-index collision and extractor failure. */
    @Test public void atomicFailures() {
        AIcMultiMappedList<Employee> employees = list();
        employees.addAll(List.of(A, B));
        Employee duplicateEmail = new Employee(5, B.email(), "Prague");
        fails(IllegalArgumentException.class, () -> employees.add(duplicateEmail));
        fails(IllegalArgumentException.class, () -> employees.addAll(List.of(C, duplicateEmail)));
        fails(IllegalArgumentException.class, () -> employees.set(0, duplicateEmail));
        check(employees.equals(List.of(A, B)), "no partial mutation");
        check(employees.mapByKeyType(byCity).size() == 2, "indexes unchanged");
        fails(NullPointerException.class, () -> employees.add(null));
        fails(IllegalArgumentException.class, () -> employees.mapByKeyType(
            new AIcKeyDefinition<>("BY_ID", Employee::id, true)));
    }

    /** Validates indexed removal via map entry iterators and derived collection views. */
    @Test public void mapCollectionRemovals() {
        AIcMultiMappedList<Employee> employees = list();
        employees.addAll(List.of(A, B, C));
        Map<String, List<Employee>> cities = employees.mapByKeyType(byCity);
        cities.keySet().remove("Prague");
        check(employees.equals(List.of(B)), "keySet.remove propagates group removal");
        employees.add(A);
        cities.entrySet().iterator().next();
        cities.values().remove(List.of(A));
        check(employees.equals(List.of(B)), "values.remove propagates");
        cities.clear();
        check(employees.isEmpty(), "map clear removes all root elements");
    }

    /** Confirms stable sublist membership across parent additions, deletions and nesting. */
    @Test public void trackedSublists() {
        AIcMultiMappedList<Employee> employees = list();
        employees.addAll(List.of(A, B, C));
        List<Employee> first = employees.subList(0, 2);
        List<Employee> second = first.subList(1, 2);
        employees.add(1, D);
        check(first.equals(List.of(A, B)) && second.equals(List.of(B)), "parent insertion hidden");
        fails(IllegalArgumentException.class, () -> first.add(C));
        check(first.equals(List.of(A, B)), "failed view add does not mutate");
        first.add(new Employee(5, "e@example.org", "Prague"));
        check(first.size() == 3 && employees.size() == 5, "sublist add inserts into parent");
        employees.remove(B);
        check(!first.contains(B) && second.isEmpty(), "external removal reaches nested view");
        second.add(new Employee(6, "f@example.org", "Brno"));
        check(second.size() == 1 && employees.size() == 5, "empty nested view can insert");
    }

    /** Exercises independent range copies, positional list mutations and composite keys. */
    @Test public void copyAndCompositeKeys() {
        AIcMultiMappedList<Employee> employees = list();
        employees.addAll(List.of(A, B, C));
        AIiMultiMappedList<Employee> copy = employees.copyRange(0, 2);
        copy.remove(0);
        check(employees.size() == 3 && copy.equals(List.of(B)), "detached shallow copy");
        check(employees.mapByKeyType(byLocation).get(new LocationKey("Prague", A.email()))
                .equals(List.of(A)), "record composite key");
        employees.removeIf(v -> v.city().equals("Prague"));
        check(employees.equals(List.of(B)), "removeIf preserves indexes");
    }

    /** Ensures replacing and sorting through supported list operations preserve consistency. */
    @Test public void replaceAndSort() {
        AIcMultiMappedList<Employee> employees = list();
        employees.addAll(List.of(C, B, A));
        List<Employee> first = employees.subList(0, 2);
        employees.sort(Comparator.comparing(Employee::id));
        check(employees.equals(List.of(A, B, C)), "sorted root");
        check(first.contains(C) && first.contains(B), "sublist entries preserved");
        employees.replaceAll(v -> new Employee(v.id(), v.email(), "Elsewhere"));
        check(employees.mapByKeyType(byCity).get("Elsewhere").size() == 3,
            "bulk replacement indexes rebuilt");
        check(first.size() == 2 && first.get(0).city().equals("Elsewhere"),
            "replaced occurrences visible in sublist");
    }

    /** Ensures entries remove through an iterator without map-value write access. */
    @Test public void mapEntryIteratorAndUnsupportedUpdates() {
        AIcMultiMappedList<Employee> employees = list();
        employees.addAll(List.of(A, B, C));
        Map<String, List<Employee>> cities = employees.mapByKeyType(byCity);
        var iter = cities.entrySet().iterator();
        Map.Entry<String, List<Employee>> first = iter.next();
        fails(UnsupportedOperationException.class, () -> first.setValue(List.of(D)));
        int count = first.getValue().size();
        iter.remove();
        check(employees.size() == 3 - count, "entrySet iterator removal propagates");
        fails(IllegalStateException.class, iter::remove);
        fails(UnsupportedOperationException.class, () -> cities.compute("X", (k, v) -> List.of(D)));
    }

    /** Tests nested membership, bulk sublist removal and atomic bulk replacement. */
    @Test public void sublistMutationsRemainCoherent() {
        AIcMultiMappedList<Employee> employees = list();
        employees.addAll(List.of(A, B, C, D));
        List<Employee> part = employees.subList(1, 4);
        List<Employee> nested = part.subList(1, 3);
        part.removeIf(v -> v.city().equals("Prague"));
        check(employees.equals(List.of(A, B)), "sublist removeIf changes parent");
        check(part.equals(List.of(B)) && nested.isEmpty(), "nested membership updated");
        part.add(new Employee(10, "ten@example.org", "Brno"));
        part.replaceAll(v -> new Employee(v.id(), v.email(), "Elsewhere"));
        check(employees.mapByKeyType(byCity).get("Elsewhere").size() == 2,
            "sublist replaceAll rebuilds all indexes");
        part.clear();
        check(employees.equals(List.of(A)), "clear via sublist propagates");
    }

    /** Verifies that two identical values remain distinguishable without a key index. */
    @Test public void equalValuesAreSeparateOccurrences() {
        AIcMultiMappedList<String> items = new AIcMultiMappedList<>();
        items.addAll(List.of("A", "A", "B"));
        List<String> first = items.subList(0, 1);
        List<String> second = items.subList(1, 2);
        items.remove(0);
        check(first.isEmpty() && second.equals(List.of("A")),
            "removal propagated by entry identity, not value equality");
    }

    /** Verifies invalid extractor results do not damage existing content or maps. */
    @Test public void extractorFailuresAreAtomic() {
        AIiKeyDefinition<Employee, String> requiredEmail = new AIcKeyDefinition<>(
            "EMAIL", employee -> {
                if (employee.id() == 99) throw new IllegalStateException("invalid");
                return employee.email();
            }, true);
        AIcMultiMappedList<Employee> employees = new AIcMultiMappedList<>(requiredEmail);
        employees.add(A);
        fails(IllegalStateException.class,
            () -> employees.add(new Employee(99, "broken@example.org", "Here")));
        check(employees.equals(List.of(A)), "failed extractor leaves parent unchanged");
        check(employees.mapByKeyType(requiredEmail).get(A.email()).equals(List.of(A)),
            "failed extractor leaves maps unchanged");
    }

    /** Ensures group uniqueness applies independently to all registered definitions. */
    @Test public void uniqueAndNonUniqueDefinitions() {
        AIcMultiMappedList<Employee> employees = list();
        employees.addAll(List.of(A, C, D));
        check(employees.mapByKeyType(byCity).get("Prague").size() == 3,
            "non-unique city supports three values");
        fails(IllegalArgumentException.class,
            () -> employees.add(new Employee(9, A.email(), "Brno")));
        check(employees.size() == 3, "unique email collision rejected");
    }
}
