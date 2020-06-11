package com.alexmegremis.planningpoker;

import java.util.*;

public class PokerService {

    // 1, Alex
    public static final Map<String, PlayerDTO> players  = new HashMap<>();
    // 1, Sprint 12
    public static final Map<String, SessionDTO> sessions = new HashMap<>();
    // 1 (session), (1 (player), 8 (vote))
    public static final Map<String, Map<String, String>> votes = new HashMap<>();

    public static String createSession(final String sessionName) {
        String sessionId = String.valueOf((Math.round(Math.random()*((999999-100000)+1))+100000));

        sessions.put(sessionId, new SessionDTO(sessionId, sessionName));
        return sessionId;
    }

    public static String createPlayer(final String playerName) {
        String playerId = String.valueOf((Math.round(Math.random()*((999999-100000)+1))+100000));
        players.put(playerId, new PlayerDTO(playerId, playerName));
        return playerId;
    }

    public static void vote(final String sessionId, final String playerId, final String vote) {
        Objects.requireNonNull(votes.putIfAbsent(sessionId, new HashMap<>())).put(playerId, vote);
    }

    public SessionDTO findSession(final String sessionId) {
        return sessions.get(sessionId);
    }
}
