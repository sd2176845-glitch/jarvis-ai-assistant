package com.jarvis.assistant;

import java.util.Locale;

/** Bounded, local-only arithmetic parser. Never evaluates Java or script code. */
public final class MathExpressionRules {
    private static final int MAX_LENGTH = 120;
    private static final int MAX_DEPTH = 20;
    private static final double MAX_ABS_VALUE = 1.0e100;

    private MathExpressionRules() { }

    public static double evaluate(String expression) {
        if (expression == null || expression.trim().isEmpty() || expression.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Enter an expression up to 120 characters.");
        }
        Parser parser = new Parser(expression);
        double result = parser.parseExpression();
        parser.skipSpaces();
        if (!parser.atEnd()) throw new IllegalArgumentException("Unexpected character at position " + (parser.position + 1) + ".");
        return checked(result);
    }

    public static String format(double value) {
        checked(value);
        String formatted = String.format(Locale.US, "%.8f", value);
        int end = formatted.length();
        while (end > 0 && formatted.charAt(end - 1) == '0') end--;
        if (end > 0 && formatted.charAt(end - 1) == '.') end--;
        String result = formatted.substring(0, end);
        return "-0".equals(result) || result.isEmpty() ? "0" : result;
    }

    private static double checked(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value) || Math.abs(value) > MAX_ABS_VALUE) {
            throw new IllegalArgumentException("Result is outside the supported range.");
        }
        return value;
    }

    private static final class Parser {
        private final String input;
        private int position;
        private int depth;

        Parser(String input) { this.input = input; }

        boolean atEnd() { return position >= input.length(); }

        void skipSpaces() {
            while (!atEnd() && Character.isWhitespace(input.charAt(position))) position++;
        }

        boolean take(char expected) {
            skipSpaces();
            if (!atEnd() && input.charAt(position) == expected) {
                position++;
                return true;
            }
            return false;
        }

        double parseExpression() {
            double value = parseTerm();
            while (true) {
                if (take('+')) value = checked(value + parseTerm());
                else if (take('-')) value = checked(value - parseTerm());
                else return value;
            }
        }

        double parseTerm() {
            double value = parseUnary();
            while (true) {
                if (take('*')) value = checked(value * parseUnary());
                else if (take('/')) {
                    double divisor = parseUnary();
                    if (divisor == 0.0d) throw new IllegalArgumentException("Cannot divide by zero.");
                    value = checked(value / divisor);
                } else return value;
            }
        }

        double parseUnary() {
            if (take('+')) return parseUnary();
            if (take('-')) return checked(-parseUnary());
            return parsePower();
        }

        double parsePower() {
            double base = parsePrimary();
            if (take('^')) {
                double exponent = parseUnary();
                if (Math.abs(exponent) > 12.0d) {
                    throw new IllegalArgumentException("Power is limited to an exponent between -12 and 12.");
                }
                base = checked(Math.pow(base, exponent));
            }
            return base;
        }

        double parsePrimary() {
            skipSpaces();
            if (take('(')) {
                depth++;
                if (depth > MAX_DEPTH) throw new IllegalArgumentException("Too many nested parentheses.");
                double value = parseExpression();
                if (!take(')')) throw new IllegalArgumentException("Missing closing parenthesis.");
                depth--;
                return value;
            }
            return parseNumber();
        }

        double parseNumber() {
            skipSpaces();
            int start = position;
            boolean digits = false;
            while (!atEnd() && Character.isDigit(input.charAt(position))) {
                position++;
                digits = true;
            }
            if (!atEnd() && input.charAt(position) == '.') {
                position++;
                while (!atEnd() && Character.isDigit(input.charAt(position))) {
                    position++;
                    digits = true;
                }
            }
            if (!digits) throw new IllegalArgumentException("Expected a number at position " + (position + 1) + ".");
            try {
                return checked(Double.parseDouble(input.substring(start, position)));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid number.");
            }
        }
    }
}
