package org.logistic.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Builder
@AllArgsConstructor
@Getter
@Data
public class Location {
    Point point;
    int serviceTimePick;
    int serviceTimeDeliver;
    int demandPick;
    int demandDeliver;
    int ltw; // low time window
    int utw; // upper time window
    boolean isPick;
    boolean isDeliver;

    public int totalServiceTime() {
        return serviceTimeDeliver + serviceTimeDeliver;
    }

    public int distance(Location oLocation) {
        return this.point.distanceTo(oLocation.point);
    }
}
