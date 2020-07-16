package com.android.tools.figma_overlay_plugin;

import com.android.tools.idea.ui.designer.OverlayData;
import com.android.tools.idea.ui.designer.OverlayProvider;
import com.intellij.openapi.project.Project;
import com.sun.net.httpserver.HttpServer;
import java.awt.Desktop;
import java.awt.Image;
import java.io.FileNotFoundException;
import java.net.InetSocketAddress;
import java.net.URI;
import javax.swing.ImageIcon;
import org.apache.commons.lang.SystemUtils;
import org.jetbrains.concurrency.AsyncPromise;
import org.jetbrains.concurrency.Promise;

public class FigmaOverlayProvider implements OverlayProvider {

  private static final String FIGMA_URI = "https://www.figma.com/";
  private static final String EMPTY_ID = "";
  private static final int PORT = 50133;
  private static final int ICON_SIZE = 15;
  private static final int SERVER_TIMEOUT_MILLIS = 120000;
  private LocalServer myServer;

  @Override
  public String getPluginName() {
    return "Figma";
  }

  @Override
  public ImageIcon getPluginIcon() {
    ImageIcon icon = new ImageIcon(this.getClass().getResource("/figma_icon.png"));
    Image image = icon.getImage();
    if (image != null) {
      image = image.getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_SMOOTH);
      return new ImageIcon(image);
    } else {
      return null;
    }
  }

  @Override
  public Promise<OverlayData> addOverlay(Project project) {
    AsyncPromise<OverlayData> promise = new AsyncPromise<>();
    addFromFigma(EMPTY_ID, promise);
    return promise;
  }

  @Override
  public Promise<OverlayData> getOverlay(String overlayId, Project project) {
    AsyncPromise<OverlayData> promise = new AsyncPromise<>();
    addFromFigma(overlayId, promise);
    return promise;
  }

  private void addFromFigma(String overlayId, AsyncPromise<OverlayData> promise) {
    try {
      runFigma();
      if (myServer != null && myServer.isRunning()) {
        myServer.stopServer();
      }
      LocalServer server = new LocalServer(HttpServer.create(new InetSocketAddress(PORT), 0),
          promise,
          overlayId, SERVER_TIMEOUT_MILLIS);
      server.startServer();
      myServer = server;
    } catch (Exception e) {
      promise.setError(e);
    }
  }

  /**
   * Opens Figma in browser
   */
  private static void runFigma() throws Exception {
    if (SystemUtils.IS_OS_LINUX) {
      // Workaround for Linux because "Desktop.getDesktop().browse()" doesn't work on some Linux implementations
      if (Runtime.getRuntime().exec(new String[]{"which", "xdg-open"}).getInputStream().read()
          != -1) {
        Runtime.getRuntime().exec(new String[]{"xdg-open", FIGMA_URI});
      } else {
        throw new FileNotFoundException("Failed to open Figma");
      }
    } else {
      if (Desktop.isDesktopSupported()) {
        Desktop.getDesktop().browse(new URI(FIGMA_URI));
      } else {
        // this shouldn't happen as android studio wouldn't be running
        // in these conditions
        throw new FileNotFoundException("Failed to open Figma");
      }
    }
  }
}
