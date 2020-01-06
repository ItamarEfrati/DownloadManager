
public class DataWrapper {

    //region Fields
    private static final int BUFFER_SIZE = 1024;
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

    public static int getBufferSize() {
        return BUFFER_SIZE;
    }

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
