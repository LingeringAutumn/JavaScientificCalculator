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
    private JTextField displayField;
    private StringBuilder currentExpression = new StringBuilder();
    private double memory = 0.0;
    private boolean startNewInput = false;
    private boolean isRadians = true;
    private static final DecimalFormat formatter = new DecimalFormat("#.##########");

    // Calculator panels
    private JPanel mainPanel;
    private JPanel displayPanel;
    private JPanel buttonPanel;

    // Menu items
    private JMenuItem copyMenuItem;
    private JMenuItem pasteMenuItem;
    private JMenuItem clearMenuItem;
    private JRadioButtonMenuItem radiansMenuItem;
    private JRadioButtonMenuItem degreesMenuItem;

    public ScientificCalculator() {
        setTitle("Scientific Calculator");
        setSize(480, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Set up the main layout
        mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(new Color(240, 240, 240));

        setupMenuBar();
        setupDisplayPanel();
        setupButtonPanel();

        add(mainPanel);
    }

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // Edit menu
        JMenu editMenu = new JMenu("Edit");
        copyMenuItem = new JMenuItem("Copy");
        pasteMenuItem = new JMenuItem("Paste");
        clearMenuItem = new JMenuItem("Clear");

        copyMenuItem.addActionListener(e -> copyToClipboard());
        pasteMenuItem.addActionListener(e -> pasteFromClipboard());
        clearMenuItem.addActionListener(e -> clearDisplay());

        copyMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
        pasteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK));
        clearMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));

        editMenu.add(copyMenuItem);
        editMenu.add(pasteMenuItem);
        editMenu.addSeparator();
        editMenu.add(clearMenuItem);

        // View menu
        JMenu viewMenu = new JMenu("View");
        ButtonGroup angleGroup = new ButtonGroup();
        radiansMenuItem = new JRadioButtonMenuItem("Radians", true);
        degreesMenuItem = new JRadioButtonMenuItem("Degrees", false);

        radiansMenuItem.addActionListener(e -> isRadians = true);
        degreesMenuItem.addActionListener(e -> isRadians = false);

        angleGroup.add(radiansMenuItem);
        angleGroup.add(degreesMenuItem);

        viewMenu.add(radiansMenuItem);
        viewMenu.add(degreesMenuItem);

        menuBar.add(editMenu);
        menuBar.add(viewMenu);

        setJMenuBar(menuBar);
    }

    private void setupDisplayPanel() {
        displayPanel = new JPanel(new BorderLayout());
        displayPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180), 1, true),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        displayPanel.setBackground(Color.WHITE);

        displayField = new JTextField();
        displayField.setFont(new Font("Arial", Font.PLAIN, 24));
        displayField.setHorizontalAlignment(JTextField.RIGHT);
        displayField.setEditable(false);
        displayField.setBorder(null);
        displayField.setBackground(Color.WHITE);

        displayPanel.add(displayField, BorderLayout.CENTER);
        mainPanel.add(displayPanel, BorderLayout.NORTH);
    }

    private void setupButtonPanel() {
        // Change layout to be more customizable
        buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setBackground(new Color(240, 240, 240));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(3, 3, 3, 3);

        // Memory buttons row
        gbc.gridy = 0;
        gbc.gridx = 0; addButton("MC", e -> memoryClear(), buttonPanel, createMemoryButtonStyle(), gbc);
        gbc.gridx = 1; addButton("MR", e -> memoryRecall(), buttonPanel, createMemoryButtonStyle(), gbc);
        gbc.gridx = 2; addButton("MS", e -> memoryStore(), buttonPanel, createMemoryButtonStyle(), gbc);
        gbc.gridx = 3; addButton("M+", e -> memoryAdd(), buttonPanel, createMemoryButtonStyle(), gbc);
        gbc.gridx = 4; addButton("M-", e -> memorySubtract(), buttonPanel, createMemoryButtonStyle(), gbc);

        // Function buttons row 1
        gbc.gridy = 1;
        gbc.gridx = 0; addButton("x²", e -> applyFunction("^2"), buttonPanel, createFunctionButtonStyle(), gbc);
        gbc.gridx = 1; addButton("x³", e -> applyFunction("^3"), buttonPanel, createFunctionButtonStyle(), gbc);
        gbc.gridx = 2; addButton("xʸ", e -> appendToExpression("^"), buttonPanel, createFunctionButtonStyle(), gbc);
        gbc.gridx = 3; addButton("10ˣ", e -> appendToExpression("10^"), buttonPanel, createFunctionButtonStyle(), gbc);
        gbc.gridx = 4; addButton("1/x", e -> calculateReciprocal(), buttonPanel, createFunctionButtonStyle(), gbc);

        // Function buttons row 2
        gbc.gridy = 2;
        gbc.gridx = 0; addButton("√", e -> applyFunction("sqrt"), buttonPanel, createFunctionButtonStyle(), gbc);
        gbc.gridx = 1; addButton("∛", e -> applyFunction("cbrt"), buttonPanel, createFunctionButtonStyle(), gbc);
        gbc.gridx = 2; addButton("log", e -> appendToExpression("log10("), buttonPanel, createFunctionButtonStyle(), gbc);
        gbc.gridx = 3; addButton("ln", e -> appendToExpression("log("), buttonPanel, createFunctionButtonStyle(), gbc);
        gbc.gridx = 4; addButton("logₙ", e -> appendCustomLogBase(), buttonPanel, createFunctionButtonStyle(), gbc);

        // Trigonometric functions row
        gbc.gridy = 3;
        gbc.gridx = 0; addButton("sin", e -> appendTrigFunction("sin"), buttonPanel, createFunctionButtonStyle(), gbc);
        gbc.gridx = 1; addButton("cos", e -> appendTrigFunction("cos"), buttonPanel, createFunctionButtonStyle(), gbc);
        gbc.gridx = 2; addButton("tan", e -> appendTrigFunction("tan"), buttonPanel, createFunctionButtonStyle(), gbc);
        gbc.gridx = 3; addButton("(", e -> appendToExpression("("), buttonPanel, createParenthesisButtonStyle(), gbc);
        gbc.gridx = 4; addButton(")", e -> appendToExpression(")"), buttonPanel, createParenthesisButtonStyle(), gbc);

        // Second row of trigonometric functions
        gbc.gridy = 4;
        gbc.gridx = 0; addButton("asin", e -> appendTrigFunction("asin"), buttonPanel, createFunctionButtonStyle(), gbc);
        gbc.gridx = 1; addButton("acos", e -> appendTrigFunction("acos"), buttonPanel, createFunctionButtonStyle(), gbc);
        gbc.gridx = 2; addButton("atan", e -> appendTrigFunction("atan"), buttonPanel, createFunctionButtonStyle(), gbc);
        gbc.gridx = 3; addButton("C", e -> clearDisplay(), buttonPanel, createClearButtonStyle(), gbc);
        gbc.gridx = 4; addButton("⌫", e -> backspace(), buttonPanel, createClearButtonStyle(), gbc);

        // Clear buttons and other functions
        gbc.gridy = 5;
        gbc.gridx = 0; addButton("sinh", e -> appendToExpression("sinh("), buttonPanel, createFunctionButtonStyle(), gbc);
        gbc.gridx = 1; addButton("cosh", e -> appendToExpression("cosh("), buttonPanel, createFunctionButtonStyle(), gbc);
        gbc.gridx = 2; addButton("tanh", e -> appendToExpression("tanh("), buttonPanel, createFunctionButtonStyle(), gbc);
        gbc.gridx = 3; addButton("CE", e -> clearEntry(), buttonPanel, createClearButtonStyle(), gbc);
        gbc.gridx = 4; addButton("%", e -> calculatePercentage(), buttonPanel, createOperatorButtonStyle(), gbc);

        // Number pad and operations (start from row 6)
        // Row with 7,8,9
        gbc.gridy = 6;
        gbc.gridx = 0; addButton("7", e -> appendToExpression("7"), buttonPanel, createNumberButtonStyle(), gbc);
        gbc.gridx = 1; addButton("8", e -> appendToExpression("8"), buttonPanel, createNumberButtonStyle(), gbc);
        gbc.gridx = 2; addButton("9", e -> appendToExpression("9"), buttonPanel, createNumberButtonStyle(), gbc);
        gbc.gridx = 3; addButton("π", e -> appendToExpression("pi"), buttonPanel, createNumberButtonStyle(), gbc);
        gbc.gridx = 4; addButton("÷", e -> appendToExpression("/"), buttonPanel, createOperatorButtonStyle(), gbc);

        // Row with 4,5,6
        gbc.gridy = 7;
        gbc.gridx = 0; addButton("4", e -> appendToExpression("4"), buttonPanel, createNumberButtonStyle(), gbc);
        gbc.gridx = 1; addButton("5", e -> appendToExpression("5"), buttonPanel, createNumberButtonStyle(), gbc);
        gbc.gridx = 2; addButton("6", e -> appendToExpression("6"), buttonPanel, createNumberButtonStyle(), gbc);
        gbc.gridx = 3; addButton("e", e -> appendToExpression("e"), buttonPanel, createNumberButtonStyle(), gbc);
        gbc.gridx = 4; addButton("×", e -> appendToExpression("*"), buttonPanel, createOperatorButtonStyle(), gbc);

        // Row with 1,2,3
        gbc.gridy = 8;
        gbc.gridx = 0; addButton("1", e -> appendToExpression("1"), buttonPanel, createNumberButtonStyle(), gbc);
        gbc.gridx = 1; addButton("2", e -> appendToExpression("2"), buttonPanel, createNumberButtonStyle(), gbc);
        gbc.gridx = 2; addButton("3", e -> appendToExpression("3"), buttonPanel, createNumberButtonStyle(), gbc);
        gbc.gridx = 3; addButton("±", e -> toggleSign(), buttonPanel, createOperatorButtonStyle(), gbc);
        gbc.gridx = 4; addButton("-", e -> appendToExpression("-"), buttonPanel, createOperatorButtonStyle(), gbc);

        // Row with 0, decimal, and equals
        gbc.gridy = 9;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        addButton("0", e -> appendToExpression("0"), buttonPanel, createNumberButtonStyle(), gbc);
        gbc.gridwidth = 1;
        gbc.gridx = 2; addButton(".", e -> appendToExpression("."), buttonPanel, createNumberButtonStyle(), gbc);
        gbc.gridx = 3; addButton("+", e -> appendToExpression("+"), buttonPanel, createOperatorButtonStyle(), gbc);
        gbc.gridx = 4; addButton("=", e -> calculateResult(), buttonPanel, createEqualsButtonStyle(), gbc);

        mainPanel.add(buttonPanel, BorderLayout.CENTER);
    }

    private JButton createButton(String text, ActionListener listener, Function<JButton, JButton> styleFunction) {
        JButton button = new JButton(text);
        button.addActionListener(listener);
        return styleFunction.apply(button);
    }

    private void addButton(String text, ActionListener listener, JPanel panel, Function<JButton, JButton> styleFunction) {
        panel.add(createButton(text, listener, styleFunction));
    }

    private void addButton(String text, ActionListener listener, JPanel panel, Function<JButton, JButton> styleFunction, GridBagConstraints gbc) {
        panel.add(createButton(text, listener, styleFunction), gbc);
    }

    private Function<JButton, JButton> createNumberButtonStyle() {
        return button -> {
            button.setFont(new Font("Arial", Font.BOLD, 18));
            button.setBackground(new Color(250, 250, 250));
            button.setFocusPainted(false);
            return button;
        };
    }

    private Function<JButton, JButton> createOperatorButtonStyle() {
        return button -> {
            button.setFont(new Font("Arial", Font.BOLD, 18));
            button.setBackground(new Color(230, 230, 250));
            button.setFocusPainted(false);
            return button;
        };
    }

    private Function<JButton, JButton> createFunctionButtonStyle() {
        return button -> {
            button.setFont(new Font("Arial", Font.PLAIN, 16));
            button.setBackground(new Color(220, 240, 250));
            button.setFocusPainted(false);
            return button;
        };
    }

    private Function<JButton, JButton> createClearButtonStyle() {
        return button -> {
            button.setFont(new Font("Arial", Font.BOLD, 16));
            button.setBackground(new Color(255, 200, 200));
            button.setFocusPainted(false);
            return button;
        };
    }

    private Function<JButton, JButton> createParenthesisButtonStyle() {
        return button -> {
            button.setFont(new Font("Arial", Font.BOLD, 18));
            button.setBackground(new Color(240, 240, 220));
            button.setFocusPainted(false);
            return button;
        };
    }

    private Function<JButton, JButton> createMemoryButtonStyle() {
        return button -> {
            button.setFont(new Font("Arial", Font.BOLD, 14));
            button.setBackground(new Color(220, 220, 240));
            button.setFocusPainted(false);
            return button;
        };
    }

    private Function<JButton, JButton> createEqualsButtonStyle() {
        return button -> {
            button.setFont(new Font("Arial", Font.BOLD, 20));
            button.setBackground(new Color(120, 180, 240));
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            return button;
        };
    }

    private void appendToExpression(String value) {
        if (startNewInput) {
            currentExpression = new StringBuilder();
            startNewInput = false;
        }
        currentExpression.append(value);
        displayField.setText(currentExpression.toString());
    }

    private void appendTrigFunction(String function) {
        if (startNewInput) {
            currentExpression = new StringBuilder();
            startNewInput = false;
        }
        // Append the appropriate function based on radians/degrees mode
        if (isRadians) {
            currentExpression.append(function).append("(");
        } else {
            // For degrees, we need to convert degrees to radians
            // Using the formula: radians = degrees * (π/180)
            currentExpression.append(function).append("(toRadians(");
        }
        displayField.setText(currentExpression.toString());
    }

    private void applyFunction(String function) {
        if (startNewInput) {
            // If we're starting a new calculation but there's a result on display,
            // wrap that result with the function
            String currentText = displayField.getText();
            currentExpression = new StringBuilder();
            if (function.equals("sqrt")) {
                currentExpression.append("sqrt(").append(currentText).append(")");
            } else if (function.equals("cbrt")) {
                currentExpression.append("cbrt(").append(currentText).append(")");
            } else if (function.equals("exp")) {
                currentExpression.append("exp(").append(currentText).append(")");
            } else if (function.equals("^2")) {
                currentExpression.append("(").append(currentText).append(")^2");
            } else if (function.equals("^3")) {
                currentExpression.append("(").append(currentText).append(")^3");
            }
            startNewInput = false;
        } else {
            // Otherwise, just append the function
            if (function.equals("sqrt")) {
                currentExpression.append("sqrt(");
            } else if (function.equals("cbrt")) {
                currentExpression.append("cbrt(");
            } else if (function.equals("exp")) {
                currentExpression.append("exp(");
            } else if (function.equals("^2")) {
                currentExpression.append("^2");
            } else if (function.equals("^3")) {
                currentExpression.append("^3");
            }
        }
        displayField.setText(currentExpression.toString());
    }

    private void appendCustomLogBase() {
        if (startNewInput) {
            currentExpression = new StringBuilder();
            startNewInput = false;
        }
        // For custom log base, we'll use the formula: log_b(x) = log(x) / log(b)
        String basePrompt = JOptionPane.showInputDialog(this, "Enter logarithm base:", "Logarithm Base", JOptionPane.QUESTION_MESSAGE);
        if (basePrompt != null && !basePrompt.isEmpty()) {
            try {
                double base = Double.parseDouble(basePrompt);
                if (base <= 0 || base == 1) {
                    JOptionPane.showMessageDialog(this, "Base must be a positive number different from 1", "Invalid Base", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                // Insert the placeholder for the argument that will be evaluated
                currentExpression.append("(log(x)/log(").append(base).append("))");
                // Prompt for the argument after setting the base
                String argPrompt = JOptionPane.showInputDialog(this, "Enter argument for logarithm:", "Logarithm Argument", JOptionPane.QUESTION_MESSAGE);
                if (argPrompt != null && !argPrompt.isEmpty()) {
                    try {
                        // Replace the placeholder 'x' with the provided argument
                        String expr = currentExpression.toString().replace("x", argPrompt);
                        currentExpression = new StringBuilder(expr);
                        displayField.setText(currentExpression.toString());
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(this, "Please enter a valid argument", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    // If no argument is provided, revert the expression
                    currentExpression = new StringBuilder(displayField.getText());
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Please enter a valid number", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearDisplay() {
        currentExpression = new StringBuilder();
        displayField.setText("");
        startNewInput = false;
    }

    private void clearEntry() {
        // Clear the current entry but not the entire expression
        if (currentExpression.length() > 0) {
            // Find the last operator and remove everything after it
            String expStr = currentExpression.toString();
            int lastOpIndex = Math.max(
                    Math.max(expStr.lastIndexOf('+'), expStr.lastIndexOf('-')),
                    Math.max(expStr.lastIndexOf('*'), expStr.lastIndexOf('/'))
            );

            if (lastOpIndex >= 0) {
                currentExpression = new StringBuilder(expStr.substring(0, lastOpIndex + 1));
            } else {
                currentExpression = new StringBuilder();
            }
            displayField.setText(currentExpression.toString());
        }
    }

    private void backspace() {
        if (currentExpression.length() > 0) {
            currentExpression.deleteCharAt(currentExpression.length() - 1);
            displayField.setText(currentExpression.toString());
        }
    }

    private void calculateResult() {
        try {
            String expressionStr = currentExpression.toString();

            // Handle degrees conversion if needed
            if (!isRadians && expressionStr.contains("toRadians(")) {
                expressionStr = expressionStr.replace("toRadians(", "");
                expressionStr = expressionStr.substring(0, expressionStr.lastIndexOf(")"));

                // Convert degrees to radians for calculation
                double degrees = evaluateExpression(expressionStr);
                double radians = Math.toRadians(degrees);

                // Reapply the trig function
                String trigFunction = expressionStr.substring(0, expressionStr.indexOf("("));
                if (trigFunction.equals("sin")) {
                    displayResult(Math.sin(radians));
                } else if (trigFunction.equals("cos")) {
                    displayResult(Math.cos(radians));
                } else if (trigFunction.equals("tan")) {
                    displayResult(Math.tan(radians));
                } else if (trigFunction.equals("asin")) {
                    displayResult(Math.asin(radians));
                } else if (trigFunction.equals("acos")) {
                    displayResult(Math.acos(radians));
                } else if (trigFunction.equals("atan")) {
                    displayResult(Math.atan(radians));
                }
            } else {
                // Normal expression evaluation
                double result = evaluateExpression(expressionStr);
                displayResult(result);
            }
        } catch (Exception e) {
            displayField.setText("Error");
            startNewInput = true;
        }
    }

    private double evaluateExpression(String expressionStr) {
        // Replace 'e' and 'pi' with their values before evaluation
        expressionStr = expressionStr.replace("e", String.valueOf(Math.E));
        expressionStr = expressionStr.replace("pi", String.valueOf(Math.PI));

        // Handle custom functions
        if (expressionStr.contains("cbrt(")) {
            expressionStr = expressionStr.replace("cbrt(", "cbrt(");
        }

        Expression expression = new ExpressionBuilder(expressionStr)
                .function(new CustomFunctions.CbrtFunction())
                .build();

        return expression.evaluate();
    }

    private void displayResult(double result) {
        // Format the result to avoid unnecessary decimal places
        String resultStr = formatter.format(result);
        displayField.setText(resultStr);
        currentExpression = new StringBuilder(resultStr);
        startNewInput = true;
    }

    private void calculatePercentage() {
        if (startNewInput) {
            // If we're just working with the result, it doesn't make sense to compute percentage
            return;
        }

        String expressionStr = currentExpression.toString();
        try {
            // Find the last number in the expression
            int lastDigitPos = -1;
            for (int i = expressionStr.length() - 1; i >= 0; i--) {
                char c = expressionStr.charAt(i);
                if ((c >= '0' && c <= '9') || c == '.') {
                    lastDigitPos = i;
                } else if (lastDigitPos != -1) {
                    break;
                }
            }

            if (lastDigitPos == -1) return;

            // Find the start of this number
            int startPos = lastDigitPos;
            while (startPos > 0) {
                char c = expressionStr.charAt(startPos - 1);
                if ((c >= '0' && c <= '9') || c == '.') {
                    startPos--;
                } else {
                    break;
                }
            }

            // Extract the number and the part before it
            String numberStr = expressionStr.substring(startPos, lastDigitPos + 1);
            String beforePart = startPos > 0 ? expressionStr.substring(0, startPos) : "";

            // Calculate percentage based on context
            double number = Double.parseDouble(numberStr);
            double percentage = number / 100.0;

            // If there's an operation before this number, calculate percentage of that value
            if (beforePart.length() > 0) {
                char lastOp = beforePart.charAt(beforePart.length() - 1);
                if (lastOp == '+' || lastOp == '-' || lastOp == '*' || lastOp == '/') {
                    String beforeOpStr = beforePart.substring(0, beforePart.length() - 1);
                    if (!beforeOpStr.isEmpty()) {
                        try {
                            double beforeValue = evaluateExpression(beforeOpStr);
                            percentage = beforeValue * percentage;
                        } catch (Exception e) {
                            // If we can't evaluate the part before, just use percentage of number
                        }
                    }
                }
            }

            // Replace the number with its percentage value
            currentExpression = new StringBuilder(beforePart).append(percentage);
            displayField.setText(currentExpression.toString());

        } catch (Exception e) {
            // If there's any error, just leave the expression as is
        }
    }

    private void calculateReciprocal() {
        if (startNewInput) {
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
            currentExpression.append("^(-1)");
            displayField.setText(currentExpression.toString());
        }
    }

    private void toggleSign() {
        if (startNewInput) {
            String currentText = displayField.getText();
            try {
                double value = Double.parseDouble(currentText);
                displayResult(-value);
            } catch (NumberFormatException e) {
                displayField.setText("Error");
            }
        } else {
            // If we're in the middle of building an expression, insert negative sign
            currentExpression.insert(0, "-(").append(")");
            displayField.setText(currentExpression.toString());
        }
    }

    private void memoryClear() {
        memory = 0.0;
    }

    private void memoryRecall() {
        if (startNewInput) {
            currentExpression = new StringBuilder();
            startNewInput = false;
        }
        currentExpression.append(formatter.format(memory));
        displayField.setText(currentExpression.toString());
    }

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

    private void memoryAdd() {
        try {
            String currentText = displayField.getText();
            if (!currentText.isEmpty() && !currentText.equals("Error")) {
                double value;
                if (startNewInput) {
                    value = Double.parseDouble(currentText);
                } else {
                    value = evaluateExpression(currentText);
                }
                memory += value;
            }
        } catch (Exception e) {
            displayField.setText("Error");
        }
    }

    private void memorySubtract() {
        try {
            String currentText = displayField.getText();
            if (!currentText.isEmpty() && !currentText.equals("Error")) {
                double value;
                if (startNewInput) {
                    value = Double.parseDouble(currentText);
                } else {
                    value = evaluateExpression(currentText);
                }
                memory -= value;
            }
        } catch (Exception e) {
            displayField.setText("Error");
        }
    }

    private void copyToClipboard() {
        String text = displayField.getText();
        if (text != null && !text.isEmpty()) {
            displayField.selectAll();
            displayField.copy();
            displayField.select(0, 0); // Deselect text
        }
    }

    private void pasteFromClipboard() {
        try {
            displayField.paste();
            currentExpression = new StringBuilder(displayField.getText());
            startNewInput = false;
        } catch (Exception e) {
            displayField.setText("Error: Invalid paste");
        }
    }

    public static void main(String[] args) {
        try {
            // Set system look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            ScientificCalculator calculator = new ScientificCalculator();
            calculator.setVisible(true);
        });
    }
}