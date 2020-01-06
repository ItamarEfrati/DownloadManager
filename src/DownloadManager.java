import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.net.URL;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadManager {

    //region Fields
    private List<URL> urlsList;
    private ArrayBlockingQueue<DataWrapper> packetDataQueue;
    private PacketWrite packetWrite;
    private ExecutorService packetDownloaderPool;
    // endregion

    //region Constructor
    public DownloadManager(List<URL> urlList, int numberOfThreads) {
        this.urlsList = urlList;
        // TODO: change the type of the queue
        this.packetDataQueue = new ArrayBlockingQueue<>(3);
        this.packetWrite = new PacketWrite();
        this.packetDownloaderPool = Executors.newFixedThreadPool(numberOfThreads);
    }
    //endregion

    //region Public methods
    public void run() {
        long fileSize = this.getFileSize();
        if (fileSize == -1) {
            System.err.println("Download fail, fail to establish connection with server to get file size");
        }
        List<Long> packetPositionsList = this.getPacketsRanges();
        this.packetWrite.run();
        int urlIndex = 0;
        for (long packetPosition : packetPositionsList) {
            URL url = this.urlsList.get(urlIndex);
            PacketDownloader packetDownloader = new PacketDownloader(this.packetDataQueue, url, packetPosition, fileSize);
            this.packetDownloaderPool.execute(packetDownloader);
            urlIndex = this.getNextUrlIndex(urlIndex);
        }
    }

    // endregion

    // region Private methods
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

    private List<Long> getPacketsRanges() {
        return new ArrayList<Long>();
    }

    private int getNextUrlIndex(int currentIndex) {
        return currentIndex < this.urlsList.size() - 1 ? ++currentIndex : 0;
    }
    //endregion
}


