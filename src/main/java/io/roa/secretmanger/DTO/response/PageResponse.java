package io.roa.secretmanger.DTO.response;

import java.util.List;

    public record PageResponse<T>(
            List<T> content,
            PagePagination pagination

    ) {
        public static <T> PageResponse<T> of(org.springframework.data.domain.Page<T> page) {
            return new PageResponse<>(
                    page.getContent(),
                    new PagePagination(
                            page.getNumber(),
                            page.getSize(),
                            page.getTotalElements(),
                            page.getTotalPages())
            );
        }
    }

