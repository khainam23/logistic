package org.logistic.model;

import lombok.*;

@Builder
@AllArgsConstructor
@Getter
@Setter
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
        return serviceTimeDeliver;
    }

    public int distance(Location oLocation) {
        return this.point.distanceTo(oLocation.point);
    }
}
