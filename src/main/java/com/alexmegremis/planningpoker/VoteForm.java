package com.alexmegremis.planningpoker;

import com.vaadin.ui.GridLayout;

public class VoteForm extends GridLayout {

    private PokerUI pokerUI;

    public VoteForm(final PokerUI pokerUI) {
        this.pokerUI = pokerUI;

        GridLayout gridLayout = new GridLayout();
    }
}
