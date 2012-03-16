package server;

import java.net.InetAddress;
import java.net.UnknownHostException;

import shared.Constants;
import shared.KatanaPacket;
import shared.KatanaSocket;
import shared.Opcode;

public class ServerMain
{
    public static void main(String[] args) throws UnknownHostException, InterruptedException
    {
        System.out.println("  _  __      _______       _   _        ");
        System.out.println(" | |/ /   /\\|__   __|/\\   | \\ | |   /\\    ");
        System.out.println(" | ' /   /  \\  | |  /  \\  |  \\| |  /  \\   ");
        System.out.println(" |  <   / /\\ \\ | | / /\\ \\ | . ` | / /\\ \\  ");
        System.out.println(" | . \\ / ____ \\| |/ ____ \\| |\\  |/ ____ \\ ");
        System.out.println(" |_|\\_|_/    \\_\\_/_/    \\_\\_| \\_/_/    \\_\\");
        
        
        KatanaServer katana = new KatanaServer();
        PingServer ping = new PingServer();
        
        
        KatanaSocket.sendPacket(InetAddress.getLocalHost().getHostAddress(), Constants.SERVER_LISTEN_PORT, new KatanaPacket(0, Opcode.C_LOGIN));
        KatanaSocket.sendPacket(InetAddress.getLocalHost().getHostAddress(), Constants.SERVER_LISTEN_PORT, new KatanaPacket(0, Opcode.C_LOGOUT));
        
        KatanaSocket.sendPacket(InetAddress.getLocalHost().getHostAddress(), Constants.SERVER_LISTEN_PORT, new KatanaPacket(1, Opcode.S_AUTH_NO));
        KatanaSocket.sendPacket(InetAddress.getLocalHost().getHostAddress(), Constants.SERVER_LISTEN_PORT, new KatanaPacket(1, Opcode.S_AUTH_OK));
        
    }
}