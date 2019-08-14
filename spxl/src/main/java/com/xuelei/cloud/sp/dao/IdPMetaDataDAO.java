/*
 * *************************************************************
 * Copyright © PING AN INSURANCE (GROUP) COMPANY OF CHINA ,LTD.
 * All Rights Reserved.
 * *************************************************************
 * PROJECT INFORMATION:
 * 项目名称：saml-demos
 * 文件名称：IdPMetaDataDAO.java
 * 代码说明：
 *
 * *************************************************************
 * CHANGE HISTORY:
 * Author			Date			Version		Reason
 * XUELEI915      2019/5/5         v1.0.0		    初始创建。
 *
 * *************************************************************
 */
package com.xuelei.cloud.sp.dao;

import com.xuelei.cloud.sp.entity.IdPMetaData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IdPMetaDataDAO extends JpaRepository<IdPMetaData, Integer> {

    IdPMetaData findByIdpEntityId(String idpEntity);
    
}
