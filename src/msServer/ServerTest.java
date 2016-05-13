package msServer;  
  
import java.io.BufferedReader;  
import java.io.ByteArrayOutputStream;  
import java.io.StringReader;  
import java.net.InetSocketAddress;  
import java.nio.ByteBuffer;  
import java.nio.channels.SelectionKey;  
import java.nio.channels.Selector;  
import java.nio.channels.ServerSocketChannel;  
import java.nio.channels.SocketChannel;  
import java.util.Iterator;  
  
public class ServerTest {  
  
    public void start() throws Exception {  
        Selector selector = Selector.open();  
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();  
        serverSocketChannel.configureBlocking(false);  
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);  
        serverSocketChannel.socket().setReuseAddress(true);  
        serverSocketChannel.socket().bind(new InetSocketAddress(80));  
        while(true){
            String id1 = "";
            String id2 = "";
            
            int t;
            while ((t = selector.select()) > 0) {  
            	System.out.println(t);
                Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
                while (selectedKeys.hasNext()) {  
                    SelectionKey key = selectedKeys.next();  
                    if (key.isAcceptable()) {  
                        ServerSocketChannel ssc = (ServerSocketChannel) key.channel();  
                        SocketChannel channel = ssc.accept();  
                        if(channel != null){  
                            channel.configureBlocking(false);  
                            channel.register(selector, SelectionKey.OP_READ);// 客户socket通道注册读操作  
                        }  
                    } else if (key.isReadable()) {  
                        SocketChannel channel = (SocketChannel) key.channel();  
                        channel.configureBlocking(false);  
                        String receive = receive(channel);  
                        BufferedReader b = new BufferedReader(new StringReader(receive));  
  
                        String s = b.readLine(); 
                        while (s !=null) {  
                            System.out.println(s);  
                            s = b.readLine();  
                        }  
                        b.close();  
                        channel.register(selector, SelectionKey.OP_WRITE);  
                    } else if (key.isWritable()) {  
                        SocketChannel channel = (SocketChannel) key.channel();  
                        String hello = "[["+id1+","+id2+"]]";  
                        ByteBuffer buffer = ByteBuffer.allocate(1024);  
                          
                        byte[] bytes = hello.getBytes();  
                        buffer.put(bytes);  
                        buffer.flip();  
                        channel.write(buffer);  
                        channel.shutdownInput();  
                        channel.close();  
                    }  
                }  
            }  
        }  
    }  
  
    // 接受数据  
    private String receive(SocketChannel socketChannel) throws Exception {  
        ByteBuffer buffer = ByteBuffer.allocate(1024);  
        byte[] bytes = null;  
        int size = 0;  
        ByteArrayOutputStream baos = new ByteArrayOutputStream();  
        while ((size = socketChannel.read(buffer)) > 0) {  
            buffer.flip();  
            bytes = new byte[size];  
            buffer.get(bytes);  
            baos.write(bytes);  
            buffer.clear();  
        }  
        bytes = baos.toByteArray();  
  
        return new String(bytes);  
    }  
  
}  