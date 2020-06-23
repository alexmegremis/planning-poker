package com.alexmegremis.planningpoker;

import com.alexmegremis.planningpoker.integration.jira.JiraIssueDTO;
import com.alexmegremis.planningpoker.integration.jira.JiraService;
import com.vaadin.spring.annotation.SpringComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@SpringComponent
@Scope (value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class PokerService {

    @Autowired
    private JiraService jiraService;

    private static final List<PlayerDTO>  players  = new CopyOnWriteArrayList<>();
    private static final List<SessionDTO> sessions = new CopyOnWriteArrayList<>();

    public SessionDTO createSession(final String sessionName) {
        String     sessionId = String.valueOf(getUniqueId(new ArrayList<>(sessions)));
        SessionDTO result    = SessionDTO.builder().id(sessionId).name(sessionName).showVotes(false).build();
        sessions.add(result);

//        for (int i = 1; i < 4; i++) {
//            PlayerDTO player = createPlayer("Session_" + sessionId + "_TestPlayer_" + i);
//            result.addPlayer(player);
//            int randomNum = ThreadLocalRandom.current().nextInt(0, PokerUI.nums.length);
//
//            vote(result, player, PokerUI.nums[randomNum]);
//        }

        return result;
    }

    public static String getUniqueId(final Collection<Identifiable> existing) {

        String newId;

        do {
            newId = String.valueOf(Math.toIntExact(Math.round(Math.random() * ((999999 - 100000) + 1)) + 100000));
        } while (exists(newId, existing));

        return newId;
    }

    public static boolean exists(final String id, final Collection<Identifiable> existing) {
        return existing.stream().anyMatch(i -> i.getId().equals(id));
    }

    public PlayerDTO createPlayer(final String playerName) {
        String    playerId = String.valueOf(getUniqueId(new ArrayList<>(players)));
        PlayerDTO result   = new PlayerDTO(playerId, playerName);
        players.add(result);
        return result;
    }

    public static String getVoteResults(final SessionDTO session) {

        String result = "n/a";

        if (session.getShowVotes()) {
            Map<String, Long> collect = session.getVotes()
                                               .stream()
                                               .filter(v -> ! StringUtils.isEmpty(v.getPrivateVote()))
                                               .map(VoteDTO :: getPrivateVote)
                                               .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
            Optional<Long>    max     = collect.values().stream().max(Comparator.naturalOrder());
            if (max.isPresent()) {
                result = collect.entrySet().stream().filter(e -> e.getValue().equals(max.get())).map(Map.Entry :: getKey).map(String :: valueOf).collect(Collectors.joining(","));
                result = result + " with " + max.get() + " votes";
            }
        }

        log.info(">>> Vote result : {}", result);
        return result;
    }

    public static void removePlayer(final PlayerDTO player) {
        players.remove(player);
        sessions.forEach(session -> session.removePlayer(player));
    }

    public static boolean vote(final SessionDTO session, final PlayerDTO player, final String vote) {
        boolean didVote = session.voteInSession(player, vote);
        if (didVote) {
            log.info(">>> {} voted {}", player.getName(), vote);
            hideVotes(session);
        }
        return didVote;
    }

    public static Optional<SessionDTO> findSession(final String sessionId) {
        return sessions.stream().filter(s -> s.getId().equals(sessionId)).findFirst();
    }

    public static void revealVotes(final SessionDTO session) {
        session.setShowVotes(true);
        session.getVotes().forEach(VoteDTO :: revealVote);
        session.updateLastModificationTimestamp();
    }

    public static void hideVotes(final SessionDTO session) {
        session.getVotes().forEach(VoteDTO :: hideVote);
        session.setShowVotes(false);
        session.updateLastModificationTimestamp();
    }

    public static void resetVotes(final SessionDTO session) {
        session.getPlayers().forEach(p -> PokerService.vote(session, p, ""));
        session.setShowVotes(false);
        session.updateLastModificationTimestamp();
    }

    public void findJiraIssue(final SessionDTO session, final String issueKey) {
        jiraService.getIssueByKey(this, session, issueKey);
    }

    public void findJiraIssueResponse(final JiraIssueDTO issue, final SessionDTO session) {
        session.setJiraIssue(issue);
        session.updateLastModificationTimestamp();
    }
}
