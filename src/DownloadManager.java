import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.net.URL;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class DownloadManager {

    List<URL> serverList = new ArrayList<>();
    int numOfWorkers;
    BlockingQueue<DataWrapper> packetDataQueue;

    public DownloadManager(List<String> urlList, int numOfThreads) throws MalformedURLException {
        initServerList(urlList);
        numOfWorkers = numOfThreads;
        packetDataQueue = new ArrayBlockingQueue<>(numOfWorkers);
    }

    private void initServerList(List<String> urlList) throws MalformedURLException {
        for(String url : urlList){
            URL server = new URL(url);

            serverList.add(server);
        }
    }

    public void run() {

    }
}
