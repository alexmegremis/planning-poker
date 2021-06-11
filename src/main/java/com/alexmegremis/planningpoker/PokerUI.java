package com.alexmegremis.planningpoker;

import com.alexmegremis.planningpoker.integration.jira.IssueView;
import com.alexmegremis.planningpoker.integration.jira.JiraService;
import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Push;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.MouseEventDetails;
import com.vaadin.shared.communication.PushMode;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.*;
import com.vaadin.ui.renderers.HtmlRenderer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.*;

import static com.vaadin.ui.Grid.Column;

@Slf4j
@PreserveOnRefresh
@Push (PushMode.AUTOMATIC)
@SpringUI
@SpringView (name = "Planning Poker")
public class PokerUI extends UI implements Serializable, View {

    @Autowired
    private JiraService  jiraService;
    @Autowired
    private PokerService pokerService;

    private static final Set<PokerUI> allUIs = new HashSet<>();

    public static void updateAll() {
        allUIs.parallelStream().forEach(PokerUI :: doRefresh);
    }

    private Boolean     detached = false;
    private PlayerForm  playerForm;
    private SessionForm sessionForm;

    private PlayerDTO  player;
    private SessionDTO session;
    private Long       knownSessionTimestamp;

    private       IssueView issueView;
    private final TextField votesResults = new TextField("Results");

    private final List<VoteDTO> votes       = new ArrayList<>();
    private final List<Button>  voteButtons = new ArrayList<>();
    private final Grid<VoteDTO> votesGrid   = new Grid<>();

    private GridLayout votingGridLayout;

    private final Label            labelSessionId        = new Label("Session ID");
    private final Label            labelSessionIdValue   = new Label();
    private final Label            labelSessionName      = new Label("Session Name");
    private final Label            labelSessionNameValue = new Label();
    private final Label            labelPlayerCount      = new Label("Player Count");
    private final Label            labelPlayerCountValue = new Label();
    private final Label            labelPlayerName       = new Label("Your Name");
    private final Label            labelPlayerNameValue  = new Label();
    private final GridLayout       sessionDetailsLayout  = new GridLayout(7, 2);
    private final VerticalLayout   pokerLayout           = new VerticalLayout();
    private final HorizontalLayout votesLayout           = new HorizontalLayout();

    private final Button toggleVoting         = new Button("Open voting");
    private final Button toggleVotesVisible   = new Button("Toggle votes");
    private final Button togglePlayersVisible = new Button("Toggle players");
    private final Button resetVotes           = new Button("Reset votes");

    private Integer playerCount = 0;

    public static final String[] nums = new String[] {"0", "0.5", "1", "2", "3", "5", "8", "13", "20", "40", "100", "?"};

    @Override
    public void enter(final ViewChangeEvent event) {
        String               param   = event.getParameters().trim();
        Optional<SessionDTO> session = pokerService.findSession(param);
        session.ifPresent(this :: setSession);
    }

    private void toggleVotingButtons() {
        voteButtons.forEach(b -> b.setEnabled(session.getVotingOpen()));
//        log.debug(">>> Player: {} have set {} voting buttons to {}", voteButtons.size(), player.getName(), session.getVotingOpen());
    }

    @Override
    protected void init(final VaadinRequest vaadinRequest) {

        PokerUI.allUIs.add(this);

        initUIs();
        processParams();
        setState();

        doRefresh();
    }

    private void setState() {

        if (! detached) {
            playerForm.setVisible(player == null);
            sessionForm.setVisible(player != null && session == null);
            votesLayout.setVisible(player != null && session != null);
            issueView.setVisible(votesLayout.isVisible());
            sessionDetailsLayout.setVisible(votesLayout.isVisible());
        }
    }

    private void processParams() {
        String               uriSessionId = Page.getCurrent().getUriFragment();
        Optional<SessionDTO> session      = pokerService.findSession(uriSessionId);
        session.ifPresent(this :: setSession);
    }

    private void initUIs() {

        // Player entrance
        this.sessionForm = new SessionForm(this, pokerService, session);
        this.playerForm = new PlayerForm(this, pokerService);

        initVotesGridLayout();
        initVotingGridLayout();
        initSessionDetailsLayout();

        this.issueView = createIssueView();

        votesLayout.addComponents(votesGrid, votingGridLayout, issueView);
        votesLayout.setExpandRatio(issueView, 0.65f);
        votesLayout.setSpacing(true);
        votesLayout.setWidthFull();
        votesLayout.setHeightUndefined();

        sessionDetailsLayout.setHeight("6em");
        sessionDetailsLayout.setWidth("25em");

        pokerLayout.addComponents(playerForm, sessionForm, sessionDetailsLayout, votesLayout);

        this.setContent(pokerLayout);
    }

    private void initSessionDetailsLayout() {

        sessionDetailsLayout.addComponents(labelSessionId, getSpacer(), labelSessionIdValue, getSpacer(), labelPlayerCount, getSpacer(), labelPlayerCountValue,

                                           labelSessionName, getSpacer(), labelSessionNameValue, getSpacer(), labelPlayerName, getSpacer(), labelPlayerNameValue);

        labelSessionIdValue.setContentMode(ContentMode.PREFORMATTED);
        labelSessionNameValue.setContentMode(ContentMode.PREFORMATTED);
        labelPlayerCountValue.setContentMode(ContentMode.PREFORMATTED);
        labelPlayerNameValue.setContentMode(ContentMode.PREFORMATTED);
    }

    private void initVotesGridLayout() {
        // Votes table
        Column<VoteDTO, String> playerNameColumn = votesGrid.addColumn(v -> v.getPlayer().getHideable(player), new HtmlRenderer());
        Column<VoteDTO, String> voteValueColumn  = votesGrid.addColumn(VoteDTO :: getHideable, new HtmlRenderer());

        playerNameColumn.setWidthUndefined();
        playerNameColumn.setCaption("Player");
        playerNameColumn.setResizable(false);
        voteValueColumn.setWidth(100);
        voteValueColumn.setCaption("Vote");
        voteValueColumn.setResizable(false);

        votesGrid.addItemClickListener(e -> {
            MouseEventDetails click = e.getMouseEventDetails();
            if (session.getOwner() == this.player && click.isAltKey() && click.isShiftKey()) {
                ConfirmationDialogue confirm = new ConfirmationDialogue("Remove this player", e.getItem().getPlayer(), pokerService :: removePlayer);
                UI.getCurrent().addWindow(confirm);
            }
        });
    }

    private void initVotingGridLayout() {
        votingGridLayout = new GridLayout(2, 20);

        for (final String num : nums) {
            votingGridLayout.addComponent(getVoteButton(num));
        }

        toggleVoting.setWidth("10em");
        toggleVotesVisible.setWidth("10em");
        togglePlayersVisible.setWidth("10em");
        votesResults.setWidth("10em");
        resetVotes.setWidth("10em");

        addToVotingGrid(votingGridLayout, getSpacer());
        addToVotingGrid(votingGridLayout, getSpacer());
        addToVotingGrid(votingGridLayout, toggleVoting);
        addToVotingGrid(votingGridLayout, getSpacer());
        addToVotingGrid(votingGridLayout, toggleVotesVisible);
        addToVotingGrid(votingGridLayout, getSpacer());
        addToVotingGrid(votingGridLayout, votesResults);
        addToVotingGrid(votingGridLayout, getSpacer());
        addToVotingGrid(votingGridLayout, resetVotes);
        addToVotingGrid(votingGridLayout, getSpacer());
        addToVotingGrid(votingGridLayout, togglePlayersVisible);

        toggleVotesVisible.addClickListener(event -> pokerService.toggleVotes(session));

        resetVotes.addClickListener(event -> {
            ConfirmationDialogue confirm = new ConfirmationDialogue(resetVotes, session, pokerService :: resetVotes);
            UI.getCurrent().addWindow(confirm);
        });

        togglePlayersVisible.addClickListener(event -> {
            ConfirmationDialogue confirm = new ConfirmationDialogue(togglePlayersVisible, session, pokerService :: togglePlayersVisible);
            confirm.setModal(true);
            UI.getCurrent().addWindow(confirm);
        });

        toggleVoting.addClickListener(event -> {
            pokerService.toggleVotingOpen(session);
        });

        votingGridLayout.setWidthUndefined();
        votingGridLayout.setHeightUndefined();

        votesResults.setReadOnly(true);
    }

    private void addToVotingGrid(final GridLayout layout, final Component component) {
        layout.addComponent(component, 0, layout.getCursorY(), 1, layout.getCursorY());
    }

    private Label getSpacer() {
        final Label spacer = new Label("&nbsp;", ContentMode.HTML);
        spacer.setWidth("1em");
        return spacer;
    }

    private Button getVoteButton(final String caption) {
        Button result = new Button(caption);
        result.setWidth("5em");
        result.addClickListener(event -> {
            boolean didVote = pokerService.vote(session, player, caption);
            if (! didVote) {
                this.detach();
            }
        });
        voteButtons.add(result);
        return result;
    }

    private IssueView createIssueView() {
        IssueView result = new IssueView();
        result.setMargin(false);
        result.setWidthFull();
        result.setHeightUndefined();

        return result;
    }

    public void doRefresh() {
        if (! detached && player != null && session != null) {
            Long latestSessionTimestamp = session.getLastModificationTimestamp();

            if (latestSessionTimestamp != null && ! latestSessionTimestamp.equals(knownSessionTimestamp)) {
                knownSessionTimestamp = latestSessionTimestamp;
                populateVotes();
                this.access(() -> this.votesResults.setValue(pokerService.getVoteResults(session)));
            }

            if (playerCount != session.getPlayers().size()) {
                playerCount = session.getPlayers().size();
                this.access(() -> this.labelPlayerCountValue.setValue(String.valueOf(playerCount)));
            }

            this.issueView.setJiraIssue(session.getJiraIssue());

            this.access(this :: toggleVotingButtons);
            toggleVoting.setCaption(session.getVotingOpen() ? "Close voting" : "Open voting");
        }
    }

    @Override
    public void detach() {
        this.detached = true;
        pokerService.removePlayer(player);
        log.info(">>> player {} has detached", player.getName());
        player = null;
        PokerUI.allUIs.remove(this);
        PokerUI.updateAll();

        playerForm.setVisible(false);
        sessionForm.setVisible(false);
        votesLayout.setVisible(false);
        issueView.setVisible(false);
        sessionDetailsLayout.setVisible(true);

        super.detach();
    }

    private void populateVotes() {
        votes.clear();
        votes.addAll(session.getVotes());
//        log.info(">>> updating {} votes for {}, for {}", votes.size(), session.getId(), player.getHideableValue());
        this.access(() -> {
            votesGrid.setItems(votes);
            votesGrid.getDataProvider().refreshAll();
            log.info(">>> updated asynchronously {} votes for {}, for {}", votes.size(), session.getId(), player.getName());
        });
    }

    public void setPlayer(final PlayerDTO player) {
        this.player = player;
        setState();
        Notification.show("Player created");
        this.labelPlayerNameValue.setValue(player.getName());

        if (this.session != null) {
            pokerService.addPlayer(player, session);
            readyToUse();
        }

        doRefresh();
    }

    public void setSession(final SessionDTO session) {
        this.session = session;
        Page.getCurrent().setUriFragment(session.getId());
        setState();
        this.knownSessionTimestamp = session.getLastModificationTimestamp();

        if (player != null) {
            pokerService.addPlayer(player, session);
            readyToUse();
        }

        if (session.getOwner() != this.player) {
            toggleVotesVisible.setVisible(false);
            resetVotes.setVisible(false);
            toggleVoting.setVisible(false);
            togglePlayersVisible.setVisible(false);
            votingGridLayout.removeComponent(toggleVotesVisible);
            votingGridLayout.removeComponent(togglePlayersVisible);
            votingGridLayout.removeComponent(resetVotes);
        }

        doRefresh();
    }

    private void readyToUse() {
        labelSessionIdValue.setValue(session.getId());
        labelSessionNameValue.setValue(session.getName());
        issueView.init(pokerService, jiraService, session);
    }
}
