package com.fleetmaster.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import org.hibernate.query.NativeQuery;
import org.hibernate.transform.AliasToEntityMapResultTransformer;

@Service
public class FleetService {

    @PersistenceContext
    private EntityManager entityManager;

    public List<Map<String, Object>> getFleetStatus(Long companyId) {
        return entityManager.createNativeQuery("SELECT * FROM sp_read_fleet_status(:cid)")
                .setParameter("cid", companyId)
                .unwrap(NativeQuery.class)
                .setResultTransformer(AliasToEntityMapResultTransformer.INSTANCE)
                .getResultList();
    }

    public List<Map<String, Object>> getOrderTracking(Long companyId) {
        return entityManager.createNativeQuery("SELECT * FROM sp_read_order_tracking(:cid)")
                .setParameter("cid", companyId)
                .unwrap(NativeQuery.class)
                .setResultTransformer(AliasToEntityMapResultTransformer.INSTANCE)
                .getResultList();
    }

    public List<Map<String, Object>> getSubscriptionUsage(Long companyId) {
        return entityManager.createNativeQuery("SELECT * FROM sp_read_subscription_usage(:cid)")
                .setParameter("cid", companyId)
                .unwrap(NativeQuery.class)
                .setResultTransformer(AliasToEntityMapResultTransformer.INSTANCE)
                .getResultList();
    }
}
