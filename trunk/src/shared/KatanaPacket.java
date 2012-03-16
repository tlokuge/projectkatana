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
        String packet = String.format(Constants.PACKET_FORMATTER, packet_id, player_id, opcode.ordinal(), data);
        return packet.getBytes();
    }
    
    public void addData(String category, String value)
    {
        data += category + ":" + value + "\n";
    }

    public void setPlayerId(int player_id){ this.player_id = player_id; }
    public void setOpcode(Opcode opcode)  { this.opcode = opcode; }
    
    public int getPacketId()  { return packet_id; }
    public int getPlayerId()  { return player_id; }
    public Opcode getOpcode() { return opcode; }
    public String getData()   { return data; }
    
    public String toString()
    {
        return packet_id + "\n" +
               player_id + "\n" +
               opcode + "\n" +
               data;
    }
    
    public static KatanaPacket createPacketFromBuffer(String buf)
    {
        String split[] = buf.split("\n");
        try
        {
            int pack_id = Integer.parseInt(split[0]);
            int play_id = Integer.parseInt(split[1]);
            Opcode op = Opcode.getOpcode(Integer.parseInt(split[2]));
            String pack_data = split[3];
            for(int i = 4; i < split.length; ++i)
                pack_data += "\n" + split[i];

            return new KatanaPacket(pack_id, play_id, op, pack_data);
        }
        catch(NumberFormatException ex)
        {
            return null;
        }
    }
}
