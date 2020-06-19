package com.alexmegremis.planningpoker.integration.jira;

import lombok.*;

import java.util.Calendar;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class JiraIssueDTO {

    private String             id;
    private String             key;
    private JiraIssueFieldsDTO fields;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @EqualsAndHashCode
    public class JiraIssueFieldsDTO {

        private Map<String, String> issueType;

        private String   description;
        private String   summary;
        private String   UAC;
        private Calendar created;
        private Person   creator;
        private Person   assignee;
        private String   customfield_10000;

        public String getUAC() {
            return getCustomfield_10000();
        }

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        @EqualsAndHashCode
        public class Person {

            private String name;
            private String displayName;
            private String key;
            private String emailAddress;
        }
    }
}
