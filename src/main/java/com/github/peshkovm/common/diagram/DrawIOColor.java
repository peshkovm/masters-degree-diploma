package com.github.peshkovm.common.diagram;

public enum DrawIOColor {
  ORANGE("#FFE6CC", "#D79B00"),
  BLUE("#DAE8FC", "#6C8EBF"),
  GREEN("#D5E8D4", "#82B366"),
  MAGENTA("#E1D5E7", "#9673A6"),
  YELLOW("#FFF2CC", "#D6B656"),
  RED("#F8CECC", "#B85450");

  public final String fillColor;
  public final String strokeColor;

  DrawIOColor(String fillColor, String strokeColor) {
    this.fillColor = fillColor;
    this.strokeColor = strokeColor;
  }
}
