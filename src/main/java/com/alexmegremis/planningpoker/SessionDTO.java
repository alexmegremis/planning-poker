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

    @Getter
    private String id;
    @Getter
    private String name;
    @Getter
    private Long   lastModificationTimestamp = Instant.now().toEpochMilli();
    @Getter
    @Setter
    private String voteResult;
    @Getter
    @Setter
    private Boolean showVotes = false;
    @Getter
    private JiraIssueDTO jiraIssue;
    @Getter
    private PokerUI pokerUI;

    @Getter
    private final List<PlayerDTO> players = new CopyOnWriteArrayList<>();

    @Getter
    private final List<VoteDTO> votes = new CopyOnWriteArrayList<>();

    public void addPlayer(final PlayerDTO player) {
        players.add(player);
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

    public void voteInSession(final PlayerDTO player, final String vote) {
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
        voteResult = PokerService.getVoteResults(this);
        updateLastModificationTimestamp();
    }

    public void setJiraIssue(final JiraIssueDTO jiraIssue) {
        this.jiraIssue = jiraIssue;
        updateLastModificationTimestamp();
    }

    public void updateLastModificationTimestamp() {
        lastModificationTimestamp = Instant.now().toEpochMilli();
        PokerUI.updateAll();
    }

    public void setPokerUI(final PokerUI pokerUI) {
        this.pokerUI = pokerUI;
        this.pokerUI.setSession(this);
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
