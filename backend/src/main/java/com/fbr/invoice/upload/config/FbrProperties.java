package com.fbr.invoice.upload.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "fbr.api")
public class FbrProperties {

    private String token;
    private String validateUrl;
    private String provincesUrl;
    private String uomUrl;
    private String transactionTypeUrl;
    private String docTypeUrl;
    private String hsCodeUrl;
    private String buyerAtlUrl;
    private String buyerRegTypeUrl;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getValidateUrl() { return validateUrl; }
    public void setValidateUrl(String validateUrl) { this.validateUrl = validateUrl; }

    public String getProvincesUrl() { return provincesUrl; }
    public void setProvincesUrl(String provincesUrl) { this.provincesUrl = provincesUrl; }

    public String getUomUrl() { return uomUrl; }
    public void setUomUrl(String uomUrl) { this.uomUrl = uomUrl; }

    public String getTransactionTypeUrl() { return transactionTypeUrl; }
    public void setTransactionTypeUrl(String transactionTypeUrl) { this.transactionTypeUrl = transactionTypeUrl; }

    public String getDocTypeUrl() { return docTypeUrl; }
    public void setDocTypeUrl(String docTypeUrl) { this.docTypeUrl = docTypeUrl; }

    public String getHsCodeUrl() { return hsCodeUrl; }
    public void setHsCodeUrl(String hsCodeUrl) { this.hsCodeUrl = hsCodeUrl; }

    public String getBuyerAtlUrl() { return buyerAtlUrl; }
    public void setBuyerAtlUrl(String buyerAtlUrl) { this.buyerAtlUrl = buyerAtlUrl; }

    public String getBuyerRegTypeUrl() { return buyerRegTypeUrl; }
    public void setBuyerRegTypeUrl(String buyerRegTypeUrl) { this.buyerRegTypeUrl = buyerRegTypeUrl; }
}
