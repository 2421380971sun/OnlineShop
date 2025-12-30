package com.example.onlineshop.webservice;

import com.example.onlineshop.entity.PointRecord;
import com.example.onlineshop.entity.User;
import com.example.onlineshop.service.PointService;
import com.example.onlineshop.service.UserService;
import jakarta.inject.Inject;
import jakarta.jws.WebService;
import jakarta.xml.ws.WebServiceException;

import java.util.List;
import java.util.logging.Logger;

@WebService(endpointInterface = "com.example.onlineshop.webservice.PointWebServiceApi")
public class PointWebService implements PointWebServiceApi {

    private static final Logger LOGGER = Logger.getLogger(PointWebService.class.getName());

    @Inject
    private UserService userService;

    @Inject
    private PointService pointService;

    @Override
    public int getUserPoints(Long userId) {
        LOGGER.info("Fetching points for userId: " + userId);

        User user = userService.findById(userId);
        if (user == null) {
            String errorMsg = "User not found for ID: " + userId;
            LOGGER.warning(errorMsg);
            throw new WebServiceException(errorMsg);
        }

        LOGGER.info("User found: " + user.getUsername() + ", Points: " + user.getPoints());
        return user.getPoints();
    }

    @Override
    public List<PointRecord> getUserPointRecords(Long userId) {
        LOGGER.info("Fetching point records for userId: " + userId);

        User user = userService.findById(userId);
        if (user == null) {
            String errorMsg = "User not found for ID: " + userId;
            LOGGER.warning(errorMsg);
            throw new WebServiceException(errorMsg);
        }

        List<PointRecord> records = pointService.findPointRecordsByUser(user);
        LOGGER.info("Found " + records.size() + " point records for user: " + user.getUsername());
        return records;
    }
}
