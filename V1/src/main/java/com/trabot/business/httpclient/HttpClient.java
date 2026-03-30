package com.trabot.business.httpclient;

import java.util.Map;

public interface HttpClient {
    
    String getRequest(String url, Map<String, String> parameters);

}
