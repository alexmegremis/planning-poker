package com.alexmegremis.planningpoker;

import com.vaadin.ui.*;

import java.util.function.Consumer;

public class ConfirmationDialogue extends Window {

    public <T> ConfirmationDialogue(final PokerUI pokerUI, T value, Consumer<T> consumer) {
        super("Are you sure?");
        center();
        setClosable(false);
        Button yes = new Button("Yes", event -> {
            consumer.accept(value);
            close();
        });
        Button           no      = new Button("No", event -> close());
        HorizontalLayout buttons = new HorizontalLayout(yes, no);
        buttons.setMargin(true);
        this.setModal(true);
        setContent(buttons);
        setWidth("20em");
    }
}
