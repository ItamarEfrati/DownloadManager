public class DataWrapper {

    //region Fields
    private static final int BUFFER_SIZE = 1024
    private int packetNumber;
    private byte[] packet = new byte[BUFFER_SIZE];
    //endregion

    //region Constructor
    public DataWrapper(int packetNumber, byte[] packet){
        this.packetNumber = packetNumber;
        this.packet = packet;
    }
    //endregion

    //region Getters & Setters

    public static int getBufferSize() {
        return BUFFER_SIZE;
    }

    public int getPacketNumber() {
        return packetNumber;
    }

    public void setPacketNumber(int packetNumber) {
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
