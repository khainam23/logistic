package org.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@Getter
public class Route {
    List<Pair<Integer, Location>> indLoc; // Vị trí của location trong tập lớn

    public void print() {
        indLoc.forEach(pair -> System.out.print(pair.getKey() + " "));
        System.out.println();
    }

    public int size() {
        return indLoc.size();
    }

    public Pair<Integer, Location> get(int i) {
        if (i >= indLoc.size() || i < 0) return null;
        return indLoc.get(i);
    }

    public void add(Pair<Integer, Location> value) {
        indLoc.add(value);
    }

    public Pair<Integer, Location> remove(int i) {
        return indLoc.remove(i);
    }

    public void set(int i, Pair<Integer, Location> value) {
        if(i >= indLoc.size())
            indLoc.add(value);
        else
            indLoc.set(i, value);
    }

    public int indexOfInd(Integer index) {
        int ind = -1;
        for (int i = 0; i < indLoc.size(); i++) {
            if (index.equals(indLoc.get(i).getKey()))
                ind = i;
        }
        return ind;
    }

    public boolean contains(Pair<Integer, Location> value) {
        return indLoc.contains(value);
    }

    public Route clone() {
        List<Pair<Integer, Location>> cloneIndLoc = new ArrayList<>(this.indLoc);
        return Route.builder().indLoc(cloneIndLoc).build();
    }
}
