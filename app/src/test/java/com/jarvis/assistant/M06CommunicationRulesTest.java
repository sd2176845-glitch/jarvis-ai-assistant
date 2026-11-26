package com.jarvis.assistant;

import org.junit.Test;
import static org.junit.Assert.*;

public class M06CommunicationRulesTest {
    @Test public void parsesAddressedSmsAndPreservesMessageCaseAndPunctuation() {
        M06CommunicationRules.AddressedDraft draft =
                M06CommunicationRules.parseAddressedDraft(
                        "Hey Jarvis message to Mom: Please Call Me, Thanks!", new String[]{"message", "sms", "text"});
        assertNotNull(draft);
        assertEquals("Mom", draft.recipient);
        assertEquals("Please Call Me, Thanks!", draft.body);
    }

    @Test public void parsesEmailRecipientAndBody() {
        M06CommunicationRules.AddressedDraft draft =
                M06CommunicationRules.parseAddressedDraft(
                        "Jarvis email to Roy@example.com: Hello Roy!", new String[]{"email", "mail"});
        assertNotNull(draft);
        assertEquals("Roy@example.com", draft.recipient);
        assertEquals("Hello Roy!", draft.body);
        assertTrue(M06CommunicationRules.isValidEmailAddress(draft.recipient));
    }

    @Test public void rejectsInvalidEmailAddresses() {
        assertFalse(M06CommunicationRules.isValidEmailAddress("not-an-email"));
        assertFalse(M06CommunicationRules.isValidEmailAddress("name@"));
        assertFalse(M06CommunicationRules.isValidEmailAddress(null));
    }

    @Test public void normalizesSmsNumbersAndRejectsInvalidValues() {
        assertEquals("+919876543210", M06CommunicationRules.normalizeSmsNumber("+91 98765-43210"));
        assertEquals("9876543210", M06CommunicationRules.normalizeSmsNumber("98765 43210"));
        assertTrue(M06CommunicationRules.isValidSmsNumber("+919876543210"));
        assertFalse(M06CommunicationRules.isValidSmsNumber("1234"));
        assertFalse(M06CommunicationRules.isValidSmsNumber(null));
    }

    @Test public void normalizesWhatsAppNumbersWithoutGuessingCountryCode() {
        assertEquals("919876543210", M06CommunicationRules.normalizeWhatsAppNumber("+91 98765-43210"));
        assertNull(M06CommunicationRules.normalizeWhatsAppNumber("12345"));
        assertNull(M06CommunicationRules.normalizeWhatsAppNumber(null));
    }

    @Test public void missingRecipientSeparatorIsNotParsed() {
        assertNull(M06CommunicationRules.parseAddressedDraft(
                "email to person@example.com hello", new String[]{"email"}));
    }
}
