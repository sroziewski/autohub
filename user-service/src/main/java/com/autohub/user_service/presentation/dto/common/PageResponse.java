package com.autohub.user_service.presentation.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * A standardized response format for paginated data.
 * This wrapper provides pagination metadata along with the actual data.
 *
 * @param <T> The type of data being returned
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    
    /**
     * The actual data for the current page
     */
    private List<T> content;
    
    /**
     * Pagination metadata
     */
    private PageMetadata metadata;
    
    /**
     * Creates a PageResponse from a Spring Data Page object
     *
     * @param page The Spring Data Page object
     * @param <T> The type of data
     * @return A new PageResponse instance
     */
    public static <T> PageResponse<T> from(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .metadata(PageMetadata.builder()
                        .page(page.getNumber())
                        .size(page.getSize())
                        .totalElements(page.getTotalElements())
                        .totalPages(page.getTotalPages())
                        .first(page.isFirst())
                        .last(page.isLast())
                        .build())
                .build();
    }
    
    /**
     * Metadata for pagination
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageMetadata {
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean first;
        private boolean last;
    }
}
