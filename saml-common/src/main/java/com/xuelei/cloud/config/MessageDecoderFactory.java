package com.xuelei.cloud.config;

import org.opensaml.messaging.decoder.servlet.BaseHttpServletRequestXMLMessageDecoder;
import org.opensaml.saml.saml2.binding.decoding.impl.HTTPPostDecoder;
import org.opensaml.saml.saml2.binding.decoding.impl.HTTPRedirectDeflateDecoder;

public class MessageDecoderFactory {
    
    private MessageDecoderFactory() {
    
    }
    
    public static BaseHttpServletRequestXMLMessageDecoder createMessageDecoder(String httpMethod) {
        
        BaseHttpServletRequestXMLMessageDecoder messageDecoder = null;
        if(httpMethod.equalsIgnoreCase("POST")) {
            messageDecoder = new HTTPPostDecoder();
        } else {
            messageDecoder = new HTTPRedirectDeflateDecoder();
        }
        return messageDecoder;
    }
    
}
