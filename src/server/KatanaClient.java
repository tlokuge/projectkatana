package server;


import java.io.IOException;
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
    
    private static int client_count = 0;
    
    public KatanaClient(Socket client)
    {
        id = -1;
        this.client = client;
        thread = new Thread(this, "Client " + client_count++ + ": " + client.getInetAddress().getHostAddress() + ":" + client.getPort());
        thread.start();
    }
    
    public void setId(long id) { this.id = id; }
    public long getId() { return id; }
    
    public void remove()
    {
        System.out.println("Removing client " + id);
        try
        {
            client.close();
            KatanaServer.instance().removeClient(id);
            thread.interrupt();
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
        
    }
    
    public void sendPacket(KatanaPacket packet)
    {
        try
        {
            if(client.isClosed() || client.isOutputShutdown() || thread.isInterrupted())
                return;
            
            OutputStream out = client.getOutputStream();
            out.write(packet.convertToBytes());
            out.flush();
            
            System.out.println("[" + packet.getPacketId() + " - " + packet.getOpcode().name() + "] ==> [CLIENT " + id + "]");
        }
        catch(SocketException ex)
        {
            System.out.println("KatanaClient: Received SocketException - " + ex.getLocalizedMessage() + " - Removing client: " + id);
            remove();
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
            if(client.isClosed() || client.isInputShutdown())
                return;
            
            byte[] buffer = new byte[Constants.MAX_PACKET_BUF];
            InputStream in = client.getInputStream();
            in.read(buffer);
            //System.out.println("Input: " +new String(buffer));
            KatanaPacket packet = KatanaPacket.createPacketFromBuffer(new String(buffer));
            if(packet != null)
            {
                System.out.println("[" + client.getInetAddress().getHostAddress() + ":" + client.getPort() + "] - " + packet.getPacketId() + " - " + packet.getOpcode().name());
                PacketHandler.handlePacket(this, packet);
            }
        }
        catch(Exception ex)
        {
            remove();
            ex.printStackTrace();
        }
    }
    
    @Override
    public void run()
    {
        while(!thread.isInterrupted())
            listen();
    }
    
    @Override
    protected void finalize() throws Throwable
    {
        try
        {
            super.finalize();
            System.out.println("ClientWorker: Closing socket: " + client);
            remove();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }
}