package com.example.onlineshop.service;

import com.example.onlineshop.entity.Product;
import com.example.onlineshop.util.PersistenceUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;

import java.util.List;

@ApplicationScoped
public class ProductService {

    private EntityManager em;

    public ProductService() {
        this.em = PersistenceUtil.getEntityManager();
    }

    // 添加商品
    public void addProduct(Product product) {
        try {
            em.getTransaction().begin();
            em.persist(product);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Failed to add product", e);
        }
    }

    // 更新商品
    public void updateProduct(Product product) {
        try {
            em.getTransaction().begin();
            em.merge(product);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Failed to update product", e);
        }
    }

    // 删除商品
    public void deleteProduct(Long id) {
        try {
            em.getTransaction().begin();
            Product product = em.find(Product.class, id);
            if (product != null) {
                em.remove(product);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Failed to delete product", e);
        }
    }

    // 查询单个商品
    public Product findProductById(Long id) {
        return em.find(Product.class, id);
    }

    // 查询所有商品
    public List<Product> findAllProducts() {
        return em.createQuery("SELECT p FROM Product p", Product.class)
                .getResultList();
    }

    // 按分类查询商品
    public List<Product> findProductsByCategory(String category) {
        return em.createQuery("SELECT p FROM Product p WHERE p.category = :category", Product.class)
                .setParameter("category", category)
                .getResultList();
    }
}