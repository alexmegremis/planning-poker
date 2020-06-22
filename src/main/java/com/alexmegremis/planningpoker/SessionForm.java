package com.alexmegremis.planningpoker;

import com.vaadin.ui.*;
import org.springframework.util.StringUtils;

import java.util.Optional;

public class SessionForm extends FormLayout {

    private TextField sessionIdInput    = new TextField("Session ID");
    private TextField sessionNameInput  = new TextField("Session Name");
    private Button    findSessionButton = new Button("Find");
    private Button    createSessionButton = new Button("Create");
    private SessionDTO session;

    private PokerUI pokerUI;
    private PokerService pokerService;

    public SessionForm(final PokerUI pokerUI, final PokerService pokerService, final String sessionId) {
        this.pokerUI = pokerUI;
        this.pokerService = pokerService;
        setSizeUndefined();
        addComponents(this.sessionIdInput, findSessionButton);
        addComponents(this.sessionNameInput, createSessionButton);

        createSessionButton.addClickListener(e -> this.save());
        findSessionButton.addClickListener(e -> this.find());
        if(!StringUtils.isEmpty(sessionId)) {
            sessionIdInput.setValue(sessionId);
            sessionIdInput.setReadOnly(true);
            findSessionButton.click();
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
