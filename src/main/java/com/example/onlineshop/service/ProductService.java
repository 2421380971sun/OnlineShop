package com.example.onlineshop.service;

import com.example.onlineshop.entity.Product;
import com.example.onlineshop.util.PersistenceUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;

/**
 * [AI Generated]
 * 基于 Prompt 要求生成的 ProductService。
 * 注意：此处保留了 @Inject 错误用法，后续将被 curator 重构。
 */
@ApplicationScoped
public class ProductService {

    // [AI] 错误：PersistenceUtil 是静态工具类，不能被 CDI 注入
    // 这行代码在运行时会导致 WELD-001408 Unsatisfied dependencies 错误
    @Inject
    private PersistenceUtil persistenceUtil;

    public void addProduct(Product product) {
        // [AI] 错误：假设了 getEntityManager 是实例方法
        EntityManager em = persistenceUtil.getEntityManager();

        // [AI] 风险：缺少 try-catch-finally，异常时会导致连接泄漏
        em.getTransaction().begin();
        em.persist(product);
        em.getTransaction().commit();
        em.close();
    }

    public void updateProduct(Product product) {
        EntityManager em = persistenceUtil.getEntityManager();
        em.getTransaction().begin();
        em.merge(product);
        em.getTransaction().commit();
        em.close();
    }

    public void deleteProduct(Long id) {
        EntityManager em = persistenceUtil.getEntityManager();
        em.getTransaction().begin();
        Product product = em.find(Product.class, id);
        if (product != null) {
            em.remove(product);
        }
        em.getTransaction().commit();
        em.close();
    }

    public Product findProductById(Long id) {
        EntityManager em = persistenceUtil.getEntityManager();
        Product product = em.find(Product.class, id);
        em.close();
        return product;
    }

    public List<Product> findAllProducts() {
        EntityManager em = persistenceUtil.getEntityManager();
        TypedQuery<Product> query = em.createQuery("SELECT p FROM Product p", Product.class);
        List<Product> results = query.getResultList();
        em.close();
        return results;
    }

    public List<Product> findProductsByCategory(String category) {
        EntityManager em = persistenceUtil.getEntityManager();
        TypedQuery<Product> query = em.createQuery("SELECT p FROM Product p WHERE p.category = :category", Product.class);
        query.setParameter("category", category);
        List<Product> results = query.getResultList();
        em.close();
        return results;
    }
}