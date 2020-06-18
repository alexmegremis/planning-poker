package com.alexmegremis.planningpoker;

import com.vaadin.server.UserError;
import com.vaadin.ui.*;
import org.springframework.util.StringUtils;

public class PlayerForm extends FormLayout {

    private TextField playerName         = new TextField("Player Name");
    private Button    playerCreateButton = new Button("Create");

    private PokerUI      pokerUI;
    private PokerService pokerService;

    public PlayerForm(final PokerUI pokerUI, final PokerService pokerService) {
        this.pokerUI = pokerUI;
        this.pokerService = pokerService;
        setSizeUndefined();
        addComponents(playerName, playerCreateButton);
        playerCreateButton.addClickListener(e -> this.save());
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
