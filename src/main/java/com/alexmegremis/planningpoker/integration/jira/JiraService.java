package com.alexmegremis.planningpoker.integration.jira;

import com.vaadin.spring.annotation.SpringComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.netty.http.client.HttpClient;

import java.util.*;

import static org.springframework.web.reactive.function.client.WebClient.ResponseSpec;

@SpringComponent
public class JiraService {

    @Value ("${jira.scheme}")
    private String jiraScheme;
    @Value ("${jira.hostname}")
    private String jiraHostname;
    @Value ("${jira.port}")
    private String jiraPort;
    @Value ("${jira.issueAPI}")
    private String issueAPI;
    @Value ("${jira.user}")
    private String user;
    @Value ("${jira.pass}")
    private String pass;

    public JiraIssueDTO getIssueByKey(final String issueKey) {

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance().scheme(jiraScheme).host(jiraHostname).port(jiraPort)
//                                                                        .pathSegment(issueAPI, issueKey)
                                                                        .queryParam("fields", "description,summary, customfield_XXXXX");

        Arrays.stream(issueAPI.split("/")).forEach(uriComponentsBuilder :: pathSegment);
        uriComponentsBuilder.pathSegment(issueKey);

        final byte[] encodedCreds = Base64.getEncoder().encode((user + ":" + pass).getBytes());

        WebClient webClient = WebClient.builder()
                                       .defaultHeaders(httpHeaders -> httpHeaders.setBasicAuth(new String(encodedCreds)))
                                       .clientConnector(new ReactorClientHttpConnector(HttpClient.create().followRedirect(true)))
                                       .build();

        ResponseSpec response = webClient.get().uri(uriComponentsBuilder.toUriString()).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).retrieve();

        JiraIssueDTO result = response.toEntity(JiraIssueDTO.class).block().getBody();

        return result;
    }
}
