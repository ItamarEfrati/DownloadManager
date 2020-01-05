import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args){
        // TODO: read the arguments and call for download manager
        String urlPath = args[0];
        int numOfThreads = 1;

        boolean isNumOfThreadProvided = args.length == 2;
        if(isNumOfThreadProvided){
            numOfThreads = Integer.parseInt(args[1]);
        }

        boolean isUrlList = !urlPath.startsWith("http://");
        List<String> urlList = new ArrayList<>();
        if(isUrlList){
            try {
                urlList = readUrlListFile(urlPath);
            }catch (IOException ex){
                System.err.println(ex.getMessage());
            }
        }else{
            urlList.add(urlPath);
        }

        DownloadManager downloadManager = new DownloadManager(urlList, numOfThreads);
        downloadManager.run();
    }

    private static List<String> readUrlListFile(String filePath) throws IOException {
        List<String> urls = new ArrayList<>();
        Scanner scanner = new Scanner(new File(filePath));
        scanner.useDelimiter(System.lineSeparator());
        while(scanner.hasNext()){
            String url = scanner.next();

            urls.add(url);
        }

        return urls;
    }
}
