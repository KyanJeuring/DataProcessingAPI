package com.fleetmaster.services;

import com.fleetmaster.dtos.CreateVehicleDto;
import com.fleetmaster.exceptions.BusinessException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.hibernate.query.NativeQuery;
import org.hibernate.transform.AliasToEntityMapResultTransformer;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

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

        // Flush to ensure vehicle is persisted before linking to company
        entityManager.flush();

        // 2. Link to company (this triggers enforce_vehicle_limit() function)
        entityManager.createNativeQuery(
                "INSERT INTO company_vehicles (company_id, vehicle_id) VALUES (:cid, :vid)")
                .setParameter("cid", companyId)
                .setParameter("vid", vehicleId.longValue())
                .executeUpdate();

        // Flush again to ensure the trigger can see subscription data
        entityManager.flush();

        return vehicleId.longValue();
    }

    public List<Map<String, Object>> getAllVehicles(Long companyId) {
        return entityManager.createNativeQuery(
                "SELECT v.* FROM vehicles v " +
                "JOIN company_vehicles cv ON v.id = cv.vehicle_id " +
                "WHERE cv.company_id = :cid")
                .setParameter("cid", companyId)
                .unwrap(NativeQuery.class)
                .setResultTransformer(AliasToEntityMapResultTransformer.INSTANCE)
                .getResultList();
    }

    public Map<String, Object> getVehicleById(Long companyId, Long vehicleId) {
        List<Map<String, Object>> results = entityManager.createNativeQuery(
                "SELECT v.* FROM vehicles v " +
                "JOIN company_vehicles cv ON v.id = cv.vehicle_id " +
                "WHERE cv.company_id = :cid AND v.id = :vid")
                .setParameter("cid", companyId)
                .setParameter("vid", vehicleId)
                .unwrap(NativeQuery.class)
                .setResultTransformer(AliasToEntityMapResultTransformer.INSTANCE)
                .getResultList();

        if (results.isEmpty()) {
            throw new BusinessException("Vehicle not found or does not belong to your company");
        }
        return results.get(0);
    }

    @Transactional
    public void updateVehicle(Long companyId, Long vehicleId, CreateVehicleDto dto) {
        // Verify ownership
        getVehicleById(companyId, vehicleId);

        String loadTypesArr = dto.getLoadTypes() == null ? "{}" : 
            "{" + String.join(",", dto.getLoadTypes()) + "}";

        int updated = entityManager.createNativeQuery(
                "UPDATE vehicles SET " +
                "load_capacity = :cap, " +
                "type = CAST(:vtype AS vehicle_type), " +
                "year_of_manufacture = :yom, " +
                "load_type = CAST(:ltypes AS load_type[]), " +
                "last_odometer = :odo " +
                "WHERE id = :vid")
                .setParameter("cap", dto.getLoadCapacity())
                .setParameter("vtype", dto.getType())
                .setParameter("yom", dto.getYearOfManufacture())
                .setParameter("ltypes", loadTypesArr)
                .setParameter("odo", dto.getLastOdometer())
                .setParameter("vid", vehicleId)
                .executeUpdate();

        if (updated == 0) {
            throw new BusinessException("Failed to update vehicle");
        }
    }

    @Transactional
    public void deleteVehicle(Long companyId, Long vehicleId) {
        // Verify ownership
        getVehicleById(companyId, vehicleId);

        // Delete from company_vehicles first (due to foreign key)
        entityManager.createNativeQuery(
                "DELETE FROM company_vehicles WHERE company_id = :cid AND vehicle_id = :vid")
                .setParameter("cid", companyId)
                .setParameter("vid", vehicleId)
                .executeUpdate();

        // Delete from vehicles
        entityManager.createNativeQuery(
                "DELETE FROM vehicles WHERE id = :vid")
                .setParameter("vid", vehicleId)
                .executeUpdate();
    }
}
