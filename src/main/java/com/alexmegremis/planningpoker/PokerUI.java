package com.alexmegremis.planningpoker;

import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.*;

@SpringUI
public class PokerUI extends UI {

    private TextField playerName = new TextField();
    private TextField sessionName = new TextField();

    @Override
    protected void init(final VaadinRequest vaadinRequest) {
        final GridLayout playerLayout = new GridLayout(5, 10);
        final GridLayout sessionLayout = new GridLayout(5, 10);
        final VerticalLayout pokerLayout = new VerticalLayout();

        playerName.setPlaceholder("your name");
        sessionName.setPlaceholder("session name");


        final Button addPlayerButton = new Button("Add");
        addPlayerButton.addClickListener(clickEvent -> {
            PokerService.createPlayer(playerName.getValue());
            setContent(sessionLayout);
            sessionLayout.setVisible(true);
        });


        final Button createSessionButton = new Button("Create Session");
        addPlayerButton.addClickListener(clickEvent -> {
            PokerService.createSession(sessionName.getValue());
            setContent(pokerLayout);
            pokerLayout.setVisible(true);
        });

        playerLayout.addComponent(playerName, 2, 2);
        playerLayout.addComponent(addPlayerButton, 3, 2);
        sessionLayout.addComponent(sessionName, 2, 2);
        sessionLayout.addComponent(createSessionButton, 3, 2);

        this.setContent(playerLayout);
    }
}
