package org.banana.exception;

/**
 * Created by Banana on 04.05.2025
 */
public class AddingCommentWhenParentCommenterIsNullException extends RuntimeException {

    private static final String MESSAGE = "You can't reply to deleted comment";

    public AddingCommentWhenParentCommenterIsNullException() {
        super(MESSAGE);
    }
}
