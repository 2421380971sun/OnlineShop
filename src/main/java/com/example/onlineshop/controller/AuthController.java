package com.example.onlineshop.controller;

import com.example.onlineshop.entity.User;
import com.example.onlineshop.service.UserService;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;

@Named
@SessionScoped
public class AuthController implements Serializable {
    private User currentUser;
    private String username;
    private String password;

    @Inject
    private UserService userService;

    public String login() {
        try {
            currentUser = userService.authenticate(username, password);
            if (currentUser != null) {
                if ("USER".equals(currentUser.getRole())) {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "权限不足", "仅管理员可访问"));
                    currentUser = null;
                    return "login?faces-redirect=true";
                }
                return "product_list?faces-redirect=true";
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "登录失败", "用户名或密码错误"));
                return "login?faces-redirect=true";
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "登录错误", "系统错误，请稍后重试"));
            return "login?faces-redirect=true";
        }
    }

    public String logout() {
        currentUser = null;
        username = null;
        password = null;
        return "login?faces-redirect=true";
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}