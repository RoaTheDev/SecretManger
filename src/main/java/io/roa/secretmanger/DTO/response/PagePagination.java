package io.roa.secretmanger.DTO.response;

public record PagePagination(int pageNumber,
                             int pageSize,
                             long totalElements,
                             int totalPages) {
}
