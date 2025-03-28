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

    public Location(Point point, int serviceTime) {
        this.point = point;
        this.serviceTimePick = serviceTime;
        this.serviceTimeDeliver = serviceTime;
    }

    public boolean isPick() {
        return isPick;
    }

    public boolean isDeliver() {
        return isDeliver;
    }

    public int getServiceTimePick() {
        return serviceTimePick;
    }

    public int getServiceTimeDeliver() {
        return serviceTimeDeliver;
    }

    public int getLtw() {
        return ltw;
    }

    public int getUtw() {
        return utw;
    }

    public int getDemandPick() {
        return demandPick;
    }

    public int getDemandDeliver() {
        return demandDeliver;
    }
}
