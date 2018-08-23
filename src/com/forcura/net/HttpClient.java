package com.forcura.net;


import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class HttpClient {
    private final OkHttpClient _internalClient;
    private final String _baseUrl;

    public HttpClient(String baseUrl, String clientIdentifier, String clientSecret) {
        _baseUrl = baseUrl;
        _internalClient = buildClient(clientIdentifier, clientSecret);
    }

    public Response get(String resource) throws IOException {
        Request request = new Request.Builder()
                .url(_baseUrl + resource)
                .method("GET", null)
                .build();

        return _internalClient.newCall(request)
                .execute();
    }

    private OkHttpClient buildClient(String clientIdentifier, String clientSecret) {
        return new OkHttpClient.Builder()
                .addInterceptor(new HMACInterceptor(clientIdentifier, clientSecret))
                .build();
    }
}
