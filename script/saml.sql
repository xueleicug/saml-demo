-- 表1
-- saml.saml_idp_user
CREATE TABLE saml.`saml_idp_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `update_date` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `login_name` varchar(45) COLLATE utf8_unicode_ci DEFAULT NULL,
  `name` varchar(45) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '姓名',
  `email` varchar(100) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(50) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '电话',
  `title` varchar(100) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '职位',
  `password` varchar(500) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `IDX_LOGIN_NAME` (`login_name`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- saml.saml_idp_sp_metadata
CREATE TABLE saml.`saml_idp_sp_metadata` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `update_date` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `sp_entity_id` varchar(100) COLLATE utf8_unicode_ci DEFAULT NULL,
  `single_sign_on_consumer` varchar(200) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '单点登录断言消费者地址',
  `single_logout_consumer` varchar(200) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '单点登出断言消费者地址',
  `sign_required` varchar(50) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '是否验证签名',
  `sp_credential_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `IDX_ENTITY_ID` (`sp_entity_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- saml.saml_idp_sp_credential
CREATE TABLE saml.`saml_idp_sp_credential` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `update_date` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `private_key` varchar(1200) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT 'sp秘钥',
  `x509_cert` varchar(1200) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT 'sp公钥',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- saml.saml_sp_idp_metadata
CREATE TABLE saml.`saml_sp_idp_metadata` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `update_date` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `idp_entity_id` varchar(100) COLLATE utf8_unicode_ci DEFAULT NULL,
  `single_sign_on_url` varchar(200) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '单点登录地址',
  `single_logout_url` varchar(200) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '单点登出地址',
  `sign_required` varchar(50) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '是否校验签名',
  `idp_credential_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `IDX_ENTITY_ID` (`idp_entity_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- saml.saml_idp_sp_credential
CREATE TABLE saml.`saml_sp_idp_credential` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `update_date` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `private_key` varchar(1200) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT 'IdP秘钥',
  `x509_cert` varchar(1200) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT 'IdP公钥',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;


INSERT INTO saml.saml_idp_user 
(login_name, name, email, phone, title, password) 
values 
("xuelei915", "薛雷", "xuelei915@pingan.com.cn", "17688997550", "开发工程师" ,"xuelei915");

INSERT INTO saml.saml_idp_sp_credential 
(private_key, x509_cert) 
values 
('',
'MIICdDCCAd2gAwIBAgIBADANBgkqhkiG9w0BAQ0FADBXMQswCQYDVQQGEwJjbjES
MBAGA1UECAwJR3Vhbmdkb25nMREwDwYDVQQKDAhTaGVuemhlbjEhMB8GA1UEAwwY
aHR0cDovL3h1ZWxlaS5tb2Nrc3AuY29tMB4XDTE5MDgxMzEzNTYyMloXDTIxMDcx
MzEzNTYyMlowVzELMAkGA1UEBhMCY24xEjAQBgNVBAgMCUd1YW5nZG9uZzERMA8G
A1UECgwIU2hlbnpoZW4xITAfBgNVBAMMGGh0dHA6Ly94dWVsZWkubW9ja3NwLmNv
bTCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEA8vOQpVFvMAY/1TuHT7qopYK9
5mulfC3k0meptx8PLZ/1+Mqfq19dPfTTkwcoyoUgUKwPhawPkOAWvs5aLqXJLiqc
L6jYDEu5tSFCxIPGG9xlOMwTBDOmZhvM8PflUQExvwTNFBKUjiL+Q8RXmcxEvhJO
9CcuUSQJTwmdBpfUGx0CAwEAAaNQME4wHQYDVR0OBBYEFBvrZi20Luvi8E9VreYR
BAdgk8/4MB8GA1UdIwQYMBaAFBvrZi20Luvi8E9VreYRBAdgk8/4MAwGA1UdEwQF
MAMBAf8wDQYJKoZIhvcNAQENBQADgYEAfeuWeR/eCcca6UPmifQS8HbQxfNkpksS
moz5a00xOYsJGJLV0nEwlnmWpnp8dvMgfhpgOVCy1cHZC92Ihp7Fu74GL1gTKkEl
d5I3YX9HEEM/OdqFJ3LVgRivGU2s0+PRD2kJuL1r7uZDDztzgKBouRB8hxa49fp1
1hRyAuN2sv4=');

INSERT INTO saml.saml_idp_sp_metadata 
(sp_entity_id, single_sign_on_consumer, single_logout_consumer, sign_required, sp_credential_id)
values 
("xuelei.mocksp", "http://119.147.81.3:9999/sp/consumer/single/login", "http://119.147.81.3:9999/sp/consumer/logout", 1, 2);



INSERT INTO saml.saml_sp_idp_credential
(private_key, x509_cert) 
values 
('', 
'MIICdjCCAd+gAwIBAgIBADANBgkqhkiG9w0BAQ0FADBYMQswCQYDVQQGEwJjbjES
MBAGA1UECAwJR3Vhbmdkb25nMREwDwYDVQQKDAhTaGVuemhlbjEiMCAGA1UEAwwZ
aHR0cDovL3h1ZWxlaS5tb2NraWRwLmNvbTAeFw0xOTA4MTMxMzU3NDVaFw0yMTA3
MTMxMzU3NDVaMFgxCzAJBgNVBAYTAmNuMRIwEAYDVQQIDAlHdWFuZ2RvbmcxETAP
BgNVBAoMCFNoZW56aGVuMSIwIAYDVQQDDBlodHRwOi8veHVlbGVpLm1vY2tpZHAu
Y29tMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCbrDX9YgPvXGLB9XDH/taa
ZkYXZ577E8Ghaa60Hki+Apl94J18AYBZf/dJiOwZEPt9txaDmdQXwPI+rLPfKcZX
WE9JtfYlvE6fFc+ZrjEXwJPDUJ/1J6BZekwxJBVBRmj/CSvgTZurSKzmFtCgigBG
wcAq/Nn4BmrVwPj+NmtFMQIDAQABo1AwTjAdBgNVHQ4EFgQU1YwVlNAYdAUVXXTi
lq1r8AHY9dIwHwYDVR0jBBgwFoAU1YwVlNAYdAUVXXTilq1r8AHY9dIwDAYDVR0T
BAUwAwEB/zANBgkqhkiG9w0BAQ0FAAOBgQBO4K7EiOdJuWTogp/rCybILjucFfy9
PWOrFm+IdwYZ2ZNBbjTBwRTLohLM0aWOt64m3XSy4UgHGnBtSR3pemaAxYuxliri
jR/JErXEN9Qepfforb5mmGZIvsJNiPbTGAc1VQOFfQgBiuGZ/ZEuba1/zd6cgxgM
IYMpID0KQ/Srww==');

INSERT INTO saml.saml_sp_idp_metadata 
(idp_entity_id, single_sign_on_url, single_logout_url, sign_required, idp_credential_id) 
values 
('xuelei.mockidp', 'http://119.147.81.3:8888/idp/sso/login', 'http://119.147.81.3:8888/idp/sso/logout', 1, 2);

