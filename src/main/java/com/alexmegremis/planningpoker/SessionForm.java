package com.alexmegremis.planningpoker;

import com.vaadin.ui.*;

import java.util.Optional;

public class SessionForm extends FormLayout {

    private final TextField sessionIdInput    = new TextField("Session ID");
    private final TextField sessionNameInput  = new TextField("Session Name");
    private final Button    findSessionButton = new Button("Find");
    private final Button    createSessionButton = new Button("Create");
    private SessionDTO session;

    private final PokerUI pokerUI;
    private final PokerService pokerService;

    public SessionForm(final PokerUI pokerUI, final PokerService pokerService, final SessionDTO existingSession) {
        this.pokerUI = pokerUI;
        this.pokerService = pokerService;
        setSizeUndefined();
        addComponents(this.sessionIdInput, findSessionButton);
        addComponents(this.sessionNameInput, createSessionButton);

        createSessionButton.addClickListener(e -> this.save());
        findSessionButton.addClickListener(e -> this.find());
        if (existingSession != null) {
            sessionIdInput.setValue(session.getId());
            sessionIdInput.setReadOnly(true);
        }
    }

    private void save() {
        SessionDTO session = pokerService.createSession(sessionNameInput.getValue().trim());
        pokerUI.setSession(session);
        sessionNameInput.clear();
    }

    private void find() {
        Optional<SessionDTO> session = PokerService.findSession(this.sessionIdInput.getValue().trim());
        if(session.isPresent()) {
            this.sessionIdInput.clear();
            pokerUI.setSession(session.get());
        } else {
            Notification.show("Session ID not found", Notification.Type.ERROR_MESSAGE);
        }
    }
}
