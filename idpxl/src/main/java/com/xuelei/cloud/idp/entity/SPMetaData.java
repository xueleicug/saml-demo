/*
 * *************************************************************
 * Copyright © PING AN INSURANCE (GROUP) COMPANY OF CHINA ,LTD.
 * All Rights Reserved.
 * *************************************************************
 * PROJECT INFORMATION:
 * 项目名称：saml-demos
 * 文件名称：SPMetaData.java
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
import javax.persistence.*;

@Entity
@Table(name = "saml_idp_sp_metadata")
@Data
@EqualsAndHashCode(callSuper = false)
public class SPMetaData extends IdEntity {

    @Column(name = "sp_entity_id")
    private String spEntityId;
    
    @Column(name = "single_sign_on_consumer")
    private String singleSignOnConsumerUrl;
    
    @Column(name = "single_logout_consumer")
    private String singleLogoutConsumerUrl;
    
    @Column(name = "sign_required")
    private boolean signatureRequired;

    @JoinColumn(name = "sp_credential_id")
    @OneToOne(fetch = FetchType.EAGER)
    private SPCredential spCredential;
}
