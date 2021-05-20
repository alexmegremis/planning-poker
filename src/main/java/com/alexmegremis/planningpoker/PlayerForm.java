package com.alexmegremis.planningpoker;

import com.vaadin.server.UserError;
import com.vaadin.ui.*;
import org.springframework.util.StringUtils;

public class PlayerForm extends FormLayout {

    private final TextField playerName         = new TextField("Player Name");

    private final PokerUI      pokerUI;
    private final PokerService pokerService;

    public PlayerForm(final PokerUI pokerUI, final PokerService pokerService) {
        this.pokerUI = pokerUI;
        this.pokerService = pokerService;
        setSizeUndefined();

        Button playerCreateButton = new Button("Create");
        playerCreateButton.addClickListener(e -> this.save());

        addComponents(playerName, playerCreateButton);
    }

    private void save() {
        if (! StringUtils.isEmpty(playerName.getValue())) {
            playerName.setComponentError(null);
            PlayerDTO player = pokerService.createPlayer(playerName.getValue().trim());
            playerName.clear();
            pokerUI.setPlayer(player);
        } else {
            playerName.setComponentError(new UserError("Required"));
        }
    }
}
