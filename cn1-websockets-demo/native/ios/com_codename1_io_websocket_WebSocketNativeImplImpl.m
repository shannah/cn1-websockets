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
#import "com_codename1_io_websocket_WebSocketNativeImplImpl.h"
#import "com_codename1_io_websocket_WebSocket.h"
#import "CodenameOne_GLViewController.h"

@implementation com_codename1_io_websocket_WebSocketNativeImplImpl

-(void)close{
    [socket close];
}

-(void)sendString:(NSString*)param {
    [socket send:param];
}

-(void)sendBytes:(NSData*)param{
    [socket send:param];
}

-(void)setUrl:(NSString*)_url{
    url = _url;
}

-(void)setId:(int)_id {
    id_ = _id;
}

-(int)getId {
    return id_;
}

-(void)connect {
    socket = [[SRWebSocket alloc] initWithURL:[NSURL URLWithString:url]];
    socket.delegate = self;
    [socket open];
}

-(BOOL)isSupported{
    return YES;
}

-(void)dealloc {
#ifndef CN1_USE_ARC
    [socket release];
#endif
    socket = nil;
    [super dealloc];
}

extern JAVA_OBJECT fromNSString(CN1_THREAD_STATE_MULTI_ARG NSString *str);

- (void)webSocketDidOpen:(SRWebSocket *)newWebSocket {
    closed = NO;
    com_codename1_io_websocket_WebSocket_openReceived___int(CN1_THREAD_GET_STATE_PASS_ARG id_);
}

- (void)webSocket:(SRWebSocket *)webSocket didFailWithError:(NSError *)error {
    com_codename1_io_websocket_WebSocket_errorReceived___int_java_lang_String_int(CN1_THREAD_GET_STATE_PASS_ARG id_, fromNSString(CN1_THREAD_GET_STATE_PASS_ARG [error localizedDescription]), [error code] );
    if (!closed && webSocket.readyState == SR_CLOSED) {
        closed = YES;
        com_codename1_io_websocket_WebSocket_closeReceived___int_int_java_lang_String(CN1_THREAD_GET_STATE_PASS_ARG id_ ,1006, fromNSString(CN1_THREAD_GET_STATE_PASS_ARG [error localizedDescription]) );
    }
}

- (void)webSocket:(SRWebSocket *)webSocket didCloseWithCode:(NSInteger)code reason:(NSString *)reason wasClean:(BOOL)wasClean {
    if (!closed) {
        closed = YES;
        com_codename1_io_websocket_WebSocket_closeReceived___int_int_java_lang_String(CN1_THREAD_GET_STATE_PASS_ARG id_ ,code, fromNSString(CN1_THREAD_GET_STATE_PASS_ARG reason) );
    }
    
}

extern JAVA_INT com_codename1_impl_ios_IOSNative_getNSDataSize___long(CN1_THREAD_STATE_MULTI_ARG JAVA_OBJECT instanceObject, JAVA_LONG nsData);

extern void com_codename1_impl_ios_IOSNative_nsDataToByteArray___long_byte_1ARRAY(CN1_THREAD_STATE_MULTI_ARG JAVA_OBJECT instanceObject, JAVA_LONG nsData, JAVA_OBJECT dataArray);

- (void)webSocket:(SRWebSocket *)webSocket didReceiveMessage:(id)message {
    POOL_BEGIN();
    if ([message isKindOfClass:[NSString class]]) {
        com_codename1_io_websocket_WebSocket_messageReceived___int_java_lang_String(CN1_THREAD_GET_STATE_PASS_ARG id_ , fromNSString(CN1_THREAD_GET_STATE_PASS_ARG (NSString*)message));
    } else {
        NSData* data = (NSData*)message;
        JAVA_OBJECT arr = com_codename1_io_websocket_WebSocket_newByteArray___int_R_byte_1ARRAY(CN1_THREAD_GET_STATE_PASS_ARG [data length]);
        com_codename1_impl_ios_IOSNative_nsDataToByteArray___long_byte_1ARRAY(CN1_THREAD_GET_STATE_PASS_ARG NULL, data, arr);
        com_codename1_io_websocket_WebSocket_messageReceived___int_byte_1ARRAY(CN1_THREAD_GET_STATE_PASS_ARG id_, arr);
    }
    POOL_END();
}

-(int)getReadyState {
    if (socket == nil) {
        return 3;
    }
    return socket.readyState;
}
@end
