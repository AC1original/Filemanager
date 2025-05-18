# 📁 FileManager

**FileManager** is a lightweight and flexible Java library for managing text file content. It provides two different classes tailored for different use cases:

- `FileManager` – stores the entire file content in memory (faster, but more memory usage).
- `FileManagerWOC` – works without caching the whole file (slower, but more memory-efficient).

---

## 🚀 Features

- Read, write, insert, and remove lines from files
- Support for both `File` and `InputStream` sources
- Manual or automatic file updates
- Optimized for both small and large files

---

## 🧱 Class Overview

### `FileManager`

- Loads the entire file into a `List<String>`
- Changes are only written to the file when calling `update()`
- Use `setAutoUpdate(true)` to automatically save after each change
- Best suited for smaller files or when performing many changes in memory

### `FileManagerWOC` (Without Cache)

- Does not load the whole file into memory
- Each operation directly reads from or writes to the file
- No need to call `update()` – changes are immediately applied
- Ideal for large files or low-memory environments

---

## 🛠️ Installation

### Maven

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.AC1original</groupId>
        <artifactId>Filemanager</artifactId>
        <version>main-SNAPSHOT</version>
    </dependency>
</dependencies>
```

### Gradle

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.AC1original:Filemanager:main-SNAPSHOT'
}
```

---

## 📄 Usage Example

```java
import de.ac.filemanager.FileManager;

FileManager manager = new FileManager(new File("resources/data/userdata.dat"));

// Remove a specific line
manager.remove(10);

// Add a new line
manager.add("New entry");

// Save changes to the file
manager.update();

// Print all lines
manager.getContentStream().forEach(System.out::println);
```

> ⚠️ **Note:** When using `FileManager`, you must call `update()` to apply changes to the file, unless you enable `setAutoUpdate(true)`.

> ✅ **With `FileManagerWOC`, changes are written immediately.** No call to `update()` is necessary.
> 
---

## ⚠️ Important Note

If you're using an `InputStream` as the source, the source is read-only.  
That means methods like `set()`, `add()`, `remove()`, etc. will have no effect.  
These operations are only supported when using a `File` as the source.

---

## 🤝 Contributing

Feel free to fork this repository, make improvements, and submit a pull request!

---

## 📬 Contact

For questions or suggestions, feel free to [contact me on Discord](https://discord.com/users/872921450691067924/).
