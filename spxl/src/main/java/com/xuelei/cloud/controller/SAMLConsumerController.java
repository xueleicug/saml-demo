package com.xuelei.cloud.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.xuelei.cloud.config.SAMLMessageHandler;
import com.xuelei.cloud.opensaml.utils.SignUtil;
import com.xuelei.cloud.sp.entity.IdPMetaData;
import com.xuelei.cloud.sp.service.IdpMetaDataService;
import lombok.extern.slf4j.Slf4j;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping(value = "/sp")
public class SAMLConsumerController {
    
    @Autowired
    private SAMLMessageHandler samlMessageHandler;
    
    @Autowired
    private IdpMetaDataService idpMetaDataService;
    
    @Value("${sp.responseUrl}")
    private String responseUrl;
    
    @RequestMapping("/consumer/single/login")
    public String consumer(HttpServletRequest request, HttpServletResponse response) {
    
        String httpMethod = request.getMethod();
        MessageContext<SAMLObject> messageContext = samlMessageHandler.getContext(request, response, httpMethod);
        Response authnResponse = (Response) messageContext.getMessage();

        String idpEntity = authnResponse.getIssuer().getValue();
        IdPMetaData idPMetaData = idpMetaDataService.getIdPMetaDataByEntityId(idpEntity);

        Signature signature = authnResponse.getSignature();
        try {
            SignUtil.validateSign(signature);
        } catch (SignatureException e) {
            e.printStackTrace();
        }

        StringBuffer stringBuffer = new StringBuffer();
        List<Attribute> attributeList = authnResponse.getAssertions().get(0).getAttributeStatements().get(0).getAttributes();
        for (int i = 0; i < attributeList.size(); i++) {
            Attribute attribute = attributeList.get(i);
            String name = attribute.getFriendlyName();
            String value = ((XSString)attribute.getAttributeValues().get(0)).getValue();
            stringBuffer.append(name).append(" : ").append(value).append("<br>");
        }
        return "success";
        
//        String samlResponse = request.getParameter("SAMLResponse");
//        String url = responseUrl + "?Action=ResolveSamlLogin";
//        HttpHeaders header = this.buildHeaders();
//        HttpHeaders headers = new HttpHeaders();
//        headers.add("Content-Type", "application/x-www-form-urlencoded");
//        MultiValueMap<String, Object> postParameters = new LinkedMultiValueMap<>();
//        postParameters.add("SAMLResponse", samlResponse);
//        postParameters.add("businessId", "d3d8555501134558a8ca538eb7e11089");
//        postParameters.add("idPConfigName", "idplocal");
//        String resp = null;
//        try {
//            resp = this.doPost(HttpMethod.POST, url, postParameters, header);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return "Fail";
//        }
//
//        JSONObject object = JSONObject.parseObject(resp);
//        String formatStr = JSON.toJSONString(object, SerializerFeature.PrettyFormat);
//        return formatStr;
    }
    
    private String doPost(HttpMethod httpMethod, String url, MultiValueMap<String, Object> paramMap, HttpHeaders headers) throws Exception {
        
        RestTemplate restTemplate = this.getRestTemplate();
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(paramMap, headers);
        log.info(">>> Start to invoke captcha interface [{}]", url);
        long startMills = System.currentTimeMillis();
        String responseMessage = null;
        try {
            responseMessage = restTemplate.postForObject(url, requestEntity, String.class);
        } catch (Exception e) {
            log.info(">>> Fail to invoke pa interface [{}]", url);
            log.info("Exception info: {}", e.getMessage());
            throw new Exception();
        }
        Long endMills = System.currentTimeMillis();
        log.info(">>> End to invoke captcha interface [{}], cost time [{}]ms, result：[{}]", url, endMills - startMills, responseMessage);
        return responseMessage;
    }
    
    private RestTemplate getRestTemplate() {
        HttpComponentsClientHttpRequestFactory httpComponentsClientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        httpComponentsClientHttpRequestFactory.setConnectTimeout(100000);
        httpComponentsClientHttpRequestFactory.setReadTimeout(100000);
        RestTemplate restTemplate = new RestTemplate(httpComponentsClientHttpRequestFactory);
        return restTemplate;
    }
    
    private HttpHeaders buildHeaders() {
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON_UTF8));
        return headers;
    }
}
