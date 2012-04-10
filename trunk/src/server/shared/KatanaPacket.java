package server.shared;

public class KatanaPacket
{
    private int packet_id;
    private Opcode opcode;
    private String data;
    
    private static int NEXT_PACKET_ID = 0;

    public KatanaPacket(Opcode opcode)
    {
        this.packet_id = ++NEXT_PACKET_ID;
        this.opcode = opcode;
        this.data = "";
    }
    
    private KatanaPacket(int packet_id, Opcode opcode, String data)
    {
        this.packet_id = packet_id;
        this.opcode = opcode;
        this.data = data;
    }

    public byte[] convertToBytes()
    {
        String packet = String.format(Constants.PACKET_FORMATTER, packet_id, opcode.ordinal(), data);
        return packet.getBytes();
    }
    
    public void addData(String data)
    {
        this.data += data + Constants.PACKET_DATA_SEPERATOR;
    }

    public void setOpcode(Opcode opcode)  { this.opcode = opcode; }
    
    public int getPacketId()  { return packet_id; }
    public Opcode getOpcode() { return opcode; }
    public String getData()   { return data; }
    
    public String toString()
    {
        return String.format(Constants.PACKET_FORMATTER, packet_id, opcode.ordinal(), data);
    }
    
    public static KatanaPacket createPacketFromBuffer(String buf)
    {
        String split[] = buf.split(Constants.PACKET_DATA_SEPERATOR);
        try
        {
            int pack_id = Integer.parseInt(split[0].trim());
            Opcode op = Opcode.getOpcode(Integer.parseInt(split[1]));
            String pack_data = split[2];
            for(int i = 3; i < split.length; ++i)
                pack_data += Constants.PACKET_DATA_SEPERATOR + split[i];

            return new KatanaPacket(pack_id, op, pack_data);
        }
        catch(NumberFormatException ex)
        {
            return null;
        }
    }
}
