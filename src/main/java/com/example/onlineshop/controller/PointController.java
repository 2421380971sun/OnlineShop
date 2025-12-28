package com.example.onlineshop.controller;

import com.example.onlineshop.entity.PointRecord;
import com.example.onlineshop.service.PointService;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@Named
@SessionScoped
public class PointController implements Serializable {
    @Inject
    private PointService pointService;

    @Inject
    private AuthController authController;

    public List<PointRecord> getPointRecords() {
        if (!authController.isLoggedIn()) {
            return Collections.emptyList();
        }
        return pointService.findPointRecordsByUser(authController.getCurrentUser());
    }

    public String goToPoints() {
        if (!authController.isLoggedIn()) {
            return "login?faces-redirect=true";
        }
        return "points?faces-redirect=true";
    }
}