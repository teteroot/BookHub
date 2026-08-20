package com.bookhub.bookservice.dtos.entries;

import java.util.UUID;

public record BookPageCountEntry(UUID bookId, Long count) {
}
