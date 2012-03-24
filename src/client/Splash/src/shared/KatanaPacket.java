package shared;

public class KatanaPacket
{
    private int packet_id;
    private int player_id;
    private Opcode opcode;
    private String data;
    private static int next_packet_id = 0;

    public KatanaPacket(int player_id, Opcode opcode)
    {
        this.packet_id = ++next_packet_id;
        this.player_id = player_id;
        this.opcode = opcode;
        this.data = "";
    }
    
    private KatanaPacket(int packet_id, int player_id, Opcode opcode, String data)
    {
        this.packet_id = packet_id;
        this.player_id = player_id;
        this.opcode = opcode;
        this.data = data;
    }

    public byte[] convertToBytes()
    {
        String packet = String.format(KatanaConstants.PACKET_FORMATTER, packet_id, player_id, opcode.ordinal(), data);
        return packet.getBytes();
    }
    
    public void addData(String data)
    {
        this.data += data + KatanaConstants.PACKET_DATA_SEPERATOR;
    }

    public void setPlayerId(int player_id){ this.player_id = player_id; }
    public void setOpcode(Opcode opcode)  { this.opcode = opcode; }
    
    public int getPacketId()  { return packet_id; }
    public int getPlayerId()  { return player_id; }
    public Opcode getOpcode() { return opcode; }
    public String getData()   { return data; }
    
    public String toString()
    {
        return String.format(KatanaConstants.PACKET_FORMATTER, packet_id, player_id, opcode.ordinal(), data);
    }
    
    public static KatanaPacket createPacketFromBuffer(String buf)
    {
    	if(buf == null || buf.length() == 0)
    			return null;
    	
        String split[] = buf.split(KatanaConstants.PACKET_DATA_SEPERATOR);
        for(int i = 0; i < split.length; ++i)
        	System.out.println("split[" + i + "]:" + split[i]);
        try
        {
            int pack_id = Integer.parseInt(split[0].trim());
            int play_id = Integer.parseInt(split[1].trim());
            Opcode op = Opcode.getOpcode(Integer.parseInt(split[2]));
            String pack_data = split[3];
            for(int i = 4; i < split.length; ++i)
                pack_data += KatanaConstants.PACKET_DATA_SEPERATOR + split[i];

            return new KatanaPacket(pack_id, play_id, op, pack_data);
        }
        catch(NumberFormatException ex)
        {
            return null;
        }
    }
}
