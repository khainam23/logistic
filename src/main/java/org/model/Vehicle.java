package org.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PROTECTED)
@Data
public abstract class Vehicle {
    Route route;
    int currentLoad;
    int capacity;

    public Route cloneRoute() {
        return route.clone();
    }
}
