package de.ac;

import java.io.*;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;

public class Filemanager {
    private File file = null;
    private InputStream inputStream = null;
    private final List<String> fileManager = new ArrayList<>();

    public Filemanager() {}

    public Filemanager(String path) throws NoSuchFileException {
        setSource(path);
    }

    public Filemanager(File file) throws NoSuchFileException {
        setSource(file);
    }

    public Filemanager(InputStream inputStream) {
        setSource(inputStream);
    }

    public void setSource(String path) throws NoSuchFileException {
        setSource(new File(path));
    }

    public void setSource(File file) throws NoSuchFileException {
        if (!file.exists()) {
            throw new NoSuchFileException(file.getPath(), null, String.format("[Filemanager]: File \"%s\" not found!\n", file.getName()));
        }
        this.file = file;
        this.inputStream = null;
        clearCache();
        copyFileContentFromFile();
    }

    public void setSource(InputStream inputStream) {
        this.inputStream = inputStream;
        this.file = null;
        clearCache();
        copyFileContentFromStream();
    }

    public String getPath() {
        return file != null ? file.getPath() : "[InputStream source]";
    }

    public String getAbsolutePath() {
        return file != null ? file.getAbsolutePath() : "[InputStream source]";
    }

    public File getFile() {
        return file;
    }

    public boolean isFolder() {
        return file != null && file.isDirectory();
    }

    public String[] getContent() {
        String[] content = new String[fileManager.size()];
        fileManager.toArray(content);
        return content;
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

    public void clearCache() {
        fileManager.clear();
    }

    public String get(int index) {
        return fileManager.get(index);
    }

    private void copyFileContentFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(getFile()))) {
            if (!isFolder()) {
                reader.lines().forEach(fileManager::add);
            }
        } catch (FileNotFoundException e) {
            System.err.printf("[Filemanager]: Failed to copy file content because file at \"%s\" not found!\n", getPath());
        } catch (IOException e) {
            System.err.println("[Filemanager]: Failed to read file content.");
        }
    }

    private void copyFileContentFromStream() {
        if (inputStream == null) {
            System.err.println("[Filemanager]: InputStream is null.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            reader.lines().forEach(fileManager::add);
        } catch (IOException e) {
            System.err.println("[Filemanager]: Failed to read content from InputStream.");
        }
    }

    public void remove(int index) {
        fileManager.remove(index);
    }

    public boolean update() {
        if (file == null) {
            System.err.println("[Filemanager]: Cannot update. No file source set.");
            return false;
        }

        try {
            trimList();
            clearFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(getFile()))) {
                for (String s : fileManager) {
                    writer.write(s);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            System.err.printf("[Filemanager]: Failed to write file \"%s\". File is a directory or cannot be opened. Maybe permission issue? Exception: %s", getFile().getName(), e);
            return false;
        }
        return true;
    }

    private void clearFile() {
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(getFile(), "rw")) {
            randomAccessFile.setLength(0);
        } catch (IOException e) {
            System.err.println("[Filemanager]: Failed to clear file: " + e);
        }
    }

    private void trimList() {
        for (int i = lines() - 1; i > 0; i--) {
            if (get(i).isEmpty()) remove(i);
            else return;
        }
    }
}
