package com.alexmegremis.planningpoker;

import com.vaadin.data.Binder;
import com.vaadin.ui.*;

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

//        binder.bindInstanceFields(this);

        createSessionButton.addClickListener(e -> this.save());
        findSessionButton.addClickListener(e -> this.find());
    }

    private void save() {
        SessionDTO session = PokerService.createSession(sessionName.getValue());
        sessionName.clear();
        pokerUI.setSession(session);
    }

    private void find() {
        SessionDTO session = PokerService.findSession(this.sessionId.getValue());
        if(session != null) {
            this.sessionId.clear();
            pokerUI.setSession(session);
        } else {
            Notification.show("Session ID not found", Notification.Type.ERROR_MESSAGE);
        }
    }
}
