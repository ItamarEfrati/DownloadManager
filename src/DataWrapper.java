
public class DataWrapper {

    public int getPacketIndex() {
        return packetIndex;
    }

    private final int packetIndex;
    //region Fields
    private long packetNumber;
    private byte[] packet;
    //endregion

    //region Constructor
    public DataWrapper(int packetIndex, long packetNumber, byte[] packet){
        this.packetNumber = packetNumber;
        this.packet = packet;
        this.packetIndex = packetIndex;
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
