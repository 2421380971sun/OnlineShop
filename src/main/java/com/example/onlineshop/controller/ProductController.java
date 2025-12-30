package com.example.onlineshop.controller;

import com.example.onlineshop.entity.Product;
import com.example.onlineshop.service.ProductService;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;

@Named
@SessionScoped
public class ProductController implements Serializable {

    @Inject
    private ProductService productService;

    private Product product = new Product(); // 用于添加或编辑商品
    private Long editProductId; // 用于编辑时保存商品 ID
    private String selectedCategory; // 用于分类筛选

    // 添加商品
    public String addProduct() {
        productService.addProduct(product);
        product = new Product(); // 清空表单
        return "product_list?faces-redirect=true";
    }

    // 开始编辑商品
    public String editProduct(Long id) {
        this.editProductId = id;
        this.product = productService.findProductById(id);
        if (this.product == null) {
            // 如果商品不存在，返回列表页面
            return "product_list?faces-redirect=true";
        }
        return "edit_product?faces-redirect=true";
    }

    // 保存编辑
    public String saveEdit() {
        productService.updateProduct(product);
        product = new Product(); // 清空表单
        return "product_list?faces-redirect=true";
    }

    // 删除商品
    public String deleteProduct(Long id) {
        productService.deleteProduct(id);
        return "product_list?faces-redirect=true";
    }

    // 获取所有商品
    public List<Product> getAllProducts() {
        return productService.findAllProducts();
    }

    // 按分类获取商品
    public List<Product> getProductsByCategory() {
        if (selectedCategory == null || selectedCategory.isEmpty()) {
            return getAllProducts();
        }
        return productService.findProductsByCategory(selectedCategory);
    }


    public String goToAddProduct() {
            // 创建一个新的Product对象
            product = new Product();
            // 返回add_product页面，并重定向
            return "add_product?faces-redirect=true";
        }

    public String cancelAddProduct() {
        product = new Product(); // 清空表单
        return "product_list?faces-redirect=true";
    }
    // Getters and Setters
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public Long getEditProductId() { return editProductId; }
    public void setEditProductId(Long editProductId) { this.editProductId = editProductId; }
    public String getSelectedCategory() { return selectedCategory; }
    public void setSelectedCategory(String selectedCategory) { this.selectedCategory = selectedCategory; }
}