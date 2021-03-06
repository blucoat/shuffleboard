package edu.wpi.first.shuffleboard.api.data.types;

import edu.wpi.first.shuffleboard.api.data.SimpleDataType;

/**
 * Represents the type of "null" or non-present data. Differs from {@link UnknownType} in that <i>no</i> data is present
 * for this type, while data <i>is</i> present but of an unknown type for the latter.
 */
public class NoneType extends SimpleDataType {

  public NoneType() {
    super("None", null);
  }

  @Override
  public Object getDefaultValue() {
    return null;
  }

}
