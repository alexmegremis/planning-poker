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

    private static final List<PlayerDTO>  players  = new CopyOnWriteArrayList<>();
    private static final List<SessionDTO> sessions = new CopyOnWriteArrayList<>();
    @Autowired
    private              JiraService      jiraService;

    public static String getVoteResults(final SessionDTO session) {

        String result = "n/a";

        if (session.getShowVotes()) {
            Map<String, Long> collect = session.getVotes()
                                               .stream()
                                               .map(VoteDTO :: getPrivateVote)
                                               .filter(privateVote -> ! StringUtils.isEmpty(privateVote))
                                               .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
            Optional<Long> max = collect.values().stream().max(Comparator.naturalOrder());
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

    public static Optional<SessionDTO> findSession(final String sessionId) {
        return sessions.stream().filter(s -> s.getId().equals(sessionId)).findFirst();
    }

    public static void toggleVotes(final SessionDTO session) {
        session.setShowVotes(! session.getShowVotes());
        session.getVotes().forEach(v -> v.setHidden(! session.getShowVotes()));
        session.updateLastModificationTimestamp();
    }

    public static void hideVotes(final SessionDTO session) {
        session.setShowVotes(false);
        session.getVotes().forEach(v -> v.setHidden(! session.getShowVotes()));
        session.updateLastModificationTimestamp();
    }

    public static void togglePlayersVisible(final SessionDTO session) {
        session.setShowPlayers(! session.getShowPlayers());
        session.getPlayers().forEach(p -> p.setHidden(! session.getShowPlayers()));
        session.updateLastModificationTimestamp();
    }

    public static void toggleVotingOpen(final SessionDTO session) {
        session.setVotingOpen(! session.getVotingOpen());
        session.updateLastModificationTimestamp();
    }

    public static void resetVotes(final SessionDTO session) {
        session.getPlayers().forEach(p -> PokerService.vote(session, p, ""));
        session.setShowVotes(false);
        session.setVotingOpen(false);
        session.getVotes().forEach(v -> v.setHidden(! session.getShowVotes()));
        session.updateLastModificationTimestamp();
    }

    public static boolean vote(final SessionDTO session, final PlayerDTO player, final String vote) {
        boolean didVote = session.voteInSession(player, vote);
        if (didVote) {
            hideVotes(session);
        }
        return didVote;
    }

    public SessionDTO createSession(final String sessionName) {
        String     sessionId = String.valueOf(getUniqueId(new ArrayList<>(sessions)));
        SessionDTO result    = SessionDTO.builder().id(sessionId).name(sessionName).showVotes(false).votingOpen(false).showPlayers(false).build();
        sessions.add(result);

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

    public void findJiraIssue(final SessionDTO session, final String issueKey) {
        jiraService.getIssueByKey(this, session, issueKey);
    }

    public void findJiraIssueResponse(final JiraIssueDTO issue, final SessionDTO session) {
        session.setJiraIssue(issue);
        session.updateLastModificationTimestamp();
    }
}
