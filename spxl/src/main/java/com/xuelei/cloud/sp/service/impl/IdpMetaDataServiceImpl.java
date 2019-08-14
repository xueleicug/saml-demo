/*
 * *************************************************************
 * Copyright © PING AN INSURANCE (GROUP) COMPANY OF CHINA ,LTD.
 * All Rights Reserved.
 * *************************************************************
 * PROJECT INFORMATION:
 * 项目名称：saml-demos
 * 文件名称：IdpMetaDataServiceImpl.java
 * 代码说明：
 *
 * *************************************************************
 * CHANGE HISTORY:
 * Author			Date			Version		Reason
 * XUELEI915      2019/5/5         v1.0.0      初始创建。
 *
 * *************************************************************
 */
package com.xuelei.cloud.sp.service.impl;

import com.xuelei.cloud.sp.dao.IdPMetaDataDAO;
import com.xuelei.cloud.sp.entity.IdPMetaData;
import com.xuelei.cloud.sp.service.IdpMetaDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IdpMetaDataServiceImpl implements IdpMetaDataService {
    
    @Autowired
    private IdPMetaDataDAO idPMetaDataDAO;
    
    @Override
    public IdPMetaData getIdPMetaDataByEntityId(String idpEntityId) {
        IdPMetaData idPMetaData = idPMetaDataDAO.findByIdpEntityId(idpEntityId);
        return idPMetaData;
    }
    
    @Override
    public IdPMetaData getIdPMetaDataByEntityId(Integer id) {
        IdPMetaData idPMetaData = idPMetaDataDAO.findOne(id);
        return idPMetaData;
    }
}
