#import "com_codename1_io_websocket_WebSocketNativeImplImpl.h"
#import "com_codename1_io_websocket_WebSocket.h"
#import "CodenameOne_GLViewController.h"

@implementation com_codename1_io_websocket_WebSocketNativeImplImpl

-(void)close{
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
    com_codename1_io_websocket_WebSocket_openReceived___int(CN1_THREAD_GET_STATE_PASS_ARG id_);
}

- (void)webSocket:(SRWebSocket *)webSocket didFailWithError:(NSError *)error {
    com_codename1_io_websocket_WebSocket_errorReceived___int_java_lang_String_int(CN1_THREAD_GET_STATE_PASS_ARG id_, fromNSString(CN1_THREAD_GET_STATE_PASS_ARG [error localizedDescription]), [error code] );
}

- (void)webSocket:(SRWebSocket *)webSocket didCloseWithCode:(NSInteger)code reason:(NSString *)reason wasClean:(BOOL)wasClean {
    com_codename1_io_websocket_WebSocket_closeReceived___int_int_java_lang_String(CN1_THREAD_GET_STATE_PASS_ARG id_ ,code, fromNSString(CN1_THREAD_GET_STATE_PASS_ARG reason) );
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
@end
