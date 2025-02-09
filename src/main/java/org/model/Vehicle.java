package org.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.List;

@FieldDefaults(level = AccessLevel.PROTECTED)
@Getter
public abstract class Vehicle {
    List<Route> routes;
    double fitness;
    int capacity;

    public Vehicle(List<Route> routes, double fitness) {
        this.routes = routes;
        this.fitness = fitness;
    }
}
