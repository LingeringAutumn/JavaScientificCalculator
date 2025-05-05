package calculator;

import net.objecthunter.exp4j.function.Function;

public class CustomFunctions {

    /**
     * Custom cube root function for exp4j
     */
    public static class CbrtFunction extends Function {
        public CbrtFunction() {
            super("cbrt", 1);
        }

        @Override
        public double apply(double... args) {
            return Math.cbrt(args[0]);
        }
    }
}