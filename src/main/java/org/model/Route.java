package org.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@Getter
public class Route {
    List<Integer> indLoc; // Vị trí của location trong tập lớn

    public void print() {
        indLoc.forEach(i -> System.out.print(i + " "));
        System.out.println();
    }

    public int size() {
        return indLoc.size();
    }

    public Integer get(int i) {
        if (i >= indLoc.size() || i < 0) return null;
        return indLoc.get(i);
    }

    public void add(Integer value) {
        indLoc.add(value);
    }

    public Integer remove(int i) {
        return indLoc.remove(i);
    }

    public void set(int i, Integer value) {
        indLoc.set(i, value);
    }

    public int indexOf(Integer value) {
       try {
           return indLoc.indexOf(value);
       } catch (IndexOutOfBoundsException e) {
           return -1;
       }
    }
}
