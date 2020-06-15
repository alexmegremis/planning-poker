package com.alexmegremis.planningpoker.integration.jira;

import com.vaadin.spring.annotation.SpringComponent;

@SpringComponent
public class JiraService {

    public JiraIssueDTO getIssueByKey(final String issueKey) {
        return new JiraIssueDTO("1231", issueKey, "Fix super critical bug", "Must fix bug that is breaking the universe", "Issue is fixed", null, null);
    }
}
