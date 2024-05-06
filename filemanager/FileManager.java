package data.filemanager;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileManager {
    private String path = null;
    private List<String> fileManager = new ArrayList<>();

    public void setupPath(String path) throws IncorrectPathException {
        this.path = path;
        fileManager.clear();
        if (path!=null) {
            try {
                File file = new File(path);
                file.setWritable(false);
                file.setReadable(false);
                FileReader fileReader = new FileReader(file);
                BufferedReader br = new BufferedReader(fileReader);
                br.lines().forEach(fileManager::add);
                fileReader.close();
                br.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new IncorrectPathException("[FileManager Error]: File path not set or wrong. Please check your path and use the `setupPath`-Method.");
        }
    }
    public void addText(String text) {
        fileManager.add(text);
    }
    public void update() throws IncorrectPathException {
        if (path != null) {
            File file = new File(path);
            file.setWritable(true);
            file.setReadable(true);
            try {
                FileWriter fileWriter = new FileWriter(file);
                BufferedWriter bw = new BufferedWriter(fileWriter);
                FileReader fileReader = new FileReader(file);
                fileManager.forEach(f -> {
                    try {
                        bw.write(f + System.lineSeparator());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                bw.flush();
                file.setWritable(false);
                file.setReadable(false);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new IncorrectPathException("[FileManager Error]: File path not set or wrong. Please check your path and use the `setupPath`-Method.");
        }
    }
    public void set(int index, String text) {
        try {
            fileManager.set(index, text);
        } catch (IndexOutOfBoundsException e) {
            for (int i = 0; i<index+1; i++) {
                fileManager.add("");
            }
            fileManager.set(index, text);
        }
    }
    public int length() {
        return fileManager.size();
    }
    public void clear() {
        fileManager.clear();
    }
    public void delete(int index) {
        fileManager.remove(index);
    }
    public String[] getContent() {
        int index = 0;
        String[] content = new String[fileManager.size()];
        for (String f : fileManager) {
            content[index] = f;
            index++;
        }
        return content;
    }
    public String get(int index) {
        return fileManager.get(index);
    }
    public int getIndexByElementName(String object) {
        return fileManager.indexOf(object);
    }
    public void remove(String object) {
        fileManager.remove(object);
    }
    public File getFile() throws IncorrectPathException {
        if (path!= null) return new File(path); else throw new IncorrectPathException("[FileManager Error]: File path not set or wrong. Please check your path and use the `setupPath`-Method.");
    }
    public List<String> getContentAsList() {
        return fileManager;
    }
    public File createFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return file;
    }
    public File createFolder(String path) {
        File file = new File(path);
        if (!file.exists()) {
            file.mkdir();
        }
        return file;
    }
}
