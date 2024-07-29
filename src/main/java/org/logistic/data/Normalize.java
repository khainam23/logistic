package org.logistic.data;
import org.apache.commons.math3.util.MathArrays;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Normalize {
    public static int[] findString(String[] l1, String s) {
        List<Integer> matchedIndexes = new ArrayList<>();
        for (int i = 0; i < l1.length; i++) {
            if (s.equals(l1[i])) {
                matchedIndexes.add(i);
            }
        }
        return matchedIndexes.stream().mapToInt(i -> i).toArray();
    }

    public static double[][] normalize(String[][] data) {
        double b = 1.0;
        double a = 0.0;
        double[][] normSam = null;

        for (int k = 0; k < data[0].length; k++) {
            String[] d = new String[data.length];
            for (int i = 0; i < data.length; i++) {
                d[i] = data[i][k];
            }

            int[] ind = findString(d, "Infinity");
            for (int index : ind) {
                d[index] = "100";
            }

            double[] numericData = Arrays.stream(d)
                    .mapToDouble(value -> {
                        if ("Infinity".equals(value)) {
                            return Double.POSITIVE_INFINITY;
                        } else {
                            return Double.parseDouble(value);
                        }
                    })
                    .toArray();

            double[] cleanData = Arrays.stream(numericData)
                    .map(value -> Double.isNaN(value) ? 0 : value)
                    .toArray();

            double mx = Arrays.stream(cleanData).max().orElse(1);
            double mn = Arrays.stream(cleanData).min().orElse(0);

            if (mx == mn) {
                mx = 1;
            }

            double[] dd = new double[cleanData.length];
            for (int i = 0; i < cleanData.length; i++) {
                dd[i] = ((b - a) * ((cleanData[i] - mn) / (mx - mn))) + a;
            }

            if (normSam == null) {
                normSam = new double[dd.length][data[0].length];
                for (int i = 0; i < dd.length; i++) {
                    normSam[i][k] = dd[i];
                }
            } else {
                for (int i = 0; i < dd.length; i++) {
                    normSam[i][k] = dd[i];
                }
            }
        }

        return normSam;
    }

    public static void main(String[] args) {
        String[][] data = {
                {"1", "2", "Infinity"},
                {"4", "5", "6"},
                {"7", "Infinity", "9"}
        };

        double[][] normalizedData = normalize(data);

        for (double[] row : normalizedData) {
            System.out.println(Arrays.toString(row));
        }
    }
}
