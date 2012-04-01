package server;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

import shared.Constants;
import shared.KatanaPacket;
import shared.Opcode;


public class KatanaClient implements Runnable
{
    private int id;
    private Socket client;
    private Player player;
    
    Thread thread;
    
    private static int client_count = 0;
    
    public KatanaClient(Socket client)
    {
        try
        {
            client.setSoLinger(true,0);
        }
        catch(SocketException ex)
        {
            ex.printStackTrace();
        }
        id = -1;
        this.client = client;
        player = null;
        
        KatanaServer.instance().addWaitingClient(this);
        
        thread = new Thread(this, "Client " + client_count++ + ": " + client.getInetAddress().getHostAddress() + ":" + client.getPort());
        thread.start();
    }
    
    public void setPlayer(Player pl) 
    {
        this.player = pl;
        this.id     = pl.getId();
    }
    public Player getPlayer() { return player; }
    public int getId()        { return id; }
    
    public void remove()
    {
        System.out.println("Removing client " + id);
        try
        {
            sendPacket(new KatanaPacket(-1, Opcode.S_LOGOUT));
            client.close();
            KatanaServer.instance().removePlayer(id);
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
            
            System.out.println("[" + packet.getPacketId() + " - " + packet.getOpcode().name() + "] ==> [CLIENT " + id + "]");
            OutputStream out = client.getOutputStream();
            out.write(packet.convertToBytes());
            out.flush();
            
        }
        catch(SocketException ex)
        {
            System.out.println("KatanaClient: Received SocketException - " + ex.getLocalizedMessage() + " - Removing client: " + id);
            remove();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            remove();
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
            KatanaPacket packet = KatanaPacket.createPacketFromBuffer(new String(buffer));
            if(packet != null)
            {
                System.out.println("[" + packet.getPacketId() + " - " + packet.getOpcode().name() + "] <== [CLIENT " + id + "]");
                if(PacketHandler.handlePacket(this, packet))
                    remove();
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