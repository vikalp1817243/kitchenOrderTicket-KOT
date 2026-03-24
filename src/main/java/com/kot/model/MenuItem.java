package com.kot.model;

import java.util.Objects;

/**
 * Represents a food item in the KOT system.
 */
public class MenuItem {
    private int itemCode;
    private String itemName;
    private String category;
    private double price;
    private String portion;

    public MenuItem(int itemCode, String itemName, String category, double price, String portion) {
        this.itemCode = itemCode;
        this.itemName = itemName;
        this.category = category;
        this.price = price;
        this.portion = portion;
    }

    public int getItemCode() { return itemCode; }
    public String getItemName() { return itemName; }
    public String getCategory() { return category; }
    public double getPrice() { return price; }
    public String getPortion() { return portion; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuItem menuItem = (MenuItem) o;
        return itemCode == menuItem.itemCode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemCode);
    }
    
    @Override
    public String toString() {
        return String.format("%d - %s (%s) - ₹%.2f [%s]", itemCode, itemName, portion, price, category);
    }
}
