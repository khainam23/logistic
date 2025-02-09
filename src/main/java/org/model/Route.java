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
    double score; // Giá trị của route này
    static int ind = 1; // Dùng cho việc in ra

    public void print() {
        System.out.printf("Route %d, score=%s: ", ind++, score);
        indLoc.forEach(i -> System.out.print(i + " "));
        System.out.println();
    }
}
