package com.example.onlineshop.controller;

import com.example.onlineshop.entity.User;
import com.example.onlineshop.service.UserService;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named
@SessionScoped
public class UserController implements Serializable {
    @Inject
    private UserService userService;

    private List<User> users;
    private User selectedUser;
    // [ME] 修复：手动实例化，防止 PropertyNotFoundException
    private User user = new User(); // 用于注册
    private String confirmPassword; // 确认密码

    public void init() {
        users = userService.findAllUsers();
        selectedUser = null;
    }

    public String editUser(Long userId) {
        selectedUser = userService.findById(userId);
        if (selectedUser == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "错误", "用户不存在"));
            return null;
        }
        return null; // 留在当前页面
    }

    public String saveUser() {
        try {
            userService.updateUser(selectedUser);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "成功", "用户信息已更新"));
            init(); // 刷新用户列表
            return null;
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "错误", "更新用户失败: " + e.getMessage()));
            return null;
        }
    }

    public String cancelEdit() {
        selectedUser = null;
        return null;
    }

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
            // 设置默认值
            user.setPoints(0); // int 类型
            user.setRole(User.Role.USER); // 枚举类型
            userService.registerUser(user);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "成功", "注册成功，请登录"));
            // 清空表单
            user = new User();
            confirmPassword = null;
            return "login?faces-redirect=true";
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "错误", "注册失败: " + e.getMessage()));
            return null;
        }
    }

    public List<User> getUsers() {
        if (users == null) {
            init();
        }
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public User getSelectedUser() {
        return selectedUser;
    }

    public void setSelectedUser(User selectedUser) {
        this.selectedUser = selectedUser;
    }

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
