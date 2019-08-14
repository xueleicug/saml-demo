/*
 * *************************************************************
 * Copyright © PING AN INSURANCE (GROUP) COMPANY OF CHINA ,LTD.
 * All Rights Reserved.
 * *************************************************************
 * PROJECT INFORMATION:
 * 项目名称：saml-demos
 * 文件名称：User.java
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
@Table(name = "saml_idp_user")
@Data
@EqualsAndHashCode(callSuper = false)
public class User extends IdEntity {

    @Column(name = "login_name")
    private String loginName;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "email")
    private String email;
    
    @Column(name = "phone")
    private String phone;
    
    @Column(name = "title")
    private String title;
    
    @Column(name = "password")
    private String password;
    
    @Column(name = "groups")
    private String groupStr;
    
    @Column(name = "policies")
    private String policyStr;

}
