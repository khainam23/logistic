package org.logistic.evalution;

public class FunctionEval {
    public static Object feval(String fname, Object soln, Object Inp, Object Tar, Object test_dat, Object test_tar) {
        Object out;
        if (fname.equals("objfun")) {
            out = Objfun.objfun(soln, Inp, Tar, test_dat, test_tar);
        } else {
            out = null;
        }
        return out;
    }
}
