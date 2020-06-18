package com.alexmegremis.planningpoker.integration.jira;

import com.alexmegremis.planningpoker.PokerService;
import com.alexmegremis.planningpoker.SessionDTO;
import com.vaadin.spring.annotation.SpringComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.netty.http.client.HttpClient;

import java.util.Arrays;
import java.util.Base64;

import static org.springframework.web.reactive.function.client.WebClient.ResponseSpec;

@Slf4j
@SpringComponent
@Scope (value = ConfigurableBeanFactory.SCOPE_SINGLETON)
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

    public void getIssueByKey(final PokerService pokerService, final SessionDTO session, final String issueKey) {

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance().scheme(jiraScheme).host(jiraHostname).port(jiraPort)
//                                                                        .pathSegment(issueAPI, issueKey)
                                                                        .queryParam("fields", "description,summary,customfield_10000,created,creator,assignee");

        Arrays.stream(issueAPI.split("/")).forEach(uriComponentsBuilder :: pathSegment);
        uriComponentsBuilder.pathSegment(issueKey);

        final byte[] encodedCreds = Base64.getEncoder().encode((user + ":" + pass).getBytes());

        WebClient webClient = WebClient.builder()
                                       .defaultHeaders(httpHeaders -> httpHeaders.setBasicAuth(new String(encodedCreds)))
                                       .clientConnector(new ReactorClientHttpConnector(HttpClient.create().followRedirect(true)))
                                       .build();

        ResponseSpec response = webClient.get().uri(uriComponentsBuilder.toUriString()).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).retrieve();

        response.bodyToFlux(JiraIssueDTO.class).subscribe(j -> pokerService.findJiraIssueResponse(j, session));
    }
}
