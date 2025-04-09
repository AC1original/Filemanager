package de.ac;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;

public class FileManagerWOC {
    @Nullable
    private File file;
    @Nullable
    private InputStream inputStream;

    public FileManagerWOC() {}

    public FileManagerWOC(@NotNull final String path) {
    }

    public FileManagerWOC(@NotNull final File file) {

    }

    public FileManagerWOC(@NotNull final InputStream inputStream) {

    }

}