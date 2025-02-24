package org.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PROTECTED)
@Data
@Builder
public class Vehicle {
    Route route;
    int currentLoad;
    int capacity;
    int currentTime;

    public Route cloneRoute() {
        return route.clone();
    }
}
