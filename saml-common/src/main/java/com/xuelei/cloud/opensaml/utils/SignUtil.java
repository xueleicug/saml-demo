package com.xuelei.cloud.opensaml.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.xml.security.Init;
import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.utils.Base64;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.common.SignableSAMLObject;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.security.x509.X509Credential;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.X509Data;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.SignatureValidator;
import org.opensaml.xmlsec.signature.support.Signer;
import sun.misc.BASE64Decoder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class SignUtil {
    
    
    private static X509Credential signCredential = null;
    
    public static X509Credential getSignCheckCredential(String crt) throws CertificateException {
        if(crt == null) {
            return null;
        }
        final CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        InputStream crtInputStream = new ByteArrayInputStream(crt.getBytes());
        final X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(crtInputStream);
        closeInputStream(crtInputStream);
        final X509Credential publicCredential = new BasicX509Credential(certificate);
        closeInputStream(crtInputStream);
        return publicCredential;
    }
    
    public static X509Credential getSignCredential() throws CertificateException {
        
        if(signCredential != null) {
            return signCredential;
        }
        InputStream crtInputStream = SignUtil.class.getResourceAsStream("/key/x509.CRT");
        final CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        
//        X509Credential
        final X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(crtInputStream);
        closeInputStream(crtInputStream);
        PrivateKey privateKey = getPrivateKey("/key/private");
        final X509Credential publicCredential = new BasicX509Credential(certificate, privateKey);
        closeInputStream(crtInputStream);
        signCredential = publicCredential;
        log.info("privateKey: {}", publicCredential.getPrivateKey());
        log.info("publicKey: {}", publicCredential.getPublicKey());
        return publicCredential;
    }
    
    private static PrivateKey getPrivateKey(String path) {
        InputStream inputStream = SignUtil.class.getResourceAsStream(path);
        PrivateKey privateKey = null;
        try {
            String privateKeyStr = inputStreamToString(inputStream);
            BASE64Decoder base64Decoder = new BASE64Decoder();
            byte[] decodeKey = base64Decoder.decodeBuffer(privateKeyStr);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodeKey);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            privateKey = keyFactory.generatePrivate(keySpec);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return  privateKey;
        }
    }
    
    public static void validateSign(Signature signature) throws SignatureException {
        if(signature != null) {
            signature.setSignatureAlgorithm(SignatureConstants.ALGO_ID_DIGEST_SHA256);
            Credential credential = null;
            try {
                credential = SignUtil.getSignCredential();
            } catch (CertificateException e) {
                e.printStackTrace();
            }
            SignatureValidator.validate(signature, credential);
            log.info("Validate Success");
        }
    }
    
    public static void validateSign(String x509Crt, Signature signature) throws SignatureException {
        if(signature != null) {
            signature.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA512);
            Credential credential = null;
            try {
                credential = SignUtil.getSignCheckCredential(x509Crt);
//                credential = SignUtil.getSignCredential();
            } catch (CertificateException e) {
                e.printStackTrace();
            }
            SignatureValidator.validate(signature, credential);
            log.info("Validate Success");
        }
    }
    
    public static SignableSAMLObject setSignature(SignableSAMLObject request, String signatureAlgorithm, X509Credential credential) {
    
        try {
            Signature signature = setSignatureRaw(signatureAlgorithm, credential);
            request.setSignature(signature);
    
            List<Signature> signatureList = new ArrayList<>();
            signatureList.add(signature);
    
            Marshaller marshaller = XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(request);
            if(marshaller != null) {
                marshaller.marshall(request);
            }
            Init.init();
            Signer.signObject(signature);
//            Signer.signObjects(signatureList);
        } catch (CertificateEncodingException | MarshallingException | SignatureException e) {
            e.printStackTrace();
        } finally {
            return request;
        }
    }
    
    public static Signature setSignatureRaw(String signatureAlgorithm, X509Credential credential) throws CertificateEncodingException {
        Signature signature = OpenSAMLUtils.buildSAMLObject(Signature.class);
        signature.setSigningCredential(credential);
        signature.setSignatureAlgorithm(signatureAlgorithm);
        signature.setCanonicalizationAlgorithm(Canonicalizer.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
        KeyInfo keyInfo = OpenSAMLUtils.buildSAMLObject(KeyInfo.class);
        X509Data x509Data = OpenSAMLUtils.buildSAMLObject(X509Data.class);
        org.opensaml.xmlsec.signature.X509Certificate x509Certificate = OpenSAMLUtils.buildSAMLObject(org.opensaml.xmlsec.signature.X509Certificate.class);
        String value = Base64.encode(credential.getEntityCertificate().getEncoded());
        x509Certificate.setValue(value);
        x509Data.getX509Certificates().add(x509Certificate);
        keyInfo.getX509Datas().add(x509Data);
        signature.setKeyInfo(keyInfo);
        return signature;
    }
    
    
    private static String inputStreamToString(InputStream inputStream) throws IOException {
        StringBuffer out = new StringBuffer();
        byte[] b = new byte[2048];
        int n = 0;
        while((n = inputStream.read(b)) != -1) {
            out.append(new String(b, 0, n));
        }
        return out.toString();
    }
    
    private static void closeInputStream(InputStream in) {
        if(in != null) {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
