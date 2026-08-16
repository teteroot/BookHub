package com.bookhub.bookservice.services.impls;


import com.bookhub.bookservice.dtos.entries.ArchiveEntry;
import com.bookhub.bookservice.services.ArchiveService;
import com.bookhub.bookservice.services.FileTempService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@RequiredArgsConstructor
@Service
public class ArchiveServiceImpl implements ArchiveService {

    private final FileTempService fileTempService;

    @Override
    public Path collectFilesToArchive(List<ArchiveEntry> entries) {
        return fileTempService.writeToTempFile("archive", ".zip", os -> {
            try (ZipOutputStream zos = new ZipOutputStream(os)) {
                for (var entry: entries){
                    try(InputStream bookStream = entry.contentProvider().get()) {
                        ZipEntry zipEntry = new ZipEntry(entry.fileName());
                        zos.putNextEntry(zipEntry);
                        bookStream.transferTo(zos);
                        zos.closeEntry();
                    } catch (IOException e) {
                        log.warn("Error while trying to write zip file {}", entry.fileName(), e);
                    }

                }
            }
        });
    }

}
