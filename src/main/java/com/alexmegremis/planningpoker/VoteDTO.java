package com.alexmegremis.planningpoker;

import lombok.AllArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
public class VoteDTO implements Serializable {
    private SessionDTO session;
    private PlayerDTO player;
    private String vote;

    public PlayerDTO getPlayer() {
        return player;
    }

    public String getPlayerName() {
        return player.getName();
    }

    public String getVote() {
        return vote;
    }
}
