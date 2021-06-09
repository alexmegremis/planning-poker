package com.alexmegremis.planningpoker;

import com.alexmegremis.planningpoker.integration.jira.JiraIssueDTO;

import java.util.Collection;
import java.util.Optional;

public interface PokerService {

    String getVoteResults(SessionDTO session);

    void removePlayer(PlayerDTO player);
    void addPlayer(PlayerDTO player, SessionDTO session);

    Optional<SessionDTO> findSession(String sessionId);

    void toggleVotes(SessionDTO session);

    void hideVotes(SessionDTO session);

    void togglePlayersVisible(SessionDTO session);

    void toggleVotingOpen(SessionDTO session);

    void resetVotes(SessionDTO session);

    boolean vote(SessionDTO session, PlayerDTO player, String vote);

    SessionDTO createSession(String sessionName);

    String getUniqueId(Collection<Identifiable> existing);

    boolean exists(String id, Collection<Identifiable> existing);

    PlayerDTO createPlayer(String playerName);

    void findJiraIssue(SessionDTO session, String issueKey);

    void findJiraIssueResponse(JiraIssueDTO issue, SessionDTO session);
}
