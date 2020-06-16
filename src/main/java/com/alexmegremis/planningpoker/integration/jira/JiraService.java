package com.alexmegremis.planningpoker.integration.jira;

import com.vaadin.spring.annotation.SpringComponent;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

import static org.springframework.web.reactive.function.client.WebClient.ResponseSpec;

@SpringComponent
public class JiraService {

    public JiraIssueDTO getIssueByKey(final String issueKey) {

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance()
                                                                        .scheme("http")
                                                                        .host("192.168.0.34")
                                                                        .port("8060")
                                                                        .pathSegment("rest", "api", "2", "issue", issueKey)
                                                                        .queryParam("fields", "description,summary");

        WebClient webClient = WebClient.builder().build();
        ResponseSpec response = webClient.get()
                                         .uri(uriComponentsBuilder.toUriString())
                                         .headers(header -> header.setBasicAuth("FIXME", "FIXME"))
                                         .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                         .retrieve();

        Map<String, Object> issueDetails = response.toEntity(Map.class).block().getBody();
//        Map<String, String> fields       = (Map<String, String>) issueDetails.get("fields");
        JiraIssueDTO        result       = response.toEntity(JiraIssueDTO.class).block().getBody();

        return result;
    }
}
