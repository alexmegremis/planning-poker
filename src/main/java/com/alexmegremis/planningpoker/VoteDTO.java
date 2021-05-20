package com.alexmegremis.planningpoker;

import com.vaadin.server.FontAwesome;
import lombok.*;
import org.springframework.util.StringUtils;

import java.io.Serializable;

@Builder
public class VoteDTO extends Hideable implements Serializable {

    private SessionDTO session;
    private PlayerDTO  player;
    @Getter
    private String     privateVote;

    public PlayerDTO getPlayer() {
        return player;
    }

    public String getVoterName() {
        return player.getHideable();
    }

    public void vote(final String vote) {
        privateVote = vote;
        hide();
    }

    @Override
    protected String getHideableValue() {
        return privateVote;
    }
}
