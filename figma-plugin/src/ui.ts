/**
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
    Server.manualCancelOverlay();
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