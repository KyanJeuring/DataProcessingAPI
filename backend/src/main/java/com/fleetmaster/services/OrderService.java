package com.fleetmaster.services;

import com.fleetmaster.dtos.AddProgressDto;
import com.fleetmaster.dtos.CreateOrderDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.postgresql.util.PGobject;

import java.sql.SQLException;
import java.util.List;

@Service
public class OrderService {

    @PersistenceContext
    private EntityManager entityManager;
    
    private final ObjectMapper objectMapper;

    public OrderService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Long createOrder(Long companyId, CreateOrderDto dto) {
        // 1. Validate ownership
        if (!isVehicleOwnedByCompany(companyId, dto.getVehicleId())) {
            throw new RuntimeException("Vehicle does not belong to your company");
        }
        if (!isDriverOwnedByCompany(companyId, dto.getDriverId())) {
            throw new RuntimeException("Driver does not belong to your company");
        }

        // 2. Prepare Points
        Object pickUpPoint = createPoint(dto.getPickUpLat(), dto.getPickUpLon());
        Object deliveryPoint = createPoint(dto.getDeliveryLat(), dto.getDeliveryLon());

        // 3. Call SP
        // sp_create_order(p_vehicle_id, p_driver_id, p_pick_up, p_delivery, p_load_type, p_departure_time, p_arrival_time)
        Number orderId = (Number) entityManager.createNativeQuery(
                "SELECT sp_create_order(:vid, :did, CAST(:pickup AS point), CAST(:delivery AS point), CAST(:ltype AS load_type), :dtime, :atime)")
                .setParameter("vid", dto.getVehicleId())
                .setParameter("did", dto.getDriverId())
                .setParameter("pickup", pickUpPoint)
                .setParameter("delivery", deliveryPoint)
                .setParameter("ltype", dto.getLoadType())
                .setParameter("dtime", dto.getDepartureTime())
                .setParameter("atime", dto.getArrivalTime())
                .getSingleResult();
        
        return orderId.longValue();
    }

    @Transactional
    public Long addProgress(Long companyId, AddProgressDto dto) {
        // 1. Validate order ownership
        if(!isOrderOwnedByCompany(companyId, dto.getOrderId())) {
             throw new RuntimeException("Order does not belong to your company");
        }

        Object position = createPoint(dto.getLat(), dto.getLon());
        String jsonDescription = "{}";
        try {
            if(dto.getDescription() != null) {
                jsonDescription = objectMapper.writeValueAsString(dto.getDescription());
            }
        } catch(Exception e) {
            throw new RuntimeException("Invalid JSON description");
        }

        // sp_add_progress(p_order_id, p_position, p_type, p_description)
        Number progressId = (Number) entityManager.createNativeQuery(
            "SELECT sp_add_progress(:oid, CAST(:pos AS point), CAST(:ptype AS progress_type), CAST(:desc AS jsonb))")
            .setParameter("oid", dto.getOrderId())
            .setParameter("pos", position)
            .setParameter("ptype", dto.getType())
            .setParameter("desc", jsonDescription)
            .getSingleResult();

        return progressId.longValue();
    }

    private boolean isVehicleOwnedByCompany(Long companyId, Long vehicleId) {
        List<?> result = entityManager.createNativeQuery(
                "SELECT 1 FROM company_vehicles WHERE company_id = :cid AND vehicle_id = :vid")
                .setParameter("cid", companyId)
                .setParameter("vid", vehicleId)
                .getResultList();
        return !result.isEmpty();
    }
    
    private boolean isDriverOwnedByCompany(Long companyId, Long driverId) {
        List<?> result = entityManager.createNativeQuery(
                "SELECT 1 FROM company_account WHERE company_id = :cid AND id = :did")
                .setParameter("cid", companyId)
                .setParameter("did", driverId)
                .getResultList();
        return !result.isEmpty();
    }

    private boolean isOrderOwnedByCompany(Long companyId, Long orderId) {
         List<?> result = entityManager.createNativeQuery(
                "SELECT 1 FROM orders o JOIN company_vehicles cv ON o.vehicle_id = cv.vehicle_id " +
                "WHERE cv.company_id = :cid AND o.id = :oid")
                .setParameter("cid", companyId)
                .setParameter("oid", orderId)
                .getResultList();
        return !result.isEmpty();
    }
    
    private String createPoint(Double lat, Double lon) {
        if (lat == null || lon == null) return null;
        return String.format("(%s,%s)", lat, lon);
    }
}
