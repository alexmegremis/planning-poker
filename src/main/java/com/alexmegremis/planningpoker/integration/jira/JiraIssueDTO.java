package com.alexmegremis.planningpoker.integration.jira;

import lombok.*;

import java.util.Calendar;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JiraIssueDTO {

    private String             id;
    private String             key;
    private JiraIssueFieldsDTO fields;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public class JiraIssueFieldsDTO {

        private Map<String, String> issueType;
        private String              description;
        private String              summary;
        private Calendar            created;

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public class Person {

            private String name;
            private String key;
            private String emailAddress;
        }
    }
}
