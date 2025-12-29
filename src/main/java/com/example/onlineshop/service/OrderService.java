package com.example.onlineshop.service;

import com.example.onlineshop.entity.*;
import com.example.onlineshop.util.PersistenceUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

@ApplicationScoped
public class OrderService implements Serializable {
    private static final Logger LOGGER = Logger.getLogger(OrderService.class.getName());

    @Inject
    private PersistenceUtil persistenceUtil;

    @Transactional
    public void createOrder(User user, List<CartItem> cart, int pointsUsed) {
        EntityManager em = persistenceUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Order order = new Order();
            order.setUser(user);
            order.setOrderDate(LocalDateTime.now());
            order.setStatus("PENDING");
            double totalAmount = cart.stream()
                    .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                    .sum();
            // 积分抵扣：100积分 = 1元
            pointsUsed = Math.max(0, pointsUsed); // 确保非负
            if (pointsUsed > 0) {
                if (pointsUsed > user.getPoints()) {
                    throw new RuntimeException("积分不足");
                }
                double discount = pointsUsed / 100.0;
                totalAmount -= discount;
                order.setPointsUsed(pointsUsed);
            } else {
                order.setPointsUsed(0); // 明确设置为 0
            }
            order.setTotalAmount(Math.max(0, totalAmount));
            for (CartItem item : cart) {
                Product product = em.find(Product.class, item.getProduct().getId());
                if (product == null) {
                    throw new RuntimeException("商品 ID " + item.getProduct().getId() + " 不存在");
                }
                if (product.getStock() < item.getQuantity()) {
                    throw new RuntimeException("商品 " + product.getName() + " 库存不足");
                }
                product.setStock(product.getStock() - item.getQuantity());
                OrderItem orderItem = new OrderItem();
                orderItem.setProduct(product);
                orderItem.setQuantity(item.getQuantity());
                orderItem.setPrice(product.getPrice());
                orderItem.setOrder(order);
                order.getOrderItems().add(orderItem);
                em.merge(product);
            }
            em.persist(order);
            em.getTransaction().commit();
            LOGGER.info("订单创建成功: orderId=" + order.getId() + ", userId=" + user.getId());
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            LOGGER.severe("创建订单失败: " + e.getMessage());
            throw new RuntimeException("创建订单失败: " + e.getMessage());
        } finally {
            em.close();
        }
    }

    public List<Order> findOrdersByUser(Long userId) {
        EntityManager em = persistenceUtil.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT o FROM Order o JOIN FETCH o.orderItems WHERE o.user.id = :userId ORDER BY o.orderDate DESC",
                            Order.class)
                    .setParameter("userId", userId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    @Transactional
    public void payOrder(Long orderId, User user) {
        EntityManager em = persistenceUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Order order = em.find(Order.class, orderId);
            if (order == null) {
                throw new RuntimeException("订单不存在");
            }
            if (!order.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("无权限支付此订单");
            }
            if (!order.getStatus().equals("PENDING")) {
                throw new RuntimeException("订单状态不支持支付");
            }
            // 扣除积分（如果有）
            int pointsUsed = order.getPointsUsed() != null ? order.getPointsUsed() : 0;
            if (pointsUsed > 0) {
                if (pointsUsed > user.getPoints()) {
                    throw new RuntimeException("积分不足");
                }
                user.setPoints(user.getPoints() - pointsUsed);
                PointRecord pointRecord = new PointRecord();
                pointRecord.setUser(user);
                pointRecord.setPoints(-pointsUsed);
                pointRecord.setSource("订单积分抵扣");
                pointRecord.setCreatedAt(LocalDateTime.now());
                em.persist(pointRecord);
            }
            // 增加积分：10元 = 1积分
            int pointsEarned = (int) (order.getTotalAmount() / 10);
            if (pointsEarned > 0) {
                user.setPoints(user.getPoints() + pointsEarned);
                PointRecord pointRecord = new PointRecord();
                pointRecord.setUser(user);
                pointRecord.setPoints(pointsEarned);
                pointRecord.setSource("订单支付");
                pointRecord.setCreatedAt(LocalDateTime.now());
                em.persist(pointRecord);
            }
            // 更新订单状态
            order.setStatus("PAID");
            em.merge(user);
            em.merge(order);
            em.getTransaction().commit();
            LOGGER.info("订单支付成功: orderId=" + orderId + ", userId=" + user.getId() + ", pointsUsed=" + pointsUsed);
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            LOGGER.severe("订单支付失败: orderId=" + orderId + ", error=" + e.getMessage());
            throw new RuntimeException("订单支付失败: " + e.getMessage());
        } finally {
            em.close();
        }
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        EntityManager em = persistenceUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Order order = em.find(Order.class, orderId);
            if (order == null) {
                throw new RuntimeException("订单不存在");
            }
            if (!order.getStatus().equals("PENDING")) {
                throw new RuntimeException("订单状态不支持取消");
            }
            order.setStatus("CANCELLED");
            em.merge(order);
            em.getTransaction().commit();
            LOGGER.info("订单取消成功: orderId=" + orderId);
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            LOGGER.severe("订单取消失败: " + e.getMessage());
            throw new RuntimeException("订单取消失败: " + e.getMessage());
        } finally {
            em.close();
        }
    }

    @Transactional
    public void updateOrderStatus(Long orderId, String status) {
        EntityManager em = persistenceUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Order order = em.find(Order.class, orderId);
            if (order == null) {
                throw new RuntimeException("订单不存在");
            }
            order.setStatus(status);
            em.merge(order);
            em.getTransaction().commit();
            LOGGER.info("订单状态更新成功: orderId=" + orderId + ", status=" + status);
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            LOGGER.severe("更新订单状态失败: " + e.getMessage());
            throw new RuntimeException("更新订单状态失败: " + e.getMessage());
        } finally {
            em.close();
        }
    }
}