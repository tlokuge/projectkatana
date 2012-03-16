import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class KatanaSocket
{
    private int listen_port;
    
    Thread thread;
    
    public KatanaSocket(int listen_port)
    {
        this.listen_port = listen_port;
    }
    
    public byte[] listen()
    {
        try
        {
            ServerSocket listener = new ServerSocket(listen_port);
            Socket socket = listener.accept();
            //System.out.println("Accepted Socket: " + socket);
            byte[] buffer = new byte[Constants.MAX_PACKET_BUF];
            InputStream in = socket.getInputStream();
            in.read(buffer);
            in.close();
            socket.close();
            listener.close();
            
            return buffer;
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
        
        return null;
    }
    
    public void sendPacket(String host, int port, KatanaPacket packet)
    {
        try
        {
            Socket socket = new Socket(host, port);
            OutputStream out = socket.getOutputStream();
            out.write(packet.convertToBytes());
            out.flush();
            out.close();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }
}