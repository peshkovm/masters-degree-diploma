package com.github.peshkovm.diagram;

import lombok.Data;

@Data
public class DiagramHelperSingleton {
  private static volatile DiagramHelperSingleton instance;

  private DiagramHelperSingleton() {}

  public static DiagramHelperSingleton getInstance() throws Exception {
    if (instance != null) {
      return instance;
    }
    synchronized (DiagramHelperSingleton.class) {
      if (instance == null) {
        instance = new DiagramHelperSingleton();
      }
      return instance;
    }
  }
}
