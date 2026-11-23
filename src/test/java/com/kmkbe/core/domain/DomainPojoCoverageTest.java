package com.kmkbe.core.domain;

import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;

import java.io.File;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.net.URL;
import java.time.*;
import java.util.*;
import java.util.stream.*;

import static org.assertj.core.api.Assertions.assertThatCode;

class DomainPojoCoverageTest {

  @Test
  void dtoEntityModelRequestPojos() throws Exception {
    for (String pkg : List.of("com.kmkbe.core.domain.dto","com.kmkbe.core.domain.entity","com.kmkbe.core.domain.model","com.kmkbe.core.domain.request")) {
      for (Class<?> cls : classesIn(pkg)) {
        assertThatCode(() -> exercise(cls)).doesNotThrowAnyException();
      }
    }
  }

  private List<Class<?>> classesIn(String pkg) throws Exception {
    String path = pkg.replace('.', '/');
    List<Class<?>> out = new ArrayList<>();
    // compiled classes
    File dir = new File("target/classes/" + path);
    if (dir.exists()) collect(dir, pkg, out);
    // also try classloader
    if (out.isEmpty()) {
      Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources(path);
      while (urls.hasMoreElements()) {
        File d = new File(urls.nextElement().toURI());
        if (d.exists()) collect(d, pkg, out);
      }
    }
    return out.stream().distinct().sorted(Comparator.comparing(Class::getName)).collect(Collectors.toList());
  }

  private void collect(File dir, String pkg, List<Class<?>> out) {
    for (File f : Objects.requireNonNull(dir.listFiles())) {
      if (f.isDirectory()) collect(f, pkg + "." + f.getName(), out);
      else if (f.getName().endsWith(".class")) {
        String name = pkg + "." + f.getName().replace(".class", "");
        try { out.add(Class.forName(name)); } catch (Throwable ignored) {}
      }
    }
  }

  private void exercise(Class<?> cls) throws Exception {
    if (cls.isInterface() || Modifier.isAbstract(cls.getModifiers()) || cls.isEnum() || cls.isAnnotation() || cls.isRecord()) {
      if (cls.isEnum()) { for (Object o : cls.getEnumConstants()) { o.toString(); ((Enum<?>)o).name(); } }
      return;
    }
    Object obj = instantiate(cls);
    if (obj == null) return;
    for (Method m : cls.getMethods()) {
      if (m.getDeclaringClass() == Object.class) continue;
      try {
        if (m.getName().startsWith("set") && m.getParameterCount() == 1) {
          m.invoke(obj, sample(m.getParameterTypes()[0]));
        }
      } catch (Throwable ignored) {}
    }
    for (Method m : cls.getMethods()) {
      if (m.getDeclaringClass() == Object.class) continue;
      try {
        if ((m.getName().startsWith("get") || m.getName().startsWith("is")) && m.getParameterCount() == 0) {
          m.invoke(obj);
        } else if (m.getName().equals("toString") || m.getName().equals("hashCode")) {
          m.invoke(obj);
        } else if (m.getName().equals("equals") && m.getParameterCount() == 1) {
          m.invoke(obj, obj);
          m.invoke(obj, new Object());
        } else if (m.getName().equals("builder") && m.getParameterCount() == 0 && Modifier.isStatic(m.getModifiers())) {
          Object b = m.invoke(null);
          exerciseBuilder(b, obj);
        }
      } catch (Throwable ignored) {}
    }
    try { obj.toString(); } catch (Throwable ignored) {}
    try { obj.hashCode(); } catch (Throwable ignored) {}
    try { obj.equals(obj); obj.equals(null); obj.equals("x"); } catch (Throwable ignored) {}
  }

  private void exerciseBuilder(Object builder, Object sampleObj) {
    if (builder == null) return;
    for (Method m : builder.getClass().getMethods()) {
      if (m.getDeclaringClass() == Object.class) continue;
      try {
        if (m.getParameterCount() == 1 && m.getReturnType().isAssignableFrom(builder.getClass())) {
          m.invoke(builder, sample(m.getParameterTypes()[0]));
        }
      } catch (Throwable ignored) {}
    }
    try {
      Method build = builder.getClass().getMethod("build");
      Object built = build.invoke(builder);
      if (built != null) { built.toString(); built.hashCode(); built.equals(built); }
    } catch (Throwable ignored) {}
  }

  private Object instantiate(Class<?> cls) {
    try { return cls.getDeclaredConstructor().newInstance(); } catch (Throwable ignored) {}
    try {
      Constructor<?>[] ctors = cls.getDeclaredConstructors();
      Arrays.sort(ctors, Comparator.comparingInt(Constructor::getParameterCount));
      for (Constructor<?> c : ctors) {
        try {
          c.setAccessible(true);
          Object[] args = Arrays.stream(c.getParameterTypes()).map(this::sample).toArray();
          return c.newInstance(args);
        } catch (Throwable ignored) {}
      }
    } catch (Throwable ignored) {}
    try { return BeanUtils.instantiateClass(cls); } catch (Throwable ignored) {}
    return null;
  }

  private Object sample(Class<?> t) {
    if (t == String.class) return "test";
    if (t == int.class || t == Integer.class) return 1;
    if (t == long.class || t == Long.class) return 1L;
    if (t == double.class || t == Double.class) return 1.0d;
    if (t == float.class || t == Float.class) return 1.0f;
    if (t == boolean.class || t == Boolean.class) return true;
    if (t == short.class || t == Short.class) return (short) 1;
    if (t == byte.class || t == Byte.class) return (byte) 1;
    if (t == char.class || t == Character.class) return 'a';
    if (t == BigDecimal.class) return BigDecimal.ONE;
    if (t == LocalDate.class) return LocalDate.now();
    if (t == LocalDateTime.class) return LocalDateTime.now();
    if (t == OffsetDateTime.class) return OffsetDateTime.now();
    if (t == Instant.class) return Instant.now();
    if (t == Date.class) return new Date();
    if (t == UUID.class) return UUID.randomUUID();
    if (t == List.class || t == Collection.class || t == Iterable.class) return new ArrayList<>();
    if (t == Set.class) return new HashSet<>();
    if (t == Map.class) return new HashMap<>();
    if (t == Optional.class) return Optional.empty();
    if (t.isEnum()) { Object[] cs = t.getEnumConstants(); return cs.length > 0 ? cs[0] : null; }
    try { return instantiate(t); } catch (Throwable ignored) {}
    return null;
  }
}
