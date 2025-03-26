package org.example.utils.observable;

import org.example.utils.events.Event;

public interface Observer<E extends Event> {
    void update(E e);
}