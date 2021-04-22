package com.github.peshkovm.diagram.pojos;

import com.github.peshkovm.diagram.converters.RootMxCellConverter;
import lombok.Data;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.convert.Convert;

@Data
@Root
@Convert(RootMxCellConverter.class)
public class RootMxCell {

  @Attribute private static final int id = 0;

  public static int getId() {
    return id;
  }
}
