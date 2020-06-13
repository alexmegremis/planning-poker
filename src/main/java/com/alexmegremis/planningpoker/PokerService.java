package com.alexmegremis.planningpoker;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public class PokerService {

    // 1, Alex
    private static final List<PlayerDTO>  players  = new CopyOnWriteArrayList<>();
    // 1, Sprint 12
    private static final List<SessionDTO> sessions = new CopyOnWriteArrayList<>();

    public static SessionDTO createSession(final String sessionName) {
        String     sessionId = String.valueOf((Math.round(Math.random() * ((999999 - 100000) + 1)) + 100000));
        SessionDTO result    = SessionDTO.builder().id(sessionId).name(sessionName).build();
        sessions.add(result);
        return result;
    }

    public static PlayerDTO createPlayer(final String playerName) {
        String    playerId = String.valueOf((Math.round(Math.random() * ((999999 - 100000) + 1)) + 100000));
        PlayerDTO result   = new PlayerDTO(playerId, playerName);
        players.add(result);
        return result;
    }

    public static void removePlayer(final PlayerDTO player) {
        players.remove(player);
        sessions.forEach(session -> session.removePlayer(player));
    }

    public static void vote(final SessionDTO session, final PlayerDTO player, final String vote) {
        session.vote(player, vote);
    }

    public static Optional<SessionDTO> findSession(final String sessionId) {
        return sessions.stream().filter(s -> s.getId().equals(sessionId)).findFirst();
    }
}
