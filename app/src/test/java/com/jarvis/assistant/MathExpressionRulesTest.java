package com.jarvis.assistant;

import org.junit.Test;
import static org.junit.Assert.*;

public class MathExpressionRulesTest {
    @Test public void respectsArithmeticPrecedenceAndParentheses() {
        assertEquals(14.0, MathExpressionRules.evaluate("2 + 3 * 4"), 0.000001);
        assertEquals(20.0, MathExpressionRules.evaluate("(2 + 3) * 4"), 0.000001);
        assertEquals(5.0, MathExpressionRules.evaluate("18 / 3 - 1"), 0.000001);
    }

    @Test public void supportsUnaryAndRightAssociativePowers() {
        assertEquals(-4.0, MathExpressionRules.evaluate("-2^2"), 0.000001);
        assertEquals(512.0, MathExpressionRules.evaluate("2^3^2"), 0.000001);
        assertEquals(0.25, MathExpressionRules.evaluate("2^-2"), 0.000001);
    }

    @Test public void formatsWithoutTrailingZeros() {
        assertEquals("12", MathExpressionRules.format(12.0));
        assertEquals("0.125", MathExpressionRules.format(0.125));
        assertEquals("0", MathExpressionRules.format(-0.0));
    }

    @Test public void rejectsDivisionByZeroAndInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> MathExpressionRules.evaluate("5/0"));
        assertThrows(IllegalArgumentException.class, () -> MathExpressionRules.evaluate("2+"));
        assertThrows(IllegalArgumentException.class, () -> MathExpressionRules.evaluate("1;2"));
        assertThrows(IllegalArgumentException.class, () -> MathExpressionRules.evaluate(""));
    }

    @Test public void boundsExpressionLengthNestingAndPower() {
        assertThrows(IllegalArgumentException.class,
                () -> MathExpressionRules.evaluate(new String(new char[121]).replace('\0', '1')));
        assertThrows(IllegalArgumentException.class,
                () -> MathExpressionRules.evaluate("2^13"));
        assertThrows(IllegalArgumentException.class,
                () -> MathExpressionRules.evaluate("(((((((((((((((((((((1)))))))))))))))))))))"));
    }
}
