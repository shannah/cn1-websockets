using System;
using Windows.Foundation;
using Windows.Networking.Sockets;
using System.Runtime.InteropServices.WindowsRuntime;
using Windows.Storage.Streams;
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
namespace com.codename1.io.websocket {
    

    public class WebSocketNativeImplImpl : IWebSocketNativeImplImpl {
        Windows.Networking.Sockets.MessageWebSocket sck;
        DataWriter messageWriter;
        private string url;
        private int id;
        private WebSocketState state = WebSocketState.CLOSED;
        

        private Windows.UI.Core.CoreDispatcher dispatcher
        {
            get
            {
                return impl.SilverlightImplementation.dispatcher;
            }
        }

        public WebSocketNativeImplImpl()
        {
            dispatcher.RunAsync(Windows.UI.Core.CoreDispatcherPriority.Normal, () =>
            {

                sck = new Windows.Networking.Sockets.MessageWebSocket();

                sck.MessageReceived += Sck_MessageReceived;
                
                sck.Closed += Sck_Closed;
                
            }).AsTask().GetAwaiter().GetResult();

        }

        private void Sck_Closed(IWebSocket sender, WebSocketClosedEventArgs args)
        {
            state = WebSocketState.CLOSED;
            com.codename1.io.websocket.WebSocket.closeReceived(id, args.Code, args.Reason);
            closeSocket();
        }

        private async void Sck_MessageReceived(MessageWebSocket sender, MessageWebSocketMessageReceivedEventArgs args)
        {

            //java.lang.System.@out.println("Message received " + args);

            if (args.MessageType == SocketMessageType.Utf8)
            {
                Windows.Storage.Streams.DataReader messageReader = args.GetDataReader();
                messageReader.UnicodeEncoding = UnicodeEncoding.Utf8;
                string messageString = messageReader.ReadString(messageReader.UnconsumedBufferLength);
                com.codename1.io.websocket.WebSocket.messageReceived(id, messageString);
            } else
            {

                Windows.Storage.Streams.IInputStream readStream = args.GetDataStream();
                byte[] readBuffer = new byte[4096];
                try
                {
                    while (true)
                    {
                        if (sender != sck)
                        {
                            return;
                        }

                        IBuffer res = await readStream.ReadAsync(readBuffer.AsBuffer(), (uint)readBuffer.Length, 0);
                        if (res.Length == 0)
                        {
                            return;
                        }
                        byte[] resArr = new byte[res.Length];
                        res.CopyTo(resArr);
                        com.codename1.io.websocket.WebSocket.messageReceived(1, resArr);

                    }
                } catch (Exception ex)
                {
                    com.codename1.io.websocket.WebSocket.errorReceived(id, ex.Message, ex.HResult);
                }
                
            }
        }

        public void close() {
            dispatcher.RunAsync(Windows.UI.Core.CoreDispatcherPriority.Normal, async () =>
            {
                try
                {
                    state = WebSocketState.CLOSING;
                    sck.Close(0, "Closed");
                    closeSocket();
                    state = WebSocketState.CLOSED;
                    
                    
                } catch (Exception ex)
                {
                    com.codename1.io.websocket.WebSocket.errorReceived(id, ex.Message, ex.HResult);
                }
                
            });
        }

        public void sendBytes(byte[] message) {
            dispatcher.RunAsync(Windows.UI.Core.CoreDispatcherPriority.Normal, async () =>
            {
                try
                {
                    sck.Control.MessageType = SocketMessageType.Binary;
                    await sck.OutputStream.WriteAsync(message.AsBuffer());
                } catch (Exception ex)
                {
                    com.codename1.io.websocket.WebSocket.errorReceived(id, ex.Message, ex.HResult);
                }
                
            }).AsTask().GetAwaiter().GetResult();
        }

        public void sendString(string message) {
            dispatcher.RunAsync(Windows.UI.Core.CoreDispatcherPriority.Normal, async () =>
            {
                try
                {
                    sck.Control.MessageType = SocketMessageType.Utf8;
                    //java.lang.System.@out.println("Sending string " + message);
                    //await sck.OutputStream.WriteAsync(System.Text.Encoding.UTF8.GetBytes(message).AsBuffer());
                    messageWriter.WriteString(message);
                    
                    await messageWriter.StoreAsync();
                    //java.lang.System.@out.println("Finished sending " + message);
                   
                }
                catch (Exception ex)
                {
                    com.codename1.io.websocket.WebSocket.errorReceived(id, ex.Message, ex.HResult);
                }
                
            });
        }

        private void closeSocket()
        {
            if (messageWriter != null)
            {
                // In order to reuse the socket with another DataWriter, the socket's output stream needs to be detached.
                // Otherwise, the DataWriter's destructor will automatically close the stream and all subsequent I/O operations
                // invoked on the socket's output stream will fail with ObjectDisposedException.
                //
                // This is only added for completeness, as this sample closes the socket in the very next code block.
                messageWriter.DetachStream();
                messageWriter.Dispose();
                messageWriter = null;
            }

            if (sck != null)
            {
                try
                {
                    sck.Close(1000, "Closed due to user request.");
                }
                catch (Exception ex)
                {
                    java.lang.System.@out.println(ex.StackTrace);
                }
                sck = null;
            }
        }

        public void setUrl(string url) {
            this.url = url;
        }

        public bool isSupported() {
            return true;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public void connect() {
            dispatcher.RunAsync(Windows.UI.Core.CoreDispatcherPriority.Normal, async () =>
            {
                try
                {
                    state = WebSocketState.CONNECTING;
                    //java.lang.System.@out.println("Connecting to " + url);
                    await sck.ConnectAsync(new Uri(url));
                    
                    state = WebSocketState.OPEN;
                    messageWriter = new DataWriter(sck.OutputStream);
                    com.codename1.io.websocket.WebSocket.openReceived(id);

                }
                catch (Exception ex)
                {
                    state = WebSocketState.CLOSED;
                    com.codename1.io.websocket.WebSocket.errorReceived(id, ex.Message, ex.HResult);
                }
            }).AsTask().GetAwaiter().GetResult();

        }
    
        public int getReadyState() {
            if (WebSocketState.CONNECTING == state)
            {
                return 0;
            } else if (WebSocketState.OPEN == state)
            {
                return 1;
            } else if (WebSocketState.CLOSING == state)
            {
                return 2;
            }
            return 3; // closed
        }

    }
}
