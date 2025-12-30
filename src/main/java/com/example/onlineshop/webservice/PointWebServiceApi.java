package com.example.onlineshop.webservice;

import com.example.onlineshop.entity.PointRecord;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;

import java.util.List;

@WebService
public interface PointWebServiceApi {

    @WebMethod
    int getUserPoints(@WebParam(name = "userId") Long userId);

    @WebMethod
    List<PointRecord> getUserPointRecords(@WebParam(name = "userId") Long userId);
}