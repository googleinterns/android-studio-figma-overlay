import {
  ID_REQUEST,
  SEND_OVERLAY_REQUEST,
  RETURN_OVERLAY_REQUEST,
  CLOSE_PLUGIN_REQUEST,
  SELECT_OVERLAY_ERR,
  MULTI_OVERLAY_ERR,
  INVALID_ID_ERR,
  FrontendRequest
} from './helper'

figma.showUI(__html__, {
  width: 250, height: 130
});

/** When the plugin is called the local server is queried 
 * for an Overlay ID to be fetched from Figma.
 */
figma.ui.postMessage({ type: ID_REQUEST })

figma.ui.onmessage = async (msg: FrontendRequest) => {
  switch (msg.type) {
    case SEND_OVERLAY_REQUEST:
      await returnSelectedOverlay()
      break;
    case RETURN_OVERLAY_REQUEST:
      await returnOverlayById(msg.id);
      break;
    case CLOSE_PLUGIN_REQUEST:
      figma.closePlugin();
      break;
  }
};

/** Method for sending the selected overlay back
 *  to Android Studio. Only 1 overlay can be 
 *  selected and sent at a time.
 */
async function returnSelectedOverlay() {
  const nodes = figma.currentPage.selection
  const toSend: SceneNode = nodes ? nodes[0] : null
  if (!toSend) {
    figma.ui.postMessage({
      type: SELECT_OVERLAY_ERR,
    })
  } else if (nodes.length > 1) {
    figma.ui.postMessage({
      type: MULTI_OVERLAY_ERR,
    })
  } else {
    await returnOverlayById(toSend.id);
  }
}

/** Method for sending a requested overlay
 *  back to Android Studio. 
 */
async function returnOverlayById(id: string) {
  if (id != '') {
    const toSend: SceneNode = figma.currentPage.findOne(n => n.id === id)
    if (!toSend) {
      // this would only happen if node was deleted in the meantime
      figma.ui.postMessage({
        type: INVALID_ID_ERR,
      })
    } else {
      const img: Uint8Array = await toSend.exportAsync();
      figma.ui.postMessage({
        type: SEND_OVERLAY_REQUEST,
        overlay: img,
        id: toSend.id,
        name: toSend.name
      })
      figma.closePlugin()
    }
  }
}
