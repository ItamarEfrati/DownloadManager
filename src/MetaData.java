import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

public class MetaData implements Serializable {
    //region Fields
    private String tempSerializationPath;
    private String serializationPath;
    private Boolean[] rangesStatus;
    private int downloadCounter = 0;
    //endregion Fields

    private MetaData(int rangesAmount, String serializationPath){
        rangesStatus = initRangesStatus(rangesAmount);
        this.serializationPath = serializationPath;
        tempSerializationPath = "temp-" + this.serializationPath;
    }

    private Boolean[] initRangesStatus(int rangesAmount) {
        Boolean[] bs = new Boolean[rangesAmount];

        Arrays.fill(bs, false);

        return bs;
    }

    public static MetaData GetMetaData(int rangesAmount, String serializationPath){
        MetaData metaData = null;
        boolean isDownloadResumed = false; // TODO: check the real value

        if(isDownloadResumed){
            metaData = ReadFromDisk(serializationPath);
        }else{
            metaData =  new MetaData(rangesAmount, serializationPath);
        }

        return metaData;
    }

    public void UpdateIndex(int indexToUpdate){
        rangesStatus[indexToUpdate] = true;
        writeToDisk();
    }

    public boolean IsIndexDownloaded(int indexToCheck){
        return rangesStatus[indexToCheck];
    }

    public boolean IsDownloadFinished() {
        return !Arrays.asList(rangesStatus).contains(false);
    }

    //region Serialization
    private void writeToDisk(){
        try(FileOutputStream fileOutputStream = new FileOutputStream(tempSerializationPath);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)){
            objectOutputStream.writeObject(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        boolean isRenamed = renameFile();
        if(!isRenamed){
            System.err.println("Problem in renaming metadata!");
        }else{
            downloadCounter++;
        }
    }

    private static MetaData ReadFromDisk(String serializationPath){
        MetaData metaData = null;
        try(FileInputStream fileInputStream = new FileInputStream(serializationPath);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)){
            metaData = (MetaData) objectInputStream.readObject();
        } catch (FileNotFoundException | ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return metaData;
    }
    //endregion Serialization

    private boolean renameFile() {
        File tmp = new File(tempSerializationPath);
        Path tmpPath = Paths.get(tmp.getAbsolutePath());
        boolean isRenamed = true;

        try {
            Files.move(tmpPath, tmpPath.resolveSibling(serializationPath), StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            isRenamed = false;
        }

        return isRenamed;
    }

    public int GetDownloadCounter(){
        return downloadCounter;
    }
}
