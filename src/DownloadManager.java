import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

public class DownloadManager {
    //region Fields
    private static final int BUFFER_SIZE = 512 * 1000;  // Each download packet size
    private static final String SERIALIZATION_PATH = "MetaData.ser";  // Path to save MetaDataFile
    private List<URL> urlsList;
    private LinkedBlockingDeque<DataWrapper> packetDataQueue;
    private ExecutorService packetDownloaderPool;
    private MetaData metaData;
    private long fileSize;
    private static List<long[]> packetPositionsPairs;
    private int urlIndex;
    // endregion

    //region Constructor
    public DownloadManager(List<URL> urlList, int numberOfThreads) {
        this.urlsList = urlList;
        this.packetDataQueue = new LinkedBlockingDeque<>();
        this.packetDownloaderPool = Executors.newFixedThreadPool(numberOfThreads);
        this.urlIndex = 0;
    }
    //endregion

    //region Public methods

    /**
     * Initiate a download process of a single file which includes accumulating download tasks to a packet downloaders
     * pool and running a packet writer thread that write the downloaded packets to the destination file.
     */
    public void run() {
        this.fileSize = this.getFileSize();
        boolean isConnectionEstablish = fileSize != -1;

        if (!isConnectionEstablish) {
            System.err.println("Download fail, fail to establish connection with server to get file size");
            return;
        }

        String url = this.urlsList.get(0).toString();
        String destinationFilePath = url.substring( url.lastIndexOf('/')+1);

        this.initMetaData(destinationFilePath);
        packetPositionsPairs = this.getPacketsRanges();

        Thread writerThread;
        try {
            writerThread = this.initPacketWriteThread(destinationFilePath);
        } catch (IOException e) {
            return;
        }
        accumulatePackets();
        this.packetDownloaderPool.shutdown();
        try {
            packetDownloaderPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            insertPoisonPill();
            writerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    // endregion

    // region Private methods
    /**
     * Accumulates tasks for the thread pool of the packet downloaders. At the end create a poison pill task to inform
     * the writer that all task are done.
     */
    private void accumulatePackets() {
        int packetIndex = 0;
        for (long[] packetPositions : packetPositionsPairs) {
            boolean isPacketDownloaded = metaData.IsIndexDownloaded(packetIndex);
            if (!isPacketDownloaded) {
                createTask(packetIndex, packetPositions);
            }

            packetIndex++;
        }
    }

    /**
     * Create a new task to the packet downloaders pool.
     * @param packetIndex the index of the packet
     * @param packetPositions long array where at index 0 is the start byte of the packet and index 1 is the end byte of
     *                        the packet
     */
    private void createTask(int packetIndex, long[] packetPositions) {
        URL url = this.urlsList.get(urlIndex);
        long packetStartPosition = packetPositions[0];
        long packetEndPosition = packetPositions[1];
        PacketDownloader packetDownloader = new PacketDownloader(this.packetDataQueue, url,
                packetStartPosition, packetEndPosition, packetIndex);

        this.packetDownloaderPool.execute(packetDownloader);
        this.setNextUrlIndex();
    }

    /**
     * Creates a poison pill task and insert it to the thread pool
     */
    private void insertPoisonPill() {
        DataWrapper poisonPill = new DataWrapper(-1, -1, null);

        packetDataQueue.add(poisonPill);
    }

    /**
     * Initiate the packet writer thread
     * @return the thread object of the packet writer
     */
    private Thread initPacketWriteThread(String destinationFileName) throws IOException {
        PacketWriter packetWrite;

        try {
            packetWrite = new PacketWriter(packetDataQueue, metaData, destinationFileName);
        }
        catch (IOException e){
            System.err.println(e.getMessage());
            throw e;
        }

        Thread packetWriteThread = new Thread(packetWrite);

        packetWriteThread.start();
        return packetWriteThread;
    }

    /**
     * Initiate a meta data object.
     */
    private void initMetaData(String destinationFilePath) {
        destinationFilePath += SERIALIZATION_PATH;
        this.metaData = MetaData.GetMetaData(getRangesAmount(), destinationFilePath);
    }

    /**
     * Create a http get request to get the size in bytes of the requested download file.
     * @return the size of the file in bytes
     */
    private long getFileSize() {
        // TODO: why loop?
        long fileSize = -1;
        for (URL url : this.urlsList) {
            HttpURLConnection httpConnection;
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

    /**
     * Calculate the amount of packet that are needed in order to download the file
     * @return int, the amount of ranges
     */
    private int getRangesAmount() {
        int rangesAmount = (int) (fileSize / BUFFER_SIZE);
        long lastIndexReminder = fileSize % (long) BUFFER_SIZE;
        boolean isReminderExists = lastIndexReminder != 0;

        if (isReminderExists) {
            rangesAmount++;
        }

        return rangesAmount;
    }

    /**
     * Craete a list that contains all the ranges of the packets of the file
     * @return the list of the ranges
     */
    private List<long[]> getPacketsRanges() {
        List<long[]> packetRanges = new ArrayList<>();

        for (int i = 0; i < getRangesAmount(); i++) {
            packetRanges.add(get_byte_range(i));
        }

        return packetRanges;
    }

    /**
     * Sets the next index that will choose the next url to download a packet from
     */
    private void setNextUrlIndex() {
        this.urlIndex = this.urlIndex < this.urlsList.size() - 1 ? ++this.urlIndex : 0;
    }

    /**
     * Calculate the range (start byte and end byte) of a given packet
     * @param packetStartPosition the index of the packet
     * @return array where at index 0 is the starting byte range and in index 1 the end byte range
     */
    private long[] get_byte_range(long packetStartPosition) {
        long packetStartByte = packetStartPosition * BUFFER_SIZE;
        long packetEndByte = packetStartByte + BUFFER_SIZE - 1;
        boolean isRangeValid = packetEndByte < this.fileSize;
        packetEndByte = isRangeValid ? packetEndByte : this.fileSize;

        return new long[]{packetStartByte, packetEndByte};
    }
    //endregion
}


