package com.xuelei.cloud.config;

import com.xuelei.cloud.IdPConstant;
import com.xuelei.cloud.idp.service.SPMetaDataService;
import lombok.extern.slf4j.Slf4j;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.SAMLObject;
import org.springframework.beans.factory.annotation.Autowired;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@WebFilter(filterName = "forceAuthnFilter", urlPatterns = "/idp/*")
public class ForceAuthnFilter implements Filter {
    
    @Autowired
    private SAMLMessageHandler samlMessageHandler;
    
    @Autowired
    private SPMetaDataService spMetaDataService;
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        try {
            InitializationService.initialize();
        } catch (InitializationException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;
        String requestUri = request.getRequestURI();
        log.info("requestUri: {}", requestUri);
        HttpSession session = request.getSession();
        log.info("session id: {}", session.getId());
        log.info("Host: {}", request.getRemoteHost());
        
        
        if (needAuth(requestUri)) {
            if (session.getAttribute("user") == null) {
                log.info("Host: {}", request.getRemoteHost());
                Object o = request.getParameter("SAMLRequest");
                
                MessageContext<SAMLObject> samlObject = samlMessageHandler.getContext(request, response, request.getMethod());
                
                if (samlObject != null) {
                    session.setAttribute("SAMLAuthn", samlObject.getMessage());
                }
                response.sendRedirect(IdPConstant.IDP_LOGIN_URI);
//                response.sendRedirect("../login");
                return;
            } else {
                chain.doFilter(request, response);
            }
        }
        response.setHeader("Access-Control-Allow-Origin", "*");
        chain.doFilter(request, response);
        return;
    }
    
    private boolean needAuth(String uri) {
        Set<String> noAuthUriSet = new HashSet<>();
        noAuthUriSet.add(IdPConstant.IDP_LOGIN_URI);
        noAuthUriSet.add(IdPConstant.IDP_LOGIN_AUTH_URI);
        noAuthUriSet.add(IdPConstant.IDP_SINGLE_LOGIN_URI);
        if (noAuthUriSet.contains(uri)) {
            return false;
        } else {
            return true;
        }
    }
    
    @Override
    public void destroy() {
    
    }
}
