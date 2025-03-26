package org.example.domain.validators;

import org.example.domain.Message;

public class MessageValidator implements Validator<Message> {

    @Override
    public void validate(Message entity) throws ValidationException {
        if(entity.getFrom() == null || entity.getTo().isEmpty() || entity.getMessage().isEmpty())
            throw new ValidationException("Message is not valid");

        if(entity.getFrom() == entity.getTo().get(0))
            throw new ValidationException("You can't send a message to yourself!");
    }
}
