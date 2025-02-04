package org.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PROTECTED)
@Getter
public abstract class Vehicle {
    int[] route;
    int capacity;
}
