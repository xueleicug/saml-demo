/*
 * *************************************************************
 * Copyright © PING AN INSURANCE (GROUP) COMPANY OF CHINA ,LTD.
 * All Rights Reserved.
 * *************************************************************
 * PROJECT INFORMATION:
 * 项目名称：saml-demos
 * 文件名称：SPCredential.java
 * 代码说明：
 *
 * *************************************************************
 * CHANGE HISTORY:
 * Author			Date			Version		Reason
 * XUELEI915      2019/5/5         v1.0.0      初始创建。
 *
 * *************************************************************
 */
package com.xuelei.cloud.idp.entity;

import com.xuelei.cloud.entity.IdEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "saml_idp_sp_credential")
@Data
@EqualsAndHashCode(callSuper = false)
public class SPCredential extends IdEntity {

//    @Column(name = "private_key")
//    private String privateKey;
    
    @Column(name = "x509_cert")
    private String x509Cert;
}
