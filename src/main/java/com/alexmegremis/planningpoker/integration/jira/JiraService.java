package com.alexmegremis.planningpoker.integration.jira;

import com.alexmegremis.planningpoker.PokerService;
import com.alexmegremis.planningpoker.SessionDTO;
import org.springframework.lang.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public interface JiraService {

    void getIssueByKey(PokerService pokerService, SessionDTO session, String issueKey);

    JiraIssueDTO createJiraIssueDTO(String jsonBody);
}
