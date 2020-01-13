import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.LinkedBlockingDeque;

public class PacketWrite implements Runnable{

    private String downloadedFilePath;
    LinkedBlockingDeque<DataWrapper> packetDataQueue;
    MetaData metaData;

    public PacketWrite(LinkedBlockingDeque<DataWrapper> packetDataQueue, MetaData metaData, String downloadedFileName){
        this.packetDataQueue = packetDataQueue;
        this.metaData = metaData;
        this.downloadedFilePath = downloadedFileName;
        createDownloadFile();
    }

    @Override
    public void run() {
        boolean isFinnishDownload = false;

        while(!isFinnishDownload) {
            DataWrapper dataToHandle = packetDataQueue.poll();
            if(dataToHandle != null) {
                long positionToUpdate =  dataToHandle.getPacketNumber();
                int packetIndex = dataToHandle.getPacketIndex();
                byte[] dataToWrite = dataToHandle.getPacket();

                writePacket(dataToWrite, positionToUpdate);
                updateMetaData(packetIndex);

                int downloadCounterStatus = metaData.GetDownloadCounter();
                System.err.println(downloadCounterStatus);

                isFinnishDownload = checkDownLoadStatus();
            }
        }
    }

    private boolean checkDownLoadStatus() {
        return metaData.IsDownloadFinished();
    }

    private void writePacket(byte[] dataToWrite, long positionToUpdate) {
        try(RandomAccessFile randomAccessFile = new RandomAccessFile(downloadedFilePath, "rw")) {

            randomAccessFile.seek(positionToUpdate);
            randomAccessFile.write(dataToWrite);
        } catch (IOException e) {
            e.printStackTrace();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createDownloadFile() {
        File myFile = new File(downloadedFilePath);
        boolean isDownloadFileExist = myFile.exists();

        if(!isDownloadFileExist){
            try {
                myFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateMetaData(int positionToUpdate) {
        metaData.UpdateIndex(positionToUpdate);
    }
}
