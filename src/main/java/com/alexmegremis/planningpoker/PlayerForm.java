package com.alexmegremis.planningpoker;

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
            PlayerDTO player = PokerService.createPlayer(playerName.getValue());
            playerName.clear();
            pokerUI.setPlayer(player);
        } else {
            Notification.show("Name cannot be blank", Notification.Type.ERROR_MESSAGE);
        }
    }
}
