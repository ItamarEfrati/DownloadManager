import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class MetaData implements Serializable {
    //region Fields
    private String serializationPath;
    private boolean[] rangesStatus;
    //endregion Fields

    public MetaData(int rangesAmount, String serializationPath){
        rangesStatus = new boolean[rangesAmount];
        this.serializationPath = serializationPath;
    }

    public void UpdateIndex(int indexToUpdate){
        rangesStatus[indexToUpdate] = true;
        writeToDisk();
    }

    public boolean IsIndexDownloaded(int indexToCheck){
        return rangesStatus[indexToCheck];
    }

    private void writeToDisk(){
        try(FileOutputStream fileOutputStream = new FileOutputStream("temp-" + serializationPath);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)){
            objectOutputStream.writeObject(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        boolean isRenamed = renameFile();
        if(!isRenamed){
            System.err.println("Problem in renaming metadata!");
        }
    }

    private boolean renameFile() {
        File tmp = new File("temp-" + serializationPath);
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

    public static MetaData ReadFromDisk(String serializationPath){
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
}
