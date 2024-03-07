/*maven
<dependencies>
    <dependency>
        <groupId>com.google.code.gson</groupId>
        <artifactId>gson</artifactId>
        <version>2.8.9</version>
    </dependency>
</dependencies>
*/
package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class CrptApi {
    private static final Object lock = new Object();
    private final long millisecondsLimit;
    private final int requestLimit;
    private int requests;
    private long lastCall = 0;
    
    //TimeUnit было сказно что как пример, по этому использовал классику
    public CrptApi(long millisecondsLimit, int requestLimit) {
        this.millisecondsLimit = millisecondsLimit;
        this.requestLimit = requestLimit;
    }

    public void createDocument(Document document) {
        //можно было и просто синхронный метод сделать, но задача слишком размытая, решил сделать так
        synchronized(lock) {
            if (lastCall == 0) {
                lastCall = System.currentTimeMillis();
                requests++;
            } else {
                if (System.currentTimeMillis() - lastCall > millisecondsLimit) {
                    lastCall = 0;
                    requests = 1;
                } else {
                    if (requests >= requestLimit) {
                        return;
                    }
                    requests++;
                }
            }

        }

        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(document);

            URL url = new URL("https://ismp.crpt.ru/api/v3/lk/documents/create");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json; utf-8");


            try(OutputStream os = conn.getOutputStream()) {
                byte[] input = json.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            /*int code = conn.getResponseCode();
            System.out.println(code);*/

            conn.disconnect();

        } catch (IOException e) {
            System.out.println(e);
        }
    }

}

class Document {
    Description description;
    String doc_id;
    String doc_status;
    String doc_type;
    boolean importRequest;
    String owner_inn;
    String participant_inn;
    String producer_inn;
    String production_date;
    String production_type;
    List<Product> products;
    String reg_date;
    String reg_number;

    class Description {
        String participantInn;
    }

    class Product {
        String certificate_document;
        String certificate_document_date;
        String certificate_document_number;
        String owner_inn;
        String producer_inn;
        String production_date;
        String tnved_code;
        String uit_code;
        String uitu_code;
    }
}
