package com.xuelei.cloud.controller;

import com.xuelei.cloud.config.SAMLMessageHandler;
import com.xuelei.cloud.idp.entity.SPMetaData;
import com.xuelei.cloud.idp.service.SPMetaDataService;
import com.xuelei.cloud.idp.service.UserService;
import com.xuelei.cloud.opensaml.utils.SignUtil;
import lombok.extern.slf4j.Slf4j;
import net.shibboleth.utilities.java.support.codec.Base64Support;
import net.shibboleth.utilities.java.support.collection.Pair;
import net.shibboleth.utilities.java.support.net.URLBuilder;
import org.apache.velocity.app.VelocityEngine;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.RequestAbstractType;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.algorithm.AlgorithmSupport;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.SignatureValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.MalformedURLException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.util.List;

@Controller
@Slf4j
@RequestMapping(value = "idp")
public class IdPAuthController {
    
    @Autowired
    private SPMetaDataService spMetaDataService;
    
    @Autowired
    private VelocityEngine velocityEngine;
    
    @Autowired
    private UserService userService;
    
    
    @Autowired
    private SAMLMessageHandler samlMessageHandler;
    
//    @Value("${idp.entityId}")
//    private String idPEntity;
    
    @RequestMapping(value = "/single/login",  method = {RequestMethod.GET, RequestMethod.POST})
    public String SingleSignOn(HttpServletRequest request, HttpServletResponse response) {
    
        String httpMethod = request.getMethod();
        MessageContext<SAMLObject> messageContext = samlMessageHandler.getContext(request, response, httpMethod);
        boolean signatureCheckPass = false;
        if(httpMethod.equalsIgnoreCase("GET")) {
            signatureCheckPass = this.redirectSignatureCheck(request, messageContext);
        } else {
            signatureCheckPass = this.postSignatureCheck(messageContext);
        }
        if(signatureCheckPass == true) {
            log.info("Host: {}", request.getRemoteHost());
        }

        AuthnRequest authnRequest = (AuthnRequest) messageContext.getMessage();
        
//        String spEntityId = authnRequest.getIssuer().getValue();
//        SPMetaData spMetaData = spMetaDataService.getSpMetaDataByEntityId(spEntityId);
//        String x509Crt = spMetaData.getSpCredential().getX509Cert();
//        Signature signature = authnRequest.getSignature();
////        try {
////            SignUtil.validateSign(x509Crt, signature);
////        } catch (SignatureException e) {
////            return "Fail to validate signature";
////        }
        HttpSession session = request.getSession();
        if(authnRequest != null) {
            session.setAttribute("SAMLAuthn", authnRequest);
        }
//        try {
//            response.sendRedirect("idp/login");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return "redirect:/idp/login";
        return "login";
    }
    
    private Boolean postSignatureCheck(MessageContext<SAMLObject> messageContext) {
    
        boolean signCheckRes = false;
        AuthnRequest authnRequest = (AuthnRequest) messageContext.getMessage();
        String spEntityId = authnRequest.getIssuer().getValue();
        SPMetaData spMetaData = spMetaDataService.getSpMetaDataByEntityId(spEntityId);
        String x509Crt = spMetaData.getSpCredential().getX509Cert();
        Signature signature = authnRequest.getSignature();
        try {
            SignUtil.validateSign(x509Crt, signature);
            signCheckRes = true;
        } catch (SignatureException e) {
            signCheckRes = false;
            log.info("Fail to validate signature");
        }
        return signCheckRes;
        
//        boolean signatureCheckRes = false;
//        AuthnRequest authnRequest = (AuthnRequest) messageContext.getMessage();
//        org.opensaml.xmlsec.signature.Signature signature = authnRequest.getSignature();
//        Credential credential = null;
//        try {
//            credential = SignUtil.getSignCredential();
//        } catch (CertificateException e) {
//            e.printStackTrace();
//        }
//        try {
//            SignatureValidator.validate(signature, credential);
//            signatureCheckRes = true;
//            log.info("Validate Success");
//        } catch (org.opensaml.xmlsec.signature.support.SignatureException e) {
//            signatureCheckRes =false;
//            e.printStackTrace();
//        } finally {
//            return signatureCheckRes;
//        }
    }
    
    private Boolean redirectSignatureCheck(HttpServletRequest request, MessageContext<SAMLObject> messageContext) {
        
        
        AuthnRequest authnRequest = (AuthnRequest) messageContext.getMessage();
        String spEntityId = authnRequest.getIssuer().getValue();
        SPMetaData spMetaData = spMetaDataService.getSpMetaDataByEntityId(spEntityId);
        String spCrt = spMetaData.getSpCredential().getX509Cert();
        Credential credential = null;
        try {
            credential = SignUtil.getSignCheckCredential(spCrt);
        } catch (CertificateException e) {
            e.printStackTrace();
        }
        boolean signatureCheckRes = false;
        String algorithmUri = request.getParameter("SigAlg");
        String signatureSrc = request.getParameter("Signature");
        String message = request.getParameter("SAMLRequest");
        String endpoint = request.getRequestURL().toString();
        URLBuilder urlBuilder = null;
        try {
            urlBuilder = new URLBuilder(endpoint);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        
        List<Pair<String, String>> queryParams = urlBuilder.getQueryParams();
        queryParams.clear();
        SAMLObject outboundMessage = messageContext.getMessage();
        if (outboundMessage instanceof RequestAbstractType) {
            queryParams.add(new Pair<>("SAMLRequest", message));
        }
        queryParams.add(new Pair<>("SigAlg", algorithmUri));
        String sigMaterial = urlBuilder.buildQueryString();
        java.security.Signature signature = null;
        try {
            String algorithmID = AlgorithmSupport.getAlgorithmID(algorithmUri);
            signature = java.security.Signature.getInstance(algorithmID);
            PublicKey publicKey = credential.getPublicKey();
            signature.initVerify(publicKey);
            signature.update(sigMaterial.getBytes());
            signatureCheckRes = signature.verify(Base64Support.decode(signatureSrc));
        } catch (java.security.SignatureException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } finally {
            return signatureCheckRes;
        }
    }
}
