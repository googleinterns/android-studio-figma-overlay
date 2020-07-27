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