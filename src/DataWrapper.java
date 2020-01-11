
public class DataWrapper {

    //region Fields
    private long packetNumber;
    private byte[] packet;
    //endregion

    //region Constructor
    public DataWrapper(long packetNumber, byte[] packet){
        this.packetNumber = packetNumber;
        this.packet = packet;
    }
    //endregion

    //region Getters & Setters
    public long getPacketNumber() {
        return packetNumber;
    }

    public void setPacketNumber(long packetNumber) {
        this.packetNumber = packetNumber;
    }

    public byte[] getPacket() {
        return packet;
    }

    public void setPacket(byte[] packet) {
        this.packet = packet;
    }
    //endregion

}
