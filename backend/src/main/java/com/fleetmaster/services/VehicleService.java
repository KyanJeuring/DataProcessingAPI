package com.fleetmaster.services;

import com.fleetmaster.dtos.CreateVehicleDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.stream.Collectors;

@Service
public class VehicleService {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public Long createVehicle(Long companyId, CreateVehicleDto dto) {
        // 1. Insert into vehicles
        // Convert List<String> to Postgres array literal like '{NORMAL, HAZARDOUS}'
        String loadTypesArr = dto.getLoadTypes() == null ? "{}" : 
            "{" + String.join(",", dto.getLoadTypes()) + "}";

        // CAST(:ltypes AS load_type[]) - might need explicit casting in SQL string
        Number vehicleId = (Number) entityManager.createNativeQuery(
                "INSERT INTO vehicles (load_capacity, type, year_of_manufacture, load_type, last_odometer) " +
                "VALUES (:cap, CAST(:vtype AS vehicle_type), :yom, CAST(:ltypes AS load_type[]), :odo) " +
                "RETURNING id")
                .setParameter("cap", dto.getLoadCapacity())
                .setParameter("vtype", dto.getType())
                .setParameter("yom", dto.getYearOfManufacture())
                .setParameter("ltypes", loadTypesArr)
                .setParameter("odo", dto.getLastOdometer())
                .getSingleResult();

        // 2. Link to company
        entityManager.createNativeQuery(
                "INSERT INTO company_vehicles (company_id, vehicle_id) VALUES (:cid, :vid)")
                .setParameter("cid", companyId)
                .setParameter("vid", vehicleId.longValue())
                .executeUpdate();

        return vehicleId.longValue();
    }
}
