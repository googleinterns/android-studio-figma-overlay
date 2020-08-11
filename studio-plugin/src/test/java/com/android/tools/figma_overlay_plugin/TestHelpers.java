// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

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
