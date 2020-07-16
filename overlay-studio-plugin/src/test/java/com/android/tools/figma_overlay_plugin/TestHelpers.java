package com.android.tools.figma_overlay_plugin;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

class TestHelpers {

  static int postData(String img, String id, String name, int port) throws IOException {
    LocalServer.OverlayMessage data = new LocalServer.OverlayMessage(img, id, name);
    Gson gson = new Gson();
    String requestBody = gson.toJson(data);
    byte[] postData = requestBody.getBytes(StandardCharsets.UTF_8);

    HttpURLConnection connection;
    URL url = new URL("http://localhost:" + port + LocalServer.RESULT_ENDPOINT);
    connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod("POST");
    connection.setRequestProperty("Content-Type", "application/json; utf-8");
    connection.setDoOutput(true);

    try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
      wr.write(postData);
    }

    int responseCode = connection.getResponseCode();

    InputStream is = connection.getInputStream();
    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
    StringBuilder response = new StringBuilder();
    String line;
    while ((line = rd.readLine()) != null) {
      response.append(line);
    }
    rd.close();

    return responseCode;
  }

  static String getOverlayIdRequest(int port) throws IOException {
    HttpURLConnection connection;
    URL url = new URL("http://localhost:" + port + LocalServer.OVERLAY_ID_ENDPOINT);
    connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod("GET");

    InputStream is = connection.getInputStream();
    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
    StringBuilder response = new StringBuilder();
    String line;
    while ((line = rd.readLine()) != null) {
      response.append(line);
    }
    rd.close();

    return response.toString();
  }

  static int cancelRequest(int port) throws IOException {
    HttpURLConnection connection;
    URL url = new URL("http://localhost:" + port + LocalServer.CANCEL_ENDPOINT);
    connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod("GET");

    int responseCode = connection.getResponseCode();

    InputStream is = connection.getInputStream();
    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
    StringBuilder response = new StringBuilder();
    String line;
    while ((line = rd.readLine()) != null) {
      response.append(line);
    }
    rd.close();

    return responseCode;
  }

  static int manualCancelRequest(int port) throws IOException {
    HttpURLConnection connection;
    URL url = new URL("http://localhost:" + port + LocalServer.MANUAL_CANCEL_ENDPOINT);
    connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod("GET");

    int responseCode = connection.getResponseCode();

    InputStream is = connection.getInputStream();
    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
    StringBuilder response = new StringBuilder();
    String line;
    while ((line = rd.readLine()) != null) {
      response.append(line);
    }
    rd.close();

    return responseCode;
  }
}
