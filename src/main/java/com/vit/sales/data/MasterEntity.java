package com.vit.sales.data;

import java.util.List;

/** Whitelisted metadata for the master-table CRUD screens. */
public record MasterEntity(String table, String title, List<Field> fields, String adminOnly, boolean staffCanEdit) {
    public record Field(String name, String label, Kind kind, boolean required) {}
    public enum Kind { TEXT, INTEGER, DECIMAL, DATE }

    public static final List<MasterEntity> ALL = List.of(
        new MasterEntity("Category", "Categories", List.of(
            f("category_name", "Category name", Kind.TEXT, true), f("description", "Description", Kind.TEXT, false)), "", true),
        new MasterEntity("Supplier", "Suppliers", List.of(
            f("supplier_name", "Supplier name", Kind.TEXT, true), f("contact_number", "Contact number", Kind.TEXT, true),
            f("email", "Email", Kind.TEXT, false), f("address", "Address", Kind.TEXT, false)), "", true),
        new MasterEntity("Product", "Products", List.of(
            f("product_name", "Product name", Kind.TEXT, true), f("category_id", "Category ID", Kind.INTEGER, false),
            f("supplier_id", "Supplier ID", Kind.INTEGER, false), f("price", "Price", Kind.DECIMAL, true),
            f("stock_quantity", "Stock quantity", Kind.INTEGER, true)), "", true),
        new MasterEntity("Customer", "Customers", List.of(
            f("customer_name", "Customer name", Kind.TEXT, true), f("phone", "Phone", Kind.TEXT, true),
            f("email", "Email", Kind.TEXT, false), f("city", "City", Kind.TEXT, false), f("join_date", "Join date (YYYY-MM-DD)", Kind.DATE, false)), "", true),
        new MasterEntity("Employee", "Employees", List.of(
            f("employee_name", "Employee name", Kind.TEXT, true), f("designation", "Designation", Kind.TEXT, false),
            f("phone", "Phone", Kind.TEXT, true), f("salary", "Salary", Kind.DECIMAL, false)), "ADMIN", false)
    );

    private static Field f(String name, String label, Kind kind, boolean required) {
        return new Field(name, label, kind, required);
    }
}
