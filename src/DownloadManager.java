import javafx.util.Pair;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

public class DownloadManager {
    //region Fields
    private static final int BUFFER_SIZE = 8092 * 1024;  // Each download packet size
    private final String serializationPath = "MetaData.ser";  // Path to save MetaDataFile
    private List<URL> urlsList;
    private LinkedBlockingDeque<DataWrapper> packetDataQueue;
    private PacketWrite packetWrite;
    private ExecutorService packetDownloaderPool;
    private MetaData metaData;
    long fileSize;
    static List<Pair<Long, Long>> packetPositionsPairs;
    // endregion

    private static DownloadManager downloadManager;

    //region Constructor
    public DownloadManager(List<URL> urlList, int numberOfThreads) {
        this.urlsList = urlList;
        this.packetDataQueue = new LinkedBlockingDeque<>();
        this.packetDownloaderPool = Executors.newFixedThreadPool(numberOfThreads);
    }
    //endregion

    //region Public methods
    public void run() {
        this.fileSize = this.getFileSize();

        if (fileSize == -1) {
            System.err.println("Download fail, fail to establish connection with server to get file size");
            return;
        }

        this.initMetaData();
        this.initPacketWriteThread();

        packetPositionsPairs = this.getPacketsRanges();
        int urlIndex = 0;
        int packetIndex = 0;
        for (Pair<Long, Long> packetPositions : packetPositionsPairs) {
            boolean isPacketDownloaded = metaData.IsIndexDownloaded(packetIndex);

            if(!isPacketDownloaded) {
                URL url = this.urlsList.get(urlIndex);
                Long packetStartPosition = packetPositions.getKey();
                Long packetEndPosition = packetPositions.getValue();
                PacketDownloader packetDownloader = new PacketDownloader(this.packetDataQueue, url,
                        packetStartPosition, packetEndPosition, fileSize, packetIndex);

                this.packetDownloaderPool.execute(packetDownloader);
                urlIndex = this.getNextUrlIndex(urlIndex);
            }

            packetIndex++;
        }
    }

    // TODO: consider a better way to bring this data
    public static long GetIndexStartPosition(int packetIndex) throws Exception {
        if(packetPositionsPairs != null) {
            return packetPositionsPairs.get(packetIndex).getKey();
        }else {
            throw new Exception("Object referenced before assignment, Please run DownloadManager first");
        }
    }
    // endregion

    // region Private methods
    private void initPacketWriteThread() {
        packetWrite = new PacketWrite(packetDataQueue, metaData, "tempName"); // TODO: change to the right name
        Thread packetWriteThread = new Thread(packetWrite);
        packetWriteThread.start();
    }

    private void initMetaData() {
        metaData = MetaData.GetMetaData(getRangesAmount(), serializationPath);
    }

    private long getFileSize() {
        long fileSize = -1;
        for (URL url : this.urlsList) {
            HttpURLConnection httpConnection = null;
            try {
                httpConnection = (HttpURLConnection) url.openConnection();
                httpConnection.setRequestMethod("HEAD");
                fileSize = httpConnection.getContentLengthLong();
                break;
            } catch (IOException ignored) {
            }
        }
        return fileSize;
    }

    private int getRangesAmount() {
        int rangesAmount =  (int) (fileSize / BUFFER_SIZE);
        long lastIndexReminder = fileSize % (long) BUFFER_SIZE;
        boolean isReminderExists = lastIndexReminder != 0;

        if(isReminderExists){
            rangesAmount++;
        }

        return rangesAmount;
    }

    private List<Pair<Long, Long>> getPacketsRanges() {
        List<Pair<Long, Long>> packetRanges = new ArrayList<>();

        for(int i =0; i < getRangesAmount(); i++){
            packetRanges.add(get_byte_range(i));
        }

        return packetRanges;
    }

    private int getNextUrlIndex(int currentIndex) {
        return currentIndex < this.urlsList.size() - 1 ? ++currentIndex : 0;
    }

    private Pair<Long, Long> get_byte_range(long packetStartPosition) {
        long packetStartByte = packetStartPosition * BUFFER_SIZE;
        long packetEndByte = packetStartByte + BUFFER_SIZE - 1;
        boolean isRangeValid = packetEndByte < this.fileSize;
        packetEndByte = isRangeValid ? packetEndByte : this.fileSize;

        return new Pair<Long, Long>(packetStartByte, packetEndByte);
    }
    //endregion
}


