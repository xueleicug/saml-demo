/*
 * *************************************************************
 * Copyright © PING AN INSURANCE (GROUP) COMPANY OF CHINA ,LTD.
 * All Rights Reserved.
 * *************************************************************
 * PROJECT INFORMATION:
 * 项目名称：saml-demos
 * 文件名称：IdPConstant.java
 * 代码说明：
 *
 * *************************************************************
 * CHANGE HISTORY:
 * Author			Date			Version		Reason
 * XUELEI915      2019/5/5         v1.0.0      初始创建。
 *
 * *************************************************************
 */
package com.xuelei.cloud;

import org.opensaml.xmlsec.signature.support.SignatureConstants;

public final class IdPConstant {
    
    public static final String IDP_SINGLE_LOGIN_URI = "/idp/single/login";
    
    public static final String IDP_LOGIN_URI = "/idp/login";
    
    public static final String IDP_LOGIN_AUTH_URI = "/idp/loginauth";
 
//    public static final String IDP_ENTITY_ID = "pingan.mockidp.com";
    
    public static final String SIGN_ALGOTITHM = SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256;
}
