package data.filemanager;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FilemanagerV2 {
    private File file = null;
    private final List<String> fileManager = new ArrayList<>();

    public FilemanagerV2() {}
    public FilemanagerV2(String path) {
        setPath(path);
    }

    public void setPath(String path) {
        try {
            this.file = new File(path);
            clear();
            copyFileContent();
        } catch (NullPointerException e) {
            System.err.printf("[Filemanager]: Path \"%s\" not found!\n", path);
        }
    }

    public String getPath() {
        return file.getPath();
    }

    public String getAbsolutePath() {
        return file.getPath();
    }

    public File getFile() {
        return file;
    }

    public boolean isFolder() {
        return file.isDirectory();
    }

    public String[] getContent() {
        return fileManager.toArray(new String[0]);
    }

    public int lines() {
        return fileManager.size();
    }

    public void add(String string) {
        fileManager.add(string);
    }

    public void add(int index, String string) {
        fileManager.add(index, string);
    }

    public void set(int index, String string) {
        if (index >= fileManager.size()) {
            for (int i = lines(); i < index; i++) {
                fileManager.add("");
            }
            fileManager.add(string);
        } else {
            fileManager.set(index, string);
        }
    }

    public void clear() {
        fileManager.clear();
    }

    public String get(int index) {
        return fileManager.get(index);
    }

    private void copyFileContent() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(getFile()));
            if (!isFolder())
                reader.lines().forEach(fileManager::add);
            reader.close();
        } catch (FileNotFoundException e) {
            System.err.printf("[Filemanager]: File at \"%s\" not found!\n", getPath());
        } catch (IOException e) {
            System.err.println("[Filemanager]: Failed to close Buffered Reader.");
        }
    }

    public void remove(int index) {
        fileManager.remove(index);
    }

    public boolean update() {
        try {
            trimList();
            clearFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(getFile()));
            for (String s : fileManager) {
                writer.write(s);
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            System.err.printf("[Filemanager]: Failed to write file \"%s\". File is a directory or cannot be opened. Maybe permission issue? Exception: %s", getFile().getName(), e);
            return false;
        }
        return true;
    }

    private void clearFile() {
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(getFile(), "rw");
            randomAccessFile.setLength(0);
        } catch (IOException e) {
            System.err.println("[Filemanager]: Failed to clear file: " + e);
        }
    }

    private void trimList() {
        for (int i = lines()-1; i>0; i--) {
            if (get(i).isEmpty()) remove(i);
            else return;
        }
    }
}
