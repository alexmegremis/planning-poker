package com.alexmegremis.planningpoker.integration.jira;

import com.alexmegremis.planningpoker.PokerUI;
import com.vaadin.ui.*;

public class CredentialsView {

    private PokerUI pokerUI;

    public CredentialsView(final PokerUI pokerUI) {
        this.pokerUI = pokerUI;

        // Content for the PopupView
        FormLayout popupContent = new FormLayout();

        TextField username = new TextField("Username");
        PasswordField password = new PasswordField("Password");
        TextField jiraBaseUri = new TextField("Jira base");

        popupContent.addComponent(username);
        popupContent.addComponent(password);
        popupContent.addComponent(jiraBaseUri);
        popupContent.addComponent(new Button("Submit"));


// The component itself
        PopupView popup = new PopupView("Pop it up", popupContent);
    }
}
