package com.bookhub.bookservice.dtos.entries;

import java.io.InputStream;
import java.util.function.Supplier;

public record ArchiveEntry(String fileName, Supplier<InputStream> contentProvider) {
}
