package com.alexmegremis.planningpoker;

import com.alexmegremis.planningpoker.integration.jira.JiraIssueDTO;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Builder
public class SessionDTO implements Identifiable, Serializable {

    private Long lastModificationTimestamp = Instant.now().toEpochMilli();

    public synchronized Long getLastModificationTimestamp() {
        return lastModificationTimestamp;
    }

    @Getter
    private final String       id;
    @Getter
    private final String       name;
    @Getter
    @Setter
    private       Boolean      showVotes;
    @Getter
    @Setter
    private       Boolean      showPlayers;
    @Getter
    @Setter
    private       Boolean      votingOpen;
    @Getter
    private       JiraIssueDTO jiraIssue;
    @Getter
    private final PokerUI      pokerUI;

    @Getter
    private final List<PlayerDTO> players = new CopyOnWriteArrayList<>();

    @Getter
    private PlayerDTO owner;

    @Getter
    private final List<VoteDTO> votes = new CopyOnWriteArrayList<>();

    public void addPlayer(final PlayerDTO player) {
        players.add(player);
        if (this.owner == null) {
            this.owner = player;
        }
        PokerService.vote(this, player, "");
        updateLastModificationTimestamp();
    }

    public void removePlayer(final PlayerDTO player) {
        players.remove(player);
        votes.forEach(v -> {
            if (v.getPlayer().equals(player)) {
                votes.remove(v);
            }
        });
        updateLastModificationTimestamp();
    }

    public boolean voteInSession(final PlayerDTO player, final String vote) {
        boolean result = players.contains(player);
        if (result) {
            Optional<VoteDTO> existingVote = votes.stream().filter(aVote -> aVote.getPlayer().equals(player)).findFirst();
            if (existingVote.isPresent()) {
                existingVote.get().vote(vote);
                log.info(">>> {} voted {} - updated", player.getName(), vote);
            } else {
                VoteDTO newVote = VoteDTO.builder().session(this).player(player).build();
                newVote.vote(vote);
                votes.add(newVote);
                log.info(">>> {} voted {} - new", player.getName(), vote);
            }
            updateLastModificationTimestamp();
        }
        return result;
    }

    public void setJiraIssue(final JiraIssueDTO jiraIssue) {
        this.jiraIssue = jiraIssue;
        updateLastModificationTimestamp();
    }

    public void updateLastModificationTimestamp() {
        lastModificationTimestamp = Instant.now().toEpochMilli();
        PokerUI.updateAll();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final SessionDTO that = (SessionDTO) o;
        return id.equals(that.id) && name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
