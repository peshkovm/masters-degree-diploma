package com.github.peshkovm.common;

import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
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
   * @param <MapType> handler's type
   * @return new mapper
   */
  public static <SuperType, MapType> MapperBuilder<SuperType, MapType> map() {
    return new MapperBuilder<>();
  }

  /**
   * Mapper for clazz and corresponding handler.
   */
  public static final class Mapper<SuperType, MapType> {

    private final Map<Class, Function<? extends SuperType, MapType>> cases;

    private Mapper(Map<Class, Function<? extends SuperType, MapType>> cases) {
      this.cases = cases;
    }

    /** Maps clazz to corresponding handler's function return type */
    @SuppressWarnings({"SuspiciousMethodCalls", "unchecked"})
    public <T extends SuperType> MapType apply(T value) {
      Function<T, MapType> mapper =
          (Function<T, MapType>) cases.getOrElse(value.getClass(), null);
      if (mapper == null) {
        throw new IllegalArgumentException("Can't find mapper for: " + value.getClass());
      }
      return mapper.apply(value);
    }
  }

  /**
   * Builder for clazz-handler pairs
   *
   * @param <SuperType> type to map
   * @param <MapType> handler's function return type
   */
  public static final class MapperBuilder<SuperType, MapType> {

    private Map<Class, Function<? extends SuperType, MapType>> map = HashMap.empty();

    /**
     * Registers handler for specified type
     *
     * @param type class to handle
     * @param handler handler's
     * @param <Type> type to handle
     * @return new MapperBuilder
     */
    public <Type extends SuperType> MapperBuilder<SuperType, MapType> when(
        Class<Type> type, Function<Type, MapType> handler) {
      map = map.put(type, handler);
      return this;
    }

    /**
     * Builds {@link Mapper} from registered handlers.
     *
     * @return new Mapper
     */
    public Mapper<SuperType, MapType> build() {
      return new Mapper<>(map);
    }
  }
}
