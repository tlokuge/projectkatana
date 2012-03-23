package server;


import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

import shared.Constants;
import shared.KatanaPacket;


public class KatanaClient implements Runnable
{
    private long id;
    private Socket client;
    Thread thread;
    
    public KatanaClient(Socket client)
    {
        id = -1;
        this.client = client;
        thread = new Thread(this);
        thread.start();
    }
    
    public void setId(long id) { this.id = id; }
    
    public void sendPacket(KatanaPacket packet)
    {
        try
        {
            OutputStream out = client.getOutputStream();
            out.write(packet.convertToBytes());
            out.flush();
            
            System.out.println("[" + packet.getPacketId() + " - " + packet.getOpcode().name() + "] ==> [CLIENT " + id + "]");
        }
        catch(SocketException ex)
        {
            System.out.println("KatanaClient: Received SocketException - " + ex.getLocalizedMessage() + " - Removing client: " + id);
            KatanaServer.instance().removeClient(id);
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    public void listen()
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
    
    @Override
    public void run()
    {
        while(true)
            listen();
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