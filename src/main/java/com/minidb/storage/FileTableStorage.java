package com.minidb.storage;

import com.minidb.exception.DbException;
import com.minidb.model.Table;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;


public class FileTableStorage implements TableStorage {
    private static final String EXT = ".tbl";

    private final Path dir;

    public FileTableStorage(Path dir) {
        this.dir = dir;
        try {
            Files.createDirectories(dir);
            removeTempFiles();
        } catch (IOException e) {
            throw new DbException("Не удалось подготовить папку данных: " + dir, e);
        }
    }

    @Override
    public void save(Table table) {
        Path target = fileOf(table.getName());
        Path tmp = dir.resolve(table.getName().toLowerCase() + EXT + ".tmp");
        try {
            try (ObjectOutputStream out = new ObjectOutputStream(
                    new BufferedOutputStream(Files.newOutputStream(tmp)))) {
                out.writeObject(table);
            }
            try {
                Files.move(tmp, target,
                        StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new DbException("Не удалось сохранить таблицу " + table.getName(), e);
        }
    }

    @Override
    public List<Table> loadAll() {
        List<Table> result = new ArrayList<>();
        try (DirectoryStream<Path> files = Files.newDirectoryStream(dir, "*" + EXT)) {
            for (Path file : files) {
                try (ObjectInputStream in = new ObjectInputStream(
                        new BufferedInputStream(Files.newInputStream(file)))) {
                    result.add((Table) in.readObject());
                } catch (IOException | ClassNotFoundException e) {
                    System.err.println("Файл повреждён и пропущен: " + file.getFileName()
                            + " (" + e.getMessage() + ")");
                }
            }
        } catch (IOException e) {
            throw new DbException("Не удалось прочитать папку данных: " + dir, e);
        }
        return result;
    }

    @Override
    public void delete(String tableName) {
        try {
            Files.deleteIfExists(fileOf(tableName));
        } catch (IOException e) {
            throw new DbException("Не удалось удалить файл таблицы " + tableName, e);
        }
    }

    private Path fileOf(String tableName) {
        return dir.resolve(tableName.toLowerCase() + EXT);
    }

    private void removeTempFiles() throws IOException {
        try (DirectoryStream<Path> files = Files.newDirectoryStream(dir, "*.tmp")) {
            for (Path file : files) {
                Files.deleteIfExists(file);
            }
        }
    }
}