package calculator;

import net.objecthunter.exp4j.function.Function;

// 自定义函数：三次根号，用在 exp4j 里
public class CustomFunctions {

    // 自定义 cbrt 函数，支持 cbrt(x) 的写法
    public static class CbrtFunction extends Function {
        public CbrtFunction() {
            super("cbrt", 1); // 函数名是 cbrt，接受 1 个参数
        }

        @Override
        public double apply(double... args) {
            return Math.cbrt(args[0]); // 直接用 Java 自带的三次根函数
        }
    }
}
