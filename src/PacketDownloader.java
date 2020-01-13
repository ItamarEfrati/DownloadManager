import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.concurrent.LinkedBlockingDeque;

public class PacketDownloader implements Runnable {
    private final int packetIndex;
    //region Fields
    private LinkedBlockingDeque<DataWrapper> packetQueue;
    private URL source;
    private long packetStartPosition;
    private long packetEndPosition;
    private long fileSize;
    //endregion

    //region Constructor

    PacketDownloader(LinkedBlockingDeque<DataWrapper> packetQueue, URL source,
                     long packetStartPosition, long packetEndPosition, long fileSize, int packetIndex) {
        this.packetQueue = packetQueue;
        this.source = source;
        this.packetStartPosition = packetStartPosition;
        this.packetEndPosition = packetEndPosition;
        this.fileSize = fileSize;
        this.packetIndex = packetIndex;
    }
    //endregion

    //region Public Methods
    @Override
    public void run() {
        this.handlePacket();
    }

    //endregion

    //region Private Methods

    /**
     * This function handles the execution of the http request to the server and download the relevant packet
     */
    private void handlePacket() {
        InputStream inputStream = this.executeContentRangeRequest();
        if (inputStream != null) {
            this.downloadPacket(inputStream);
        }
    }

    /**
     * This functoin execute a GET request to the source url with the Content-Range header to download a specific packet
     * @return InputStream, an open inputStream to the source url
     */
    private InputStream executeContentRangeRequest() {
        InputStream inputStream = null;
        try {
            String range = this.get_byte_range();
            HttpURLConnection httpConnection = (HttpURLConnection) source.openConnection();
            try {
                httpConnection.setRequestMethod("GET");
            } catch (ProtocolException e) {
                System.err.printf("Fail to execute http request to %s, wrong request method\n", this.source.toString());
            }
            httpConnection.setRequestProperty("Range", range);
            int responseCode = httpConnection.getResponseCode();
            inputStream = responseCode == HttpURLConnection.HTTP_PARTIAL ? httpConnection.getInputStream() : null;

        } catch (IOException e) {
            System.err.printf("Fail to execute http request to %s\n", this.source.toString());
        }

        return inputStream;
    }

    /**
     * This function download the bytes of the packet from the url, create a data wrapper and insert the data wrapper
     * to the packets queue
     * @param inputStream, and open input stream to the url
     */
    private void downloadPacket(InputStream inputStream) {
        try {
            byte[] buffer = inputStream.readAllBytes();
            DataWrapper dataWrapper = new DataWrapper(packetIndex, packetStartPosition, buffer);
            this.packetQueue.add(dataWrapper);
        } catch (IOException e) {
            System.err.printf("Fail to download packet %d from %s\n", this.packetStartPosition, this.source.toString());
        }
    }

    private String get_byte_range(){
        return String.format("Bytes=%d-%d", packetStartPosition, packetEndPosition);
    }
    //endregion
}

