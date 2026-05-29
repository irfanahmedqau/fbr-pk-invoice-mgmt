package com.fbr.invoice.upload.service;

import com.fbr.invoice.upload.config.FbrProperties;
import com.fbr.invoice.upload.dto.FbrInvoicePayload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class FbrApiService {

    @Autowired
    private FbrProperties props;

    @Autowired
    private RestTemplate restTemplate;

    // ------------------------------------------------------------------
    // Invoice channeling / validation
    // ------------------------------------------------------------------

    public Object validateInvoice(FbrInvoicePayload payload) {
        HttpEntity<FbrInvoicePayload> entity = new HttpEntity<>(payload, postBearerHeaders());
        try {
            ResponseEntity<Object> response = restTemplate.postForEntity(
                    props.getValidateUrl(), entity, Object.class);
            return response.getBody();
        } catch (HttpClientErrorException ex) {
            throw new RuntimeException("FBR validation error: " + ex.getResponseBodyAsString(), ex);
        }
    }

    public Object postInvoice(FbrInvoicePayload payload) {
        HttpEntity<FbrInvoicePayload> entity = new HttpEntity<>(payload, postBearerHeaders());
        try {
            ResponseEntity<Object> response = restTemplate.postForEntity(
                    props.getPostUrl(), entity, Object.class);
            return response.getBody();
        } catch (HttpClientErrorException ex) {
            throw new RuntimeException("FBR post error: " + ex.getResponseBodyAsString(), ex);
        }
    }

    // ------------------------------------------------------------------
    // Reference / lookup endpoints (cached by caller if needed)
    // ------------------------------------------------------------------

    public Object getProvinces() {
        return getFromFbr(props.getProvincesUrl());
    }

    public Object getUom() {
        return getFromFbr(props.getUomUrl());
    }

    public Object getTransactionTypes() {
        return getFromFbr(props.getTransactionTypeUrl());
    }

    public Object getDocTypes() {
        return getFromFbr(props.getDocTypeUrl());
    }

    public Object getHsCodes() {
        return getFromFbr(props.getHsCodeUrl());
    }

    // ------------------------------------------------------------------
    // Buyer validation
    // ------------------------------------------------------------------

    public Object getBuyerAtl(String ntn, String date) {
        String url = UriComponentsBuilder.fromUriString(props.getBuyerAtlUrl())
                .queryParam("regno", ntn)
                .queryParam("date", date)
                .toUriString();
        return getFromFbr(url);
    }

    public Object getBuyerRegType(String ntn) {
        String url = UriComponentsBuilder.fromUriString(props.getBuyerRegTypeUrl())
                .queryParam("Registration_No", ntn)
                .toUriString();
        return getFromFbr(url);
    }

    // ------------------------------------------------------------------
    // Internal helpers
    // ------------------------------------------------------------------

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private Object getFromFbr(String url) {
        HttpEntity<Void> entity = new HttpEntity<>(getBearerHeaders());
        try {
            ResponseEntity<Object> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, Object.class);
            return response.getBody();
        } catch (HttpServerErrorException ex) {
            // FBR uses HTTP 5xx for some valid business responses (e.g. "unregistered" taxpayer).
            // If the body is JSON, return it transparently instead of propagating as an error.
            String body = ex.getResponseBodyAsString();
            if (body != null && body.trim().startsWith("{")) {
                try { return MAPPER.readValue(body, Object.class); }
                catch (Exception ignored) {}
            }
            throw new RuntimeException("FBR API error [" + url + "]: " + body, ex);
        } catch (HttpClientErrorException ex) {
            throw new RuntimeException("FBR API error [" + url + "]: " + ex.getResponseBodyAsString(), ex);
        }
    }

    /** GET requests — Bearer only, no Content-Type (no body on GET). */
    private HttpHeaders getBearerHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(props.getToken());
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        return headers;
    }

    /** POST requests — Bearer + Content-Type: application/json. */
    private HttpHeaders postBearerHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(props.getToken());
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        return headers;
    }
}
