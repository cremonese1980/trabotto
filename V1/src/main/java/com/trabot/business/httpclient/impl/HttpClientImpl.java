package com.trabot.business.httpclient.impl;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import org.apache.http.client.utils.URIBuilder;
import org.springframework.stereotype.Service;

import com.trabot.business.httpclient.HttpClient;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class HttpClientImpl implements HttpClient {
    
    private java.net.http.HttpClient client;
    
    public HttpClientImpl() {
	client = java.net.http.HttpClient.newHttpClient();
    }

    @Override
    public String getRequest(String url, Map<String, String> parameters) {

	try {
	    URIBuilder uriBuilder = new URIBuilder(url);
	    parameters.forEach(uriBuilder::addParameter);
	    HttpRequest request = HttpRequest.newBuilder()
	                .uri(uriBuilder.build())
	                .GET()
	                .build();
	    
	    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
	    
	    log.info(response.body());
	    
	    return response.body();
	    
	} catch (URISyntaxException | IOException | InterruptedException e) {
	   log.error(e.getMessage(), e);
	}
	       
	return null;
    }

}
