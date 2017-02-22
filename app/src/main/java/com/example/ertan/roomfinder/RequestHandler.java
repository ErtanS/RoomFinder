package com.example.ertan.roomfinder;


import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;

/**
 * The type Request handler.
 */
public class RequestHandler {

    private final HashMap<String, String> phpVars = new HashMap<>();
    private String url = "http://192.168.0.52/smart/createLayout.php";

    /**
     * Methode zum Senden von httpPostRequest
     *
     * @param requestURL     URL zur Phpdatei
     * @param postDataParams Parameter hashmap
     * @return Rückgabe der Phpdatei
     */
    private String sendPostRequest(String requestURL, HashMap<String, String> postDataParams) {
        URL url;
        StringBuilder sb = new StringBuilder();

        try {
            url = new URL(requestURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            OutputStream os = conn.getOutputStream();

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(getPostDataString(postDataParams));
            writer.flush();
            writer.close();
            os.close();

            int responseCode = conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                sb = new StringBuilder();
                String response;
                while ((response = br.readLine()) != null) {
                    sb.append(response);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }


    /**
     * Verarbeitung der Übergabeparameter
     *
     * @param params Übergabeparameter
     * @return rückgabe der Phpdatei
     * @throws UnsupportedEncodingException Fehlerhandling
     */
    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    /**
     * Hinzufügen zusätzlicher Php Paramter
     *
     * @param key   Php Identifikations String
     * @param value Wert
     */
    private void addPhpVar(String key, String value) {
        phpVars.put(key, value);
    }


    public String transferData(String value) {
        addPhpVar("room", value);
        return sendPostRequest();
    }

    private String sendPostRequest() {
        SelectAsyncPost s = new SelectAsyncPost();
        try {
            return s.execute().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    private class SelectAsyncPost extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            return sendPostRequest(url, phpVars);
        }
    }


}
