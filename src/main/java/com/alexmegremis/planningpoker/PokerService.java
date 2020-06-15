package com.alexmegremis.planningpoker;

import com.vaadin.spring.annotation.SpringComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@SpringComponent
public class PokerService {

    // 1, Alex
    private static final List<PlayerDTO>  players  = new CopyOnWriteArrayList<>();
    // 1, Sprint 12
    private static final List<SessionDTO> sessions = new CopyOnWriteArrayList<>();

    public static SessionDTO createSession(final String sessionName) {
        String     sessionId = String.valueOf((Math.round(Math.random() * ((999999 - 100000) + 1)) + 100000));
        SessionDTO result    = SessionDTO.builder().id(sessionId).name(sessionName).build();
        sessions.add(result);

        for (int i = 1; i < 4; i++) {
            PlayerDTO player = createPlayer("Session_" + sessionId + "_TestPlayer_" + i);
            result.addPlayer(player);
            int randomNum = ThreadLocalRandom.current().nextInt(0, PokerUI.nums.length);

            vote(result, player, PokerUI.nums[randomNum]);
        }

        return result;
    }

    public static PlayerDTO createPlayer(final String playerName) {
        String    playerId = String.valueOf((Math.round(Math.random() * ((999999 - 100000) + 1)) + 100000));
        PlayerDTO result   = new PlayerDTO(playerId, playerName);
        players.add(result);
        return result;
    }

    public static String getVoteResults(final SessionDTO session) {

        Map<String, Long> collect = session.getVotes()
                                           .stream()
                                           .filter(v -> ! StringUtils.isEmpty(v.getPrivateVote()))
                                           .map(VoteDTO :: getPrivateVote)
                                           .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        Optional<Long>    max     = collect.values().stream().max(Comparator.naturalOrder());
        String            result  = "n/a";
        if (max.isPresent()) {
            result = collect.entrySet().stream().filter(e -> e.getValue().equals(max.get())).map(Map.Entry :: getKey).map(String :: valueOf).collect(Collectors.joining(","));
            result = result + " with " + max.get() + " votes";
        }

        log.info(">>> Vote result : {}", result);
        return result;
    }

    public static void removePlayer(final PlayerDTO player) {
        players.remove(player);
        sessions.forEach(session -> session.removePlayer(player));
    }

    public static void vote(final SessionDTO session, final PlayerDTO player, final String vote) {
        session.voteInSession(player, vote);
        log.info(">>> {} voted {}", player.getName(), vote);
        hideVotes(session);
    }

    public static Optional<SessionDTO> findSession(final String sessionId) {
        return sessions.stream().filter(s -> s.getId().equals(sessionId)).findFirst();
    }

    public static void revealVotes(final SessionDTO session) {
        session.getVotes().forEach(VoteDTO :: revealVote);
        session.updateLastModificationTimestamp();
    }

    public static void hideVotes(final SessionDTO session) {
        session.getVotes().forEach(VoteDTO :: hideVote);
    }

    public static void resetVotes(final SessionDTO session) {
        session.getPlayers().forEach(p -> PokerService.vote(session, p, ""));
    }
}
