package de.ac;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
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
     * DON'T FORGET TO CLOSE THIS STREAM AFTER USE!
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
            return reader.lines().onClose(() -> {
                try {
                    reader.close();
                } catch (IOException e) {
                    System.err.println("[FileManager] Failed to close reader: " + e);
                }
            });
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
     * @param index   the index where the content should be added
     * @param content the content to add
     */
    @Override
    public void add(int index, String content) {
        try {
            editFileLineByLine((line, i) -> {
                if (i == index) return LineEditResult.insert(content);
                return LineEditResult.keep();
            }, index < 0, content);
        } catch (IOException e) {
            System.err.println("[FileManager] Failed to add content at index " + index + ": " + e);
        }
    }

    /**
     * Sets content at the specified index in the file or stream, replacing the existing content.
     *
     * @param index   the index where the content should be set
     * @param content the content to set
     */
    @Override
    public void set(int index, String content) {
        try {
            editFileLineByLine((line, i) -> {
                if (i == index) return LineEditResult.replace(content);
                return LineEditResult.keep();
            }, true, content);
        } catch (IOException e) {
            System.err.println("[FileManager] Failed to set content at index " + index + ": " + e);
        }
    }

    /**
     * Removes the content at the specified index in the file or stream.
     *
     * @param index the index of the content to remove
     */
    @Override
    public void remove(int index) {
        try {
            editFileLineByLine((line, i) -> i == index ? LineEditResult.remove() : LineEditResult.keep(), false, null);
        } catch (IOException e) {
            System.err.println("[FileManager] Failed to remove line at index " + index + ": " + e);
        }
    }

    /**
     * Removes the first occurrence of the specified content from the file or stream.
     *
     * @param content the content to remove
     */
    @Override
    public void remove(@NotNull String content) {
        try {
            boolean[] removed = {false};
            editFileLineByLine((line, i) -> {
                if (!removed[0] && line.equals(content)) {
                    removed[0] = true;
                    return LineEditResult.remove();
                }
                return LineEditResult.keep();
            }, false, null);
        } catch (IOException e) {
            System.err.println("[FileManager] Failed to remove content '" + content + "': " + e);
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
        try {
            editFileLineByLine((line, i) -> line.trim().isEmpty() ? LineEditResult.remove() : LineEditResult.keep(), false, null);
        } catch (IOException e) {
            System.err.println("[FileManager] Failed to trim content: " + e);
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
     * Returns the type of the source, either "File" or "InputStream".
     *
     * @return a string indicating the source type
     */
    @Override
    public String getSource() {
        return getFile() == null ? "InputStream" : "File";
    }

    private void editFileLineByLine(LineEditor editor, boolean appendIfMissing, @Nullable String finalLine) throws IOException {
        File file = getFile();
        if (file == null) {
            System.err.println("[FileManager] File source is null.");
            return;
        }

        Path originalPath = file.toPath();
        Path tempPath = Files.createTempFile(originalPath.getParent(), "temp_", ".txt");

        try (BufferedReader reader = new BufferedReader(new FileReader(file));
             BufferedWriter writer = Files.newBufferedWriter(tempPath, StandardCharsets.UTF_8)) {

            String line;
            int index = 0;
            boolean actionPerformed = false;

            while ((line = reader.readLine()) != null) {
                LineEditResult result = editor.editLine(line, index++);
                switch (result.action) {
                    case KEEP -> {
                        writer.write(line);
                        writer.newLine();
                    }
                    case REPLACE -> {
                        writer.write(result.newLine);
                        writer.newLine();
                        actionPerformed = true;
                    }
                    case INSERT -> {
                        writer.write(result.newLine);
                        writer.newLine();
                        writer.write(line);
                        writer.newLine();
                        actionPerformed = true;
                    }
                    case REMOVE -> {
                        actionPerformed = true;
                    }
                }
            }

            if (!actionPerformed && appendIfMissing && finalLine != null) {
                writer.write(finalLine);
                writer.newLine();
            }
        }

        Files.move(tempPath, originalPath, StandardCopyOption.REPLACE_EXISTING);
    }

    private interface LineEditor {
        LineEditResult editLine(String line, int index);
    }

    private record LineEditResult(FileManagerWOC.LineEditResult.Action action, String newLine) {
            enum Action {KEEP, REPLACE, INSERT, REMOVE}

        static LineEditResult keep() {
            return new LineEditResult(Action.KEEP, null);
        }

            static LineEditResult replace(String newLine) {
                return new LineEditResult(Action.REPLACE, newLine);
            }

            static LineEditResult insert(String newLine) {
                return new LineEditResult(Action.INSERT, newLine);
            }

            static LineEditResult remove() {
                return new LineEditResult(Action.REMOVE, null);
            }
        }
}