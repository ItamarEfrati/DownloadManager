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
    private static final int BUFFER_SIZE = 8092 * 64;  // Each download packet size
    private final String serializationPath = "MetaData.ser";  // Path to save MetaDataFile
    private List<URL> urlsList;
    private LinkedBlockingDeque<DataWrapper> packetDataQueue;
    private PacketWrite packetWrite;
    private ExecutorService packetDownloaderPool;
    private MetaData metaData;
    long fileSize;
    static List<long[]> packetPositionsPairs;
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
        Thread writerThread = this.initPacketWriteThread();

        packetPositionsPairs = this.getPacketsRanges();
        int urlIndex = 0;
        int packetIndex = 0;
        for (long[] packetPositions : packetPositionsPairs) {
            boolean isPacketDownloaded = metaData.IsIndexDownloaded(packetIndex);

            if (!isPacketDownloaded) {
                URL url = this.urlsList.get(urlIndex);
                Long packetStartPosition = packetPositions[0];
                Long packetEndPosition = packetPositions[1];
                PacketDownloader packetDownloader = new PacketDownloader(this.packetDataQueue, url,
                        packetStartPosition, packetEndPosition, fileSize, packetIndex);

                this.packetDownloaderPool.execute(packetDownloader);
                urlIndex = this.getNextUrlIndex(urlIndex);
            }

            packetIndex++;
        }

        this.packetDownloaderPool.shutdown();
        try {
            writerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // TODO: consider a better way to bring this data
    public static long GetIndexStartPosition(int packetIndex) throws Exception {
        if (packetPositionsPairs != null) {
            return packetPositionsPairs.get(packetIndex)[0];
        } else {
            throw new Exception("Object referenced before assignment, Please run DownloadManager first");
        }
    }
    // endregion

    // region Private methods
    private Thread initPacketWriteThread() {
        packetWrite = new PacketWrite(packetDataQueue, metaData, "tempName"); // TODO: change to the right name
        Thread packetWriteThread = new Thread(packetWrite);
        packetWriteThread.start();
        return packetWriteThread;
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
        int rangesAmount = (int) (fileSize / BUFFER_SIZE);
        long lastIndexReminder = fileSize % (long) BUFFER_SIZE;
        boolean isReminderExists = lastIndexReminder != 0;

        if (isReminderExists) {
            rangesAmount++;
        }

        return rangesAmount;
    }

    private List<long[]> getPacketsRanges() {
        List<long[]> packetRanges = new ArrayList<>();

        for (int i = 0; i < getRangesAmount(); i++) {
            packetRanges.add(get_byte_range(i));
        }

        return packetRanges;
    }

    private int getNextUrlIndex(int currentIndex) {
        return currentIndex < this.urlsList.size() - 1 ? ++currentIndex : 0;
    }

    private long[] get_byte_range(long packetStartPosition) {
        long packetStartByte = packetStartPosition * BUFFER_SIZE;
        long packetEndByte = packetStartByte + BUFFER_SIZE - 1;
        boolean isRangeValid = packetEndByte < this.fileSize;
        packetEndByte = isRangeValid ? packetEndByte : this.fileSize;

        return new long[]{packetStartByte, packetEndByte};
    }
    //endregion
}


