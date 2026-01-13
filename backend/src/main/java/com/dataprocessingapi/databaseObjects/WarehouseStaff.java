package com.dataprocessingapi.databaseObjects;

import java.util.ArrayList;

public class WarehouseStaff {
    private ArrayList<WarehouseStock> warehouseStocks;

    public WarehouseStaff() {

    }

    public ArrayList<WarehouseStock> getWarehouseStocks() {
        return this.warehouseStocks;
    }

    public void setWarehouseStocks(ArrayList<WarehouseStock> warehouseStocks) {
        this.warehouseStocks = warehouseStocks;
    }
}
