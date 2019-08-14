/*
 * *************************************************************
 * Copyright © PING AN INSURANCE (GROUP) COMPANY OF CHINA ,LTD.
 * All Rights Reserved.
 * *************************************************************
 * PROJECT INFORMATION:
 * 项目名称：saml-demos
 * 文件名称：IdPMetadata.java
 * 代码说明：
 *
 * *************************************************************
 * CHANGE HISTORY:
 * Author			Date			Version		Reason
 * XUELEI915      2019/5/5         v1.0.0      初始创建。
 *
 * *************************************************************
 */
package com.xuelei.cloud.sp.entity;

import com.xuelei.cloud.entity.IdEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.persistence.*;

@Entity
@Table(name = "saml_sp_idp_metadata")
@Data
@EqualsAndHashCode(callSuper = false)
public class IdPMetaData extends IdEntity {
    
    @Column(name = "idp_entity_id")
    private String idpEntityId;
    
    @Column(name = "single_sign_on_url")
    private String singleSignOnUrl;
    
    @Column(name = "single_logout_url")
    private String singleLogoutUrl;
    
    @Column(name = "sign_required")
    private Boolean signRequired;
    
    @JoinColumn(name = "idp_credential_id")
    @OneToOne(fetch = FetchType.EAGER)
    private IdPCredential idpCredential;
}
