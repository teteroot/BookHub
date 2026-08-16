package com.bookhub.bookservice.services.impls;

import com.bookhub.bookservice.exceptions.extensions.ContentLoadException;
import com.bookhub.bookservice.exceptions.extensions.ContentSaveException;
import com.bookhub.bookservice.services.FileTempService;
import com.bookhub.bookservice.services.IOConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.List;

@Slf4j
@Service
public
class FileTempServiceImpl implements FileTempService {
    @Override
    public Path writeToTempFile(String prefix, String suffix, IOConsumer<OutputStream> writer) {
        Path tempFile;
        try {
            tempFile = Files.createTempFile(prefix, suffix);
        } catch (IOException e) {
            throw new ContentLoadException();
        }
        try (OutputStream os = new FileOutputStream(tempFile.toFile())) {
            writer.accept(os);

        } catch (Exception e) {
            deleteQuietly(List.of(tempFile));
            throw new ContentSaveException();
        }
        return tempFile;
    }

    @Override
    public InputStream openStream(Path path) {
        try {
            return Files.newInputStream(path);
        } catch (IOException e) {
            throw new ContentLoadException();
        }
    }

    @Override
    public InputStream openStreamWithOption(Path path, OpenOption... option) {
        try {
            return Files.newInputStream(path, option);
        } catch (IOException e) {
            throw new ContentLoadException();
        }
    }

    @Override
    public long sizeOf(Path path) {
        try {
            return Files.size(path);
        } catch (IOException e) {
            throw new ContentLoadException();
        }
    }

    @Override
    public void deleteQuietly(List<Path> paths) {
        for (Path path : paths) {
            if (path != null){
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    log.warn("Could not delete file {}", path, e);
                }
            }
        }
    }
}
