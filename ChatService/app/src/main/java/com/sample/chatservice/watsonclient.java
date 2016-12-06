package com.sample.chatservice;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by fechavez on 18/11/2016.
 */
public class watsonclient {
    public String enviarMensaje(String msj){
        String serverURL = "http://conversation-simple.mybluemix.net/api/message";
        Log.i("Watson -" ,readJSONWatsonResponse(serverURL));
        // Use AsyncTask execute Method To Prevent ANR Problem
        return "Hello";

    }

    public String readJSONWatsonResponse(String URL) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(URL);

            conn = (HttpURLConnection) url.openConnection();

            conn.setRequestProperty("Content-Type", "application/json");
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            String requestBody = "";

            OutputStream outputStream = new BufferedOutputStream(conn.getOutputStream());
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "utf-8"));
            writer.write(requestBody);
            writer.flush();
            writer.close();
            outputStream.close();

            return conn.getResponseMessage();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            conn.disconnect();
        }
        return "No response";
    }
}
