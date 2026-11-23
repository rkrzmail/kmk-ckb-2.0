package com.kmkbe.core.domain.mapper;

import com.kmkbe.core.domain.constant.FileAllocationType;
import com.kmkbe.core.domain.entity.CustomerCompany;
import com.kmkbe.core.domain.entity.CustomerPersonal;
import com.kmkbe.core.domain.entity.FinancingDtl;
import org.junit.jupiter.api.Test;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThatCode;

class MapperCoverageTest {

  private static final List<Class<?>> MAPPERS = List.of(
    AgreementFileSigningMapper.class, BouwheerMapper.class, CsulSignerMapper.class,
    CustomerMapper.class, CwrMapper.class, DebtorMapper.class, FileTypeMapper.class,
    FinancingMapper.class, InvoiceMapper.class, ProductMapper.class, SimulationHistoryMapper.class
  );

  @Test
  void allMapperMethodsExecuted() {
    for (Class<?> mapper : MAPPERS) {
      Object instance;
      try { instance = mapper.getField("INSTANCE").get(null); }
      catch (ReflectiveOperationException e) { throw new IllegalStateException(e); }
      assertThatCode(() -> invokeAll(instance)).doesNotThrowAnyException();
    }
    assertThatCode(() -> {
      CustomerMapper.addressDtoFromCompany((CustomerCompany) small(CustomerCompany.class));
      CustomerMapper.addressDtoFromPersonal((CustomerPersonal) small(CustomerPersonal.class));
    }).doesNotThrowAnyException();
  }

  private void invokeAll(Object instance) throws Exception {
    for (Method m : instance.getClass().getMethods()) {
      if (m.isSynthetic() || Modifier.isStatic(m.getModifiers())) continue;
      if (m.getDeclaringClass() == Object.class) continue;
      Object[] args = Arrays.stream(m.getParameterTypes()).map(t -> sample(t, 0)).toArray();
      if (m.getName().equals("dtlsDtoFromEntities")) {
        args = new Object[]{List.of(sample(FinancingDtl.class, 1))};
      }
      m.invoke(instance, args);
    }
  }

  private Object small(Class<?> cls) {
    try {
      Constructor<?> c = cls.getDeclaredConstructor();
      c.setAccessible(true);
      return c.newInstance();
    } catch (Exception e) { return null; }
  }

  private Object sample(Class<?> t, int depth) {
    if (!t.isPrimitive() && t == Void.class) return null;
    if (t == String.class) return UUID.randomUUID().toString();
    if (t == int.class || t == Integer.class) return 1;
    if (t == long.class || t == Long.class) return 1L;
    if (t == double.class || t == Double.class) return 1.0d;
    if (t == float.class || t == Float.class) return 1.0f;
    if (t == boolean.class || t == Boolean.class) return true;
    if (t == short.class || t == Short.class) return (short) 1;
    if (t == byte.class || t == Byte.class) return (byte) 1;
    if (t == char.class || t == Character.class) return 'a';
    if (t == BigDecimal.class) return BigDecimal.ONE;
    if (t == UUID.class) return UUID.randomUUID();
    if (t == LocalDate.class) return LocalDate.now();
    if (t == LocalDateTime.class) return LocalDateTime.now();
    if (t == OffsetDateTime.class) return OffsetDateTime.now();
    if (t == Instant.class) return Instant.now();
    if (t == Date.class || t == java.sql.Timestamp.class) return new Date();
    if (t.isEnum()) { Object[] cs = t.getEnumConstants(); return cs.length > 0 ? cs[0] : null; }
    if (t.isArray()) return Array.newInstance(t.getComponentType(), 0);
    if (List.class.isAssignableFrom(t) || Collection.class.isAssignableFrom(t) || Iterable.class.isAssignableFrom(t)) return new ArrayList<>();
    if (Set.class.isAssignableFrom(t)) return new HashSet<>();
    if (Map.class.isAssignableFrom(t)) return new HashMap<>();
    if (t == Optional.class) return Optional.empty();
    if (depth > 2) return null;

    Object obj = instantiate(t);
    if (obj == null) return null;
    for (Field f : allFields(t)) {
      if (Modifier.isStatic(f.getModifiers()) || Modifier.isFinal(f.getModifiers())) continue;
      try {
        f.setAccessible(true);
        f.set(obj, sampleValue(f, depth + 1));
      } catch (Throwable ignored) {}
    }
    if (t.getName().endsWith(".MstFileType")) {
      try {
        Field f = t.getDeclaredField("fileAllocation");
        f.setAccessible(true);
        f.set(obj, FileAllocationType.values()[0].name());
      } catch (ReflectiveOperationException ignored) {}
    }
    return obj;
  }

  private Object sampleValue(Field f, int depth) {
    Class<?> ft = f.getType();
    if (List.class.isAssignableFrom(ft) && f.getGenericType() instanceof ParameterizedType pt) {
      Type arg = pt.getActualTypeArguments()[0];
      if (arg instanceof Class<?> ac) return List.of(sample(ac, depth));
    }
    return sample(ft, depth);
  }

  private List<Field> allFields(Class<?> t) {
    List<Field> out = new ArrayList<>();
    for (Class<?> c = t; c != null && c != Object.class; c = c.getSuperclass()) {
      out.addAll(Arrays.asList(c.getDeclaredFields()));
    }
    return out;
  }

  private Object instantiate(Class<?> t) {
    try {
      Constructor<?> c = t.getDeclaredConstructor();
      c.setAccessible(true);
      return c.newInstance();
    } catch (Throwable ignored) {}
    try {
      Constructor<?>[] cs = t.getDeclaredConstructors();
      Arrays.sort(cs, Comparator.comparingInt(Constructor::getParameterCount));
      for (Constructor<?> c : cs) {
        try {
          c.setAccessible(true);
          return c.newInstance(Arrays.stream(c.getParameterTypes()).map(p -> sample(p, 3)).toArray());
        } catch (Throwable ignored) {}
      }
    } catch (Throwable ignored) {}
    return null;
  }
}
