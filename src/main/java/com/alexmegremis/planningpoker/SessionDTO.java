package com.alexmegremis.planningpoker;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

@Builder
public class SessionDTO implements Serializable {

    @Getter
    private String id;
    @Getter
    private String name;
    @Getter
    private Long   lastModificationTimestamp = Instant.now().toEpochMilli();

    @Getter
    private final List<PlayerDTO> players = new CopyOnWriteArrayList<>();

    @Getter
    private final List<VoteDTO> votes = new CopyOnWriteArrayList<>();

    public void addPlayer(final PlayerDTO player) {
        players.add(player);
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

    public void vote(final PlayerDTO player, final String vote) {
        votes.add(new VoteDTO(this, player, vote));
        updateLastModificationTimestamp();
    }

    private void updateLastModificationTimestamp() {
        lastModificationTimestamp = Instant.now().toEpochMilli();
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
