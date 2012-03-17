
package server;

import java.util.ArrayList;
import shared.KatanaPacket;

public abstract class PacketHandler
{
    public static void handlePacket(KatanaPacket packet)
    {
        if(packet == null)
            return;
        
        switch(packet.getOpcode())
        {
            case C_REGISTER: handleRegisterPacket(packet); break;
                
            case C_LOGIN:
                break;
                
            case C_LOGOUT:
                break;
                
            case C_PONG:
                break;
                
            case C_ROOM_CREATE:
                break;
                
            case C_ROOM_DESTROY:
                break;
                
            case C_ROOM_JOIN:
                break;
                
            case C_ROOM_LIST:
                break;
                
            case C_CLASS_CHANGE:
                break;
                
            case C_LEADERBOARD:
                break;
                
            case C_GAME_START:
                break;
                
            case C_MOVE:
                break;
                
            case C_SPELL:
                break;
        }
    }
    
    // Packet data format expected to be username / password / location
    private static void handleRegisterPacket(KatanaPacket packet)
    {
        System.out.println("handleRegisterPacket: INCOMPLETE");
        String[] data = packet.getData().split("\n");
        if(data.length < 3)
        {
            System.err.println("handleRegisterPacket: Invalid data in packet (" + packet.getData() + ")");
            // TODO: Send response to client
            return;
        }
        
        String username = data[0].trim();
        String password = data[1];
        String location = data[2]; // TODO
        
        SQLHandler sql = SQLHandler.instance();
        ArrayList results = sql.execute("SELECT `username` FROM `users` WHERE `username` LIKE '" + username + "';");
        if(results != null && !results.isEmpty())
        {
            System.err.println("handleRegisterPacket: Username [" + username + "] already exists in database. Prevent registration");
            // Send response to client
            return;
        }
        
        sql.executeQuery("INSERT INTO `users` (`username`,`password`) VALUES ('" + username + "', SHA1('" + password + "'));");
        
    }
    
}
