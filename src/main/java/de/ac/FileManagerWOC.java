package de.ac;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * This class implements the {@link IFileManager} interface. It manages file operations while minimizing memory usage
 * by using streams, instead of loading the entire file content into memory. It is suitable for working with large files
 * or when memory resources are limited. (Slower than {@link FileManager})
 */
public class FileManagerWOC implements IFileManager {

    @Nullable
    private File fileSource;

    @Nullable
    private InputStream inputStreamSource;

    /**
     * Default constructor for the {@link FileManagerWOC} instance.
     */
    public FileManagerWOC() {
    }

    /**
     * Constructs a {@link FileManagerWOC} instance with a file path as the source.
     *
     * @param path the file path
     * @throws IOException if the file cannot be found or opened
     */
    public FileManagerWOC(@NotNull String path) throws IOException {
        setSource(path);
    }

    /**
     * Constructs a {@link FileManagerWOC} instance with a {@link File} as the source.
     *
     * @param file the file
     * @throws IOException if the file cannot be found or opened
     */
    public FileManagerWOC(@NotNull File file) throws IOException {
        setSource(file);
    }

    /**
     * Constructs a {@link FileManagerWOC} instance with an {@link InputStream} as the source.
     *
     * @param stream the input stream
     * @throws IOException if the stream cannot be opened
     */
    public FileManagerWOC(@NotNull InputStream stream) throws IOException {
        setSource(stream);
    }

    /**
     * Sets the file source using the provided file path.
     *
     * @param path the file path
     * @throws IOException if the file cannot be found or opened
     */
    @Override
    public void setSource(@NotNull String path) throws IOException {
        setSource(new File(path));
    }

    /**
     * Sets the file source using the provided file.
     *
     * @param file the file
     * @throws IOException if the file cannot be found or opened
     */
    @Override
    public void setSource(@NotNull File file) throws IOException {
        if (!file.exists()) throw new FileNotFoundException();

        this.fileSource = file;
    }

    /**
     * Sets the input stream source.
     *
     * @param inputStream the input stream
     * @throws IOException if the input stream cannot be opened
     */
    @Override
    public void setSource(@NotNull InputStream inputStream) throws IOException {
        this.inputStreamSource = inputStream;
    }

    /**
     * Returns the path of the file source.
     *
     * @return the file path, or {@code null} if no file is set
     */
    @Override
    public @Nullable String getPath() {
        return fileSource != null ? fileSource.getPath() : null;
    }

    /**
     * Returns the absolute path of the file source.
     *
     * @return the absolute file path, or {@code null} if no file is set
     */
    @Override
    public @Nullable String getAbsolutePath() {
        return fileSource != null ? fileSource.getAbsolutePath() : null;
    }

    /**
     * Returns the file source.
     *
     * @return the file, or {@code null} if no file is set
     */
    @Override
    public @Nullable File getFile() {
        return fileSource;
    }

    /**
     * Returns the input stream source.
     *
     * @return the input stream, or {@code null} if no input stream is set
     */
    @Override
    public @Nullable InputStream getInputStream() {
        return inputStreamSource;
    }

    /**
     * Loads the content of the file or stream and returns it as a list of strings, where each string represents a line.
     *
     * @return a list of lines from the source
     */
    @Override
    public List<String> loadContent() {
        FileManager manager = null;
        try {
            if (getInputStream() == null) {
                manager = new FileManager(getInputStream());
            } else {
                manager = new FileManager(getFile());
            }
        } catch (Exception e) {
            System.err.printf("[FileManager] Error while loading content: %s%n", e);
        }

        return manager != null ? manager.loadContent() : List.of();
    }

    /**
     * Returns a stream of the content of the file or stream, allowing for line-by-line processing without loading
     * the entire content into memory.
     *
     * @return a stream of lines from the source
     */
    @Override
    public Stream<String> getContentStream() {
        if (getInputStream() == null && getFile() == null) {
            return Stream.empty();
        }

        try {
            var inputStream = getInputStream() != null ? getInputStream() : new FileInputStream(getFile());
            var reader = new BufferedReader(new InputStreamReader(inputStream));
            return reader.lines();
        } catch (Exception e) {
            System.err.println("[FileManager] Failed to read input content: " + e);
        }
        return Stream.empty();
    }

    /**
     * Returns the number of lines in the file or stream.
     *
     * @return the number of lines
     */
    @Override
    public long lines() {
        try (var stream = getContentStream()) {
            return stream.count();
        }
    }

    /**
     * Adds content to the end of the file or stream.
     *
     * @param content the content to add
     */
    @Override
    public void add(String content) {
        this.add((int) lines(), content);
    }

    /**
     * Adds content at the specified index in the file or stream.
     *
     * @param index the index where the content should be added
     * @param content the content to add
     */
    @Override
    public void add(int index, String content) {
        File file = getFile();
        if (file == null) {
            System.err.println("[FileManager] Failed to read file source: null.");
            return;
        }

        Path originalPath = file.toPath();
        Path tempPath = null;
        File tempFile = null;

        try {
            tempPath = Files.createTempFile("temp_", ".txt");
            tempFile = tempPath.toFile();

            try (BufferedReader reader = new BufferedReader(new FileReader(file));
                 BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

                String line;
                int currentIndex = 0;

                while ((line = reader.readLine()) != null) {
                    if (currentIndex == index) {
                        writer.write(content);
                        writer.newLine();
                    }
                    writer.write(line);
                    writer.newLine();
                    currentIndex++;
                }

                if (index >= currentIndex) {
                    writer.write(content);
                    writer.newLine();
                }
            }

            Files.move(tempPath, originalPath, StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e) {
            System.err.println("[FileManager] Failed to add content at index " + index + ": " + e);
        } finally {
            if (tempFile != null && tempFile.exists()) {
                if (!tempFile.delete()) {
                    System.err.println("[FileManager] Failed to delete temp file: " + tempFile.getAbsolutePath());
                }
            }
        }
    }


    /**
     * Sets content at the specified index in the file or stream, replacing the existing content.
     *
     * @param index the index where the content should be set
     * @param content the content to set
     */
    @Override
    public void set(int index, String content) {
        File file = getFile();
        if (file == null) {
            System.err.println("[FileManager] Failed to read file source: null.");
            return;
        }

        Path originalPath = file.toPath();
        Path tempPath = null;
        File tempFile = null;

        try {
            tempPath = Files.createTempFile("temp_", ".txt");
            tempFile = tempPath.toFile();

            try (BufferedReader reader = new BufferedReader(new FileReader(file));
                 BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

                String line;
                int currentIndex = 0;
                boolean contentSet = false;

                while ((line = reader.readLine()) != null) {
                    if (currentIndex == index) {
                        writer.write(content);
                        contentSet = true;
                    } else {
                        writer.write(line);
                    }
                    writer.newLine();
                    currentIndex++;
                }

                while (currentIndex < index) {
                    writer.newLine();
                    currentIndex++;
                }

                if (!contentSet) {
                    writer.write(content);
                    writer.newLine();
                }
            }

            Files.move(tempPath, originalPath, StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e) {
            System.err.println("[FileManager] Failed to set content at index " + index + ": " + e);
        } finally {
            if (tempFile != null && tempFile.exists()) {
                if (!tempFile.delete()) {
                    System.err.println("[FileManager] Failed to delete temp file: " + tempFile.getAbsolutePath());
                }
            }
        }
    }

    /**
     * Removes the content at the specified index in the file or stream.
     *
     * @param index the index of the content to remove
     */
    @Override
    public void remove(int index) {
        File file = getFile();
        if (file == null) {
            System.err.println("[FileManager] Failed to read file source: null.");
            return;
        }

        Path originalPath = file.toPath();
        Path tempPath = null;
        File tempFile = null;

        try {
            tempPath = Files.createTempFile("temp_", ".txt");
            tempFile = tempPath.toFile();

            try (BufferedReader reader = new BufferedReader(new FileReader(file));
                 BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

                String line;
                int currentIndex = 0;
                boolean removed = false;

                while ((line = reader.readLine()) != null) {
                    if (currentIndex != index) {
                        writer.write(line);
                        writer.newLine();
                    } else {
                        removed = true;
                    }
                    currentIndex++;
                }

                if (!removed) {
                    System.err.println("[FileManager] Index is out of bounds.");
                }
            }

            Files.move(tempPath, originalPath, StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e) {
            System.err.println("[FileManager] Failed to remove content at index " + index + ": " + e);
        } finally {
            if (tempFile != null && tempFile.exists()) {
                if (!tempFile.delete()) {
                    System.err.println("[FileManager] Failed to delete temp file: " + tempFile.getAbsolutePath());
                }
            }
        }
    }

    /**
     * Removes the first occurrence of the specified content from the file or stream.
     *
     * @param content the content to remove
     */
    @Override
    public void remove(@NotNull String content) {
        File file = getFile();
        if (file == null) {
            System.err.println("[FileManager] Failed to read file source: null.");
            return;
        }

        Path originalPath = file.toPath();
        Path tempPath = null;
        File tempFile = null;

        try {
            tempPath = Files.createTempFile("temp_", ".txt");
            tempFile = tempPath.toFile();

            try (BufferedReader reader = new BufferedReader(new FileReader(file));
                 BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

                String line;
                boolean removed = false;

                while ((line = reader.readLine()) != null) {
                    if (!removed && line.equals(content)) {
                        removed = true;
                        continue;
                    }
                    writer.write(line);
                    writer.newLine();
                }
            }

            Files.move(tempPath, originalPath, StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e) {
            System.err.println("[FileManager] Failed to remove content '" + content + "': " + e);
        } finally {
            if (tempFile != null && tempFile.exists()) {
                if (!tempFile.delete()) {
                    System.err.println("[FileManager] Failed to delete temp file: " + tempFile.getAbsolutePath());
                }
            }
        }
    }

    /**
     * Retrieves the content at the specified index in the file or stream.
     *
     * @param index the index of the content to retrieve
     * @return the content at the index, or {@code null} if not found
     */
    @Override
    public @Nullable String get(int index) {
        try (var stream = getContentStream()) {
            return stream
                    .skip(index)
                    .findFirst()
                    .orElse(null);
        }
    }

    /**
     * Clears the content of the file, leaving it empty.
     */
    @Override
    public void clearFile() {
        if (getFile() == null) {
            System.err.println("[FileManager] File source is null.");
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(getFile()))) {
            // Write nothing to clear the file
        } catch (IOException e) {
            System.err.println("[FileManager] Failed to clear the file: " + e);
        }
    }

    /**
     * Removes all empty lines from the file.
     * The content of the file is preserved, but any lines that are completely empty (or contain only whitespace) will be deleted.
     */
    @Override
    public void trimContent() {
        File file = getFile();
        if (file == null) {
            System.err.println("[FileManager] File source is null.");
            return;
        }

        Path tempPath = null;

        try {
            tempPath = Files.createTempFile("temp_", ".txt");

            try (
                    BufferedReader reader = new BufferedReader(new FileReader(file));
                    BufferedWriter writer = Files.newBufferedWriter(tempPath, StandardOpenOption.WRITE)
            ) {
                String line;

                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        writer.write(line);
                        writer.newLine();
                    }
                }
            }

            Files.move(tempPath, file.toPath(), StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e) {
            System.err.println("[FileManager] Failed to trim content of the file: " + e);
        } finally {
            if (tempPath != null) {
                try {
                    Files.deleteIfExists(tempPath);
                } catch (IOException e) {
                    System.err.println("[FileManager] Failed to delete temporary file: " + e);
                }
            }
        }
    }

    /**
     * Determines whether the file source is editable.
     *
     * @return {@code true} if the file is editable, otherwise {@code false}
     */
    @Override
    public boolean isSourceEditable() {
        return getFile() != null && getFile().isFile() && !isSourceReadOnly();
    }

    /**
     * Determines whether the file source is read-only.
     *
     * @return {@code true} if the file is read-only, otherwise {@code false}
     */
    @Override
    public boolean isSourceReadOnly() {
        return getFile() == null || !getFile().canRead();
    }

    /**
     * Returns the type of the source, either "File" or "InputStream".
     *
     * @return a string indicating the source type
     */
    @Override
    public String getSource() {
        return getFile() == null ? "InputStream" : "File";
    }
}