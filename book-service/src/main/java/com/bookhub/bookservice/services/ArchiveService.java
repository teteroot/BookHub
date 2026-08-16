package com.bookhub.bookservice.services;

import com.bookhub.bookservice.dtos.entries.ArchiveEntry;

import java.nio.file.Path;
import java.util.List;

public interface ArchiveService {

    Path collectFilesToArchive(List<ArchiveEntry> entries);
}
