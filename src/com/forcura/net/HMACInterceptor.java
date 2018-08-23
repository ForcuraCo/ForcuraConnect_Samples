package com.forcura.net;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

/*
Authorization = "Forcura" + " " + ClientIdentifier + ":" + Signature + ":" + UnixTimestamp

  Signature = Base64( HMAC-SHA256(ClientSecret, UTF-8-Encoding-Of ( RequestStringToSign ) ) )

  RequestStringToSign = HTTP-Verb + "\n" +
                        RequestURI + "\n" +
                        Content-MD5 + "\n" +
                        UnixTimestamp
 */

/*
    Think of this as middleware, this interceptor will catch the request prior to it being sent to alter the request
    We will use this to create a unique signature for this request, there are definitely some inefficiencies here, but
    more focused on a usable demo.  Not sure if you all will be using OKHttp but I've used it in the past on several
    Android projects and it's never failed me.  If not, I'm sure this at least will get the point across and can be adapted.
 */
public class HMACInterceptor implements Interceptor {
    private final String hmacAlgorithm = "HmacSHA256";
    private final String _clientIdentifier;
    private final byte[] _clientSecret;

    public HMACInterceptor(String clientIdentifier, String clientSecret) {
        _clientIdentifier = clientIdentifier;
        _clientSecret = Convert.fromBase64String(clientSecret);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        // if authorization header is set, ignore it and trust the provided authorization header
        // this is good here when performing User (Bearer) authorization with the provided JWT token
        if (request.header("Authorization") != null)
            return chain.proceed(request);

        try {
            final Request.Builder requestBuilder = request.newBuilder();  // this builder will be used to modify the existing request
            final RequestBody requestBody = request.body();         // request body
            final String httpVerb = request.method();               // request method
            final String requestURI = request.url().toString();     // request uri
            final long unixTimestamp = new Date().getTime() / 1000; // create unix timestamp (elapsed seconds from 1/1/1970)
            String md5 = "";

            // create an MD5 hash of the request body if one is provided
            if (requestBody != null && requestBody.contentLength() > 0) {
                final Request requestCopy = request.newBuilder().build();
                final Buffer buffer = new Buffer();

                requestCopy.body().writeTo(buffer);
                md5 = generateMD5(buffer.readByteArray());
            }

            // create string to sign  with clientSecret
            String requestStringToSign = httpVerb + "\n" +
                    URLEncoder.encode(requestURI, "UTF-8").toLowerCase() + "\n" +
                    md5 + "\n" +
                    unixTimestamp;
            System.out.println("String to Sign: " + requestStringToSign);

            // HMACSHA256 hash and base64 encode the result for sending in the request header
            String b64Hash = hmacShaHash(requestStringToSign);

            // concatenate using Forcura scheme to create header value
            String headerValue = "Forcura " + _clientIdentifier + ":" + b64Hash + ":" + unixTimestamp;
            System.out.println("Signature: " + headerValue);

            // build new request with Authorization header
            Request newRequest = requestBuilder
                    .addHeader("Authorization", headerValue)
                    .build();

            return chain.proceed(newRequest);
        } catch (Exception e) {
            // log exception
        }
        return chain.proceed(request);
    }

    private String hmacShaHash(String stringToSign) throws NoSuchAlgorithmException, InvalidKeyException {
        final Mac sha256HMAC = Mac.getInstance(hmacAlgorithm);

        final SecretKeySpec secretKey = new SecretKeySpec(_clientSecret, hmacAlgorithm);
        sha256HMAC.init(secretKey);

        final byte[] hmacSig = sha256HMAC.doFinal(Convert.toBytes(stringToSign));
        return Convert.toBase64String(hmacSig);
    }

    private String generateMD5(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(data);
        byte[] digest = md.digest();

        return Convert.bytesToHex(digest);
    }
}
