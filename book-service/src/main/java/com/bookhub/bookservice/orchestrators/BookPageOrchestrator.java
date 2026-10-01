package com.bookhub.bookservice.orchestrators;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface BookPageOrchestrator {

    Integer getCountOfPages(UUID bookId);

    Map<UUID, Integer> getAllCountOfPages(List<UUID> bookIds);


    void swapBookPages(UUID bookId, UUID authorId, UUID pageId, UUID swapPageId);

    void moveBookPage(UUID bookId, UUID authorId, UUID pageId, Integer pageNumber);
}