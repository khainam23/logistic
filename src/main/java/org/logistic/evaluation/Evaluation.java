package org.logistic.evaluation;

import java.util.Arrays;


// Đánh giá hiệu quả của mô hình dựa trên các chỉ số đo lường
public class Evaluation {
    public static double[] evaln(int[][] sp, int[] act) {
        int numActs = act.length;
        double[] Tp = new double[numActs];
        double[] Fp = new double[numActs];
        double[] Tn = new double[numActs];
        double[] Fn = new double[numActs];

        for (int i = 0; i < numActs; i++) {
            int[] p = sp[i];
            int a = act[i];
            int tp = 0, tn = 0, fp = 0, fn = 0;

            for (int j = 0; j < p.length; j++) {
                if (a == 1 && p[j] == 1) {
                    tp++;
                } else if (a == 0 && p[j] == 0) {
                    tn++;
                } else if (a == 0 && p[j] == 1) {
                    fp++;
                } else if (a == 1 && p[j] == 0) {
                    fn++;
                }
            }
            Tp[i] = tp;
            Fp[i] = fp;
            Tn[i] = tn;
            Fn[i] = fn;
        }

        double tp = Arrays.stream(Tp).sum();
        double fp = Arrays.stream(Fp).sum();
        double tn = Arrays.stream(Tn).sum();
        double fn = Arrays.stream(Fn).sum();

        double accuracy = (tp + tn) / (tp + tn + fp + fn);
        double sensitivity = tp / (tp + fn);
        double specificity = tn / (tn + fp);
        double precision = tp / (tp + fp);
        double FPR = fp / (fp + tn);
        double FNR = fn / (tp + fn);
        double NPV = tn / (tn + fp);
        double FDR = fp / (tp + fp);
        double F1_score = (2 * tp) / (2 * tp + fp + fn);
        double MCC = ((tp * tn) - (fp * fn)) / Math.sqrt((tp + fp) * (tp + fn) * (tn + fp) * (tn + fn));

        return new double[]{tp, tn, fp, fn, accuracy, sensitivity, specificity, precision, FPR, FNR, NPV, FDR, F1_score, MCC};
    }
}
