package com.alexmegremis.planningpoker;

import com.vaadin.annotations.*;
import com.vaadin.navigator.View;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.communication.PushMode;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Theme ("valo")
@PreserveOnRefresh
@Push (PushMode.AUTOMATIC)
@SpringUI
@SpringView
public class PokerUI extends UI implements Serializable, View {

    private final PlayerForm  playerForm  = new PlayerForm(this);
    private final SessionForm sessionForm = new SessionForm(this);

    private PlayerDTO  player;
    private SessionDTO session;
    private Long       knownSessionTimestamp;

    private final List<VoteDTO> votes     = new ArrayList<>();
    private final Grid<VoteDTO> votesGrid = new Grid<>(VoteDTO.class);

    private final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    private       Runnable               bgChecker;

    private final Label      labelSessionId        = new Label("Session ID");
    private final Label      labelSessionIdValue   = new Label();
    private final Label      labelSessionName      = new Label("Session Name");
    private final Label      labelSessionNameValue = new Label();
    private final Label      labelPlayerCount      = new Label("Player Count");
    private final Label      labelPlayerCountValue = new Label();
    private final Label      labelPlayerName       = new Label("Your Name");
    private final Label      labelPlayerNameValue  = new Label();
    private final GridLayout sessionDetailsLayout  = new GridLayout(7, 2);

    final GridLayout voteOptionsGrid = new GridLayout(3, 3);

    private Integer playerCount = 0;

    private Label getSpacer() {
        final Label spacer = new Label("&nbsp;", ContentMode.HTML);
        spacer.setWidth("1em");
        return spacer;
    }

    @Override
    protected void init(final VaadinRequest vaadinRequest) {
        executor.initialize();

        sessionDetailsLayout.addComponents(labelSessionId, getSpacer(), labelSessionIdValue, getSpacer(), labelPlayerCount, getSpacer(), labelPlayerCountValue, labelSessionName, getSpacer(),
                                           labelSessionNameValue, getSpacer(), labelPlayerName, getSpacer(), labelPlayerNameValue);

        labelSessionIdValue.setContentMode(ContentMode.PREFORMATTED);
        labelSessionNameValue.setContentMode(ContentMode.PREFORMATTED);
        labelPlayerCountValue.setContentMode(ContentMode.PREFORMATTED);
        labelPlayerNameValue.setContentMode(ContentMode.PREFORMATTED);

        final VerticalLayout pokerLayout = new VerticalLayout();

        labelSessionIdValue.setEnabled(false);
        labelSessionNameValue.setEnabled(true);
        sessionDetailsLayout.setVisible(false);

        pokerLayout.addComponents(sessionDetailsLayout, playerForm, sessionForm, votesGrid);
        sessionDetailsLayout.setVisible(false);
        sessionForm.setVisible(false);
        votesGrid.setVisible(false);
        votesGrid.setColumns("playerName", "vote");

        this.setContent(pokerLayout);

        bgChecker = () -> {
            boolean doContinue = true;
            do {
                if (session != null) {
                    Long latestSessionTimestamp = session.getLastModificationTimestamp();
                    if (latestSessionTimestamp != null && ! latestSessionTimestamp.equals(knownSessionTimestamp)) {
                        knownSessionTimestamp = latestSessionTimestamp;
                        populateVotes();
                    }

                    if (playerCount != session.getPlayers().size()) {
                        playerCount = session.getPlayers().size();
                        this.access(() -> {
                            this.labelPlayerCountValue.setValue(String.valueOf(playerCount));
                        });
                    }

                    try {
                        Thread.sleep(200l);
                    } catch (InterruptedException e) {
                        doContinue = false;
                        log.info(">>> end checking for {}, for {}", session.getId(), player.getName());
                    }
                }
            } while (doContinue);
        };

        executor.execute(bgChecker);
    }

    @Override
    public void detach() {
        PokerService.removePlayer(player);
        executor.shutdown();
        log.info(">>> player {} has detached", player.getName());
        player = null;
        populateVotes();
        super.detach();
    }

    private void populateVotes() {
        votes.clear();
        votes.addAll(session.getVotes());
        log.info(">>> updating {} votes for {}, for {}", votes.size(), session.getId(), player.getName());
        this.access(() -> {
            votesGrid.setItems(votes);
            votesGrid.getDataProvider().refreshAll();
            log.info(">>> updated asynchronously {} votes for {}, for {}", votes.size(), session.getId(), player.getName());
        });
    }

    public void setPlayer(final PlayerDTO player) {
        playerForm.setVisible(false);
        sessionForm.setVisible(true);
        this.player = player;
        Notification.show("Player created");
        this.labelPlayerNameValue.setValue(player.getName());
    }

    public void setSession(final SessionDTO session) {
        sessionForm.setVisible(false);
        this.session = session;
        this.knownSessionTimestamp = session.getLastModificationTimestamp();
        session.addPlayer(player);

        labelSessionIdValue.setValue(session.getId());
        labelSessionNameValue.setValue(session.getName());

        sessionDetailsLayout.setVisible(true);
        votesGrid.setVisible(true);

        PokerService.vote(session, player, "5");
    }
}
