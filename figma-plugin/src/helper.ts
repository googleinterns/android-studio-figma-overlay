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

export const API = "http://localhost:50133"

export const ID_REQUEST = "request-id";
export const SEND_OVERLAY_REQUEST = 'send-overlay';
export const CLOSE_PLUGIN_REQUEST = 'close-plugin';
export const RETURN_OVERLAY_REQUEST = 'return-overlay';

export const INVALID_ID_ERR = "invalid-id";
export const SELECT_OVERLAY_ERR = "select-overlay";
export const MULTI_OVERLAY_ERR = "multi-overlay";

export type ExportOverlayRequest = {
    type: 'send-overlay'
    overlay: Uint8Array
    id: string
    name: string
}

interface SendOverlayRequest {
    type: 'send-overlay'
}

interface ReturnOverlayRequest {
    type: 'return-overlay'
    id: string
}

interface ClosePluginRequest {
    type: 'close-plugin'
}

export type FrontendRequest = SendOverlayRequest | ReturnOverlayRequest | ClosePluginRequest;

export function setError(msg: string) {
    const snackbar = document.getElementById('snackbar');
    snackbar.innerText = msg;
    snackbar.className = 'show';
    setTimeout(function () { snackbar.className = snackbar.className.replace("show", ""); }, 3000);
}

export function makePostData(data: ExportOverlayRequest): string {
    const binstr = Array.prototype.map.call(data.overlay, function (ch) {
        return String.fromCharCode(ch);
    }).join('');

    return JSON.stringify({
        overlay: btoa(binstr),
        id: data.id,
        name: data.name
    })
}