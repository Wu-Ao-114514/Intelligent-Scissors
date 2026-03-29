package A2;

import java.util.Scanner;
import java.util.Stack;

public class P5 {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        while (input.hasNext()) {
            String s = input.nextLine();
            String postfix = infixToPostfix(s);
            double result = evaluatePostfix(postfix);
            System.out.printf("%s,%.2f%n", postfix, result);
        }
        input.close();
    }

    public static String infixToPostfix(String s) {
        StringBuilder output = new StringBuilder();
        Stack<Character> stack = new Stack<>();
        int length = s.length();

        for (int i = 0; i < length; i++) {
            char c = s.charAt(i);

            if (Character.isDigit(c)) {
                String num = getWholeNumber(s, i);
                output.append(num).append(" ");
                i += num.length() - 1;
            }

            else if (c == '(') {
                stack.push(c);
            }

            else if (c == ')') {
                while (!stack.isEmpty() && stack.peek() != '(') {
                    output.append(stack.pop()).append(" ");
                }
                if (!stack.isEmpty()) {
                    stack.pop();
                }
            }

            else if (isOperator(c)) {
                while (!stack.isEmpty() && precedence(c) <= precedence(stack.peek())) {
                    output.append(stack.pop()).append(" ");
                }
                stack.push(c);
            }
        }

        while (!stack.isEmpty()) {
            output.append(stack.pop()).append(" ");
        }

        return output.toString().trim();
    }

    static String getWholeNumber(String s, int i) {
        StringBuilder result = new StringBuilder();
        while (i < s.length() && Character.isDigit(s.charAt(i))) {
            result.append(s.charAt(i));
            i++;
        }
        return result.toString();
    }

    static boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }

    static int precedence(char c) {
        if (c == '+' || c == '-') return 1;
        if (c == '*' || c == '/') return 2;
        return 0;
    }

    public static double evaluatePostfix(String postfix) {
        Stack<Double> stack = new Stack<>();
        String[] pieces = postfix.split(" ");

        for (String piece : pieces) {
            if (piece.isEmpty()) continue;

            if (isNumeric(piece)) {
                stack.push(Double.parseDouble(piece));
            } else {
                double b = stack.pop();
                double a = stack.pop();
                double result = calculate(a, b, piece.charAt(0));
                stack.push(result);
            }
        }
        return stack.pop();
    }

    static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    static double calculate(double a, double b, char operator) {
        return switch (operator) {
            case '+' -> a + b;
            case '-' -> a - b;
            case '*' -> a * b;
            case '/' -> a / b;
            default -> 0;
        };
    }
}