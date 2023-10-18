package io.whitefox.core.types;

public class LongType extends BasePrimitiveType {
  public static final LongType LONG = new LongType();

  private LongType() {
    super("long");
  }
}
