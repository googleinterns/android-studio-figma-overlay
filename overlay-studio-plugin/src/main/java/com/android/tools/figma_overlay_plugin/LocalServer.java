package com.android.tools.figma_overlay_plugin;

import static org.apache.http.HttpStatus.SC_OK;

import com.android.tools.idea.ui.designer.OverlayData;
import com.android.tools.idea.ui.designer.OverlayNotFoundException;
import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeoutException;
import javax.imageio.ImageIO;
import org.jetbrains.concurrency.AsyncPromise;

class LocalServer {

  @VisibleForTesting
  static final String OVERLAY_ID_ENDPOINT = "/overlay_id";
  @VisibleForTesting
  static final String RESULT_ENDPOINT = "/result";
  @VisibleForTesting
  static final String CANCEL_ENDPOINT = "/cancel";
  @VisibleForTesting
  static final String MANUAL_CANCEL_ENDPOINT = "/manual_cancel";

  private final int serverTimeoutMillis;
  private final HttpServer myServer;
  private final AsyncPromise<OverlayData> myPromise;
  private final String myOverlayId;
  private boolean myServerRunning = false;


  LocalServer(HttpServer server, AsyncPromise<OverlayData> promise, String currentOverlayId,
      int timeout) {
    myPromise = promise;
    myOverlayId = currentOverlayId;
    myServer = server;
    serverTimeoutMillis = timeout;
  }

  OverlayData parseResponseJson(InputStreamReader reader) throws NullPointerException, IOException {
    Gson g = new Gson();
    OverlayMessage p = g.fromJson(reader, OverlayMessage.class);

    if (p == null || p.id == null || p.name == null || p.overlay == null) {
      throw new NullPointerException("Overlay data was null");
    }

    return p.toOverlayData();
  }

  synchronized void startServer() {
    myServer.createContext(OVERLAY_ID_ENDPOINT, new GetOverlayHandler(myOverlayId));
    myServer.createContext(RESULT_ENDPOINT, new PostResultHandler(this));
    myServer.createContext(CANCEL_ENDPOINT, new CancelHandler(this));
    myServer.createContext(MANUAL_CANCEL_ENDPOINT, new ManualCancelHandler(this));
    myServer.setExecutor(AppExecutorUtil.getAppExecutorService());
    myServer.start();
    myServerRunning = true;
    Timer t = new Timer();
    t.schedule(new TimerTask() {
      @Override
      public void run() {
        if (stopServer() && !myPromise.isDone()) {
          myPromise.setError(new TimeoutException("Server timed out."));
        }
      }
    }, serverTimeoutMillis);
  }

  public synchronized boolean stopServer() {
    if (isRunning()) {
      myServerRunning = false;
      myServer.stop(0);
      return true;
    }

    return false;
  }

  public synchronized boolean isRunning() {
    return myServerRunning;
  }

  public AsyncPromise<OverlayData> getPromise() {
    return myPromise;
  }

  public int getServerTimeout() {
    return serverTimeoutMillis;
  }

  @VisibleForTesting
  public int getPort() {
    return myServer.getAddress().getPort();
  }

  static class GetOverlayHandler implements HttpHandler {

    private final String overlayId;

    GetOverlayHandler(String overlayId) {
      this.overlayId = overlayId;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
      sendResponse(httpExchange, overlayId);
    }
  }

  static class PostResultHandler implements HttpHandler {

    private final WeakReference<LocalServer> myServer;

    PostResultHandler(LocalServer server) {
      this.myServer = new WeakReference<>(server);
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
      InputStreamReader reader = new InputStreamReader(httpExchange.getRequestBody(),
          StandardCharsets.UTF_8);

      LocalServer server = myServer.get();
      OverlayData data;
      if (server != null) {

        try {
          data = server.parseResponseJson(reader);
          server.getPromise().setResult(data);
        } catch (Exception e) {
          server.getPromise().setError(e);
        } finally {
          sendResponse(httpExchange, "");
          server.stopServer();
        }
      }
    }
  }

  static class CancelHandler implements HttpHandler {

    private final WeakReference<LocalServer> myServer;

    CancelHandler(LocalServer server) {
      this.myServer = new WeakReference<>(server);
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
      LocalServer server = myServer.get();
      if (server != null) {
        server.getPromise().setError(new OverlayNotFoundException("ID does not exist anymore"));
        sendResponse(httpExchange, "");
        server.stopServer();
      }
    }
  }

  static class ManualCancelHandler implements HttpHandler {

    private final WeakReference<LocalServer> myServer;

    ManualCancelHandler(LocalServer server) {
      this.myServer = new WeakReference<>(server);
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
      LocalServer server = myServer.get();
      if (server != null) {
        server.getPromise().setError(new CancellationException());
        sendResponse(httpExchange, "");
        server.stopServer();
      }
    }
  }

  private static void sendResponse(HttpExchange httpExchange, String response) throws IOException {
    httpExchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
    httpExchange.sendResponseHeaders(SC_OK, response.getBytes().length);
    OutputStream os = httpExchange.getResponseBody();
    os.write(response.getBytes());
    os.close();
  }

  static class OverlayMessage {

    String overlay;
    String id;
    String name;

    OverlayMessage(String overlay, String id, String name) {
      this.overlay = overlay;
      this.id = id;
      this.name = name;
    }

    OverlayData toOverlayData() throws IOException {
      byte[] decodedBytes = Base64.getDecoder().decode(overlay);
      ByteArrayInputStream overlayStream = new ByteArrayInputStream(decodedBytes);
      BufferedImage overlayImage = ImageIO.read(overlayStream);
      return new OverlayData(id, name, overlayImage);
    }
  }
}
