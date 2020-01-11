import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingDeque;

public class PacketWrite implements Runnable{

    private final String downloadedFilePath = "";
    LinkedBlockingDeque<DataWrapper> packetDataQueue;
    MetaData metaData;

    public PacketWrite(LinkedBlockingDeque<DataWrapper> packetDataQueue, MetaData metaData){
        this.packetDataQueue = packetDataQueue;
        this.metaData = metaData;
    }

    @Override
    public void run() {
        while(true) {
            DataWrapper dataToHandle = packetDataQueue.pop();
            int positionToUpdate = (int)dataToHandle.getPacketNumber();
            byte[] dataToWrite = dataToHandle.getPacket();

            writePacket(dataToWrite);
            updateMetaData(positionToUpdate);
        }
    }

    private void writePacket(byte[] dataToWrite) {
        try(FileOutputStream fileOutputStream = new FileOutputStream(downloadedFilePath)) {
            fileOutputStream.write(dataToWrite);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateMetaData(int positionToUpdate) {
        metaData.UpdateIndex(positionToUpdate);
    }
}
