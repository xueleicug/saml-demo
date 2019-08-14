/*
 * *************************************************************
 * Copyright © PING AN INSURANCE (GROUP) COMPANY OF CHINA ,LTD.
 * All Rights Reserved.
 * *************************************************************
 * PROJECT INFORMATION:
 * 项目名称：saml-demos
 * 文件名称：SPMetaDataServiceImpl.java
 * 代码说明：
 *
 * *************************************************************
 * CHANGE HISTORY:
 * Author			Date			Version		Reason
 * XUELEI915      2019/5/5         v1.0.0      初始创建。
 *
 * *************************************************************
 */
package com.xuelei.cloud.idp.service.impl;

import com.xuelei.cloud.idp.dao.SPMetaDataDAO;
import com.xuelei.cloud.idp.entity.SPMetaData;
import com.xuelei.cloud.idp.service.SPMetaDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SPMetaDataServiceImpl implements SPMetaDataService {
    
    @Autowired
    private SPMetaDataDAO spMetaDataDAO;
    
    @Override
    public SPMetaData getSpMetaDataByEntityId(String spEntityId) {
        SPMetaData spMetaData = spMetaDataDAO.findBySpEntityId(spEntityId);
        return  spMetaData;
    }
}
