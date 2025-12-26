package com.example.onlineshop.controller;

import com.example.onlineshop.entity.CartItem;
import com.example.onlineshop.entity.Order;
import com.example.onlineshop.entity.Product;
import com.example.onlineshop.entity.User;
import com.example.onlineshop.service.OrderService;
import com.example.onlineshop.service.ProductService;
import com.example.onlineshop.service.UserService;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named
@SessionScoped
public class OrderController implements Serializable {
    @Inject private UserService userService;
    @Inject private ProductService productService;
    @Inject private OrderService orderService;
    @Inject private AuthController authController;

    private String username;
    private String password;
    private List<CartItem> cartItems = new ArrayList<>();
    private int pointsToUse;
    private double pointsDiscount;

    public String login() {
        User user = userService.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            if ("USER".equals(user.getRole())) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "权限不足", "仅管理员可访问"));
                return "login?faces-redirect=true";
            }
            authController.setCurrentUser(user);
            System.out.println("login: username=" + username + ", points=" + user.getPoints());
            return "product_list?faces-redirect=true";
        }
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "用户名或密码错误", null));
        return null;
    }

    public void addToCart(Long productId, int quantity) {
        Product product = productService.findProductById(productId);
        if (product != null && product.getStock() >= quantity) {
            for (CartItem item : cartItems) {
                if (item.getProduct().getId().equals(productId)) {
                    item.setQuantity(item.getQuantity() + quantity);
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO, "已更新购物车：" + product.getName(), null));
                    return;
                }
            }
            cartItems.add(new CartItem(product, quantity));
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "已加入购物车：" + product.getName(), null));
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "库存不足或商品不存在", null));
        }
    }

    public String updateCartItem(Long productId, int quantity) {
        for (CartItem item : cartItems) {
            if (item.getProduct().getId().equals(productId)) {
                if (quantity <= 0) {
                    cartItems.remove(item);
                } else {
                    Product product = productService.findProductById(productId);
                    if (product != null && product.getStock() >= quantity) {
                        item.setQuantity(quantity);
                    } else {
                        FacesContext.getCurrentInstance().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_ERROR, "库存不足", null));
                        return null;
                    }
                }
                break;
            }
        }
        return "cart?faces-redirect=true";
    }

    public String removeFromCart(Long productId) {
        cartItems.removeIf(item -> item.getProduct().getId().equals(productId));
        return "cart?faces-redirect=true";
    }

    public String applyPoints() {
        pointsToUse = Math.max(0, pointsToUse);
        if (pointsToUse == 0) {
            pointsDiscount = 0;
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "已移除积分抵扣", null));
            return null;
        }
        if (!authController.isLoggedIn()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "请先登录", null));
            return null;
        }
        User user = authController.getCurrentUser();
        if (pointsToUse > user.getPoints()) {
            pointsToUse = 0;
            pointsDiscount = 0;
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "积分不足", null));
            return null;
        }
        pointsDiscount = pointsToUse / 100.0;
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "积分抵扣应用成功", null));
        return null;
    }

    public String submitOrder() {
        if (!authController.isLoggedIn()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "请先登录", null));
            return "login?faces-redirect=true";
        }
        if (cartItems.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "购物车为空", null));
            return null;
        }
        try {
            pointsToUse = Math.max(0, pointsToUse);
            orderService.createOrder(authController.getCurrentUser(), cartItems, pointsToUse);
            cartItems.clear();
            pointsToUse = 0;
            pointsDiscount = 0;
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "订单提交成功！", null));
            return "order_list?faces-redirect=true";
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "订单提交失败：" + e.getMessage(), null));
            return null;
        }
    }

    public String payOrder(Long orderId) {
        if (!authController.isLoggedIn()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "请先登录", null));
            return "login?faces-redirect=true";
        }
        try {
            orderService.payOrder(orderId, authController.getCurrentUser());
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "订单支付成功", null));
            return "order_list?faces-redirect=true";
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "支付失败：" + e.getMessage(), null));
            return "order_list?faces-redirect=true";
        }
    }

    public String cancelOrder(Long orderId) {
        if (!authController.isLoggedIn()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "请先登录", null));
            return "login?faces-redirect=true";
        }
        try {
            orderService.cancelOrder(orderId);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "订单已取消", null));
            return "order_list?faces-redirect=true";
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "取消失败：" + e.getMessage(), null));
            return "order_list?faces-redirect=true";
        }
    }

    public String goToCart() {
        if (!authController.isLoggedIn()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "请先登录", null));
            return "login?faces-redirect=true";
        }
        return "cart?faces-redirect=true";
    }

    public String goToOrders() {
        if (!authController.isLoggedIn()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "请先登录", null));
            return "login?faces-redirect=true";
        }
        return "order_list?faces-redirect=true";
    }

    public String goToProductList() {
        return "product_list?faces-redirect=true";
    }

    public double getCartTotal() {
        double total = cartItems.stream()
                .mapToDouble(item -> item.getQuantity() * item.getProduct().getPrice())
                .sum();
        return Math.max(0, total - pointsDiscount);
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public User getCurrentUser() { return authController.getCurrentUser(); }
    public void setCurrentUser(User currentUser) { authController.setCurrentUser(currentUser); }
    public List<CartItem> getCartItems() { return cartItems; }
    public void setCartItems(List<CartItem> cartItems) { this.cartItems = cartItems; }
    public int getPointsToUse() { return pointsToUse; }
    public void setPointsToUse(int pointsToUse) { this.pointsToUse = pointsToUse; }
    public double getPointsDiscount() { return pointsDiscount; }
    public void setPointsDiscount(double pointsDiscount) { this.pointsDiscount = pointsDiscount; }
    public List<Order> getOrders() {
        if (!authController.isLoggedIn()) {
            return new ArrayList<>();
        }
        return orderService.findOrdersByUser(authController.getCurrentUser().getId());
    }
}