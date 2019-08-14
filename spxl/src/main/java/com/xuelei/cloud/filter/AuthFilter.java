package com.xuelei.cloud.filter;

import com.xuelei.cloud.constant.SPConstant;
import com.xuelei.cloud.opensaml.utils.OpenSAMLUtils;
import com.xuelei.cloud.opensaml.utils.SignUtil;
import com.xuelei.cloud.sp.entity.IdPMetaData;
import com.xuelei.cloud.sp.service.IdpMetaDataService;
import lombok.extern.slf4j.Slf4j;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import org.apache.velocity.app.VelocityEngine;
import org.joda.time.DateTime;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.encoder.MessageEncodingException;
import org.opensaml.saml.common.messaging.context.SAMLEndpointContext;
import org.opensaml.saml.common.messaging.context.SAMLPeerEntityContext;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.binding.encoding.impl.HTTPPostEncoder;
import org.opensaml.saml.saml2.binding.encoding.impl.HTTPRedirectDeflateEncoder;
import org.opensaml.saml.saml2.core.*;
import org.opensaml.saml.saml2.metadata.Endpoint;
import org.opensaml.saml.saml2.metadata.SingleSignOnService;
import org.opensaml.security.x509.X509Credential;
import org.opensaml.xmlsec.SignatureSigningParameters;
import org.opensaml.xmlsec.context.SecurityParametersContext;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.cert.CertificateException;

@Slf4j
@WebFilter(filterName = "authFilter", urlPatterns = "/*")
//@Component
public class AuthFilter implements Filter {
    
    @Autowired
    private VelocityEngine velocityEngine;
    
    @Autowired
    private IdpMetaDataService idpMetaDataService;
    
    @Value("${sp.loginConsumer}")
    private String spLoginConsumer;
//
    @Value("${sp.entityId}")
    private String spEntityId;
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        try {
            log.info("Initializing");
            //正式初始化SAML服务
            InitializationService.initialize();
        } catch (InitializationException e) {
            throw new RuntimeException("Initialization failed");
        }
    }
    
    @Override
    public void doFilter(ServletRequest reqt, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
    
        HttpServletRequest request = (HttpServletRequest) reqt;
        HttpServletResponse response = (HttpServletResponse) resp;
        String reqestUri = request.getRequestURI();
        HttpSession session = request.getSession();
        if (this.needIdPAuth(reqestUri)) {
            if (session.getAttribute("user") == null)
            {
                Integer idpId = Integer.valueOf(request.getParameter("idpid"));
                IdPMetaData idPMetaData = idpMetaDataService.getIdPMetaDataByEntityId(idpId);
                AuthnRequest authnRequest = this.buildAuthRequest(idPMetaData);
                this.redirectUserForAuthentication(response, authnRequest, idPMetaData);
                return;
            }
        }
        chain.doFilter(reqt, resp);
    }
    
    @Override
    public void destroy () {
    
    }

    private boolean needIdPAuth(String uri) {
        if(uri.equals("/sp/login")) {
            return true;
        } else {
            return false;
        }
    }
    
    private AuthnRequest buildAuthRequest(IdPMetaData idPMetaData) {
        
        AuthnRequest authnRequest = OpenSAMLUtils.buildSAMLObject(AuthnRequest.class);
        authnRequest.setIssueInstant(new DateTime());
        authnRequest.setDestination(idPMetaData.getSingleSignOnUrl());
        authnRequest.setProtocolBinding(SAMLConstants.SAML2_POST_BINDING_URI);
        authnRequest.setAssertionConsumerServiceURL(spLoginConsumer);
        authnRequest.setID(OpenSAMLUtils.generateSecureRandomId());
        authnRequest.setIssuer(this.buildSPInssuer());
        authnRequest.setNameIDPolicy(buildNameIdPolicy());
        authnRequest.setRequestedAuthnContext(this.buildReuqestedAuthnContext());
        return authnRequest;
    }
    
    private Issuer buildSPInssuer() {
        Issuer issuer = OpenSAMLUtils.buildSAMLObject(Issuer.class);
        issuer.setValue(spEntityId);
        return issuer;
    }
    
    private NameIDPolicy buildNameIdPolicy() {
        NameIDPolicy nameIDPolicy = OpenSAMLUtils.buildSAMLObject(NameIDPolicy.class);
        nameIDPolicy.setAllowCreate(true);
        nameIDPolicy.setFormat(NameIDType.ENTITY);
        return nameIDPolicy;
    }
    
    private RequestedAuthnContext buildReuqestedAuthnContext() {
        RequestedAuthnContext requestedAuthnContext = OpenSAMLUtils.buildSAMLObject(RequestedAuthnContext.class);
        requestedAuthnContext.setComparison(AuthnContextComparisonTypeEnumeration.MINIMUM);
        AuthnContextClassRef passwordAuthnContextClassRef = OpenSAMLUtils.buildSAMLObject(AuthnContextClassRef.class);
        passwordAuthnContextClassRef.setAuthnContextClassRef(AuthnContext.PASSWORD_AUTHN_CTX);
        requestedAuthnContext.getAuthnContextClassRefs().add(passwordAuthnContextClassRef);
        return requestedAuthnContext;
    }
    
    private void redirectUserForAuthentication(HttpServletResponse response, AuthnRequest authnRequest, IdPMetaData idPMetaData) {
    
        MessageContext context = new MessageContext();
        context.setMessage(authnRequest);
        SAMLPeerEntityContext peerEntityContext = context.getSubcontext(SAMLPeerEntityContext.class, true);
        SAMLEndpointContext endpointContext = peerEntityContext.getSubcontext(SAMLEndpointContext.class, true);
        endpointContext.setEndpoint(getIdPEndpoint(idPMetaData));
        
        SignatureSigningParameters signatureSigningParameters = new SignatureSigningParameters();
        X509Credential credential = null;
        try {
            credential = SignUtil.getSignCredential();
        } catch (CertificateException e) {
            e.printStackTrace();
        }
        signatureSigningParameters.setSigningCredential(credential);
        signatureSigningParameters.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256);
        SignUtil.setSignature(authnRequest,SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256, credential);
        SecurityParametersContext securityContext = context.getSubcontext(SecurityParametersContext.class, true);
        securityContext.setSignatureSigningParameters(signatureSigningParameters);
        
        //        HTTPRedirectDeflateEncoder encoder = new HTTPRedirectDeflateEncoder();
        HTTPPostEncoder encoder = new HTTPPostEncoder();
        encoder.setVelocityEngine(velocityEngine);
        encoder.setMessageContext(context);
        encoder.setHttpServletResponse(response);
        try {
            encoder.initialize();
        } catch (ComponentInitializationException e) {
            e.printStackTrace();
        }
        String authnRequestStr = OpenSAMLUtils.logSAMLObject(authnRequest);
        log.info("AuthnRequest: {}", authnRequestStr);
    
        try {
            encoder.encode();
        } catch (MessageEncodingException e) {
            e.printStackTrace();
        }
    }
    
    
    private Endpoint getIdPEndpoint(IdPMetaData idPMetaData) {
        SingleSignOnService singleSignOnEndPoint = OpenSAMLUtils.buildSAMLObject(SingleSignOnService.class);
        singleSignOnEndPoint.setBinding(SAMLConstants.SAML2_POST_BINDING_URI);
        singleSignOnEndPoint.setLocation(idPMetaData.getSingleSignOnUrl());
        return singleSignOnEndPoint;
    }
    
}

















































