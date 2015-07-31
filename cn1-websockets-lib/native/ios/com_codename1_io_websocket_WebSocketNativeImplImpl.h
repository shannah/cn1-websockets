#import <Foundation/Foundation.h>
#import "SRWebSocket.h"

@interface com_codename1_io_websocket_WebSocketNativeImplImpl : NSObject <SRWebSocketDelegate>{
    SRWebSocket *socket;
    NSString *url;
    int id_;
}

-(void)close;
-(void)sendString:(NSString*)param;
-(void)sendBytes:(NSData*)param;
-(void)setUrl:(NSString*)param;
-(void)setId:(int)param;
-(int)getId;
-(void)connect;

-(BOOL)isSupported;
@end
