package com.alexmegremis.planningpoker;

import com.vaadin.server.FontAwesome;
import lombok.*;

import java.io.Serializable;

@Builder
public class VoteDTO implements Serializable {

    private SessionDTO session;
    private PlayerDTO  player;
    @Getter
    private String     vote;
    @Getter
    private String     privateVote;

    public PlayerDTO getPlayer() {
        return player;
    }

    public String getPlayerName() {
        return player.getName();
    }

    public void revealVote() {
        vote = privateVote;
    }

    public void hideVote() {
        vote = FontAwesome.EYE_SLASH.getHtml();
    }

    public void vote(final String vote) {
        privateVote = vote;
    }
}
