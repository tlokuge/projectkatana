package shared;

import com.katana.splash.LoginActivity;

public class PacketHandler
{
    public PacketHandler() {

    }
        
    public void parsePacket(KatanaPacket packet){
    	System.out.println("KatanaSocket received: " + packet.getOpcode().name());
    
    	switch(packet.getOpcode()){
    		case S_REG_OK: break;
    		case S_REG_NO: break;
    		default: break;
    	}
    }
}