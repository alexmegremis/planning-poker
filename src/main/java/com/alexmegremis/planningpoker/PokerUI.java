package com.alexmegremis.planningpoker;

import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.Serializable;
import java.util.List;

@Slf4j
@Theme("valo")
@SpringUI
public class PokerUI extends UI implements Serializable {

    private PlayerForm playerForm = new PlayerForm(this);
    private SessionForm sessionForm = new SessionForm(this);

    private PlayerDTO player;
    private SessionDTO session;
    private Long       knownSessionTimestamp;

    private List<VoteDTO> votes;
    private Grid<VoteDTO> votesGrid = new Grid<>(VoteDTO.class);

    private final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

    final Label labelSessionId = new Label("Session ID");
    final TextField fieldSessionId = new TextField();
    final Label labelSessionName = new Label("Session Name");
    final TextField fieldSessionName = new TextField();
    final GridLayout sessionLayout = new GridLayout(4, 1, labelSessionId, fieldSessionId, labelSessionName, fieldSessionName);

    @Override
    protected void init(final VaadinRequest vaadinRequest) {
        executor.initialize();

        log.info(">>> NEW VIEW");
        final VerticalLayout pokerLayout = new VerticalLayout();

        fieldSessionId.setEnabled(false);
        fieldSessionName.setEnabled(true);
        sessionLayout.setVisible(false);

        pokerLayout.addComponents(sessionLayout, playerForm, sessionForm, votesGrid);
        sessionLayout.setVisible(false);
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
    }

    private void populateVotes() {
        votes = PokerService.votes.get(session);
        log.info(">>> updating votes for {}, for {}", session.getId(), player.getName());
        votesGrid.setItems(votes);
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

        fieldSessionId.setValue(session.getId());
        fieldSessionName.setValue(session.getName());

        sessionLayout.setVisible(true);

        PokerService.vote(session, player, "5");

        populateVotes();
        votesGrid.setVisible(true);
    }
}
