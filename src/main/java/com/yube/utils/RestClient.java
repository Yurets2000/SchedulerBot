package com.yube.utils;


import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public final class RestClient {

    private final Client client;

    public RestClient() {
        this.client = Client.create();
    }

    public String post(String url, String json) {
        try {
            WebResource webResource = client.resource(url);
            ClientResponse response = webResource.type("application/json")
                    .post(ClientResponse.class, json);
            return response.getEntity(String.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
