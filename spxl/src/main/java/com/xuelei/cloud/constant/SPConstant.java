/*
 * *************************************************************
 * Copyright © PING AN INSURANCE (GROUP) COMPANY OF CHINA ,LTD.
 * All Rights Reserved.
 * *************************************************************
 * PROJECT INFORMATION:
 * 项目名称：saml-demos
 * 文件名称：SPConstant.java
 * 代码说明：
 *
 * *************************************************************
 * CHANGE HISTORY:
 * Author			Date			Version		Reason
 * XUELEI915      2019/5/5         v1.0.0      初始创建。
 *
 * *************************************************************
 */
package com.xuelei.cloud.constant;

import org.opensaml.xmlsec.signature.support.SignatureConstants;

public final class SPConstant {
    
//    public static final String SP_SINGLE_LOGIN_CONSUMER = "http://localhost:9999/sp/consumer/single/login";
    
//    public static final String SP_SINGLE_LOGOUT_CONSUMER = "http://localhost:9999/sp/consumer/single/logout";
    
    public static final String SP_ISSURE_NAME = "spmock";
    
//    public static final String SP_ENTITY_ID = "pingan.mocksp.com";
    
    public static final String SIGN_ALGOTITHM = SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256;
}
