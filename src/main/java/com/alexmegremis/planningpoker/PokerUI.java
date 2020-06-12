package com.alexmegremis.planningpoker;

import com.vaadin.annotations.*;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.communication.PushMode;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.spring.annotation.SpringUI;
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
public class PokerUI extends UI implements Serializable {

    private PlayerForm  playerForm  = new PlayerForm(this);
    private SessionForm sessionForm = new SessionForm(this);

    private PlayerDTO  player;
    private SessionDTO session;
    private Long       knownSessionTimestamp;

    private       List<VoteDTO> votes     = new ArrayList<>();
    private final Grid<VoteDTO> votesGrid = new Grid<>(VoteDTO.class);

    private final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

    final Label      labelSessionId        = new Label("Session ID");
    final Label      labelSessionIdValue   = new Label();
    final Label      labelSessionName      = new Label("Session Name");
    final Label      labelSessionNameValue = new Label();
    final Label      labelPlayerCount      = new Label("Player Count");
    final Label      labelPlayerCountValue = new Label();
    final Label      labelPlayerName       = new Label("Your Name");
    final Label      labelPlayerNameValue  = new Label();
    final GridLayout sessionDetailsLayout  = new GridLayout(4, 2, labelSessionId, labelSessionIdValue, labelPlayerCount, labelPlayerCountValue, labelSessionName,
                                                            labelSessionNameValue, labelPlayerName, labelPlayerNameValue);

    private static Integer playerCount;

    @Override
    protected void init(final VaadinRequest vaadinRequest) {
        executor.initialize();

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

//        Runnable bgChecker = () -> {
//            boolean doContinue = true;
//            do {
//                if (session != null) {
//                    Long latestSessionTimestamp = PokerService.modification.get(session);
//                    if (latestSessionTimestamp != null && ! latestSessionTimestamp.equals(knownSessionTimestamp)) {
//                        knownSessionTimestamp = latestSessionTimestamp;
//                        populateVotes();
//                    }
//                    try {
//                        Thread.sleep(200l);
//                    } catch (InterruptedException e) {
//                        doContinue = false;
//                        log.info(">>> end checking for {}, for {}", session.getId(), player.getName());
//                    }
//                }
//            } while (doContinue);
//        };
//
//        executor.execute(bgChecker);
    }

    @Override
    public void detach() {
        PokerService.removePlayer(player);
        log.info(">>> player {} has detached", player.getName());
        player = null;
        super.detach();
    }

    private void populateVotes() {
        votes.clear();
        votes.addAll(PokerService.votes.get(session));
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
//        this.access(() -> {
//            this.labelPlayerCountValue.setValue(String.valueOf(PokerService.players.size()));
//        });
    }

    public void setSession(final SessionDTO session) {
        sessionForm.setVisible(false);
        this.session = session;
        this.knownSessionTimestamp = PokerService.modification.get(session);

        labelSessionIdValue.setValue(session.getId());
        labelSessionNameValue.setValue(session.getName());

        sessionDetailsLayout.setVisible(true);

        PokerService.vote(session, player, "5");

        populateVotes();
        votesGrid.setVisible(true);
    }
}
