package com.kmkbe.helpers.utils;

import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.exception.BusinessException;
import org.springframework.http.HttpStatus;

import java.time.ZoneId;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

public final class ListPagination {
  private ListPagination() {}

  public static <T> PaginationResult<T> of(List<T> source, PaginationRequest request,
      Map<String, Function<T, ? extends Comparable<?>>> fields, String defaultSort) {
    String sortBy = blank(request.getSortBy()) ? defaultSort : request.getSortBy().trim();
    Function<T, ? extends Comparable<?>> sortField = field(fields, sortBy, "sortBy");
    Function<T, ? extends Comparable<?>> tieBreaker = field(fields, defaultSort, "sortBy");
    boolean descending = "desc".equalsIgnoreCase(request.getSortType());
    if (!blank(request.getSortType()) && !descending && !"asc".equalsIgnoreCase(request.getSortType())) {
      throw badRequest("sortType harus asc atau desc.");
    }

    List<T> data = new ArrayList<>(source);
    if (!blank(request.getSearchValue())) {
      Function<T, ? extends Comparable<?>> searchField = field(fields, request.getSearchBy(), "searchBy");
      String needle = request.getSearchValue().trim().toLowerCase(Locale.ROOT);
      data.removeIf(item -> !matches(searchField.apply(item), needle));
    }

    Comparator<T> comparator = (left, right) -> {
      int primary = compare(sortField.apply(left), sortField.apply(right), descending);
      return primary != 0 || sortBy.equals(defaultSort) ? primary :
        compare(tieBreaker.apply(left), tieBreaker.apply(right), false);
    };
    data.sort(comparator);

    int pageNo = request.getPageNo() == null ? 1 : request.getPageNo();
    int pageSize = request.getPageSize() == null ? 10 : request.getPageSize();
    if (pageNo < 1 || pageSize < 1) throw badRequest("pageNo dan pageSize harus minimal 1.");
    int totalPages = (int) ((data.size() + (long) pageSize - 1) / pageSize);
    long start = (long) (pageNo - 1) * pageSize;
    List<T> page = start >= data.size() ? List.of() :
      new ArrayList<>(data.subList((int) start, (int) Math.min(start + pageSize, data.size())));
    return PaginationResult.<T>builder()
      .currentPage(pageNo).totalPage(totalPages).totalData((long) data.size()).list(page).build();
  }

  private static <T> Function<T, ? extends Comparable<?>> field(
      Map<String, Function<T, ? extends Comparable<?>>> fields, String name, String parameter) {
    Function<T, ? extends Comparable<?>> result = name == null ? null : fields.get(name.trim());
    if (result == null) {
      throw badRequest(parameter + " tidak didukung: " + name + ". Pilihan: " + String.join(", ", fields.keySet()));
    }
    return result;
  }

  private static boolean matches(Object value, String needle) {
    if (value == null) return false;
    String text = value.toString().toLowerCase(Locale.ROOT);
    if (text.contains(needle)) return true;
    if (value instanceof Date date) {
      return DateTimeFormatter.ofPattern("dd/MM/yyyy")
        .format(date.toInstant().atZone(ZoneId.of("Asia/Jakarta"))).contains(needle);
    }
    if (value instanceof LocalDate date) {
      return date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")).contains(needle)
        || date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).contains(needle);
    }
    if (value instanceof LocalDateTime dateTime) {
      return dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).contains(needle);
    }
    return false;
  }

  private static boolean blank(String value) { return value == null || value.isBlank(); }

  @SuppressWarnings("unchecked")
  private static int compare(Comparable<?> left, Comparable<?> right, boolean descending) {
    if (left == null) return right == null ? 0 : 1;
    if (right == null) return -1;
    int result = ((Comparable<Object>) left).compareTo(right);
    return descending ? -result : result;
  }

  private static BusinessException badRequest(String message) {
    return new BusinessException(HttpStatus.BAD_REQUEST, 400, message);
  }
}
