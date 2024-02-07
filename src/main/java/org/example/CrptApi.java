package org.example;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CrptApi {
    private final int requestLimit;
    private final long requestPeriodInMillis;
    private final Object lock = new Object();
    private int requestCounter = 0;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.requestLimit = requestLimit;
        this.requestPeriodInMillis = timeUnit.toMillis(1);
    }

    public Document createDocument(Document document, String signature) throws InterruptedException {
        synchronized (lock) {
            while (requestCounter >= requestLimit) {
                lock.wait();
            }

            try {
                CloseableHttpClient httpClient = HttpClients.createDefault();
                HttpPost httpPost = new HttpPost("https://ismp.crpt.ru/api/v3/lk/documents/create");

                Gson gson = new Gson();
                String jsonBody = gson.toJson(document);
                httpPost.setEntity(new StringEntity(jsonBody));

                CloseableHttpResponse response = httpClient.execute(httpPost);

                BufferedReader reader = new BufferedReader(new InputStreamReader
                        (response.getEntity().getContent()));

                String line;
                StringBuilder responseBody = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    responseBody.append(line);
                }
                System.out.println("Response: " + responseBody.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }

            requestCounter++;

            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            synchronized (lock) {
                                requestCounter--;
                                lock.notifyAll();
                            }
                        }
                    },
                    requestPeriodInMillis
            );
        }

        return document;
    }

    static class Document{
        @SerializedName("description")
        private Description description;

        @SerializedName("doc_id")
        private String doc_id;

        @SerializedName("doc_status")
        private String doc_status;

        @SerializedName("doc_type")
        private String doc_type;

        @SerializedName("importRequest")
        private boolean importRequest;

        @SerializedName("owner_inn")
        private String owner_inn;

        @SerializedName("participant_inn")
        private String participant_inn;

        @SerializedName("producer_inn")
        private String producer_inn;

        @SerializedName("production_date")
        private String production_date;

        @SerializedName("production_type")
        private String production_type;

        @SerializedName("products")
        private List<Products> products;
        @SerializedName("reg_date")
        private String reg_date;

        @SerializedName("reg_number")
        private String reg_number;



    }

    class Description{
        @SerializedName("participantInn")
        private String participantInn;

    }

    class Products{
        @SerializedName("certificate_document")
        private String certificate_document;

        @SerializedName("certificate_document_date")
        private String certificate_document_date;

        @SerializedName("certificate_document_number")
        private String certificate_document_number;

        @SerializedName("owner_inn")
        private String owner_inn;

        @SerializedName("producer_inn")
        private String producer_inn;

        @SerializedName("production_date")
        private String production_date;

        @SerializedName("tnved_code")
        private String tnved_code;

        @SerializedName("uit_code")
        private String uit_code;

        @SerializedName("uitu_code")
        private String uitu_code;
    }
}