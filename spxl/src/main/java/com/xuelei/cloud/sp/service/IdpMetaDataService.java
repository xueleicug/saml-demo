/*
 * *************************************************************
 * Copyright © PING AN INSURANCE (GROUP) COMPANY OF CHINA ,LTD.
 * All Rights Reserved.
 * *************************************************************
 * PROJECT INFORMATION:
 * 项目名称：saml-demos
 * 文件名称：IdpMetaDataService.java
 * 代码说明：
 *
 * *************************************************************
 * CHANGE HISTORY:
 * Author			Date			Version		Reason
 * XUELEI915      2019/5/5         v1.0.0      初始创建。
 *
 * *************************************************************
 */
package com.xuelei.cloud.sp.service;

import com.xuelei.cloud.sp.entity.IdPMetaData;

public interface IdpMetaDataService {
    
    IdPMetaData getIdPMetaDataByEntityId(String idpEntityId);
    
    IdPMetaData getIdPMetaDataByEntityId(Integer id);
    
}
