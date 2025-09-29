package com.eafit.tutorial.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Respuesta paginada que envuelve contenido paginado.
 * Proporciona información tanto del contenido como de los metadatos de paginación.
 * 
 * @param <T> Tipo de datos contenidos en la página
 * @author Juan Pablo Rua
 * @version 1.0
 * @since 2025-09-29
 */
@Schema(description = "Respuesta paginada con contenido y metadatos de paginación")
public class PagedResponse<T> {

    @Schema(description = "Lista de elementos en la página actual")
    private List<T> content;

    @Schema(description = "Metadatos de paginación")
    private PageMetadata page;

    /**
     * Constructor por defecto.
     */
    public PagedResponse() {
    }

    /**
     * Constructor con contenido y metadatos.
     * 
     * @param content Lista de elementos
     * @param page Metadatos de paginación
     */
    public PagedResponse(List<T> content, PageMetadata page) {
        this.content = content;
        this.page = page;
    }

    /**
     * Método estático para convertir Spring Page a PagedResponse.
     * 
     * @param page Spring Page a convertir
     * @param <T> Tipo de datos
     * @return PagedResponse con contenido y metadatos
     */
    public static <T> PagedResponse<T> of(Page<T> page) {
        PageMetadata metadata = new PageMetadata(
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isFirst(),
            page.isLast(),
            page.hasNext(),
            page.hasPrevious()
        );
        return new PagedResponse<>(page.getContent(), metadata);
    }

    // Getters y Setters

    /**
     * Obtiene el contenido de la página.
     * @return Lista de elementos
     */
    public List<T> getContent() {
        return content;
    }

    /**
     * Establece el contenido de la página.
     * @param content Lista de elementos
     */
    public void setContent(List<T> content) {
        this.content = content;
    }

    /**
     * Obtiene los metadatos de paginación.
     * @return Metadatos de paginación
     */
    public PageMetadata getPage() {
        return page;
    }

    /**
     * Establece los metadatos de paginación.
     * @param page Metadatos de paginación
     */
    public void setPage(PageMetadata page) {
        this.page = page;
    }

    /**
     * Clase interna estática que contiene los metadatos de paginación.
     */
    @Schema(description = "Metadatos de paginación")
    public static class PageMetadata {

        @Schema(description = "Número de página actual (base 0)", example = "0")
        private int number;

        @Schema(description = "Tamaño de página (número de elementos por página)", example = "20")
        private int size;

        @Schema(description = "Número total de elementos", example = "100")
        private long totalElements;

        @Schema(description = "Número total de páginas", example = "5")
        private int totalPages;

        @Schema(description = "Indica si es la primera página", example = "true")
        private boolean first;

        @Schema(description = "Indica si es la última página", example = "false")
        private boolean last;

        @Schema(description = "Indica si hay página siguiente", example = "true")
        private boolean hasNext;

        @Schema(description = "Indica si hay página anterior", example = "false")
        private boolean hasPrevious;

        /**
         * Constructor por defecto.
         */
        public PageMetadata() {
        }

        /**
         * Constructor completo.
         * 
         * @param number Número de página
         * @param size Tamaño de página
         * @param totalElements Total de elementos
         * @param totalPages Total de páginas
         * @param first Si es primera página
         * @param last Si es última página
         * @param hasNext Si hay página siguiente
         * @param hasPrevious Si hay página anterior
         */
        public PageMetadata(int number, int size, long totalElements, int totalPages,
                           boolean first, boolean last, boolean hasNext, boolean hasPrevious) {
            this.number = number;
            this.size = size;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
            this.first = first;
            this.last = last;
            this.hasNext = hasNext;
            this.hasPrevious = hasPrevious;
        }

        // Getters y Setters

        /**
         * Obtiene el número de página actual.
         * @return Número de página (base 0)
         */
        public int getNumber() {
            return number;
        }

        /**
         * Establece el número de página actual.
         * @param number Número de página
         */
        public void setNumber(int number) {
            this.number = number;
        }

        /**
         * Obtiene el tamaño de página.
         * @return Número de elementos por página
         */
        public int getSize() {
            return size;
        }

        /**
         * Establece el tamaño de página.
         * @param size Número de elementos por página
         */
        public void setSize(int size) {
            this.size = size;
        }

        /**
         * Obtiene el total de elementos.
         * @return Número total de elementos
         */
        public long getTotalElements() {
            return totalElements;
        }

        /**
         * Establece el total de elementos.
         * @param totalElements Número total de elementos
         */
        public void setTotalElements(long totalElements) {
            this.totalElements = totalElements;
        }

        /**
         * Obtiene el total de páginas.
         * @return Número total de páginas
         */
        public int getTotalPages() {
            return totalPages;
        }

        /**
         * Establece el total de páginas.
         * @param totalPages Número total de páginas
         */
        public void setTotalPages(int totalPages) {
            this.totalPages = totalPages;
        }

        /**
         * Verifica si es la primera página.
         * @return true si es la primera página
         */
        public boolean isFirst() {
            return first;
        }

        /**
         * Establece si es la primera página.
         * @param first true si es la primera página
         */
        public void setFirst(boolean first) {
            this.first = first;
        }

        /**
         * Verifica si es la última página.
         * @return true si es la última página
         */
        public boolean isLast() {
            return last;
        }

        /**
         * Establece si es la última página.
         * @param last true si es la última página
         */
        public void setLast(boolean last) {
            this.last = last;
        }

        /**
         * Verifica si hay página siguiente.
         * @return true si hay página siguiente
         */
        public boolean isHasNext() {
            return hasNext;
        }

        /**
         * Establece si hay página siguiente.
         * @param hasNext true si hay página siguiente
         */
        public void setHasNext(boolean hasNext) {
            this.hasNext = hasNext;
        }

        /**
         * Verifica si hay página anterior.
         * @return true si hay página anterior
         */
        public boolean isHasPrevious() {
            return hasPrevious;
        }

        /**
         * Establece si hay página anterior.
         * @param hasPrevious true si hay página anterior
         */
        public void setHasPrevious(boolean hasPrevious) {
            this.hasPrevious = hasPrevious;
        }

        @Override
        public String toString() {
            return "PageMetadata{" +
                    "number=" + number +
                    ", size=" + size +
                    ", totalElements=" + totalElements +
                    ", totalPages=" + totalPages +
                    ", first=" + first +
                    ", last=" + last +
                    ", hasNext=" + hasNext +
                    ", hasPrevious=" + hasPrevious +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "PagedResponse{" +
                "content=" + content +
                ", page=" + page +
                '}';
    }
}