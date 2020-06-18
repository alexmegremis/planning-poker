package com.alexmegremis.planningpoker.integration.jira;

import com.alexmegremis.planningpoker.PokerService;
import com.alexmegremis.planningpoker.SessionDTO;
import com.vaadin.ui.*;
import lombok.Getter;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.function.Consumer;

public class IssueView extends VerticalLayout {

    //    private final PokerUI     pokerUI;
    private SessionDTO   session;
    private JiraService  jiraService;
    private PokerService pokerService;

    @Getter
    private int currentHashCode = 0;

    private JiraIssueDTO jiraIssue;

    private final TextField     issueSummary     = new TextField("Summary");
    private final TextArea      issueDescription = new TextArea("Description");
    private final TextArea      issueUAC         = new TextArea("UAC");
    private final TextField     inputIssueKey    = new TextField("Issue Key");
    private final TextField     issueCreator     = new TextField("Reporter");
    private final TextField     issueAssignee    = new TextField("Assignee");
    private final DateTimeField issueCreated     = new DateTimeField("Created");
    private final Button        buttonFindIssue  = new Button("Find");

    private final FormLayout issueForm = new FormLayout(inputIssueKey, buttonFindIssue);

    public IssueView() {

        issueSummary.setReadOnly(true);
        issueDescription.setReadOnly(true);
        issueUAC.setReadOnly(true);
        issueAssignee.setReadOnly(true);
        issueCreator.setReadOnly(true);
        issueCreated.setReadOnly(true);

        issueForm.setMargin(false);

        issueSummary.setWidthFull();

        issueDescription.setHeightUndefined();
        issueDescription.setWidthFull();
        issueDescription.setWordWrap(true);

        issueUAC.setHeightUndefined();
        issueUAC.setWidthFull();
        issueUAC.setWordWrap(true);

        issueDescription.addStyleName("activity-log");

        buttonFindIssue.addClickListener(event -> {
            buttonFindIssue.setComponentError(null);
            inputIssueKey.setValue(inputIssueKey.getValue().trim());
            pokerService.findJiraIssue(this.session, inputIssueKey.getValue());
        });

        addComponents(issueForm, issueSummary, issueDescription, issueUAC, issueCreated, issueCreator, issueAssignee);

//        issueCreator.setVisible(false);
//        issueCreated.setVisible(false);
//        issueAssignee.setVisible(false);
    }

    public void init(final PokerService pokerService, final JiraService jiraService, final SessionDTO session) {
        Assert.notNull(session, "No session linked.");
        Assert.notNull(pokerService, "No pokerService linked.");
        Assert.notNull(jiraService, "No jiraService linked.");

        this.session = session;
        this.jiraService = jiraService;
        this.pokerService = pokerService;
    }

    public void setJiraIssue(final JiraIssueDTO jiraIssue) {
        if (jiraIssue != null) {
            int newHashCode = jiraIssue.hashCode();
            if (newHashCode != this.currentHashCode) {
                this.jiraIssue = jiraIssue;
                this.currentHashCode = newHashCode;

                JiraIssueDTO.JiraIssueFieldsDTO issueFields = jiraIssue.getFields();
                issueSummary.setValue(issueFields.getSummary());

                setIfNotNull(jiraIssue.getKey(), inputIssueKey :: setValue);

                if (issueFields != null) {

                    setIfNotNull(issueFields.getDescription(), issueDescription :: setValue);
                    setIfNotNull(issueFields.getDescription(), issueDescription :: setValue);
                    setIfNotNull(issueFields.getUAC(), issueUAC :: setValue);
                    setIfNotNull(issueFields.getSummary(), issueSummary :: setValue);

                    setPersonIfNotNull(issueFields.getAssignee(), issueAssignee :: setValue);
                    setPersonIfNotNull(issueFields.getCreator(), issueCreator :: setValue);

                    if (issueFields.getCreated() != null) {
                        issueCreated.setValue(LocalDateTime.ofInstant(issueFields.getCreated().toInstant(), issueFields.getCreated().getTimeZone().toZoneId()));
                    }
                }
            }
        }
    }

    private void setPersonIfNotNull(JiraIssueDTO.JiraIssueFieldsDTO.Person person, Consumer<String> consumer) {
        if (person != null) {
            setIfNotNull(person.getName(), consumer);
        }
    }

    private void setIfNotNull(String value, Consumer<String> consumer) {
        if (! StringUtils.isEmpty(value)) {
            consumer.accept(value);
        }
    }
}
