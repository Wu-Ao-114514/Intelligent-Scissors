package A2;

import java.util.Random;
import java.util.Scanner;
import java.util.Stack;

import static A2.P5.*;
import static A2.P5.calculate;
import static A2.P5.precedence;

public class P5Test {
    private static final String[] OPERATORS = {"+", "-", "*", "/"};
    private static final Random RANDOM = new Random();

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        System.out.println("Enter the times of test: ");
        int testTime = input.nextInt();
        for (int i = 0; i < testTime; i++) {
            int num = RANDOM.nextInt(4,10);
            String test = generateRandomExpression(num);
            System.out.printf("Test %d: \n",i+1);
            System.out.println(test);
            String postfix = infixToPostfix(test);
            double result = evaluatePostfix(postfix);
            System.out.printf("%s,%.2f,%b\n\n", postfix,result,result==infixCalculator(test));

        }

    }
    public static String generateRandomExpression(int numOperands) {
        StringBuilder expression = new StringBuilder();
        for (int i = 0; i < numOperands; i++) {

            expression.append(RANDOM.nextInt(99)); // 0 到 9 之间的数字
            if (i < numOperands - 1) {
                String operator = OPERATORS[RANDOM.nextInt(OPERATORS.length)];
                expression.append(" ").append(operator).append(" ");
            }
        }
        return expression.toString();
    }
    public static double infixCalculator(String s) {
        Stack<Double> values = new Stack<>();
        Stack<Character> operators = new Stack<>();

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            if (c == ' ')
                continue;
            if (Character.isDigit(c)) {
                StringBuilder sb = new StringBuilder();
                while (i < s.length() && (Character.isDigit(s.charAt(i)) || s.charAt(i) == '.')) {
                    sb.append(s.charAt(i));
                    i++;
                }
                values.push(Double.parseDouble(sb.toString()));
                i--;
            }

            else if (c == '(') {
                operators.push(c);
            }
            else if (c == ')') {
                while (!operators.isEmpty() && operators.peek() != '(') {
                    double b = values.pop();
                    double a = values.pop();
                    char operator = operators.pop();
                    values.push(calculate(a, b, operator));
                }
                operators.pop();
            }
            else if (isOperator(c)) {
                while (!operators.isEmpty() && precedence(c) <= precedence(operators.peek())) {
                    double b = values.pop();
                    double a = values.pop();
                    char operator = operators.pop();
                    values.push(calculate(a, b, operator));
                }
                operators.push(c);
            }
        }
        while (!operators.isEmpty()) {
            double b = values.pop();
            double a = values.pop();
            char operator = operators.pop();
            values.push(calculate(a, b, operator));
        }
        return values.pop();
    }
}
