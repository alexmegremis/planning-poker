package com.alexmegremis.planningpoker;

import com.vaadin.server.FontAwesome;
import lombok.*;
import org.springframework.util.StringUtils;

import java.io.Serializable;

@Builder
public class VoteDTO extends Hideable implements Serializable {

    private SessionDTO session;
    @Getter
    private PlayerDTO  player;
    @Getter
    private String     privateVote;

    public void vote(final String vote) {
        privateVote = vote;
        hide();
    }

    @Override
    protected String getHideableValue() {
        return privateVote;
    }
}
