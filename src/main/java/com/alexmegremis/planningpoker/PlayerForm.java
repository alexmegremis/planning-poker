package com.alexmegremis.planningpoker;

import com.vaadin.data.Binder;
import com.vaadin.ui.*;

public class PlayerForm extends FormLayout {
    private TextField playerName = new TextField("Player Name");
    private Button playerCreateButton = new Button("Create");
    Binder<PlayerDTO> binder = new Binder<>(PlayerDTO.class);

    private PokerUI pokerUI;

    public PlayerForm(final PokerUI pokerUI) {
        this.pokerUI = pokerUI;
        setSizeUndefined();
        addComponents(playerName, playerCreateButton);

        binder.bindInstanceFields(this);

        playerCreateButton.addClickListener(e -> this.save());
    }

    private void save() {
        PokerService.createPlayer(playerName.getValue());
        playerName.clear();
        setVisible(false);
    }
}
