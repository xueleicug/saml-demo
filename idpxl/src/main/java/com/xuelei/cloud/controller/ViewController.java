package com.xuelei.cloud.controller;

import com.google.common.collect.Lists;
import com.xuelei.cloud.IdPConstant;
import com.xuelei.cloud.idp.entity.SPMetaData;
import com.xuelei.cloud.idp.entity.User;
import com.xuelei.cloud.idp.service.SPMetaDataService;
import com.xuelei.cloud.idp.service.UserService;
import com.xuelei.cloud.opensaml.utils.OpenSAMLUtils;
import com.xuelei.cloud.opensaml.utils.SignUtil;
import lombok.extern.slf4j.Slf4j;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.app.VelocityEngine;
import org.joda.time.DateTime;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.core.xml.schema.impl.XSStringBuilder;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.encoder.MessageEncodingException;
import org.opensaml.saml.common.SignableSAMLObject;
import org.opensaml.saml.common.messaging.context.SAMLEndpointContext;
import org.opensaml.saml.common.messaging.context.SAMLPeerEntityContext;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.binding.encoding.impl.HTTPPostEncoder;
import org.opensaml.saml.saml2.core.*;
import org.opensaml.saml.saml2.metadata.AssertionConsumerService;
import org.opensaml.security.x509.X509Credential;
import org.opensaml.xmlsec.signature.Signature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.security.cert.CertificateException;
import java.util.*;

@Controller
@RequestMapping(value = "/idp")
@Slf4j
public class ViewController {
    
    @Value("${idp.entityId}")
    private String idPEntity;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private VelocityEngine velocityEngine;
    
    @Autowired
    private SPMetaDataService spMetaDataService;
    
    @RequestMapping(value = "/", method = {RequestMethod.GET, RequestMethod.POST})
    public String index() {
        return "";
    }
    
    @RequestMapping(value = "/login", method = {RequestMethod.GET, RequestMethod.POST})
    public String loginPage(HttpServletRequest request, HttpServletResponse response) {
        response.addHeader("Access-Control-Allow-Origin", "*");
        return "login";
    }
    
    @RequestMapping(value = "/loginauth", method = {RequestMethod.GET, RequestMethod.POST})
    public String login(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) {
        String userName = request.getParameter("username");
        String password = request.getParameter("password");
    
        User user = null;
        try {
            user = userService.queryUserByName(userName);
        } catch (Exception e) {
            return "userNotFound";
        }
    
        if (user.getPassword().equalsIgnoreCase(password)) {
            request.getSession().setAttribute("user", user);
        }
    
        HttpSession session = request.getSession();
        log.info("SessionId: {}", session.getId());
        log.info("Host: {}", request.getRemoteHost());
        if (session.getAttribute("SAMLAuthn") != null) {
    
            AuthnRequest authnRequest = (AuthnRequest) session.getAttribute("SAMLAuthn");
    
            String spEntityId = authnRequest.getIssuer().getValue();
            SPMetaData spMetaData = spMetaDataService.getSpMetaDataByEntityId(spEntityId);
            
            
            Status status = this.buildSuccessStatus();
            Response authnResponse = OpenSAMLUtils.buildSAMLObject(Response.class);
            Issuer issuer = buildIssuer();
            authnResponse.setIssuer(issuer);
            authnResponse.setID(OpenSAMLUtils.generateSecureRandomId());
            authnResponse.setIssueInstant(new DateTime());
            authnResponse.setInResponseTo(authnRequest.getID());
            authnResponse.setStatus(status);
            authnResponse.setDestination(spMetaData.getSingleSignOnConsumerUrl());
            Assertion assertion = OpenSAMLUtils.buildSAMLObject(Assertion.class);
            assertion.setID(randomSAMLId());
            assertion.setIssueInstant(new DateTime());
            Subject subject = this.buildSubject();
            subject.getNameID().setValue(user.getLoginName());
            Audience audience = buildAudience(spMetaData);
            AudienceRestriction audienceRestriction = buildAudienceRestrict(audience);
            Conditions conditions = buildConditions(audienceRestriction);
            AuthnContextClassRef authnContextClassRef = buildAuthnContextClassRef();
            AuthenticatingAuthority authenticatingAuthority = buildAuthenticatingAuthority();
            AuthnContext authnContext = buildAuthnContext(authnContextClassRef, authenticatingAuthority);
            AuthnStatement authnStatement = buildAuthnStatement(authnContext);
            assertion.setSubject(subject);
            assertion.setConditions(conditions);
            assertion.getAuthnStatements().add(authnStatement);
            this.buildAttributeStatement(assertion, user);
            authnResponse.getAssertions().add(assertion);
            this.signAssert(authnResponse, spMetaData);
            MessageContext outMessageContext = this.buildMessageContext(spMetaData, authnResponse);
            this.encodeMessageContext(response, outMessageContext);
            String authResponseStr = OpenSAMLUtils.logSAMLObject(authnResponse);
            log.info("AuthnResponse:\n{}", authResponseStr);
        }
        return null;
    }
    
    
    @GetMapping("/user")
    public String user(HttpServletRequest request, ModelMap modelMap) {

        User user = (User) request.getSession().getAttribute("user");
        modelMap.addAttribute("user", user);
        return "user";
    }
    
    
    private void encodeMessageContext(HttpServletResponse response, MessageContext outMessageContext) {
        HTTPPostEncoder encoder = new HTTPPostEncoder();
        encoder.setVelocityEngine(velocityEngine);
        encoder.setMessageContext(outMessageContext);
        encoder.setHttpServletResponse(response);
        try {
            encoder.initialize();
            encoder.encode();
        } catch (ComponentInitializationException | MessageEncodingException e) {
            e.printStackTrace();
        }
    }

    private MessageContext buildMessageContext(SPMetaData spMetaData, Response authnResponse) {
        MessageContext outMessageContext = new MessageContext();
        outMessageContext.setMessage(authnResponse);
        SAMLPeerEntityContext peerEntityContext = outMessageContext.getSubcontext(SAMLPeerEntityContext.class, true);
        SAMLEndpointContext endpointContext = peerEntityContext.getSubcontext(SAMLEndpointContext.class, true);
        endpointContext.setEndpoint(getSPEndpoint(spMetaData.getSingleSignOnConsumerUrl()));
        return outMessageContext;
    }

    private void buildAttributeStatement(Assertion assertion, User user) {
        AttributeStatement attributeStatement = OpenSAMLUtils.buildSAMLObject(AttributeStatement.class);
        List<Attribute> attributeList = buildUserAttributeList(user);
        attributeStatement.getAttributes().addAll(attributeList);
        assertion.getAttributeStatements().add(attributeStatement);
    }

    private void signAssert(SignableSAMLObject signableSAMLObject, SPMetaData spMetaData) {
        try {
            X509Credential signCredential = SignUtil.getSignCredential();
//            X509Credential signCredential = SignUtil.getSignCheckCredential(spMetaData.getSpCredential().getX509Cert());
//            Signature sign = OpenSAMLUtils.buildSAMLObject(Signature.class);
//            signableSAMLObject.setSignature(sign);
            SignUtil.setSignature(signableSAMLObject, IdPConstant.SIGN_ALGOTITHM, signCredential);
        } catch (CertificateException e) {
            log.info("Signature Fail");
            e.printStackTrace();
        }
    }

    private List<Attribute> buildUserAttributeList(User user) {

        List<Attribute> attributeList = Lists.newArrayList();

        String groupValue = user.getGroupStr();
        if(!StringUtils.isEmpty(groupValue)) {
            String[] groups = groupValue.split(",");
            List<String> groupNames = Arrays.asList(groups);
            Attribute groupAttribute = this.buildUserAttributeWithMultiValue("groupName", "用户群组", groupNames);
            attributeList.add(groupAttribute);
        }
        
        String policyValue = user.getPolicyStr();
        if(!StringUtils.isEmpty(policyValue)) {
            String[] policys = policyValue.split(",");
            List<String> policyNames = Arrays.asList(policys);
            Attribute policyAttribute = this.buildUserAttributeWithMultiValue("policiesName", "用户权限", policyNames);
            attributeList.add(policyAttribute);
        }
        
        if(!StringUtils.isEmpty(user.getPhone())) {
            Attribute phoneAttribute = this.buildUserAttribute("phone", "手机号", user.getPhone());
            attributeList.add(phoneAttribute);
        }
        
        if(!StringUtils.isEmpty(user.getEmail())) {
            Attribute emailAttribute = this.buildUserAttribute("email", "邮箱", user.getEmail());
            attributeList.add(emailAttribute);
        }
        
        if(!StringUtils.isEmpty(user.getName())) {
            Attribute nameAttribute = this.buildUserAttribute("displayName", "姓名", user.getName());
            attributeList.add(nameAttribute);
        }
        
        if(!StringUtils.isEmpty(user.getTitle())) {
            Attribute titleAttribute = this.buildUserAttribute("title", "职务", user.getTitle());
            attributeList.add(titleAttribute);
        }
        
        return attributeList;
    }

    private Attribute buildUserAttribute(String name, String friendlyName, String value) {

        Attribute attribute = OpenSAMLUtils.buildSAMLObject(Attribute.class);
        attribute.setName(name);
        attribute.setFriendlyName(friendlyName);
        attribute.setNameFormat(Attribute.BASIC);
        XSStringBuilder xsStringBuilder = new XSStringBuilder();
        XSString strValue = xsStringBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, org.opensaml.xml.schema.XSString.TYPE_NAME);
        strValue.setValue(value);
        List<XSString> xsStringList = new ArrayList<>();
        xsStringList.add(strValue);
        attribute.getAttributeValues().addAll(xsStringList);
        return attribute;
    }

    private Attribute buildUserAttributeWithMultiValue(String name, String friendlyName, List<String> values) {

        Attribute attribute = OpenSAMLUtils.buildSAMLObject(Attribute.class);
        attribute.setName(name);
        attribute.setFriendlyName(friendlyName);
        attribute.setNameFormat(Attribute.BASIC);
        XSStringBuilder xsStringBuilder = new XSStringBuilder();
        List<XSString> xsStringList = new ArrayList<>();
        for(String value : values) {
            XSString strValue = xsStringBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, org.opensaml.xml.schema.XSString.TYPE_NAME);
            strValue.setValue(value);
            xsStringList.add(strValue);
        }
        attribute.getAttributeValues().addAll(xsStringList);
        return attribute;
    }

    private AuthnStatement buildAuthnStatement(AuthnContext authnContext) {
        AuthnStatement authnStatement = OpenSAMLUtils.buildSAMLObject(AuthnStatement.class);
        authnStatement.setAuthnContext(authnContext);
        authnStatement.setAuthnInstant(new DateTime());
        return authnStatement;
    }

    private AuthnContext buildAuthnContext(AuthnContextClassRef authnContextClassRef, AuthenticatingAuthority authenticatingAuthority) {
        AuthnContext authnContext = OpenSAMLUtils.buildSAMLObject(AuthnContext.class);
        authnContext.setAuthnContextClassRef(authnContextClassRef);
        authnContext.getAuthenticatingAuthorities().add(authenticatingAuthority);
        return authnContext;
    }

    private AuthenticatingAuthority buildAuthenticatingAuthority() {
        return OpenSAMLUtils.buildSAMLObject(AuthenticatingAuthority.class);
    }

    private AuthnContextClassRef buildAuthnContextClassRef() {
        return OpenSAMLUtils.buildSAMLObject(AuthnContextClassRef.class);
    }

    private Conditions buildConditions(AudienceRestriction audienceRestriction) {
        Conditions conditions = OpenSAMLUtils.buildSAMLObject(Conditions.class);
        conditions.getAudienceRestrictions().add(audienceRestriction);

        Date dateNow = new Date();
        DateTime dateTimeNow = new DateTime(dateNow);
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(dateNow);
        calendar.add(calendar.DATE,1);//把日期往前减少一天，若想把日期向后推一天则将负数改为正数
        Date dateTommorrow = calendar.getTime();
        DateTime dateTimeTommorrow = new DateTime(dateTommorrow);
        conditions.setNotBefore(dateTimeNow);
        conditions.setNotOnOrAfter(dateTimeTommorrow);
        return conditions;
    }

    private AudienceRestriction buildAudienceRestrict(Audience audience) {
        AudienceRestriction audienceRestriction = OpenSAMLUtils.buildSAMLObject(AudienceRestriction.class);
        audienceRestriction.getAudiences().add(audience);
        return audienceRestriction;
    }

    private Audience buildAudience(SPMetaData spMetaData) {
        Audience audience = OpenSAMLUtils.buildSAMLObject(Audience.class);
        audience.setAudienceURI(spMetaData.getSpEntityId());
        return audience;
    }

    private Subject buildSubject() {
        Subject subject = OpenSAMLUtils.buildSAMLObject(Subject.class);
        NameID nameID = OpenSAMLUtils.buildSAMLObject(NameID.class);
        nameID.setFormat(NameIDType.ENTITY);
        subject.setNameID(nameID);
        return subject;
    }

    private Issuer buildIssuer() {
        Issuer issuer = OpenSAMLUtils.buildSAMLObject(Issuer.class);
        issuer.setValue(idPEntity);
        issuer.setFormat(NameIDType.ENTITY);
        return issuer;
    }

    private Status buildSuccessStatus() {
        Status status = OpenSAMLUtils.buildSAMLObject(Status.class);
        StatusCode statusCode = OpenSAMLUtils.buildSAMLObject(StatusCode.class);
        statusCode.setValue(StatusCode.SUCCESS);
        status.setStatusCode(statusCode);
        return status;
    }

    private String randomSAMLId() {
        return "_" + UUID.randomUUID().toString();
    }

    public AssertionConsumerService getSPEndpoint(String consumerUrl) {

        AssertionConsumerService assertionConsumerService = OpenSAMLUtils.buildSAMLObject(AssertionConsumerService.class);
        assertionConsumerService.setBinding(SAMLConstants.SAML2_POST_BINDING_URI);
        assertionConsumerService.setLocation(consumerUrl);
        return assertionConsumerService;
    }
    
}
