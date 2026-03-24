package com.kot.model;

/**
 * Abstract base class for all restaurant employees.
 */
public abstract class Employee {
    protected int employeeId;
    protected String name;
    protected String role;

    public Employee(int employeeId, String name, String role) {
        this.employeeId = employeeId;
        this.name = name;
        this.role = role;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }
}
