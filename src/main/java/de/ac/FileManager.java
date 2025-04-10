package de.ac;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class FileManager implements IFileManager {
    private final List<String> content = new ArrayList<>();
    private boolean autoUpdate = false;
    @Nullable
    private File fileSource;
    @Nullable
    private InputStream inputStreamSource;

    public FileManager() {
    }

    public FileManager(@NotNull String path) throws IOException {
        setSource(path);
    }

    public FileManager(@NotNull File file) throws IOException {
        setSource(file);
    }

    public FileManager(@NotNull InputStream stream) throws IOException {
        setSource(stream);
    }

    @Override
    public void setSource(@NotNull String path) throws IOException {
        this.setSource(new File(path));
    }

    @Override
    public void setSource(@NotNull File file) throws IOException {
        if (!file.exists()) throw new FileNotFoundException();
        this.fileSource = file;

        refreshCache(file);
    }

    @Override
    public void setSource(@NotNull InputStream inputStream) throws IOException {
        this.inputStreamSource = inputStream;

        refreshCache(inputStream);
    }

    @Override
    public @Nullable String getPath() {
        return fileSource != null ? fileSource.getPath() : null;
    }

    @Override
    public @Nullable String getAbsolutePath() {
        return fileSource != null ? fileSource.getAbsolutePath() : null;
    }

    @Override
    public @Nullable File getFile() {
        return fileSource;
    }

    @Override
    public @Nullable InputStream getInputStream() {
        return inputStreamSource;
    }

    @Override
    public List<String> loadContent() {
        try {
            refreshCache(getFile(), getInputStream());
        } catch (IOException e) {
            System.err.printf("[FileManager] Error while reloading content: %s%n", e);
        }
        return content;
    }

    @Override
    public Stream<String> getContentStream() {
        return content.stream();
    }

    @Override
    public long lines() {
        return content.size();
    }

    @Override
    public void add(String content) {
        this.content.add(content);
        if (autoUpdate) update();
    }

    @Override
    public void add(int index, String content) {
        this.content.add(index, content);
        if (autoUpdate) update();
    }

    @Override
    public void set(int index, String string) {
        if (index > lines()) {
            long lineCount = lines();
            for (long i = lineCount; i < index; i++) {
                this.content.add("");
            }
            this.content.set(index, string);
        } else {
            this.content.set(index, string);
        }
        if (autoUpdate) update();
    }

    @Override
    public @Nullable String get(int index) {
        try {
            return content.get(index);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

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

    @Override
    public void trimContent() {
        getContentStream().filter(String::isEmpty).forEach(content::remove);

        if (autoUpdate) update();
    }

    @Override
    public boolean isSourceEditable() {
        return getFile() != null && getFile().isFile() && isSourceReadOnly();
    }

    @Override
    public boolean isSourceReadOnly() {
        return getFile() == null && getFile().canRead();
    }

    @Override
    public String getSource() {
        return getFile() == null ? "InputStream" : "File";
    }

    public void refreshCache(@NotNull File fileSource) throws IOException {
        this.refreshCache(fileSource, null);
    }

    public void refreshCache(@NotNull InputStream inputStreamSource) throws IOException {
        this.refreshCache(null, inputStreamSource);
    }

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

    public boolean isAutoUpdate() {
        return autoUpdate;
    }

    public void setAutoUpdate(boolean autoUpdate) {
        this.autoUpdate = autoUpdate;
    }
}
