import {
    ID_REQUEST,
    SEND_OVERLAY_REQUEST,
    CLOSE_PLUGIN_REQUEST,
    SELECT_OVERLAY_ERR,
    MULTI_OVERLAY_ERR,
    INVALID_ID_ERR,
    setError
} from './helper'

import * as Server from './server_requests'

document.getElementById('send').onclick = () => {
    parent.postMessage({ pluginMessage: { type: SEND_OVERLAY_REQUEST } }, '*')
}

document.getElementById('cancel').onclick = () => {
    Server.cancelOverlay();
    parent.postMessage({ pluginMessage: { type: CLOSE_PLUGIN_REQUEST } }, '*')
}

window.onmessage = async (event) => {
    switch (event.data.pluginMessage.type) {
        case ID_REQUEST:
            Server.requestOverlayId();
            break;
        case SEND_OVERLAY_REQUEST:
            await Server.sendOverlay(event.data.pluginMessage);
            break;
        case CLOSE_PLUGIN_REQUEST:
            Server.cancelOverlay();
            break;
        case INVALID_ID_ERR:
            setError("This overlay does not exist anymore.");
            Server.cancelOverlay();
            break;
        case SELECT_OVERLAY_ERR:
            setError("Please select an overlay before trying to send to Android Studio.")
            break;
        case MULTI_OVERLAY_ERR:
            setError("Please select only one overlay.");
            break;
    }
}