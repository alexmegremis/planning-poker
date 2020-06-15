package com.alexmegremis.planningpoker;

import com.alexmegremis.planningpoker.integration.jira.IssueView;
import com.alexmegremis.planningpoker.integration.jira.JiraService;
import com.vaadin.annotations.*;
import com.vaadin.navigator.View;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.communication.PushMode;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.*;
import com.vaadin.ui.renderers.HtmlRenderer;
import com.vaadin.ui.renderers.TextRenderer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.vaadin.ui.Grid.Column;

@Slf4j
@Theme ("valo")
@PreserveOnRefresh
@Push (PushMode.AUTOMATIC)
@SpringUI
@SpringView(name = "Planning Poker")
public class PokerUI extends UI implements Serializable, View {

    @Autowired
    private JiraService jiraService;

    private final PlayerForm  playerForm  = new PlayerForm(this);
    private final SessionForm sessionForm = new SessionForm(this);

    private PlayerDTO  player;
    private SessionDTO session;
    private Long       knownSessionTimestamp;
    private Runnable   bgChecker;
    private IssueView  issueView;
    private TextField  votesResults = new TextField("Results");

    private final List<VoteDTO> votes     = new ArrayList<>();
    private final Grid<VoteDTO> votesGrid = new Grid<>();

    private final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

    private final Label            labelSessionId        = new Label("Session ID");
    private final Label            labelSessionIdValue   = new Label();
    private final Label            labelSessionName      = new Label("Session Name");
    private final Label            labelSessionNameValue = new Label();
    private final Label            labelPlayerCount      = new Label("Player Count");
    private final Label            labelPlayerCountValue = new Label();
    private final Label            labelPlayerName       = new Label("Your Name");
    private final Label            labelPlayerNameValue  = new Label();
    private final GridLayout       sessionDetailsLayout  = new GridLayout(7, 2);
    private final CssLayout        pokerLayout           = new CssLayout();
    private final HorizontalLayout votesLayout           = new HorizontalLayout();

    private Integer playerCount = 0;

    public static final String[] nums = new String[] {"0", "0.5", "1", "2", "3", "5", "8", "13", "20", "40", "100", "?"};

    private Label getSpacer() {
        final Label spacer = new Label("&nbsp;", ContentMode.HTML);
        spacer.setWidth("1em");
        return spacer;
    }

    private Button getVoteButton(final String caption) {
        Button result = new Button(caption);
        result.setWidth("5em");
        result.addClickListener(event -> {
            PokerService.vote(session, player, caption);
        });
        return result;
    }

    private GridLayout createVotingGrid() {
        final GridLayout result = new GridLayout(2, 15);

        for (int i = 0; i < nums.length; i++) {
            result.addComponent(getVoteButton(nums[i]));
        }

        result.addComponent(getSpacer(), 0, 6, 1, 7);


        Button revealVotes = new Button("Show votes");
        revealVotes.setWidth("10em");
        result.addComponent(revealVotes, 0, 8, 1, 8);
        revealVotes.addClickListener(event -> {
            PokerService.revealVotes(session);
        });

        votesResults.setWidth("10em");
        result.addComponent(getSpacer(), 0, 9, 1, 9);
        result.addComponent(votesResults, 0, 10, 1, 10);


        Button resetVotes = new Button("Reset votes");
        resetVotes.setWidth("10em");
        resetVotes.addClickListener(event -> {
            PokerService.resetVotes(session);
            votesResults.setValue(session.getVoteResult());
            session.updateLastModificationTimestamp();
        });
        result.addComponent(getSpacer(), 0, 11, 1, 11);
        result.addComponent(resetVotes, 0, 12, 1, 12);

        votesResults.setReadOnly(true);
        votesResults.setVisible(false);

        return result;
    }

    private CssLayout createIssueLayout() {
        return null;
    }

    @Override
    protected void init(final VaadinRequest vaadinRequest) {

        final VerticalLayout wrapper = new VerticalLayout();

        Column<VoteDTO, String> playerNameColumn = votesGrid.addColumn(v -> v.getPlayerName(), new TextRenderer());
        Column<VoteDTO, String> voteValueColumn  = votesGrid.addColumn(v -> v.getVote(), new HtmlRenderer());
        playerNameColumn.setWidthUndefined();
        playerNameColumn.setCaption("Player");
        playerNameColumn.setResizable(false);
        voteValueColumn.setWidth(100);
        voteValueColumn.setCaption("Vote");
        voteValueColumn.setResizable(false);
        voteValueColumn.setStyleGenerator(item -> "center-align");

//        votesGrid.setWidthFull();

        GridLayout votingGrid = createVotingGrid();
        votingGrid.setWidthUndefined();
        votingGrid.setHeightUndefined();

        this.issueView = createIssueView();
        issueView.setWidthFull();
        issueView.setHeightUndefined();

        initSessionDetailsDisplay();
        votesLayout.addComponents(votesGrid, votingGrid, issueView);
//        votesLayout.setExpandRatio(votesGrid, 0.25f);
//        votesLayout.setExpandRatio(votingGrid, 0.1f);
        votesLayout.setExpandRatio(issueView, 0.65f);
        votesLayout.setVisible(false);
        votesLayout.setSpacing(true);
        votesLayout.setWidthFull();
        votesLayout.setHeightUndefined();
        sessionDetailsLayout.setVisible(false);

        pokerLayout.addComponents(playerForm, sessionForm, votesLayout);
        sessionForm.setVisible(false);
//        votesGrid.setVisible(false);

        initBgChecker();

        pokerLayout.setSizeFull();
        sessionDetailsLayout.setVisible(false);
        sessionDetailsLayout.setHeight("6em");
        sessionDetailsLayout.setWidth("25em");
        votesLayout.setWidthFull();
        votesLayout.setHeightUndefined();
        wrapper.addComponents(sessionDetailsLayout, pokerLayout);
        this.setContent(wrapper);
    }

    private IssueView createIssueView() {
        IssueView result = new IssueView(this, jiraService);
        result.setMargin(false);
        result.setWidthUndefined();
        result.setHeightUndefined();
//        result.setVisible(false);

        return result;
    }

    private void initBgChecker() {
        bgChecker = () -> {
            boolean doContinue = true;
            do {
                if (session != null) {
                    Long latestSessionTimestamp = session.getLastModificationTimestamp();
                    if (latestSessionTimestamp != null && ! latestSessionTimestamp.equals(knownSessionTimestamp)) {
                        knownSessionTimestamp = latestSessionTimestamp;
                        populateVotes();
                        this.votesResults.setValue(session.getVoteResult());
                        this.votesResults.setValue(session.getVoteResult());
                        this.votesResults.setVisible(session.getShowVotes());
                        this.votesResults.setVisible(session.getShowVotes());
                        log.info(">>> updated session {} for {}, session showVotes is {}, UI {} showVotes is {}", session.getId(), player.getName(), session.getShowVotes(), votesResults, votesResults.isVisible());
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

        executor.initialize();
        executor.execute(bgChecker);
    }

    private void initSessionDetailsDisplay() {
        sessionDetailsLayout.addComponents(labelSessionId, getSpacer(), labelSessionIdValue, getSpacer(), labelPlayerCount, getSpacer(), labelPlayerCountValue, labelSessionName,
                                           getSpacer(), labelSessionNameValue, getSpacer(), labelPlayerName, getSpacer(), labelPlayerNameValue);

        labelSessionIdValue.setContentMode(ContentMode.PREFORMATTED);
        labelSessionNameValue.setContentMode(ContentMode.PREFORMATTED);
        labelPlayerCountValue.setContentMode(ContentMode.PREFORMATTED);
        labelPlayerNameValue.setContentMode(ContentMode.PREFORMATTED);
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
        votesLayout.setVisible(true);
        issueView.setVisible(true);
    }
}
