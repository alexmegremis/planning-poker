package com.alexmegremis.planningpoker;

import com.vaadin.data.Binder;
import com.vaadin.ui.*;

public class SessionForm extends FormLayout {

    private TextField sessionId           = new TextField("Session ID");
    private TextField sessionName         = new TextField("Player Name");
    private Button    findSessionButton   = new Button("Find");
    private Button    createSessionButton = new Button("Create");
    Binder<SessionDTO> binder = new Binder<>(SessionDTO.class);

    private PokerUI pokerUI;

    public SessionForm(final PokerUI pokerUI) {
        this.pokerUI = pokerUI;
        setSizeUndefined();
        addComponents(sessionId, findSessionButton);
        addComponents(sessionName, createSessionButton);

        binder.bindInstanceFields(this);

        findSessionButton.addClickListener(e -> this.save());
    }

    private void save() {
        PokerService.createSession(sessionName.getValue());
        sessionName.clear();
        setVisible(false);
    }

    private void find() {
        PokerService.findSession(sessionId.getValue());
        sessionId.clear();
        setVisible(false);
    }
}
