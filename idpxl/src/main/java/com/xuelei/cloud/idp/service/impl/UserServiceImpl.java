/*
 * *************************************************************
 * Copyright © PING AN INSURANCE (GROUP) COMPANY OF CHINA ,LTD.
 * All Rights Reserved.
 * *************************************************************
 * PROJECT INFORMATION:
 * 项目名称：saml-demos
 * 文件名称：UserServiceImpl.java
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

import com.xuelei.cloud.idp.dao.UserDAO;
import com.xuelei.cloud.idp.entity.User;
import com.xuelei.cloud.idp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;

@Service
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserDAO userDAO;
    
    @Override
    public User queryUserByName(String loginName) throws Exception {
        User user = userDAO.findByLoginName(loginName);
        if(user == null) {
            throw new Exception(loginName);
        }
        return user;
    }
    
//    @Override
//    public Boolean loginAuth(String loginName, String password) {
//        User user = null;
//        try {
//            user = this.queryUserByName(loginName);
//        }
//        if(user != null) {
//            if(user.getLoginName().equalsIgnoreCase(loginName) && user.getPassword().equals(password)) {
//                return true;
//            }
//        }
//        return false;
//    }
}
