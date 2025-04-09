package de.ac;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Stream;

public interface IFileManager {

    void setSource(@NotNull final String path);
    void setSource(@NotNull final File file);
    void setSource(@NotNull final InputStream inputStream);

    @Nullable String getPath();

    @Nullable File getFile();

    @Nullable InputStream getInputStream();

    List<String> loadContent();

    Stream<String> getContentStream();

    long lines();

    void add(final String content);
    void add(final int index, final String content);

    void set(final int index, final String content);

    @Nullable String get(final int index);

    long find(final String content);
    long find(final String startsWith);
    long find(final String contains);

    void clearFile();

    void trimFile();

    boolean isSourceEditable();

    boolean isReadOnly();

    @Nullable String getSource();
}