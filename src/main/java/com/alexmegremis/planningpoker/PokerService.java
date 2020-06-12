package com.alexmegremis.planningpoker;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

public class PokerService {

    // 1, Alex
    public static final ConcurrentMap<String, PlayerDTO>  players  = new ConcurrentHashMap<>();
    // 1, Sprint 12
    public static final ConcurrentMap<String, SessionDTO> sessions = new ConcurrentHashMap<>();

    public static final ConcurrentMap<SessionDTO, List<VoteDTO>> votes = new ConcurrentHashMap<>();
    public static final ConcurrentMap<SessionDTO, Long> modification = new ConcurrentHashMap<>();

    public static SessionDTO createSession(final String sessionName) {
        String     sessionId = String.valueOf((Math.round(Math.random() * ((999999 - 100000) + 1)) + 100000));
        SessionDTO result    = new SessionDTO(sessionId, sessionName);
        sessions.put(sessionId, result);
        modification.put(result, Instant.now().toEpochMilli());
        return result;
    }

    public static PlayerDTO createPlayer(final String playerName) {
        String    playerId = String.valueOf((Math.round(Math.random() * ((999999 - 100000) + 1)) + 100000));
        PlayerDTO result   = new PlayerDTO(playerId, playerName);
        players.put(playerId, result);
        return result;
    }

    public static void vote(final SessionDTO session, final PlayerDTO player, final String vote) {
        votes.putIfAbsent(session, new CopyOnWriteArrayList<>());
        Objects.requireNonNull(votes.get(session)).add(new VoteDTO(session, player, vote));
        modification.put(session, Instant.now().toEpochMilli());
    }

    public static SessionDTO findSession(final String sessionId) {
        return sessions.get(sessionId);
    }
}
