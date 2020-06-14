package com.alexmegremis.planningpoker;

import com.vaadin.ui.*;

public class PlayerForm extends FormLayout {
    private TextField playerName = new TextField("Player Name");
    private Button playerCreateButton = new Button("Create");
//    private Binder<PlayerDTO> binder = new Binder<>(PlayerDTO.class);
    private PlayerDTO player;

    private PokerUI pokerUI;

    public PlayerForm(final PokerUI pokerUI) {
        this.pokerUI = pokerUI;
        setSizeUndefined();
        addComponents(playerName, playerCreateButton);
        playerCreateButton.addClickListener(e -> this.save());

        playerName.setValue("Alex");
        playerName.setReadOnly(true);
    }

    private void save() {
        PlayerDTO player = PokerService.createPlayer(playerName.getValue());
        playerName.clear();
        pokerUI.setPlayer(player);
    }
}
