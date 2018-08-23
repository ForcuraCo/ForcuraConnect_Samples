package com.forcura;

import com.forcura.net.HttpClient;
import okhttp3.Response;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {
       HttpClient httpClient = new HttpClient("https://integrations-sandbox.myforcura.com/", "CLIENTID", "CLIENTSECRET");

        try {
            Response response = httpClient.get("v1/documentstatus");

            String responseBody = response.body().string();

            System.out.println(responseBody);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
