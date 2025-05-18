package org.banana.exception;

public class AddingCommentWhenParentCommenterIsNullException extends AbstractConflictException {

    private static final String MESSAGE = "You can't reply to deleted comment";

    public AddingCommentWhenParentCommenterIsNullException() {
        super(MESSAGE);
    }
}
