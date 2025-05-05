package calculator;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.util.function.Function;

public class ScientificCalculator extends JFrame {

    // 字体设一下，用来支持特殊符号，比如 π、√ 等
    public static final String fontUI = "Segoe UI Symbol";

    private JTextField displayField;                // 显示输入和结果
    private StringBuilder currentExpression = new StringBuilder();  // 当前表达式
    private double memory = 0.0;                    // 存储器里的值
    private boolean startNewInput = false;          // 是否开始新一轮输入（比如按了 =）
    private boolean isRadians = true;               // 默认是弧度模式
    private static final DecimalFormat formatter = new DecimalFormat("#.##########");  // 控制小数位数显示

    // 主面板区域
    private JPanel mainPanel;
    private JPanel displayPanel;
    private JPanel buttonPanel;

    // 菜单栏按钮
    private JMenuItem copyMenuItem;
    private JMenuItem pasteMenuItem;
    private JMenuItem clearMenuItem;
    private JRadioButtonMenuItem radiansMenuItem;
    private JRadioButtonMenuItem degreesMenuItem;

    // 构造函数，初始化窗口和各部分界面
    public ScientificCalculator() {
        setTitle("Scientific Calculator");
        setSize(480, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // 居中显示

        // 主界面面板（上下布局）
        mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(new Color(240, 240, 240));

        setupMenuBar();       // 顶部菜单栏
        setupDisplayPanel();  // 上方显示区域
        setupButtonPanel();   // 中间按钮区域

        add(mainPanel);
    }

    // 构建菜单栏：包括 编辑（复制/粘贴/清空） 和 设置（角度/弧度）
    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // “编辑”菜单
        JMenu editMenu = new JMenu("编辑(E)");
        copyMenuItem = new JMenuItem("复制");
        pasteMenuItem = new JMenuItem("粘贴");
        clearMenuItem = new JMenuItem("清空");

        // 三个菜单项的快捷键和绑定事件
        copyMenuItem.addActionListener(e -> copyToClipboard());
        pasteMenuItem.addActionListener(e -> pasteFromClipboard());
        clearMenuItem.addActionListener(e -> clearDisplay());

        copyMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
        pasteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK));
        clearMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));

        // 添加到菜单
        editMenu.add(copyMenuItem);
        editMenu.add(pasteMenuItem);
        editMenu.addSeparator();
        editMenu.add(clearMenuItem);

        // “设置”菜单：角度/弧度切换
        JMenu viewMenu = new JMenu("设置(S)");
        ButtonGroup angleGroup = new ButtonGroup(); // 互斥按钮组
        radiansMenuItem = new JRadioButtonMenuItem("弧度制", true);
        degreesMenuItem = new JRadioButtonMenuItem("角度制", false);

        // 切换角度/弧度的事件
        radiansMenuItem.addActionListener(e -> isRadians = true);
        degreesMenuItem.addActionListener(e -> isRadians = false);

        angleGroup.add(radiansMenuItem);
        angleGroup.add(degreesMenuItem);
        viewMenu.add(radiansMenuItem);
        viewMenu.add(degreesMenuItem);

        // 把两个菜单加到栏里
        menuBar.add(editMenu);
        menuBar.add(viewMenu);

        setJMenuBar(menuBar);
    }

    // 显示区域的 UI 设置（就是上面那个输入/输出框）
    private void setupDisplayPanel() {
        displayPanel = new JPanel(new BorderLayout());
        displayPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180), 1, true),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        displayPanel.setBackground(Color.WHITE);

        // 文本框用来显示表达式/结果，不可编辑
        displayField = new JTextField();
        displayField.setFont(new Font(fontUI, Font.PLAIN, 24));
        displayField.setHorizontalAlignment(JTextField.RIGHT); // 靠右对齐更像计算器
        displayField.setEditable(false);
        displayField.setBorder(null);
        displayField.setBackground(Color.WHITE);

        displayPanel.add(displayField, BorderLayout.CENTER);
        mainPanel.add(displayPanel, BorderLayout.NORTH); // 顶部加入主面板
    }

    // 主体按钮区域（科学计算器的各种按钮）
    private void setupButtonPanel() {
        buttonPanel = new JPanel(new GridBagLayout()); // 用网格布局方便控制位置
        buttonPanel.setBackground(new Color(240, 240, 240));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(3, 3, 3, 3); // 按钮之间留一点缝

        // row 0: 存储器相关按钮
        gbc.gridy = 0;
        gbc.gridx = 0;
        addButton("MC", e -> memoryClear(), buttonPanel, createMemoryButtonStyle(), gbc);
        gbc.gridx = 1;
        addButton("MR", e -> memoryRecall(), buttonPanel, createMemoryButtonStyle(), gbc);
        gbc.gridx = 2;
        addButton("MS", e -> memoryStore(), buttonPanel, createMemoryButtonStyle(), gbc);
        gbc.gridx = 3;
        addButton("M+", e -> memoryAdd(), buttonPanel, createMemoryButtonStyle(), gbc);
        gbc.gridx = 4;
        addButton("M-", e -> memorySubtract(), buttonPanel, createMemoryButtonStyle(), gbc);

        // row 1: 常见幂运算相关
        gbc.gridy = 1;
        gbc.gridx = 0;
        addButton("x²", e -> applyFunction("^2"), buttonPanel, createFunctionButtonStyle(), gbc);
        gbc.gridx = 1;
        addButton("x³", e -> applyFunction("^3"), buttonPanel, createFunctionButtonStyle(), gbc);
        gbc.gridx = 2;
        addButton("x^y", e -> appendToExpression("^"), buttonPanel, createFunctionButtonStyle(), gbc);
        gbc.gridx = 3;
        addButton("10^x", e -> appendToExpression("10^"), buttonPanel, createFunctionButtonStyle(), gbc);
        gbc.gridx = 4;
        addButton("1/x", e -> calculateReciprocal(), buttonPanel, createFunctionButtonStyle(), gbc);

        // row 2: log、根号
        gbc.gridy = 2;
        gbc.gridx = 0;
        addButton("√", e -> applyFunction("sqrt"), buttonPanel, createFunctionButtonStyle(), gbc);
        gbc.gridx = 1;
        addButton("∛", e -> applyFunction("cbrt"), buttonPanel, createFunctionButtonStyle(), gbc);
        gbc.gridx = 2;
        addButton("log", e -> appendToExpression("log10("), buttonPanel, createFunctionButtonStyle(), gbc);
        gbc.gridx = 3;
        addButton("ln", e -> appendToExpression("log("), buttonPanel, createFunctionButtonStyle(), gbc);
        gbc.gridx = 4;
        addButton("logₙ", e -> appendCustomLogBase(), buttonPanel, createFunctionButtonStyle(), gbc);

        // row 3: 三角函数 + 括号
        gbc.gridy = 3;
        gbc.gridx = 0;
        addButton("sin", e -> appendTrigFunction("sin"), buttonPanel, createFunctionButtonStyle(), gbc);
        gbc.gridx = 1;
        addButton("cos", e -> appendTrigFunction("cos"), buttonPanel, createFunctionButtonStyle(), gbc);
        gbc.gridx = 2;
        addButton("tan", e -> appendTrigFunction("tan"), buttonPanel, createFunctionButtonStyle(), gbc);
        gbc.gridx = 3;
        addButton("(", e -> appendToExpression("("), buttonPanel, createParenthesisButtonStyle(), gbc);
        gbc.gridx = 4;
        addButton(")", e -> appendToExpression(")"), buttonPanel, createParenthesisButtonStyle(), gbc);

        // row 4: 反三角函数 + 清除
        gbc.gridy = 4;
        gbc.gridx = 0;
        addButton("asin", e -> appendTrigFunction("asin"), buttonPanel, createFunctionButtonStyle(), gbc);
        gbc.gridx = 1;
        addButton("acos", e -> appendTrigFunction("acos"), buttonPanel, createFunctionButtonStyle(), gbc);
        gbc.gridx = 2;
        addButton("atan", e -> appendTrigFunction("atan"), buttonPanel, createFunctionButtonStyle(), gbc);
        gbc.gridx = 3;
        addButton("C", e -> clearDisplay(), buttonPanel, createClearButtonStyle(), gbc);
        gbc.gridx = 4;
        addButton("⌫", e -> backspace(), buttonPanel, createClearButtonStyle(), gbc);

        // row 5: 双曲函数 + 百分比
        gbc.gridy = 5;
        gbc.gridx = 0;
        addButton("sinh", e -> appendToExpression("sinh("), buttonPanel, createFunctionButtonStyle(), gbc);
        gbc.gridx = 1;
        addButton("cosh", e -> appendToExpression("cosh("), buttonPanel, createFunctionButtonStyle(), gbc);
        gbc.gridx = 2;
        addButton("tanh", e -> appendToExpression("tanh("), buttonPanel, createFunctionButtonStyle(), gbc);
        gbc.gridx = 3;
        addButton("CE", e -> clearEntry(), buttonPanel, createClearButtonStyle(), gbc);
        gbc.gridx = 4;
        addButton("%", e -> calculatePercentage(), buttonPanel, createOperatorButtonStyle(), gbc);

        // row 6: 数字 7 8 9 π ÷
        gbc.gridy = 6;
        gbc.gridx = 0;
        addButton("7", e -> appendToExpression("7"), buttonPanel, createNumberButtonStyle(), gbc);
        gbc.gridx = 1;
        addButton("8", e -> appendToExpression("8"), buttonPanel, createNumberButtonStyle(), gbc);
        gbc.gridx = 2;
        addButton("9", e -> appendToExpression("9"), buttonPanel, createNumberButtonStyle(), gbc);
        gbc.gridx = 3;
        addButton("π", e -> appendToExpression("pi"), buttonPanel, createNumberButtonStyle(), gbc);
        gbc.gridx = 4;
        addButton("÷", e -> appendToExpression("/"), buttonPanel, createOperatorButtonStyle(), gbc);

        // row 7: 4 5 6 e ×
        gbc.gridy = 7;
        gbc.gridx = 0;
        addButton("4", e -> appendToExpression("4"), buttonPanel, createNumberButtonStyle(), gbc);
        gbc.gridx = 1;
        addButton("5", e -> appendToExpression("5"), buttonPanel, createNumberButtonStyle(), gbc);
        gbc.gridx = 2;
        addButton("6", e -> appendToExpression("6"), buttonPanel, createNumberButtonStyle(), gbc);
        gbc.gridx = 3;
        addButton("e", e -> appendToExpression("e"), buttonPanel, createNumberButtonStyle(), gbc);
        gbc.gridx = 4;
        addButton("×", e -> appendToExpression("*"), buttonPanel, createOperatorButtonStyle(), gbc);

        // row 8: 1 2 3 ± -
        gbc.gridy = 8;
        gbc.gridx = 0;
        addButton("1", e -> appendToExpression("1"), buttonPanel, createNumberButtonStyle(), gbc);
        gbc.gridx = 1;
        addButton("2", e -> appendToExpression("2"), buttonPanel, createNumberButtonStyle(), gbc);
        gbc.gridx = 2;
        addButton("3", e -> appendToExpression("3"), buttonPanel, createNumberButtonStyle(), gbc);
        gbc.gridx = 3;
        addButton("±", e -> toggleSign(), buttonPanel, createOperatorButtonStyle(), gbc);
        gbc.gridx = 4;
        addButton("-", e -> appendToExpression("-"), buttonPanel, createOperatorButtonStyle(), gbc);

        // row 9: 0 . + =
        gbc.gridy = 9;
        gbc.gridx = 0;
        gbc.gridwidth = 2; // “0” 占两个格子
        addButton("0", e -> appendToExpression("0"), buttonPanel, createNumberButtonStyle(), gbc);
        gbc.gridwidth = 1;
        gbc.gridx = 2;
        addButton(".", e -> appendToExpression("."), buttonPanel, createNumberButtonStyle(), gbc);
        gbc.gridx = 3;
        addButton("+", e -> appendToExpression("+"), buttonPanel, createOperatorButtonStyle(), gbc);
        gbc.gridx = 4;
        addButton("=", e -> calculateResult(), buttonPanel, createEqualsButtonStyle(), gbc);

        mainPanel.add(buttonPanel, BorderLayout.CENTER); // 把按钮区加进主面板
    }

    // 创建一个通用按钮（带事件 + 样式）
    private JButton createButton(String text, ActionListener listener, Function<JButton, JButton> styleFunction) {
        JButton button = new JButton(text);
        button.addActionListener(listener);  // 按钮点击时触发事件
        return styleFunction.apply(button); // 应用样式
    }

    // 给某个面板直接添加一个按钮（不带约束布局）
    private void addButton(String text, ActionListener listener, JPanel panel, Function<JButton, JButton> styleFunction) {
        panel.add(createButton(text, listener, styleFunction));
    }

    // 给面板添加按钮（带 GridBagLayout 的位置参数）
    private void addButton(String text, ActionListener listener, JPanel panel, Function<JButton, JButton> styleFunction, GridBagConstraints gbc) {
        panel.add(createButton(text, listener, styleFunction), gbc);
    }

    // 数字按钮样式，比如 1~9、0
    private Function<JButton, JButton> createNumberButtonStyle() {
        return button -> {
            button.setFont(new Font(fontUI, Font.BOLD, 18));
            button.setBackground(new Color(250, 250, 250));
            button.setFocusPainted(false); // 去掉点击边框
            return button;
        };
    }

    // 运算符按钮样式，比如 + - × ÷
    private Function<JButton, JButton> createOperatorButtonStyle() {
        return button -> {
            button.setFont(new Font(fontUI, Font.BOLD, 18));
            button.setBackground(new Color(230, 230, 250)); // 稍微偏蓝
            button.setFocusPainted(false);
            return button;
        };
    }

    // 三角函数、对数等功能按钮样式
    private Function<JButton, JButton> createFunctionButtonStyle() {
        return button -> {
            button.setFont(new Font(fontUI, Font.PLAIN, 16));
            button.setBackground(new Color(220, 240, 250)); // 更淡的蓝色
            button.setFocusPainted(false);
            return button;
        };
    }

    // 清除类按钮样式（比如 C、⌫、CE）
    private Function<JButton, JButton> createClearButtonStyle() {
        return button -> {
            button.setFont(new Font(fontUI, Font.BOLD, 16));
            button.setBackground(new Color(255, 200, 200)); // 淡红色，提示“危险”操作
            button.setFocusPainted(false);
            return button;
        };
    }

    // 括号按钮样式（左括号、右括号）
    private Function<JButton, JButton> createParenthesisButtonStyle() {
        return button -> {
            button.setFont(new Font(fontUI, Font.BOLD, 18));
            button.setBackground(new Color(240, 240, 220)); // 偏黄一些
            button.setFocusPainted(false);
            return button;
        };
    }

    // 存储器相关按钮样式（MC、MR、MS 等）
    private Function<JButton, JButton> createMemoryButtonStyle() {
        return button -> {
            button.setFont(new Font(fontUI, Font.BOLD, 14));
            button.setBackground(new Color(220, 220, 240));
            button.setFocusPainted(false);
            return button;
        };
    }

    // “等于”按钮样式，强调一下它是“主按钮”
    private Function<JButton, JButton> createEqualsButtonStyle() {
        return button -> {
            button.setFont(new Font(fontUI, Font.BOLD, 20));
            button.setBackground(new Color(120, 180, 240)); // 蓝色更深
            button.setForeground(Color.WHITE); // 白字
            button.setFocusPainted(false);
            return button;
        };
    }

    // 通用的添加表达式内容（比如按下数字或运算符）
    private void appendToExpression(String value) {
        if (startNewInput) {
            // 如果是刚算完结果，开始新输入就先清空表达式
            currentExpression = new StringBuilder();
            startNewInput = false;
        }
        currentExpression.append(value); // 把值加进去
        displayField.setText(currentExpression.toString()); // 显示出来
    }

    // 添加三角函数（要区分是弧度制还是角度制）
    private void appendTrigFunction(String function) {
        if (startNewInput) {
            currentExpression = new StringBuilder();
            startNewInput = false;
        }

        if (isRadians) {
            // 默认直接拼 sin(...
            currentExpression.append(function).append("(");
        } else {
            // 如果是角度，就多包一层 toRadians(...)
            currentExpression.append(function).append("(toRadians(");
        }
        displayField.setText(currentExpression.toString());
    }

    // 一些函数按钮（如 √、x²、x³）处理逻辑
    private void applyFunction(String function) {
        if (startNewInput) {
            // 如果是刚刚算完结果，我们把这个结果直接套上函数
            String currentText = displayField.getText();
            currentExpression = new StringBuilder();

            switch (function) {
                case "sqrt":
                    currentExpression.append("sqrt(").append(currentText).append(")");
                    break;
                case "cbrt":
                    currentExpression.append("cbrt(").append(currentText).append(")");
                    break;
                case "exp":
                    currentExpression.append("exp(").append(currentText).append(")");
                    break;
                case "^2":
                    currentExpression.append("(").append(currentText).append(")^2");
                    break;
                case "^3":
                    currentExpression.append("(").append(currentText).append(")^3");
                    break;
            }

            startNewInput = false;
        } else {
            // 如果是在输入中，就直接拼对应函数
            switch (function) {
                case "sqrt":
                    currentExpression.append("sqrt(");
                    break;
                case "cbrt":
                    currentExpression.append("cbrt(");
                    break;
                case "exp":
                    currentExpression.append("exp(");
                    break;
                case "^2":
                    currentExpression.append("^2");
                    break;
                case "^3":
                    currentExpression.append("^3");
                    break;
            }
        }

        displayField.setText(currentExpression.toString());
    }

    // 自定义底数对数 logₙ(x) = log(x)/log(n)
    private void appendCustomLogBase() {
        if (startNewInput) {
            currentExpression = new StringBuilder();
            startNewInput = false;
        }

        // 弹窗询问底数
        String basePrompt = JOptionPane.showInputDialog(this, "Enter logarithm base:", "Logarithm Base", JOptionPane.QUESTION_MESSAGE);
        if (basePrompt != null && !basePrompt.isEmpty()) {
            try {
                double base = Double.parseDouble(basePrompt);
                if (base <= 0 || base == 1) {
                    JOptionPane.showMessageDialog(this, "Base must be a positive number different from 1", "Invalid Base", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // 把表达式模板拼好（先占个 x 位置）
                currentExpression.append("(log(x)/log(").append(base).append("))");

                // 继续弹窗询问 x 的值
                String argPrompt = JOptionPane.showInputDialog(this, "Enter argument for logarithm:", "Logarithm Argument", JOptionPane.QUESTION_MESSAGE);
                if (argPrompt != null && !argPrompt.isEmpty()) {
                    // 替换 x 为输入的值
                    String expr = currentExpression.toString().replace("x", argPrompt);
                    currentExpression = new StringBuilder(expr);
                    displayField.setText(currentExpression.toString());
                } else {
                    // 没填值就撤销
                    currentExpression = new StringBuilder(displayField.getText());
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Please enter a valid number", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // 清空所有内容，表达式和显示框一起清
    private void clearDisplay() {
        currentExpression = new StringBuilder();
        displayField.setText("");
        startNewInput = false;
    }

    // CE 按钮：只清当前输入部分，不清整个表达式
    private void clearEntry() {
        if (currentExpression.length() > 0) {
            String expStr = currentExpression.toString();

            // 找最后一个运算符的位置
            int lastOpIndex = Math.max(
                    Math.max(expStr.lastIndexOf('+'), expStr.lastIndexOf('-')),
                    Math.max(expStr.lastIndexOf('*'), expStr.lastIndexOf('/'))
            );

            if (lastOpIndex >= 0) {
                // 删除最后一个运算符后面的部分
                currentExpression = new StringBuilder(expStr.substring(0, lastOpIndex + 1));
            } else {
                // 没有运算符，直接清空
                currentExpression = new StringBuilder();
            }

            displayField.setText(currentExpression.toString());
        }
    }

    // ⌫ 回退一格
    private void backspace() {
        if (currentExpression.length() > 0) {
            currentExpression.deleteCharAt(currentExpression.length() - 1);
            displayField.setText(currentExpression.toString());
        }
    }

    // 等号按下：计算表达式结果
    private void calculateResult() {
        try {
            String expressionStr = currentExpression.toString();

            // 如果当前是角度制，并且用了 toRadians() 包裹
            if (!isRadians && expressionStr.contains("toRadians(")) {
                // 拿掉 toRadians( 和末尾的括号
                expressionStr = expressionStr.replace("toRadians(", "");
                expressionStr = expressionStr.substring(0, expressionStr.lastIndexOf(")"));

                // 把度数转为弧度
                double degrees = evaluateExpression(expressionStr);
                double radians = Math.toRadians(degrees);

                // 判断具体是哪个三角函数，再手动计算
                String trigFunction = expressionStr.substring(0, expressionStr.indexOf("("));
                switch (trigFunction) {
                    case "sin":
                        displayResult(Math.sin(radians));
                        break;
                    case "cos":
                        displayResult(Math.cos(radians));
                        break;
                    case "tan":
                        displayResult(Math.tan(radians));
                        break;
                    case "asin":
                        displayResult(Math.asin(radians));
                        break;
                    case "acos":
                        displayResult(Math.acos(radians));
                        break;
                    case "atan":
                        displayResult(Math.atan(radians));
                        break;
                }
            } else {
                // 普通模式，直接走表达式求值
                double result = evaluateExpression(expressionStr);
                displayResult(result);
            }

        } catch (Exception e) {
            // 出错就显示 Error，防止崩溃
            displayField.setText("Error");
            startNewInput = true;
        }
    }

    // 表达式求值（支持 cbrt 等自定义函数）
    private double evaluateExpression(String expressionStr) {
        // 替换常量 e 和 π
        expressionStr = expressionStr.replace("e", String.valueOf(Math.E));
        expressionStr = expressionStr.replace("pi", String.valueOf(Math.PI));


        // 用 exp4j 来解析和计算表达式（支持我们自定义的 cbrt 函数）
        Expression expression = new ExpressionBuilder(expressionStr)
                .function(new CustomFunctions.CbrtFunction())
                .build();

        return expression.evaluate(); // 最终返回结果
    }

    // 显示计算结果，并准备进入“新一轮输入”状态
    private void displayResult(double result) {
        String resultStr = formatter.format(result); // 避免显示太多小数位
        displayField.setText(resultStr);
        currentExpression = new StringBuilder(resultStr); // 把结果变成下一轮起点
        startNewInput = true;
    }

    // 百分比按钮的处理逻辑
    private void calculatePercentage() {
        if (startNewInput) {
            // 结果显示状态下，按 % 没意义，直接忽略
            return;
        }

        String expressionStr = currentExpression.toString();

        try {
            // 先定位最后一个数字的位置（从后往前找）
            int lastDigitPos = -1;
            for (int i = expressionStr.length() - 1; i >= 0; i--) {
                char c = expressionStr.charAt(i);
                if ((c >= '0' && c <= '9') || c == '.') {
                    lastDigitPos = i;
                } else if (lastDigitPos != -1) {
                    break;
                }
            }

            if (lastDigitPos == -1) return; // 找不到就跳出

            // 然后向前找这个数字的开头
            int startPos = lastDigitPos;
            while (startPos > 0) {
                char c = expressionStr.charAt(startPos - 1);
                if ((c >= '0' && c <= '9') || c == '.') {
                    startPos--;
                } else {
                    break;
                }
            }

            // 拆成：前半部分 + 数字部分
            String numberStr = expressionStr.substring(startPos, lastDigitPos + 1);
            String beforePart = startPos > 0 ? expressionStr.substring(0, startPos) : "";

            // 先简单地除以 100
            double number = Double.parseDouble(numberStr);
            double percentage = number / 100.0;

            // 如果前面是个加减乘除，再对它取百分比（比如 200 + 10% 就变成 200 + 20）
            if (beforePart.length() > 0) {
                char lastOp = beforePart.charAt(beforePart.length() - 1);
                if (lastOp == '+' || lastOp == '-' || lastOp == '*' || lastOp == '/') {
                    String beforeOpStr = beforePart.substring(0, beforePart.length() - 1);
                    if (!beforeOpStr.isEmpty()) {
                        try {
                            double beforeValue = evaluateExpression(beforeOpStr);
                            percentage = beforeValue * percentage;
                        } catch (Exception e) {
                            // 出错就不处理，直接按默认逻辑用
                        }
                    }
                }
            }

            // 把原来的数字换成百分比值
            currentExpression = new StringBuilder(beforePart).append(percentage);
            displayField.setText(currentExpression.toString());

        } catch (Exception e) {
            // 任意错误就啥也不做
        }
    }

    // 计算倒数功能（1/x）
    private void calculateReciprocal() {
        if (startNewInput) {
            // 如果是在结果显示状态，就直接对结果取倒数
            String currentText = displayField.getText();
            try {
                double value = Double.parseDouble(currentText);
                if (value == 0) {
                    displayField.setText("Error: Division by zero");
                } else {
                    double reciprocal = 1.0 / value;
                    displayResult(reciprocal);
                }
            } catch (NumberFormatException e) {
                displayField.setText("Error");
            }
        } else {
            // 如果是在输入中，就追加一个 “^(-1)” 代表 1/x
            currentExpression.append("^(-1)");
            displayField.setText(currentExpression.toString());
        }
    }

    // 切换正负号 ±
    private void toggleSign() {
        if (startNewInput) {
            // 如果当前是结果状态，直接把结果变成相反数
            String currentText = displayField.getText();
            try {
                double value = Double.parseDouble(currentText);
                displayResult(-value);
            } catch (NumberFormatException e) {
                displayField.setText("Error");
            }
        } else {
            // 如果是在编辑输入，就用 -(...) 包一层
            currentExpression.insert(0, "-(").append(")");
            displayField.setText(currentExpression.toString());
        }
    }

    // 清空内存 MC
    private void memoryClear() {
        memory = 0.0;
    }

    // 读取内存 MR
    private void memoryRecall() {
        if (startNewInput) {
            currentExpression = new StringBuilder();
            startNewInput = false;
        }
        currentExpression.append(formatter.format(memory));
        displayField.setText(currentExpression.toString());
    }

    // 把当前值存到内存 MS
    private void memoryStore() {
        try {
            String currentText = displayField.getText();
            if (!currentText.isEmpty() && !currentText.equals("Error")) {
                if (startNewInput) {
                    memory = Double.parseDouble(currentText);
                } else {
                    memory = evaluateExpression(currentText);
                }
            }
        } catch (Exception e) {
            displayField.setText("Error");
        }
    }

    // 当前值加到内存 M+
    private void memoryAdd() {
        try {
            String currentText = displayField.getText();
            if (!currentText.isEmpty() && !currentText.equals("Error")) {
                double value = startNewInput ? Double.parseDouble(currentText)
                        : evaluateExpression(currentText);
                memory += value;
            }
        } catch (Exception e) {
            displayField.setText("Error");
        }
    }

    // 当前值从内存中减去 M-
    private void memorySubtract() {
        try {
            String currentText = displayField.getText();
            if (!currentText.isEmpty() && !currentText.equals("Error")) {
                double value = startNewInput ? Double.parseDouble(currentText)
                        : evaluateExpression(currentText);
                memory -= value;
            }
        } catch (Exception e) {
            displayField.setText("Error");
        }
    }

    // 将显示框内容复制到剪贴板
    private void copyToClipboard() {
        String text = displayField.getText();
        if (text != null && !text.isEmpty()) {
            displayField.selectAll();   // 全选
            displayField.copy();        // 执行复制
            displayField.select(0, 0);  // 取消选中状态
        }
    }

    // 从剪贴板粘贴到显示框，并尝试更新表达式
    private void pasteFromClipboard() {
        try {
            displayField.paste();  // 粘贴文本
            currentExpression = new StringBuilder(displayField.getText()); // 更新表达式
            startNewInput = false;
        } catch (Exception e) {
            displayField.setText("Error: Invalid paste");
        }
    }

    public static void main(String[] args) {
        try {
            // 使用操作系统原生的窗口外观（更像 Windows/macOS 自带的样式）
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace(); // 设置失败就打个日志
        }

        // 启动计算器窗口（事件分发线程中执行）
        SwingUtilities.invokeLater(() -> {
            ScientificCalculator calculator = new ScientificCalculator();
            calculator.setVisible(true);
        });
    }
}