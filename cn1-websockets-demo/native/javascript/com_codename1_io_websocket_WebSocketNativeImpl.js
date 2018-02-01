/**
 *  Copyright (c) 2015 Steve Hannah
 * 
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 * 
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 * 
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
(function(exports){


    function toByteArray(arr) {
        var len = arr.length;
        var out = $rt_createByteArray(len);
        for (var i=0; i<len; i++) {
            out.data[i] = arr[i];
        }
        return out;
    }

var o = {};

    o.close_ = function(callback) {
        try {
            this.socket.close();
            callback.complete();
        } catch (e) {
            callback.error($rt_str(""+e));
        }
    };

    o.sendBytes__byte_1ARRAY = function(message, callback) {
        try {
            this.socket.send(new Uint8Array(message).buffer);
            callback.complete();
        } catch (e) {
            callback.error($rt_str(""+e));
        }
    };
    
    o.sendString__java_lang_String = function(message, callback) {
        try {
            this.socket.send(message);
            callback.complete();
        } catch (e) {
            callback.error($rt_str(""+e));
        }
    };

    o.setUrl__java_lang_String = function(url, callback) {
        var self = this;
        this.url = url;
        
        callback.complete();
    };
    
    o.setId__int = function(id, callback) {
        this.id = id;
        callback.complete();
    };
    
    o.getId_ = function (callback) {
        callback.complete(this.id);
    };

    o.isSupported_ = function(callback) {
        callback.complete(true);
    };
    
    o.getReadyState_ = function(callback) {
        if (!this.socket) {
            callback.complete(3);
        } else {
            callback.complete(this.socket.readyState);
        }
    };
    
    o.connect_ = function(callback) {
        try {
            var self = this;
            this.socket = new WebSocket(this.url);
            this.socket.binaryType = "arraybuffer";
            var messageReceivedStr = self.$GLOBAL$.com_codename1_io_websocket_WebSocket.messageReceived__int_java_lang_String$async;
            var messageReceivedBytes = self.$GLOBAL$.com_codename1_io_websocket_WebSocket.messageReceivedBytes__int_byte_1ARRAY$async;
            var openReceived = self.$GLOBAL$.com_codename1_io_websocket_WebSocket.openReceived__int$async;
            var closeReceived = self.$GLOBAL$.com_codename1_io_websocket_WebSocket.closeReceived__int_int_java_lang_String$async;
            var errorReceived = self.$GLOBAL$.com_codename1_io_websocket_WebSocket.errorReceived__int_java_lang_String_int$async;


            this.socket.onerror = function(evt) {
                errorReceived(self.id, $rt_str(""+evt.data), 500);
            };

            this.socket.onmessage = function(e) {
                if (typeof e.data === 'string') {
                    messageReceivedStr(self.id, $rt_str(e.data));
                } else if (e.data instanceof ArrayBuffer) {
                    messageReceivedBytes(self.id, toByteArray(new Uint8Array(e.data)));
                } else if (e.data instanceof Blob) {
                    var arrayBuffer;
                    var fileReader = new FileReader();
                    fileReader.onload = function() {
                        arrayBuffer = this.result;
                        messageReceivedBytes(self.id, toByteArray(new Uint8Array(arrayBuffer)));
                    };
                    fileReader.readAsArrayBuffer(e.data);

                } else {
                    throw new Error("Unknown message type in WebSocket.onmessage: "+e.data);
                }
            };

            this.socket.onclose = function (event) {
                var reason;
                if (event.code == 1000)
                    reason = "Normal closure, meaning that the purpose for which the connection was established has been fulfilled.";
                else if(event.code == 1001)
                    reason = "An endpoint is \"going away\", such as a server going down or a browser having navigated away from a page.";
                else if(event.code == 1002)
                    reason = "An endpoint is terminating the connection due to a protocol error";
                else if(event.code == 1003)
                    reason = "An endpoint is terminating the connection because it has received a type of data it cannot accept (e.g., an endpoint that understands only text data MAY send this if it receives a binary message).";
                else if(event.code == 1004)
                    reason = "Reserved. The specific meaning might be defined in the future.";
                else if(event.code == 1005)
                    reason = "No status code was actually present.";
                else if(event.code == 1006)
                   reason = "The connection was closed abnormally, e.g., without sending or receiving a Close control frame";
                else if(event.code == 1007)
                    reason = "An endpoint is terminating the connection because it has received data within a message that was not consistent with the type of the message (e.g., non-UTF-8 [http://tools.ietf.org/html/rfc3629] data within a text message).";
                else if(event.code == 1008)
                    reason = "An endpoint is terminating the connection because it has received a message that \"violates its policy\". This reason is given either if there is no other sutible reason, or if there is a need to hide specific details about the policy.";
                else if(event.code == 1009)
                   reason = "An endpoint is terminating the connection because it has received a message that is too big for it to process.";
                else if(event.code == 1010) // Note that this status code is not used by the server, because it can fail the WebSocket handshake instead.
                    reason = "An endpoint (client) is terminating the connection because it has expected the server to negotiate one or more extension, but the server didn't return them in the response message of the WebSocket handshake. <br /> Specifically, the extensions that are needed are: " + event.reason;
                else if(event.code == 1011)
                    reason = "A server is terminating the connection because it encountered an unexpected condition that prevented it from fulfilling the request.";
                else if(event.code == 1015)
                    reason = "The connection was closed due to a failure to perform a TLS handshake (e.g., the server certificate can't be verified).";
                else
                    reason = "Unknown reason";

                closeReceived(self.id, $rt_str(reason), event.code);

            };

            this.socket.onopen = function(event) {
                openReceived(self.id);
            };
            callback.complete();
        } catch (e) {
            callback.error($rt_str(""+e));
        }
    };

exports.com_codename1_io_websocket_WebSocketNativeImpl= o;

})(cn1_get_native_interfaces());
