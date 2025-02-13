package de.ac;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * The {@code Filemanager} class provides functionality to manage file contents,
 * allowing reading, writing, and modifying text files.
 * It supports both file-based and InputStream-based sources.
 */
public class Filemanager {

    private File file = null;
    private InputStream inputStream = null;

    /** 
     * Stores the file content as a list of strings.
     * Modified content is kept here until explicitly written to the file with {@link #update()}.
     */
    private final List<String> fileManager = new ArrayList<>();

    /**
     * Creates an empty Filemanager instance.
     * Source can be manually set with {@code setSource(Args)}
     */
    public Filemanager() {}

    /**
     * Creates a Filemanager instance and sets the source file.
     *
     * @param path The path of the file to manage.
     * @throws IOException If the file doesn't exist, cannot be read or something like that.
     */
    public Filemanager(@NotNull final String path) throws IOException {
        setSource(path);
    }

    /**
     * Creates a Filemanager instance with a given file.
     *
     * @param file The file to manage.
     * @throws NoSuchFileException If the file does not exist.
     * @throws IOException If the file cannot be read or something like that.
     */
    public Filemanager(@NotNull final File file) throws IOException {
        setSource(file);
    }

    /**
     * Creates a Filemanager instance with an InputStream as a source.
     *
     * @param inputStream The InputStream to read from.
     */
    public Filemanager(@NotNull final InputStream inputStream) {
        setSource(inputStream);
    }

    /**
     * Sets the file source for this Filemanager.
     *
     * @param path The path of the file.
     * @throws NoSuchFileException If the file does not exist.
     * @throws IOException If the file cannot be read or something like that.
     */
    public void setSource(@NotNull final String path) throws IOException {
        setSource(new File(path));
    }

    /**
     * Sets the file source for this Filemanager.
     *
     * @param file The file to set as source.
     * @throws NoSuchFileException If the file does not exist.
     * @throws IOException If the file cannot be read or something like that.
     */
    public void setSource(@NotNull final File file) throws IOException {
        if (!file.exists()) {
            throw new NoSuchFileException(file.getPath(), null,
                    String.format("File \"%s\" not found!\n", file.getName()));
        }
        this.file = file;
        this.inputStream = null;
        clearCache();
        copyFileContentFromFile();
    }

    /**
     * Sets an InputStream as the source for this Filemanager.
     *
     * @param inputStream The InputStream to read from.
     */
    public void setSource(@NotNull final InputStream inputStream) {
        this.inputStream = inputStream;
        this.file = null;
        clearCache();
        copyFileContentFromStream();
    }

    /**
     * Returns the file path or a placeholder if using an InputStream.
     *
     * @return The file path or "[InputStream source]".
     */
    public String getPath() {
        return file != null ? file.getPath() : "[InputStream source]";
    }

    /**
     * Returns the absolute path of the file.
     *
     * @return The absolute file path or "[InputStream source]".
     */
    public String getAbsolutePath() {
        return file != null ? file.getAbsolutePath() : "[InputStream source]";
    }

    /**
     * Returns the managed file.
     *
     * @return The file or null if using an InputStream.
     */
    @Nullable
    public File getFile() {
        return file;
    }

    /**
     * Checks if the source is a directory.
     *
     * @return {@code true} if the file is a directory, {@code false} otherwise.
     */
    public boolean isFolder() {
        return file != null && file.isDirectory();
    }

    /**
     * Returns the file content as array of strings.
     *
     * @return The content of the file.
     */
    public String[] getContent() {
        String[] content = new String[fileManager.size()];
        fileManager.toArray(content);
        return content;
    }

    /**
     * Returns the file content as stream.
     *
     * @return The content of the file as Stream.
     */
    public Stream<String> getContentStream() {
        return fileManager.stream();
    }

    /**
     * Returns the number of lines in the file.
     *
     * @return The line count.
     */
    public int lines() {
        return fileManager.size();
    }

    /**
     * Adds a line to the file content.
     *
     * @param string The line to add.
     */
    public void add(final String string) {
        fileManager.add(string);
    }

    /**
     * Adds a specified element at the specified position in this list.
     * Uses {@link java.util.List#add(int, Object)}
     *
     * @param index  The index at which the line is to be inserted.
     * @param string The line to add.
     */
    public void add(final int index, final String string) {
        fileManager.add(index, string);
    }

    /**
     * Adds a line to the file content at a specific index.
     *
     * @param index  The index you want to modify or add.
     * @param string The content at the specific index you want to set.
     */
    public void set(final int index, final String string) {
        if (index >= fileManager.size()) {
            for (int i = lines(); i < index; i++) {
                fileManager.add("");
            }
            fileManager.add(string);
        } else {
            fileManager.set(index, string);
        }
    }

    /**
     * Clears the internal file content cache.
     */
    public void clearCache() {
        fileManager.clear();
    }

    /**
     * Returns the content at the specified index.
     *
     * @param index The index of the content to return.
     * @return The content at the specific index.
     * @throws IndexOutOfBoundsException if the index is out of range.
     */
    public String get(final int index) {
        return fileManager.get(index);
    }

    private void copyFileContentFromFile() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(getFile()))) {
            if (!isFolder()) {
                reader.lines().forEach(fileManager::add);
            }
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException(
                    String.format("Failed to copy file content because file at \"%s\" not found!", getPath()));
        } catch (IOException e) {
            throw new IOException(
                    String.format("Failed to read file content of file at \"%s\"! Maybe permission issue?", getPath()), e);
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

    /**
     * Removes a line at the specified index.
     *
     * @param index The index of the line to remove.
     */
    public void remove(final int index) {
        fileManager.remove(index);
    }

    /**
     * Updates the file with the current content stored in memory.
     * If you don't use this after changes, the content you modify will never get written to your specific file.
     *
     * @return {@code true} if the update was successful, {@code false} otherwise.
     */
    public boolean update() {
        return update(true);
    }

    /**
     * Updates the file with the current content stored in memory.
     * If you don't use this after changes, the content you modify will never get written to your specific file.
     *
     * @param trimList If you want to trim your list before updating. See {@link #trimList()}
     * @return {@code true} if the update was successful, {@code false} otherwise.
     */
    public boolean update(final boolean trimList) {
        if (file == null) {
            System.err.println("[Filemanager]: Cannot update. No file source set.");
            return false;
        }
        try {
            if (trimList) trimList();
            clearFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(getFile()))) {
                for (String s : fileManager) {
                    writer.write(s);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            System.err.printf("[Filemanager]: Failed to write file \"%s\". File is a directory or cannot be opened. Maybe permission issue? Exception: %s",
                    getFile().getName(), e);
            return false;
        }
        return true;
    }

    /**
     * Clears the entire contents of the file.
     */
    public void clearFile() {
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(getFile(), "rw")) {
            randomAccessFile.setLength(0);
        } catch (IOException e) {
            System.err.println("[Filemanager]: Failed to clear file: " + e);
        }
    }

    /**
     * Removes empty lines.
     */
    public void trimList() {
        for (int i = lines() - 1; i > 0; i--) {
            if (get(i).isEmpty()) {
                remove(i);
            } else {
                return;
            }
        }
    }

    /**
     * Check if the source, the Filemanager is using, is a file.
     *
     * @return {@code true} if a file is used as source, {@code false} if an InputStream is used or no source is set.
     */
    public boolean isSourceFile() {
        return file != null;
    }

    /**
     * Gets the source the Filemanager is using.
     *
     * @return {@code "Null"} if no source is set, {@code "File"} if a file is used as source, {@code "InputStream"} if an InputStream is used as source.
     */
    public String getSource() {
        return file == null && inputStream == null ? "Null"
                : isSourceFile() ? "File"
                : "InputStream";
    }
}
