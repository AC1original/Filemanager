# File Manager
Simple file manager library for Java. This file manager provides functionality to manage file contents, allowing reading, writing, and modifying text files. It supports both file-based and InputStream-based sources.
## Dependency
### Maven
```
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
        <version>v1.0.9</version>
    </dependency>
</dependencies>
```
### Gradle 
```
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.AC1original:Filemanager:v1.0.9'
}
```
## How to use
Example code:
```java
Filemanager manager = null;

try {
    manager = new Filemanager(new File("ressources/data/userdata.dat")); //Initialize new file manager
} catch (NoSuchFileException e) {
    System.err.println("File not found! " + e);
}

manager.remove(10); //Remove content of specific index
manager.add("Text"); //Add new line
manager.update(); //Write new content to file

for (String content : manager.getContent()) {
    System.out.println(content);
}
```
## Important Node
Make sure to use ```#update()``` after modifying a file to apply and save the changes to that file!
