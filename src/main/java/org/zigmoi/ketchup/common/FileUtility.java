package org.zigmoi.ketchup.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class FileUtility {

    public static String readDataFromFile(File file) throws IOException {
        return Arrays.toString(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
    }

    public static String readDataFromFile(String path) throws IOException {
        return Arrays.toString(Files.readAllBytes(Paths.get(path)));
    }

    public static void createAndWrite(File file, String data) throws IOException {
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        FileOutputStream outputStream = new FileOutputStream(file.getAbsoluteFile());
        outputStream.write(data.getBytes());
        outputStream.close();
    }

    public static void deleteDirectory(File file) throws IOException {
        if (file.isDirectory()) {
            delete(file);
        }
    }

    public static void delete(File file) throws IOException {
        if (file.isDirectory()) {
            if (Objects.requireNonNull(file.list()).length == 0) {
                file.delete();
            } else {
                for (String temp : Objects.requireNonNull(file.list())) {
                    File fileDelete = new File(file, temp);
                    delete(fileDelete);
                }
                if (Objects.requireNonNull(file.list()).length == 0) {
                    file.delete();
                }
            }
        } else {
            file.delete();
        }
    }

    public static List<File> listOnlyDirs(File file, boolean recursive) {
        return null;
    }
}
