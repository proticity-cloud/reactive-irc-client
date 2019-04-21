package org.proticity.irc.client.parser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class IrcParseExceptionTest {
    @Test
    public void testExceptionProperties() {
        var e = new IrcParseException("test input", 4, "Error in parser.");
        Assertions.assertEquals("test input", e.getInput());
        Assertions.assertEquals(4, e.getPosition());
        Assertions.assertEquals("Error in parser.", e.getMessage());
    }

    @Test
    public void testExceptionPropertiesWithCause() {
        var cause = new Throwable();
        var e = new IrcParseException("test input", 4, "Error in parser.", cause);
        Assertions.assertEquals("test input", e.getInput());
        Assertions.assertEquals(4, e.getPosition());
        Assertions.assertEquals("Error in parser.", e.getMessage());
        Assertions.assertSame(cause, e.getCause());
    }
}
