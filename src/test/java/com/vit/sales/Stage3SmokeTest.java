package com.vit.sales;

import com.vit.sales.data.MasterEntity;
import java.util.Set;
import java.util.stream.Collectors;

/** Offline smoke checks for fixed CRUD metadata; requires no database. */
public final class Stage3SmokeTest {
    public static void main(String[] args) {
        Set<String> expected = Set.of("Category", "Supplier", "Product", "Customer", "Employee");
        Set<String> actual = MasterEntity.ALL.stream().map(MasterEntity::table).collect(Collectors.toSet());
        if (!actual.equals(expected)) throw new AssertionError("Master entity list mismatch: " + actual);
        for (MasterEntity entity : MasterEntity.ALL) {
            if (entity.fields().isEmpty()) throw new AssertionError("No fields configured for " + entity.table());
            if (entity.fields().stream().anyMatch(f -> !f.name().matches("[a-z_]+")))
                throw new AssertionError("Unexpected SQL identifier in " + entity.table());
        }
        if (MasterEntity.ALL.stream().filter(e -> e.table().equals("Employee")).findFirst().orElseThrow().staffCanEdit())
            throw new AssertionError("Employees must not be editable by staff");
        System.out.println("PASS: five master entities and field metadata are configured; Employee is admin-only metadata.");
    }
}
