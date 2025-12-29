package com.example.onlineshop.service;

import com.example.onlineshop.entity.*;
import com.example.onlineshop.util.PersistenceUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import java.util.List;

@ApplicationScoped
public class OrderService {

    // [AI] 原始代码：逻辑简单，存在资源泄漏风险
    public Order createOrder(User user, List<CartItem> cartItems, int pointsUsed) {
        // [AI] AI 试图用 @Inject，但我们手动改为了静态调用以匹配上一轮决策
        EntityManager em = PersistenceUtil.getEntityManager();
        em.getTransaction().begin();

        double totalAmount = 0;
        // 1. 扣减库存
        for (CartItem item : cartItems) {
            Product p = em.find(Product.class, item.getProduct().getId());
            if (p.getStock() < item.getQuantity()) {
                throw new RuntimeException("库存不足");
            }
            p.setStock(p.getStock() - item.getQuantity());
            totalAmount += p.getPrice() * item.getQuantity();
        }

        // 2. 积分抵扣 (AI 逻辑：未检查抵扣上限)
        if (pointsUsed > 0) {
            if (user.getPoints() < pointsUsed) {
                throw new RuntimeException("积分不足");
            }
            double discount = pointsUsed / 100.0;
            totalAmount -= discount; // [RISK] 可能变成负数
        }

        Order order = new Order();
        order.setUser(user);
        order.setTotalAmount(totalAmount);
        order.setPointsUsed(pointsUsed);
        order.setStatus("PENDING");

        em.persist(order);

        // [RISK] 如果上面抛错，em 永远不会关闭
        em.getTransaction().commit();
        em.close();
        return order;
    }
}