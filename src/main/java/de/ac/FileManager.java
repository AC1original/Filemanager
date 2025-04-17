package de.ac;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * The {@code FileManager} class implements the {@link IFileManager} interface and provides functionality for managing
 * file content. It supports loading, modifying, and saving content from files and streams with caching capabilities.
 */
public class FileManager implements IFileManager {
    private final List<String> content = new ArrayList<>();
    private boolean autoUpdate = false;
    @Nullable
    private File fileSource;
    @Nullable
    private InputStream inputStreamSource;

    /**
     * Default constructor.
     */
    public FileManager() {
    }

    /**
     * Constructor that initializes the {@code FileManager} with a file path.
     *
     * @param path The file path.
     * @throws IOException If an I/O error occurs while setting the source.
     */
    public FileManager(@NotNull String path) throws IOException {
        setSource(path);
    }

    /**
     * Constructor that initializes the {@code FileManager} with a {@link File} object.
     *
     * @param file The file.
     * @throws IOException If an I/O error occurs while setting the source.
     */
    public FileManager(@NotNull File file) throws IOException {
        setSource(file);
    }

    /**
     * Constructor that initializes the {@code FileManager} with an {@link InputStream}.
     *
     * @param stream The input stream.
     * @throws IOException If an I/O error occurs while setting the source.
     */
    public FileManager(@NotNull InputStream stream) throws IOException {
        setSource(stream);
    }

    /**
     * Sets the source of the content using a file path.
     *
     * @param path The file path.
     * @throws IOException If the file does not exist or an I/O error occurs.
     */
    @Override
    public void setSource(@NotNull String path) throws IOException {
        this.setSource(new File(path));
    }

    /**
     * Sets the source of the content using a {@link File}.
     *
     * @param file The file.
     * @throws IOException If the file does not exist or an I/O error occurs.
     */
    @Override
    public void setSource(@NotNull File file) throws IOException {
        if (!file.exists()) throw new FileNotFoundException();
        this.fileSource = file;

        refreshCache(file);
    }

    /**
     * Sets the source of the content using an {@link InputStream}.
     *
     * @param inputStream The input stream.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public void setSource(@NotNull InputStream inputStream) throws IOException {
        this.inputStreamSource = inputStream;

        refreshCache(inputStream);
    }

    /**
     * Retrieves the relative file path, or {@code null} if the source is an {@link InputStream}.
     *
     * @return The relative file path or {@code null}.
     */
    @Override
    public @Nullable String getPath() {
        return fileSource != null ? fileSource.getPath() : null;
    }

    /**
     * Retrieves the absolute file path, or {@code null} if the source is an {@link InputStream}.
     *
     * @return The absolute file path or {@code null}.
     */
    @Override
    public @Nullable String getAbsolutePath() {
        return fileSource != null ? fileSource.getAbsolutePath() : null;
    }

    /**
     * Retrieves the {@link File} object if the source is a file.
     *
     * @return The {@link File} object, or {@code null} if the source is an {@link InputStream}.
     */
    @Override
    public @Nullable File getFile() {
        return fileSource;
    }

    /**
     * Retrieves the {@link InputStream} if the source is a stream.
     *
     * @return The {@link InputStream} object, or {@code null} if the source is a file.
     */
    @Override
    public @Nullable InputStream getInputStream() {
        return inputStreamSource;
    }

    /**
     * Loads the content from the source into memory and returns it as a list.
     * It reloads the content from the source if the content is stale.
     *
     * @return A list of strings representing the content of the source.
     */
    @Override
    public List<String> loadContent() {
        try {
            refreshCache(getFile(), getInputStream());
        } catch (IOException e) {
            System.err.printf("[FileManager] Error while reloading content: %s%n", e);
        }
        return List.copyOf(content);
    }

    /**
     * Returns a stream of strings representing the content of the file or stream.
     *
     * @return A stream of strings representing the content.
     */
    @Override
    public Stream<String> getContentStream() {
        return content.stream();
    }

    /**
     * Returns the number of lines in the content.
     *
     * @return The number of lines.
     */
    @Override
    public long lines() {
        return content.size();
    }

    /**
     * Adds new content at the end of the list and updates the source if auto-update is enabled.
     *
     * @param content The content to append.
     */
    @Override
    public void add(String content) {
        this.content.add(content);
        if (autoUpdate) update();
    }

    /**
     * Inserts content at a specific index and updates the source if auto-update is enabled.
     *
     * @param index   The index at which to insert content.
     * @param content The content to insert.
     */
    @Override
    public void add(int index, String content) {
        this.content.add(index, content);
        if (autoUpdate) update();
    }

    /**
     * Sets the content at a specific index and fills empty lines if necessary, and updates the source if auto-update is enabled.
     *
     * @param index   The index at which to set content.
     * @param string  The content to set.
     */
    @Override
    public void set(int index, String string) {
        if (index > lines()) {
            long lineCount = lines();
            for (long i = lineCount; i <= index; i++) {
                this.content.add("");
            }
            this.content.set(index, string);
        } else {
            this.content.set(index, string);
        }
        if (autoUpdate) update();
    }

    /**
     * Removes content at a specific index and updates the source if auto-update is enabled.
     *
     * @param index The index of the content to remove.
     */
    @Override
    public void remove(int index) {
        content.remove(index);

        if (autoUpdate) update();
    }

    /**
     * Removes all occurrences of the specified content and updates the source if auto-update is enabled.
     *
     * @param content The content to remove.
     */
    @Override
    public void remove(@NotNull String content) {
        this.content.removeIf(c -> c.equals(content));

        if (autoUpdate) update();
    }

    /**
     * Retrieves the content at a specific index.
     *
     * @param index The index of the content to retrieve.
     * @return The content at the specified index, or {@code null} if the index is out of bounds.
     */
    @Override
    public @Nullable String get(int index) {
        try {
            return content.get(index);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    /**
     * Clears the content of the file. Does nothing if the source is an {@link InputStream}.
     */
    @Override
    public void clearFile() {
        if (getFile() == null) {
            System.err.println("[FileManager] Failed to write changes to file! File source is null. Maybe you are using InputStream source?");
            return;
        }
        try {
            new FileOutputStream(getFile().getPath()).close();
        } catch (IOException e) {
            System.err.println("[FileManager] Failed to write changes to file! " + e);
        }
    }

    /**
     * Trims empty lines from the content.
     * Updates the source if auto-update is enabled.
     */
    @Override
    public void trimContent() {
        for (int i = 0; i<lines(); i++) {
            if (content.get(i).isEmpty()) remove(i);
        }

        if (autoUpdate) update();
    }

    /**
     * Checks if the source file is editable.
     *
     * @return {@code true} if the source is a writable file, otherwise {@code false}.
     */
    @Override
    public boolean isSourceEditable() {
        return getFile() != null && getFile().isFile() && !isSourceReadOnly();
    }

    /**
     * Checks if the source file is read-only.
     *
     * @return {@code true} if the source is read-only, otherwise {@code false}.
     */
    @Override
    public boolean isSourceReadOnly() {
        return getFile() == null || !getFile().canRead();
    }

    /**
     * Returns the type of the source, either "File" or "InputStream".
     *
     * @return The source type.
     */
    @Override
    public String getSource() {
        return getFile() == null ? "InputStream" : "File";
    }

    /**
     * Refreshes the cache by reading content from the file.
     *
     * @param fileSource The file to read content from.
     * @throws IOException If an error occurs while reading the file.
     */
    public void refreshCache(@NotNull File fileSource) throws IOException {
        this.refreshCache(fileSource, null);
    }

    /**
     * Refreshes the cache by reading content from the input stream.
     *
     * @param inputStreamSource The input stream to read content from.
     * @throws IOException If an error occurs while reading the input stream.
     */
    public void refreshCache(@NotNull InputStream inputStreamSource) throws IOException {
        this.refreshCache(null, inputStreamSource);
    }

    /**
     * Refreshes the cache by reading content from the given file or input stream.
     *
     * @param fileSource        The file to read content from, or {@code null} if using an input stream.
     * @param inputStreamSource The input stream to read content from, or {@code null} if using a file.
     * @throws IOException If an error occurs while reading the source.
     */
    public void refreshCache(@Nullable File fileSource, @Nullable InputStream inputStreamSource) throws IOException {
        if (fileSource != null) {
            try (var reader = new BufferedReader(new FileReader(fileSource))) {
                reader.lines().forEach(content::add);
            }
        } else if (inputStreamSource != null) {
            try (var reader = new BufferedReader(new InputStreamReader(inputStreamSource))) {
                reader.lines().forEach(content::add);
            }
        }
    }

    /**
     * Updates the source by writing the cached content back to the file.
     * Does nothing if the source is an {@link InputStream}.
     */
    public void update() {
        if (getFile() == null) {
            System.err.println("[FileManager] Failed to write changes to file! File source is null. Maybe you are using InputStream source?");
            return;
        }

        clearFile();
        try (var writer = new BufferedWriter(new FileWriter(getFile()))) {
            for (var line : content) {
                writer.write(line);
                writer.newLine();
            }
            writer.flush();
        } catch (IOException e) {
            System.err.println("[FileManager] Failed to write changes to file! " + e);
        }
    }

    /**
     * Checks if the current cached content matches the content of the file or input stream.
     *
     * @return {@code true} if the content is up-to-date, otherwise {@code false}.
     */
    public boolean isManagerUpToDate() {
        Reader reader = Optional.ofNullable(getFile())
                .<Reader>map(file -> {
                    try {
                        return new FileReader(file);
                    } catch (FileNotFoundException e) {
                        System.err.println("[FileManager] Failed to read file! " + e);
                        return null;
                    }
                })
                .or(() -> Optional.ofNullable(getInputStream())
                        .map(InputStreamReader::new))
                .orElse(null);

        if (reader == null) {
            return false;
        }

        try (BufferedReader bufferedReader = new BufferedReader(reader)) {
            int index = 0;
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String expected = get(index++);
                if (expected == null || !expected.equals(line)) {
                    return false;
                }
            }
            return true;
        } catch (IOException e) {
            System.err.println("[FileManager] Failed to read file! " + e);
            return false;
        }
    }

    /**
     * Returns whether the auto-update feature is enabled.
     *
     * @return {@code true} if auto-update is enabled, otherwise {@code false}.
     */
    public boolean isAutoUpdate() {
        return autoUpdate;
    }

    /**
     * Enables or disables the auto-update feature.
     *
     * @param autoUpdate {@code true} to enable auto-update, {@code false} to disable it.
     */
    public void setAutoUpdate(boolean autoUpdate) {
        this.autoUpdate = autoUpdate;
    }
}
