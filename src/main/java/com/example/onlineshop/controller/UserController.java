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
public class UserController implements Serializable {

    @Inject
    private UserService userService;


    private User user;

    private String confirmPassword;

    public String register() {
        // 验证密码匹配
        if (!user.getPassword().equals(confirmPassword)) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "错误", "两次输入的密码不一致"));
            return null;
        }

        // 检查用户名是否已存在
        if (userService.findByUsername(user.getUsername()) != null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "错误", "用户名已存在"));
            return null;
        }

        try {

            user.setPoints(0L);
            user.setRole("USER");

            userService.registerUser(user);

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "成功", "注册成功"));


            user = new User();
            return "login?faces-redirect=true";

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "错误", "注册失败: " + e.getMessage()));
            return null;
        }
    }

    // Getters and Setters
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}