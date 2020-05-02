package com.github.hornta.commando;

import java.io.File;
import java.util.*;

public class Util {
  private static Map<Class<?>, Set<Class<?>>> memoizedInterfaces = new HashMap<>();

  public static Set<Class<?>> getSuperInterfaces(Class<?> clazz) {
    if (memoizedInterfaces.containsKey(clazz)) {
      return memoizedInterfaces.get(clazz);
    }

    Queue<Class<?>> queue = new LinkedList<Class<?>>();
    Set<Class<?>> types = new HashSet<>();
    queue.add(clazz);
    types.add(clazz);

    while (!queue.isEmpty()) {
      Class<?> curr = queue.poll();
      Class<?>[] supers = curr.getInterfaces();
      for (Class<?> next : supers) {
        if (next != null && !types.contains(next)) {
          types.add(next);
          queue.add(next);
        }
      }
      Class<?> next = curr.getSuperclass();
      if (next != null && !types.contains(next)) {
        queue.add(next);
        types.add(next);
      }
    }

    memoizedInterfaces.put(clazz, types);

    return types;
  }

  public static String getFilenameWithoutExtension(File file) {
    String filename = file.getName();
    int lastDotIndex = filename.lastIndexOf('.');

    if(lastDotIndex == -1) {
      return filename;
    }

    return filename.substring(0, lastDotIndex);
  }
}

