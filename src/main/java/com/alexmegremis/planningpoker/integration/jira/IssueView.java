package com.alexmegremis.planningpoker.integration.jira;

import com.alexmegremis.planningpoker.PokerUI;
import com.vaadin.ui.*;

public class IssueView extends VerticalLayout {

    private final PokerUI pokerUI;

    private JiraService jiraService;

    public IssueView(final PokerUI pokerUI, final JiraService jiraService) {
        this.pokerUI = pokerUI;
        this.jiraService = jiraService;

        TextField  inputIssueKey   = new TextField("Issue Key");
        Button     buttonFindIssue = new Button("Find");
        FormLayout issueForm       = new FormLayout(inputIssueKey, buttonFindIssue);

        TextArea issueSummary = new TextArea("Summary");
        issueSummary.setHeightUndefined();
        TextArea issueDescription = new TextArea("Description");
        issueDescription.setHeightUndefined();
        issueDescription.setVisible(true);
        issueDescription.setWidthFull();
        issueDescription.setWordWrap(true);
        TextArea issueUAC = new TextArea("UAC");
        issueUAC.setHeightUndefined();
        issueUAC.setVisible(false);
        issueUAC.setWidthFull();
        issueUAC.setWordWrap(true);

        buttonFindIssue.addClickListener(event -> {
            JiraIssueDTO issue = jiraService.getIssueByKey(inputIssueKey.getValue());
            issueSummary.setValue(issue.getSummary());
            issueDescription.setValue(issue.getDescription());
            issueUAC.setValue(issue.getUAC());
            issueDescription.setSizeFull();
            issueDescription.setVisible(true);
            issueUAC.setSizeFull();
            issueUAC.setVisible(true);
        });

        addComponents(issueForm, issueSummary, issueSummary, issueDescription);
    }
}
