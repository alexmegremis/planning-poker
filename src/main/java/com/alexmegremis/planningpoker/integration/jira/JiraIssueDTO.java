package com.alexmegremis.planningpoker.integration.jira;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@SuperBuilder ()
public class JiraIssueDTO {

    private String id;
    private String key;

    private Map<String, String> issueType;

    private String       description;
    private String       summary;
    private String       UAC;
    private Calendar     created;
    private Person       creator;
    private Person       assignee;
    private List<String> labels;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @EqualsAndHashCode
    @Builder
    public static class Person {

        private String name;
        private String displayName;
        private String key;
        private String emailAddress;
    }
}
