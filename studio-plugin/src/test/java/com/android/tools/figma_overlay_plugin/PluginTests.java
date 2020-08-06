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

import static com.intellij.testFramework.UsefulTestCase.assertThrows;
import static org.apache.http.HttpStatus.SC_OK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.android.tools.idea.ui.designer.OverlayData;
import com.google.gson.Gson;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.sun.net.httpserver.HttpServer;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import org.jetbrains.concurrency.AsyncPromise;
import org.jetbrains.concurrency.Promise.State;
import org.junit.Test;
import org.mockito.Mockito;

public class PluginTests {

  private final String TEST_OVERLAY_ID = "test id";
  private final String TEST_OVERLAY_NAME = "test name";
  private final String OVERLAY_PLACEHOLDER = "";

  private final int TIMEOUT_LONG = 120000;
  private final int TIMEOUT_SHORT = 1000;
  private final int DELAY = 200;

  private LocalServer myServer = null;

  private int makeAndStartServer() throws IOException {
    InetSocketAddress addr = new InetSocketAddress(0);
    AsyncPromise<OverlayData> promise = new AsyncPromise<>();
    promise.onError(t -> {
    });
    myServer = new LocalServer(HttpServer.create(addr, 0),
        promise, TEST_OVERLAY_ID, TIMEOUT_LONG);
    myServer.startServer();

    return myServer.getPort();
  }

  @Test
  public void testServerStartsCorrectly() {
    HttpServer server = mock(HttpServer.class);
    AsyncPromise<OverlayData> promise = new AsyncPromise<>();
    promise.onError(t -> {
    });
    LocalServer localsServer = new LocalServer(server, promise, TEST_OVERLAY_ID,
        TIMEOUT_LONG);
    localsServer.startServer();

    verify(server, times(4)).createContext(any(), any());
    verify(server, times(1)).setExecutor(AppExecutorUtil.getAppExecutorService());
    verify(server, times(1)).start();

    localsServer.stopServer();

    verify(server, times(1)).stop(0);
  }

  @Test
  public void checkServerTimesOutAfterTwoMinutes() throws Exception {
    HttpServer server = Mockito.mock(HttpServer.class);
    LocalServer localServer = new LocalServer(server, new AsyncPromise<>(), TEST_OVERLAY_ID,
        TIMEOUT_SHORT);
    localServer.startServer();

    Thread.sleep(localServer.getServerTimeout() / 2);
    assert (localServer.isRunning());
    assertFalse(localServer.getPromise().isDone());
    Thread.sleep((localServer.getServerTimeout() / 2) + DELAY);
    assertFalse(localServer.isRunning());
    assert (localServer.getPromise().getState() == State.REJECTED);
  }

  @Test
  public void testInvalidOverlayResponseParsing() {
    LocalServer.OverlayMessage data = new LocalServer.OverlayMessage(null, TEST_OVERLAY_ID,
        TEST_OVERLAY_NAME);
    Gson gson = new Gson();
    String result = gson.toJson(data);
    InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(result.getBytes(
        StandardCharsets.UTF_8)));
    LocalServer server = new LocalServer(mock(HttpServer.class), new AsyncPromise<>(),
        TEST_OVERLAY_ID, TIMEOUT_LONG);
    assertThrows(NullPointerException.class, () -> server.parseResponseJson(reader));
  }

  @Test
  public void testInvalidIdResponseParsing() {
    LocalServer.OverlayMessage data = new LocalServer.OverlayMessage(OVERLAY_PLACEHOLDER, null,
        TEST_OVERLAY_NAME);
    Gson gson = new Gson();
    String result = gson.toJson(data);
    InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(result.getBytes(
        StandardCharsets.UTF_8)));
    LocalServer server = new LocalServer(mock(HttpServer.class), new AsyncPromise<>(),
        TEST_OVERLAY_ID, TIMEOUT_LONG);

    assertThrows(NullPointerException.class, () -> server.parseResponseJson(reader));
  }

  @Test
  public void testInvalidNameResponseParsing() {
    LocalServer.OverlayMessage data = new LocalServer.OverlayMessage(OVERLAY_PLACEHOLDER,
        TEST_OVERLAY_ID, null);
    Gson gson = new Gson();
    String result = gson.toJson(data);
    LocalServer server = new LocalServer(mock(HttpServer.class), new AsyncPromise<>(),
        TEST_OVERLAY_ID, TIMEOUT_LONG);
    InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(result.getBytes(
        StandardCharsets.UTF_8)));

    assertThrows(NullPointerException.class, () -> server.parseResponseJson(reader));
  }

  @Test
  public void testValidResponseParsing() throws Exception {
    LocalServer.OverlayMessage data = new LocalServer.OverlayMessage("", "id", "name");
    Gson gson = new Gson();
    String result = gson.toJson(data);
    InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(result.getBytes(
        StandardCharsets.UTF_8)));
    LocalServer server = new LocalServer(mock(HttpServer.class), new AsyncPromise<>(),
        TEST_OVERLAY_ID, TIMEOUT_LONG);

    assertNotNull(server.parseResponseJson(reader));
  }

  @Test
  public void checkPluginIconNotNull() {
    FigmaOverlayProvider provider = new FigmaOverlayProvider();
    assertNotNull(provider.getPluginIcon());
  }

  @Test
  public void checkGetRequest() throws IOException {
    int port = makeAndStartServer();
    String response = TestHelpers.getOverlayIdRequest(port);

    assert (response.equals(TEST_OVERLAY_ID));
    assert (myServer.isRunning());

    myServer.stopServer();

    assertFalse(myServer.isRunning());
  }

  @Test
  public void checkValidPostRequest() throws Exception {
    int port = makeAndStartServer();
    int responseCode = TestHelpers.postData("", TEST_OVERLAY_ID, TEST_OVERLAY_NAME, port);

    assertEquals(SC_OK, responseCode);

    assertFalse(myServer.isRunning());
    assert (myServer.getPromise().isSucceeded());
  }

  @Test
  public void checkNullImagePostRequest() throws Exception {
    int port = makeAndStartServer();
    int responseCode = TestHelpers.postData(null, TEST_OVERLAY_ID, TEST_OVERLAY_NAME, port);

    assertEquals(SC_OK, responseCode);

    assertFalse(myServer.isRunning());
    assertEquals(myServer.getPromise().getState(), State.REJECTED);
  }

  @Test
  public void checkNullIdPostRequest() throws Exception {
    int port = makeAndStartServer();
    int responseCode = TestHelpers.postData("", null, TEST_OVERLAY_NAME, port);

    assertEquals(SC_OK, responseCode);

    assertFalse(myServer.isRunning());
    assertEquals(myServer.getPromise().getState(), State.REJECTED);

  }

  @Test
  public void checkNullNamePostRequest() throws Exception {
    int port = makeAndStartServer();
    int responseCode = TestHelpers.postData("", TEST_OVERLAY_ID, null, port);

    assertEquals(SC_OK, responseCode);

    assertFalse(myServer.isRunning());
    assertEquals(myServer.getPromise().getState(), State.REJECTED);
  }

  @Test
  public void checkCancelRequest() throws Exception {
    int port = makeAndStartServer();
    int responseCode = TestHelpers.cancelRequest(port);

    assertEquals(SC_OK, responseCode);

    assertFalse(myServer.isRunning());
    assertEquals(myServer.getPromise().getState(), State.REJECTED);
  }

  @Test
  public void checkManualCancelRequest() throws Exception {
    int port = makeAndStartServer();
    int responseCode = TestHelpers.manualCancelRequest(port);

    assertEquals(SC_OK, responseCode);

    assertFalse(myServer.isRunning());
    assertEquals(myServer.getPromise().getState(), State.REJECTED);
  }
}