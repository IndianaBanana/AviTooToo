package org.banana.dto;

/**
 * Created by Banana on 27.04.2025
 */
public class ValidationConstants {

    public static final int PASSWORD_MIN_LENGTH = 4;
    /**
     * OWASP Email Validation Regular Expression
     */
    public static final String EMAIL_REGEX = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    public static final String PHONE_REGEX = "^\\\\+?[0-9]{10,18}$";
    public static final String PHONE_ERROR_MESSAGE = "Invalid phone number";
}
