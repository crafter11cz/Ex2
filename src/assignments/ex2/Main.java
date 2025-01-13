package assignments.ex2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Stack;

public class Main implements Sheet {
    private Cell[][] table;

    // Constructor to initialize the spreadsheet with specified dimensions.
    public Main(int x, int y) {
        table = new Cell[x][y];
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                table[i][j] = new SCell(Ex2Utils.EMPTY_CELL); // Initialize with empty cells
            }
        }
    }

    @Override
    public String value(int x, int y) {
        return table[x][y].getData(); // Return the current value of a cell
    }

    @Override
    public void set(int x, int y, String c) {
        table[x][y] = new SCell(c); // Set the cell value at the specified coordinates
    }

    @Override
    public void eval() {
        for (int i = 0; i < width(); i++) {
            for (int j = 0; j < height(); j++) {
                Cell cell = table[i][j];
                if (cell.getType() == Ex2Utils.FORM) {
                    String formula = cell.getData();
                    String computedValue = evaluateFormula(formula, i, j);
                    cell.setData(computedValue);
                }
            }
        }
    }

    private String evaluateFormula(String formula, int x, int y) {
        if (formula.startsWith("=")) {
            try {
                String expression = formula.substring(1).toUpperCase();
                for (int i = 0; i < width(); i++) {
                    for (int j = 0; j < height(); j++) {
                        String cellName = (char) ('A' + i) + Integer.toString(j + 1);
                        if (expression.contains(cellName)) {
                            expression = expression.replace(cellName, table[i][j].getData());
                        }
                    }
                }
                double result = evaluateMath(expression);

                // Handle Infinity result
                if (Double.isInfinite(result)) {
                    return "Infinity";
                }
                return Double.toString(result);
            } catch (Exception e) {
                return Ex2Utils.ERR_FORM;
            }
        }
        return formula;
    }

    private double evaluateMath(String expression) {
        Stack<Double> values = new Stack<>();
        Stack<Character> operators = new Stack<>();

        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);

            if (Character.isDigit(c)) {
                StringBuilder sb = new StringBuilder();
                while (i < expression.length() && (Character.isDigit(expression.charAt(i)) || expression.charAt(i) == '.')) {
                    sb.append(expression.charAt(i));
                    i++;
                }
                i--;
                values.push(Double.parseDouble(sb.toString()));
            } else if (c == '(') {
                operators.push(c);
            } else if (c == ')') {
                while (!operators.isEmpty() && operators.peek() != '(') {
                    values.push(applyOperation(operators.pop(), values.pop(), values.pop()));
                }
                operators.pop();
            } else if (c == '+' || c == '-' || c == '*' || c == '/') {
                while (!operators.isEmpty() && hasPrecedence(c, operators.peek())) {
                    values.push(applyOperation(operators.pop(), values.pop(), values.pop()));
                }
                operators.push(c);
            }
        }

        while (!operators.isEmpty()) {
            values.push(applyOperation(operators.pop(), values.pop(), values.pop()));
        }

        return values.pop();
    }

    private boolean hasPrecedence(char op1, char op2) {
        if (op2 == '(' || op2 == ')') {
            return false;
        }
        if ((op1 == '*' || op1 == '/') && (op2 == '+' || op2 == '-')) {
            return false;
        }
        return true;
    }

    private double applyOperation(char op, double b, double a) {
        switch (op) {
            case '+':
                return a + b;
            case '-':
                return a - b;
            case '*':
                return a * b;
            case '/':
                if (b == 0) {
                    return Double.POSITIVE_INFINITY; // Return Infinity for division by zero
                }
                return a / b;
        }
        return 0;
    }

    @Override
    public int[][] depth() {
        int[][] depths = new int[width()][height()];
        for (int i = 0; i < width(); i++) {
            for (int j = 0; j < height(); j++) {
                Cell cell = table[i][j];
                if (cell.getType() == Ex2Utils.NUMBER || cell.getType() == Ex2Utils.TEXT) {
                    depths[i][j] = 0;
                } else if (cell.getType() == Ex2Utils.FORM) {
                    depths[i][j] = 1; // Example depth for formulas
                }
            }
        }
        return depths;
    }

    @Override
    public boolean isIn(int x, int y) {
        return x >= 0 && y >= 0 && x < width() && y < height();
    }

    @Override
    public int width() {
        return table.length;
    }

    @Override
    public int height() {
        return table[0].length;
    }

    @Override
    public Cell get(int x, int y) {
        return table[x][y];
    }

    @Override
    public Cell get(String entry) {
        int x = xCell(entry);
        int y = yCell(entry);
        if (isIn(x, y)) {
            return table[x][y];
        }
        return null;
    }

    private int xCell(String c) {
        return c.charAt(0) - 'A';
    }

    private int yCell(String c) {
        return Integer.parseInt(c.substring(1)) - 1;
    }

    @Override
    public void load(String fileName) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(",");
            if (parts.length >= 3) {
                int x = Integer.parseInt(parts[0]);
                int y = Integer.parseInt(parts[1]);
                String data = parts[2];
                set(x, y, data);
            }
        }
        reader.close();
    }

    @Override
    public void save(String fileName) throws IOException {
        // Implement save logic if needed
    }

    @Override
    public String eval(int x, int y) {
        Cell cell = get(x, y);
        if (cell != null && cell.getType() == Ex2Utils.FORM) {
            String formula = cell.getData();
            return evaluateFormula(formula, x, y);
        }
        return cell != null ? cell.getData() : Ex2Utils.EMPTY_CELL;
    }

    public void handleMouseClick(double mx, double my) {
        int x = (int) ((mx - Ex2Utils.GUI_X_START) / Ex2Utils.GUI_X_SPACE);
        int y = height() - (int) (my - Ex2Utils.GUI_Y_TEXT_START) - 1;

        if (isIn(x, y)) {
            String cellName = (char) ('A' + x) + Integer.toString(y + 1);
            System.out.println("Clicked on: " + cellName);
        }
    }
}