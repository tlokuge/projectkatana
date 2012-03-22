package shared;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class KatanaSocket
{
    class KatanaSocketListener implements Runnable
    {
        private Socket listener;
        private Thread thread;
        
        public KatanaSocketListener(Socket socket)
        {
            listener = socket;
            thread = new Thread(this);
            thread.start();
        }
        
        public void listen()
        {
            if(listener == null)
                return;

            try
            {
                byte[] buffer = new byte[Constants.MAX_PACKET_BUF];
                InputStream in = listener.getInputStream();
                in.read(buffer);

                KatanaPacket packet = KatanaPacket.createPacketFromBuffer(new String(buffer));
                packetHandler.parsePacket(packet);
                
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }
            
            listen();
        }
        
        @Override
        public void run() { listen(); }
    }
    
    private PacketHandler packetHandler = new PacketHandler();
    private Socket socket;
    private KatanaSocketListener socketListener;
    
    public KatanaSocket(String host, int port)
    {
        try
        {
            socket = new Socket(host, port);
            socketListener = new KatanaSocketListener(socket);
        }
        catch(Exception ex)
        {
            socket = null;
            socketListener = null;
            ex.printStackTrace();
        }
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
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }
}