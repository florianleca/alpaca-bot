package fr.flolec.alpacabot.alpacaapi.httprequests;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class HttpRequestService {

    private final OkHttpClient okHttpClient = getOkHttpClient();
    @Value("${ALPACA_API_KEY_ID}")
    private String keyId;
    @Value("${ALPACA_API_SECRET_KEY}")
    private String secretKey;

    @Bean
    public OkHttpClient getOkHttpClient() {
        return new OkHttpClient();
    }

    public Response get(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("accept", "application/json")
                .addHeader("APCA-API-KEY-ID", keyId)
                .addHeader("APCA-API-SECRET-KEY", secretKey)
                .build();
        return okHttpClient.newCall(request).execute();
    }

//    String url = HttpUrl.parse(endpoint + orderId).newBuilder()
//            .addQueryParameter("", "")
//            .build()
//            .toString();

    public Response post(String url, RequestBody body) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("accept", "application/json")
                .addHeader("APCA-API-KEY-ID", keyId)
                .addHeader("APCA-API-SECRET-KEY", secretKey)
                .build();
        return okHttpClient.newCall(request).execute();
    }

    public Response delete(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .delete(null)
                .addHeader("accept", "application/json")
                .addHeader("APCA-API-KEY-ID", keyId)
                .addHeader("APCA-API-SECRET-KEY", secretKey)
                .build();
        return okHttpClient.newCall(request).execute();
    }

}
