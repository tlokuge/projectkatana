package shared;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class KatanaSocket
{
    private Socket socket;
    
    public KatanaSocket(String host, int port)
    {
        try
        {
            socket = new Socket(host, port);
        }
        catch(Exception ex)
        {
            socket = null;
            ex.printStackTrace();
        }
    }
    
    public byte[] listen()
    {
        if(socket == null)
            return null;
        
        try
        {
            byte[] buffer = new byte[Constants.MAX_PACKET_BUF];
            InputStream in = socket.getInputStream();
            in.read(buffer);
            in.close();
            
            return buffer;
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        
        return null;
    }
    
    public void sendPacket(KatanaPacket packet)
    {
        if(socket == null)
            return;
        
        try
        {
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