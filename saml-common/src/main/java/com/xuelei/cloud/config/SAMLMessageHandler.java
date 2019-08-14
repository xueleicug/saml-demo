package com.xuelei.cloud.config;

import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.decoder.MessageDecodingException;
import org.opensaml.messaging.decoder.servlet.BaseHttpServletRequestXMLMessageDecoder;
import org.opensaml.saml.common.SAMLObject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SAMLMessageHandler {
    
    public MessageContext<SAMLObject> getContext(HttpServletRequest request, HttpServletResponse response, String httpMethod) {
    
        
        BaseHttpServletRequestXMLMessageDecoder decoder = MessageDecoderFactory.createMessageDecoder(httpMethod);
        decoder.setHttpServletRequest(request);
    
        BasicParserPool parserPool = new BasicParserPool();
        try {
            parserPool.initialize();
            decoder.setParserPool(parserPool);
            decoder.initialize();
            decoder.decode();
        } catch (ComponentInitializationException | MessageDecodingException e) {
            e.printStackTrace();
        }
        MessageContext messageContext = decoder.getMessageContext();
        return  messageContext;
    }
}
