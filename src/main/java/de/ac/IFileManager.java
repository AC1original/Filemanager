package de.ac;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Stream;

/**
 * The {@code IFileManager} interface defines methods for managing and manipulating file contents.
 * It provides a uniform abstraction for loading, modifying, and saving file contents,
 * supporting various data sources like files and input streams.
 */
public interface IFileManager {

    /**
     * Sets the source of the file content using a file path.
     *
     * @param path The relative or absolute path to the file.
     * @throws IOException If an I/O error occurs while accessing the file.
     */
    void setSource(@NotNull final String path) throws IOException;

    /**
     * Sets the source of the file content using a {@link File} object.
     *
     * @param file The {@link File} object representing the file.
     * @throws IOException If an I/O error occurs while accessing the file.
     */
    void setSource(@NotNull final File file) throws IOException;

    /**
     * Sets the source of the file content using an {@link InputStream}.
     *
     * @param inputStream The {@link InputStream} from which the file content will be loaded.
     * @throws IOException If an I/O error occurs while accessing the stream.
     */
    void setSource(@NotNull final InputStream inputStream) throws IOException;

    /**
     * Retrieves the relative file path, or {@code null} if the source is an {@link InputStream}.
     *
     * @return The relative file path or {@code null}.
     */
    @Nullable String getPath();

    /**
     * Retrieves the absolute file path, or {@code null} if the source is an {@link InputStream}.
     *
     * @return The absolute file path or {@code null}.
     */
    @Nullable String getAbsolutePath();

    /**
     * Retrieves the {@link File} object if the source is a file.
     *
     * @return The {@link File} object, or {@code null} if the source is an {@link InputStream}.
     */
    @Nullable File getFile();

    /**
     * Retrieves the {@link InputStream} if the source is a stream.
     *
     * @return The {@link InputStream} object, or {@code null} if the source is a file.
     */
    @Nullable InputStream getInputStream();

    /**
     * Loads the content of the source file or stream into memory as a list of strings.
     *
     * @return A list of strings representing the content of the file or stream.
     */
    List<String> loadContent();

    /**
     * Returns a stream of strings representing the content of the file or stream.
     *
     * @return A {@link Stream} of strings representing the file content.
     */
    Stream<String> getContentStream();

    /**
     * Returns the number of lines in the file or stream content.
     *
     * @return The number of lines in the content.
     */
    long lines();

    /**
     * Appends a new line of content to the file or stream.
     *
     * @param content The content to append.
     */
    void add(final String content);

    /**
     * Inserts a new line of content at the specified index.
     *
     * @param index   The index at which the content should be inserted.
     * @param content The content to insert.
     */
    void add(final int index, final String content);

    /**
     * Sets the content at the specified index.
     * If the index exceeds the current number of lines, blank lines are added to the file.
     *
     * @param index   The index at which to set the content.
     * @param content The content to set at the specified index.
     */
    void set(final int index, final String content);

    /**
     * Removes the line at the specified index.
     *
     * @param index The index of the line to remove.
     */
    void remove(final int index);

    /**
     * Removes all lines that match the specified content.
     *
     * @param content The content to remove from the file.
     */
    void remove(@NotNull final String content);

    /**
     * Retrieves the content at the specified index.
     *
     * @param index The index of the content to retrieve.
     * @return The content at the specified index, or {@code null} if the index is out of bounds.
     */
    @Nullable String get(final int index);

    /**
     * Clears all content from the file if the source is a file.
     */
    void clearFile();

    /**
     * Removes all empty lines from the content.
     */
    void trimContent();

    /**
     * Determines if the source is editable (i.e., a writable file).
     *
     * @return {@code true} if the source is editable, otherwise {@code false}.
     */
    boolean isSourceEditable();

    /**
     * Determines if the source is read-only (i.e., cannot be modified).
     *
     * @return {@code true} if the source is read-only, otherwise {@code false}.
     */
    boolean isSourceReadOnly();

    /**
     * Returns a string identifying the source type, either "File" or "InputStream".
     *
     * @return A string representing the source type.
     */
    String getSource();
}
