import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class IdcDm {
    public static List<URL> parseArguments(String urlArgument) {
        List<URL> urlsList = new ArrayList<>();
        boolean isUrlList = !urlArgument.startsWith("http://");

        try {
            if (isUrlList) {
                Scanner scanner = new Scanner(new File(urlArgument));
                scanner.useDelimiter(System.lineSeparator());
                while (scanner.hasNext()) {
                    String url = scanner.next();
                    urlsList.add(new URL(url));
                }
            } else {
                urlsList.add(new URL(urlArgument));
            }
        } catch (MalformedURLException e) {
            System.err.println("Fail to execute program, invalid url");
        } catch (FileNotFoundException e) {
            System.err.println("Fail to execute program, can't find urls list file");
        }

        return urlsList;
    }

    public static void main(String[] args) {
        int numberOfThreads = 0;
        List<URL> urlsList = null;
        boolean isNumOfThreadProvided = args.length == 2;

        try {
            numberOfThreads = isNumOfThreadProvided ? Integer.parseInt(args[1]) : 1;
        } catch (NumberFormatException e) {
            System.err.println("Fail to execute program, invalid number of threads");
        }

        boolean isThreadsArgumentValid = numberOfThreads > 0;

        if(isThreadsArgumentValid) {
            urlsList = parseArguments(args[0]);
        }

        boolean isUrlArgumentValid = urlsList != null && urlsList.size() > 0;

        if(isUrlArgumentValid) {
            DownloadManager downloadManager = new DownloadManager(urlsList, numberOfThreads);
            downloadManager.run();
        }
    }
}
