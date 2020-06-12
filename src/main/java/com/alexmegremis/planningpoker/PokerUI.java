package com.alexmegremis.planningpoker;

import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Theme("valo")
@PreserveOnRefresh
@SpringUI
public class PokerUI extends UI implements Serializable {

    private static final List<Grid> VOTES_GRIDS = new ArrayList<>();

    private PlayerForm playerForm = new PlayerForm(this);
    private SessionForm sessionForm = new SessionForm(this);

    private PlayerDTO player;
    private SessionDTO session;
    private Long       knownSessionTimestamp;

    private List<VoteDTO> votes;
    private Grid<VoteDTO> votesGrid = new Grid<>(VoteDTO.class);

    private final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

    final Label            labelSessionId      = new Label("Session ID");
    final Label            labelSessionIdValue = new Label();
    final Label            labelSessionName      = new Label("Session Name");
    final Label            labelSessionNameValue = new Label();
    final GridLayout sessionDetailsLayout = new GridLayout(2, 2, labelSessionId, labelSessionIdValue, labelSessionName, labelSessionNameValue);
//    final VerticalLayout   sessionDetailsLayout  = new VerticalLayout(sessionIdDetails, sessionNameDetails);

    @Override
    protected void init(final VaadinRequest vaadinRequest) {
        executor.initialize();

//        labelSessionId.setWidth("70%");
        labelSessionIdValue.setContentMode(ContentMode.PREFORMATTED);
//        labelSessionName.setWidth("70%");
        labelSessionNameValue.setContentMode(ContentMode.PREFORMATTED);

        log.info(">>> NEW VIEW");
        final VerticalLayout pokerLayout = new VerticalLayout();

        labelSessionIdValue.setEnabled(false);
        labelSessionNameValue.setEnabled(true);
        sessionDetailsLayout.setVisible(false);

        pokerLayout.addComponents(sessionDetailsLayout, playerForm, sessionForm, votesGrid);
        sessionDetailsLayout.setVisible(false);
        sessionForm.setVisible(false);
        votesGrid.setVisible(false);

        this.setContent(pokerLayout);

        Runnable bgChecker = () -> {
            boolean doContinue = true;
            do {
                if(session != null) {
                    Long latestSessionTimestamp = PokerService.modification.get(session);
                    if (latestSessionTimestamp != null && ! latestSessionTimestamp.equals(knownSessionTimestamp)) {
                        knownSessionTimestamp = latestSessionTimestamp;
                        populateVotes();
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

        VOTES_GRIDS.add(votesGrid);
    }

    private void populateVotes() {
        votes = PokerService.votes.get(session);
        log.info(">>> updating votes for {}, for {}", session.getId(), player.getName());
        votesGrid.setItems(votes);
        votesGrid.markAsDirty();
        votesGrid.getDataProvider().refreshAll();
        votesGrid.getDataCommunicator().reset();
        Notification.show("votes updated");
    }

    public void setPlayer(final PlayerDTO player) {
        playerForm.setVisible(false);
        sessionForm.setVisible(true);
        this.player = player;
        Notification.show("Player created");
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
