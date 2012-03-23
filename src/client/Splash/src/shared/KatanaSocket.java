package shared;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;

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
            if(listener == null || listener.isClosed())
                return;

            try
            {
                byte[] buffer = new byte[Constants.MAX_PACKET_BUF];
                InputStream in = listener.getInputStream();
                in.read(buffer);

                KatanaPacket packet = KatanaPacket.createPacketFromBuffer(new String(buffer));
                System.out.println("KatanaSocket received: " + packet.getOpcode().name());
                //

            }
            catch(ConnectException ex)
            {
                System.err.println("KatanaSocketListener: Received ConnectException: " + ex.getLocalizedMessage());
                closeListener();
                thread.interrupt();
            }
            catch(SocketException ex)
            {
                System.err.println("KatanaSocketListener: Received SocketException: " + ex.getLocalizedMessage());
                closeListener();
                thread.interrupt();
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }
        }
        
        private void closeListener()
        {
            try
            {
                if(listener != null && !listener.isClosed())
                    listener.close();
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }
        }
        
        @Override
        public void run() 
        {
            // RAWR GO FOREVERRRR
            while(true)
                listen(); 
        }
        @Override
        public void finalize() throws Throwable
        {
            super.finalize();
            closeListener();
        }
    }
    
    
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