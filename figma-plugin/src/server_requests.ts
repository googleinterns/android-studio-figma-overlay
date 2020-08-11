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

import { API, RETURN_OVERLAY_REQUEST, ExportOverlayRequest, setError, makePostData } from './helper'

/** Method that sends the (decoded) fetched overlay, along with its
 * ID and display name. 
*/
export async function sendOverlay(msg: ExportOverlayRequest) {
    const data: string = makePostData(msg);
    console.log("sending ", data)

    var request = new XMLHttpRequest()
    request.open('POST', API + "/result")
    request.onerror = () => {
        setError("Connection timed out. Please request the overlay again.")
    }
    request.send(data)
}

/** Method that requests the overlay ID to be fetched 
 * from Android Studio plugin local server */
export function requestOverlayId() {
    var request = new XMLHttpRequest()
    request.open('GET', API + "/overlay_id")
    request.onload = () => {
        window.parent.postMessage({ pluginMessage: { type: RETURN_OVERLAY_REQUEST, id: request.response } }, '*')
    };
    request.onerror = () => {
        setError("Connection timed out. Please request the overlay again.")
    }
    request.send()
}

/** This is a simple call to the local server 
 *  which stops the server.
 */
export function cancelOverlay() {
    var request = new XMLHttpRequest();
    request.open('GET', API + "/cancel");
    request.send();
}

/**  This is a simple call to the local server 
 *  which stops the server,triggered
 * when the user manually closes the plugin */
export function manualCancelOverlay() {
    var request = new XMLHttpRequest();
    request.open('GET', API + "/manual_cancel");
    request.send();
}