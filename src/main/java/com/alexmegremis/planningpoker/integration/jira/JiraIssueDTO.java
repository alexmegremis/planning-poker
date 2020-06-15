package com.alexmegremis.planningpoker.integration.jira;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JiraIssueDTO {
    private String id;
    private String key;
    private String summary;
    private String description;
    private String UAC;
    private String points;
    private String originalEstimate;
}
