package com.alexmegremis.planningpoker.integration.jira;

import com.alexmegremis.planningpoker.PokerService;
import com.alexmegremis.planningpoker.SessionDTO;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.renderers.HtmlRenderer;
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

    private final TextField     issueLabels      = new TextField("labels");
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

        issueLabels.setReadOnly(true);
        issueSummary.setReadOnly(true);
        issueDescription.setReadOnly(true);
        issueUAC.setReadOnly(true);
        issueAssignee.setReadOnly(true);
        issueCreator.setReadOnly(true);
        issueCreated.setReadOnly(true);

        issueForm.setMargin(false);
        issueForm.setSpacing(true);
        issueForm.setDescription("Find Issue", ContentMode.TEXT);

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

        Label label01 = new Label();
        label01.setContentMode(ContentMode.HTML);
        label01.setValue(FontAwesome.TAG.getHtml() + "Refined");

        Label label02 = new Label();
        label02.setContentMode(ContentMode.HTML);
        label02.setValue(FontAwesome.TAG.getHtml() + "ELMA");

        addComponents(issueForm, issueLabels,label01, label02, issueSummary, issueDescription, issueUAC, issueCreated, issueCreator, issueAssignee);
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

                StringBuilder sb = new StringBuilder();
                jiraIssue.getLabels().stream().map(l -> FontAwesome.TAG.toString() + l).forEach(sb :: append);

                issueLabels.setValue(sb.toString());
                issueSummary.setValue(jiraIssue.getSummary());

                setIfNotNull(jiraIssue.getKey(), inputIssueKey :: setValue);

                setIfNotNull(jiraIssue.getDescription(), issueDescription :: setValue);
                setIfNotNull(jiraIssue.getUAC(), issueUAC :: setValue);
                setIfNotNull(jiraIssue.getSummary(), issueSummary :: setValue);

                setPersonIfNotNull(jiraIssue.getAssignee(), issueAssignee);
                setPersonIfNotNull(jiraIssue.getCreator(), issueCreator);

                if (jiraIssue.getCreated() != null) {
                    issueCreated.setValue(LocalDateTime.ofInstant(jiraIssue.getCreated().toInstant(), jiraIssue.getCreated().getTimeZone().toZoneId()));
                }
            }
        }
    }

    private void setPersonIfNotNull(JiraIssueDTO.Person person, AbstractTextField displayField) {
        if (person != null) {
            setIfNotNull(person.getDisplayName(), displayField :: setValue);
            setIfNotNull(person.getName(), displayField :: setDescription);
        }
    }

    private void setIfNotNull(String value, Consumer<String> consumer) {
        if (! StringUtils.isEmpty(value)) {
            consumer.accept(value);
        } else {
            consumer.accept("");
        }
    }
}
