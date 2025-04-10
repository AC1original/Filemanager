package de.ac;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Stream;

public interface IFileManager {

    void setSource(@NotNull final String path) throws IOException;
    void setSource(@NotNull final File file) throws IOException;
    void setSource(@NotNull final InputStream inputStream) throws IOException;

    @Nullable String getPath();

    @Nullable String getAbsolutePath();

    @Nullable File getFile();

    @Nullable InputStream getInputStream();

    List<String> loadContent();

    Stream<String> getContentStream();

    long lines();

    void add(final String content);
    void add(final int index, final String content);

    void set(final int index, final String content);

    @Nullable String get(final int index);

    void clearFile();

    void trimContent();

    boolean isSourceEditable();

    boolean isSourceReadOnly();

    String getSource();
}