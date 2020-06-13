package com.alexmegremis.planningpoker;

import lombok.*;

import java.io.Serializable;

@Builder
public class VoteDTO implements Serializable {

    private SessionDTO session;
    private PlayerDTO  player;
    @Getter
    @Setter
    private String     vote;

    public PlayerDTO getPlayer() {
        return player;
    }

    public String getPlayerName() {
        return player.getName();
    }
}
