package com.bookhub.bookservice.services;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.List;

public interface FileTempService {
    Path writeToTempFile(String prefix, String suffix, IOConsumer<OutputStream> writer);
    InputStream openStream(Path path);
    InputStream openStreamWithOption(Path path, OpenOption... option);
    long sizeOf(Path path);
    void deleteQuietly(List<Path> paths);
}
