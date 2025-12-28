package com.example.onlineshop.service;

import com.example.onlineshop.entity.PointRecord;
import com.example.onlineshop.entity.User;
import com.example.onlineshop.util.PersistenceUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.io.Serializable;
import java.util.List;

@ApplicationScoped
public class PointService implements Serializable {
    @Inject
    private PersistenceUtil persistenceUtil;

    @Transactional
    public void createPointRecord(User user, int points, String source) {
        EntityManager em = persistenceUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            PointRecord pointRecord = new PointRecord();
            pointRecord.setUser(user);
            pointRecord.setPoints(points);
            pointRecord.setSource(source);
            pointRecord.setCreatedAt(java.time.LocalDateTime.now());
            em.persist(pointRecord);
            user.setPoints(user.getPoints() + points);
            em.merge(user);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("创建积分记录失败: " + e.getMessage());
        } finally {
            em.close();
        }
    }

    public List<PointRecord> findPointRecordsByUser(User user) {
        EntityManager em = persistenceUtil.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT pr FROM PointRecord pr WHERE pr.user.id = :userId ORDER BY pr.createdAt DESC",
                            PointRecord.class)
                    .setParameter("userId", user.getId())
                    .getResultList();
        } finally {
            em.close();
        }
    }
}