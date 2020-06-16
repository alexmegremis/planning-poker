package com.alexmegremis.planningpoker;

import com.vaadin.server.UserError;
import com.vaadin.ui.*;
import org.springframework.util.StringUtils;

public class PlayerForm extends FormLayout {
    private TextField playerName = new TextField("Player Name");
    private Button playerCreateButton = new Button("Create");
    private PlayerDTO player;

    private PokerUI pokerUI;

    public PlayerForm(final PokerUI pokerUI) {
        this.pokerUI = pokerUI;
        setSizeUndefined();
        addComponents(playerName, playerCreateButton);
        playerCreateButton.addClickListener(e -> this.save());
    }

    private void save() {
        if(!StringUtils.isEmpty(playerName.getValue())) {
            playerName.setComponentError(null);
            PlayerDTO player = PokerService.createPlayer(playerName.getValue().trim());
            playerName.clear();
            pokerUI.setPlayer(player);
        } else {
            playerName.setComponentError(new UserError("Required"));
        }
    }
}
