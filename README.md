# Scientific Calculator

基于 Java Swing 的科学计算器项目。

## 一、项目地址

https://github.com/LingeringAutumn/JavaScientificCalculator

## 二、项目结构

```
calculator/
├── CalculatorApp.java // 程序入口
├── ScientificCalculator.java // 主界面与核心逻辑
├── CustomFunctions.java // 自定义函数 (如 cbrt)
```


## 三、依赖安装

使用 [exp4j](https://www.objecthunter.net/exp4j/) 解析表达式：

1. 下载 `exp4j-0.4.8.jar`
2. 放入 `lib/` 文件夹中或添加到项目依赖

## 四、基本功能

- 四则运算、括号、小数点
- 平方、立方、开方、对数、指数
- 三角函数（支持角度/弧度切换）
- 内存功能：MC / MR / MS / M+ / M-
- 复制、粘贴、清空等快捷操作

## 五、如何运行

```bash
javac -cp lib/exp4j-0.4.8.jar calculator/*.java
java -cp .;lib/exp4j-0.4.8.jar calculator.CalculatorApp
```