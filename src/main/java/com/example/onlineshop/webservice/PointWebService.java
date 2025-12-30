package com.example.onlineshop.webservice;

import com.example.onlineshop.entity.PointRecord;
import com.example.onlineshop.entity.User;
import com.example.onlineshop.util.PersistenceUtil;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;
import jakarta.persistence.EntityManager;
import java.util.List;

@WebService(serviceName = "PointService")
public class PointWebService {

    @WebMethod(operationName = "getUserPoints")
    public int getUserPoints(@WebParam(name = "username") String username) {
        EntityManager em = PersistenceUtil.getEntityManager();
        try {
            User user = em.createQuery("SELECT u FROM User u WHERE u.username = :username", User.class)
                    .setParameter("username", username)
                    .getSingleResult();
            return user.getPoints();
        } catch (Exception e) {
            return -1; // 用户不存在或错误
        } finally {
            em.close();
        }
    }

    // ... 其他 WebMethod 实现
}