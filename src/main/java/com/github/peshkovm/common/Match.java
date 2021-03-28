package com.github.peshkovm.common;

import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Registers {@link Class clazz}-{@link Function handler} pairs and maps clazz to value, returned by
 * corresponding handler.
 */
public final class Match {

  /**
   * Returns {@link MapperBuilder mapper} to register handlers.
   *
   * @param <SuperType> type to handle
   * @return new mapper
   */
  public static <SuperType> MapperBuilder<SuperType> map() {
    return new MapperBuilder<>();
  }

  /**
   * Mapper for clazz and corresponding handler.
   */
  public static final class Mapper<SuperType> {

    private final Map<Class, Consumer<? extends SuperType>> cases;

    private Mapper(Map<Class, Consumer<? extends SuperType>> cases) {
      this.cases = cases;
    }

    /** Maps clazz to corresponding handler's function return type */
    @SuppressWarnings({"SuspiciousMethodCalls", "unchecked"})
    public <T extends SuperType> void apply(T value) {
      Consumer<T> mapper = (Consumer<T>) cases.getOrElse(value.getClass(), null);
      if (mapper == null) {
        throw new IllegalArgumentException("Can't find mapper for: " + value.getClass());
      }
      mapper.accept(value);
    }
  }

  /**
   * Builder for clazz-handler pairs
   *
   * @param <SuperType> type to map
   */
  public static final class MapperBuilder<SuperType> {

    private Map<Class, Consumer<? extends SuperType>> map = HashMap.empty();

    /**
     * Registers handler for specified type
     *
     * @param type class to handle
     * @param handler handler's
     * @param <Type> type to handle
     * @return new MapperBuilder
     */
    public <Type extends SuperType> MapperBuilder<SuperType> when(
        Class<Type> type, Consumer<Type> handler) {
      map = map.put(type, handler);
      return this;
    }

    /**
     * Builds {@link Mapper} from registered handlers.
     *
     * @return new Mapper
     */
    public Mapper<SuperType> build() {
      return new Mapper<>(map);
    }
  }
}
