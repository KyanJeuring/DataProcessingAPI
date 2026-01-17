package main.java.com.fleetmaster.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.util.ArrayList;

@Entity
@Table(name = "warehouse_staff")
public class WarehouseStaff {
    @Column(name = "warehouse_stock")
    private ArrayList<WarehouseStock> warehouseStocks = new ArrayList<>();

    public ArrayList<WarehouseStock> getWarehouseStocks() {
        return this.warehouseStocks;
    }

    public void addWarehouseStock(WarehouseStock warehouseStock) {
        if (warehouseStock == null) {
            throw new IllegalArgumentException("Please provide a warehouse stock.");
        }
        
        this.warehouseStocks.add(warehouseStock);
    }
}
