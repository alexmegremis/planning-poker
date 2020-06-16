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

        issueForm.setMargin(false);

        TextField issueSummary = new TextField("Summary");
//        issueSummary.setHeightUndefined();
        issueSummary.setWidthFull();
        issueSummary.setReadOnly(true);
        TextArea issueDescription = new TextArea("Description");
        issueDescription.setHeightUndefined();
        issueDescription.setWidthFull();
        issueDescription.setWordWrap(true);
        issueDescription.setReadOnly(true);
        TextArea issueUAC = new TextArea("UAC");
        issueUAC.setHeightUndefined();
        issueUAC.setWidthFull();
        issueUAC.setWordWrap(true);
        issueUAC.setReadOnly(true);

        issueDescription.addStyleName("activity-log");

        buttonFindIssue.addClickListener(event -> {
            buttonFindIssue.setComponentError(null);
            JiraIssueDTO issue = jiraService.getIssueByKey(inputIssueKey.getValue());
            issueSummary.setValue(issue.getFields().getSummary());
            issueDescription.setValue(issue.getFields().getDescription());
            issueDescription.setSizeFull();
            issueDescription.setVisible(true);
//            issueUAC.setValue(issue.getUAC());
            issueUAC.setSizeFull();
            issueUAC.setVisible(true);
        });

        addComponents(issueForm, issueSummary, issueDescription, issueUAC);
    }
}
