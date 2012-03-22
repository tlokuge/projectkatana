package server;


import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import shared.Constants;
import shared.KatanaPacket;


public class KatanaClient implements Runnable
{
    private Socket client;
    Thread thread;
    
    public KatanaClient(Socket client)
    {
        this.client = client;
        thread = new Thread(this);
        thread.start();
    }
    
    public void sendPacket(KatanaPacket packet)
    {
        try
        {
            OutputStream out = client.getOutputStream();
            out.write(packet.convertToBytes());
            out.flush();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    public void run()
    {
        try
        {
            byte[] buffer = new byte[Constants.MAX_PACKET_BUF];
            InputStream in = client.getInputStream();
            in.read(buffer);
            KatanaPacket packet = KatanaPacket.createPacketFromBuffer(new String(buffer));
            if(packet != null)
            {
                System.out.println("[" + client.getInetAddress().getHostAddress() + ":" + client.getPort() + "] - " + packet.getPacketId() + " - " + packet.getOpcode().name());
                PacketHandler.handlePacket(this, packet);
            }
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    protected void finalize() throws Throwable
    {
        try
        {
            super.finalize();
            System.out.println("ClientWorker: Closing socket: " + client);
            client.close();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }
}