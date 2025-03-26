package org.example.utils.events;


import org.example.domain.Message;

public class MessageChangeEvent implements Event {

    private final ChangeEventType type;
    private final Message data;

    public MessageChangeEvent(ChangeEventType type, Message data) {
        this.type = type;
        this.data = data;
    }

    public ChangeEventType getType() {
        return type;
    }

    public Message getData() {
        return data;
    }
}
