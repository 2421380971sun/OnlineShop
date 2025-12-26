package com.example.onlineshop.service;

import com.example.onlineshop.entity.PointRecord;
import com.example.onlineshop.entity.User;
import com.example.onlineshop.util.PersistenceUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class UserService {

    @Inject
    private PersistenceUtil persistenceUtil;

    public User authenticate(String username, String password) {
        EntityManager em = persistenceUtil.getEntityManager();
        try {
            return em.createQuery("SELECT u FROM User u WHERE u.username = :username AND u.password = :password", User.class)
                    .setParameter("username", username)
                    .setParameter("password", password)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    public User findByUsername(String username) {
        EntityManager em = persistenceUtil.getEntityManager();
        try {
            return em.createQuery("SELECT u FROM User u WHERE u.username = :username", User.class)
                    .setParameter("username", username)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    public User findById(Long id) {
        EntityManager em = persistenceUtil.getEntityManager();
        try {
            return em.find(User.class, id);
        } finally {
            em.close();
        }
    }

    public List<Long> findAllUserIds() {
        EntityManager em = persistenceUtil.getEntityManager();
        try {
            return em.createQuery("SELECT u.id FROM User u", Long.class).getResultList();
        } finally {
            em.close();
        }
    }

    public List<User> findAllUsers() {
        EntityManager em = persistenceUtil.getEntityManager();
        try {
            return em.createQuery("SELECT u FROM User u ORDER BY u.id", User.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    @Transactional
    public void updateUser(User user) {
        EntityManager em = persistenceUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(user);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("更新用户失败: " + e.getMessage());
        } finally {
            em.close();
        }
    }

    public List<PointRecord> findPointRecordsByUser(Long userId) {
        EntityManager em = persistenceUtil.getEntityManager();
        try {
            return em.createQuery("SELECT pr FROM PointRecord pr WHERE pr.user.id = :userId ORDER BY pr.createdAt DESC", PointRecord.class)
                    .setParameter("userId", userId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public void registerUser(User user) {
        EntityManager em = persistenceUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(user);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
}