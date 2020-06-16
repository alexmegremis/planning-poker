package com.alexmegremis.planningpoker;

import com.vaadin.ui.*;

import java.util.Optional;

public class SessionForm extends FormLayout {

    private TextField sessionId           = new TextField("Session ID");
    private TextField sessionName         = new TextField("Session Name");
    private Button    findSessionButton   = new Button("Find");
    private Button    createSessionButton = new Button("Create");
    private SessionDTO session;

    private PokerUI pokerUI;

    public SessionForm(final PokerUI pokerUI) {
        this.pokerUI = pokerUI;
        setSizeUndefined();
        addComponents(sessionId, findSessionButton);
        addComponents(sessionName, createSessionButton);

        createSessionButton.addClickListener(e -> this.save());
        findSessionButton.addClickListener(e -> this.find());
    }

    private void save() {
        SessionDTO session = PokerService.createSession(sessionName.getValue().trim());
        sessionName.clear();
        pokerUI.setSession(session);
    }

    private void find() {
        Optional<SessionDTO> session = PokerService.findSession(this.sessionId.getValue().trim());
        if(session.isPresent()) {
            this.sessionId.clear();
            pokerUI.setSession(session.get());
        } else {
            Notification.show("Session ID not found", Notification.Type.ERROR_MESSAGE);
        }
    }
}
