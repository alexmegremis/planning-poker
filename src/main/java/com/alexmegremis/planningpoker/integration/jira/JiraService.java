package com.alexmegremis.planningpoker.integration.jira;

import com.alexmegremis.planningpoker.PokerService;
import com.alexmegremis.planningpoker.SessionDTO;
import com.google.gson.*;
import com.vaadin.spring.annotation.SpringComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.netty.http.client.HttpClient;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.springframework.web.reactive.function.client.WebClient.ResponseSpec;

@Slf4j
@SpringComponent
@Scope (value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class JiraService {

    public static final String TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    @Value ("${jira.scheme}")
    private String jiraScheme;
    @Value ("${jira.hostname}")
    private String jiraHostname;
    @Value ("${jira.port}")
    private String jiraPort;
    @Value ("${jira.issueAPI}")
    private String issueAPI;
    @Value ("${jira.uac.fieldname}")
    private String fieldNameUAC;
    @Value ("${jira.user}")
    private String user;
    @Value ("${jira.pass}")
    private String pass;

    public void getIssueByKey(final PokerService pokerService, final SessionDTO session, final String issueKey) {

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance()
                                                                        .scheme(jiraScheme)
                                                                        .host(jiraHostname)
                                                                        .port(jiraPort)
                                                                        .queryParam("fields", "description,summary,created,creator,assignee,labels," + fieldNameUAC);

        Arrays.stream(issueAPI.split("/")).forEach(uriComponentsBuilder :: pathSegment);
        uriComponentsBuilder.pathSegment(issueKey);

        final byte[] encodedCreds = Base64.getEncoder().encode((user + ":" + pass).getBytes());

        WebClient webClient = WebClient.builder()
                                       .defaultHeaders(httpHeaders -> httpHeaders.setBasicAuth(new String(encodedCreds)))
                                       .clientConnector(new ReactorClientHttpConnector(HttpClient.create().followRedirect(true)))
                                       .build();

        ResponseSpec response = webClient.get().uri(uriComponentsBuilder.toUriString()).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).retrieve();

        response.bodyToMono(String.class).subscribe(j -> pokerService.findJiraIssueResponse(createJiraIssueDTO(j), session));
    }

    public JiraIssueDTO createJiraIssueDTO(final String jsonBody) {
        JiraIssueDTO result;

        JsonObject root = JsonParser.parseString(jsonBody).getAsJsonObject();

        final JsonObject fields = root.getAsJsonObject("fields");

        Calendar created       = null;
        String   createdAsText = getJsonValueSafe(fields, "created");
        if (! StringUtils.isEmpty(createdAsText)) {
            created = Calendar.getInstance();
            created.setTime(fromTimeString(createdAsText));
        }

        List<String> labels = new ArrayList<>();
        fields.getAsJsonArray("labels").forEach(l -> labels.add(l.getAsString()));

        result = JiraIssueDTO.builder()
                             .id(getJsonValueSafe(root, "id"))
                             .key(getJsonValueSafe(root, "key"))
                             .summary(getJsonValueSafe(fields, "summary"))
                             .description(getJsonValueSafe(fields, "description"))
                             .UAC(getJsonValueSafe(fields, fieldNameUAC))
                             .assignee(createIssuePerson(fields.get("assignee").getAsJsonObject()))
                             .creator(createIssuePerson(fields.get("creator").getAsJsonObject()))
                             .created(created)
                             .labels(labels)
                             .build();

        return result;
    }

    private String getJsonValueSafe(final JsonObject jsonObject, final String name) {
        String            result      = "";
        final JsonElement jsonElement = jsonObject.get(name);
        if (! jsonElement.isJsonNull()) {
            result = jsonElement.getAsString();
        }
        return result;
    }

    private JiraIssueDTO.Person createIssuePerson(final JsonObject person) {
        JiraIssueDTO.Person result = JiraIssueDTO.Person.builder()
                                                        .name(getJsonValueSafe(person, "name"))
                                                        .displayName(getJsonValueSafe(person, "displayName"))
                                                        .key(getJsonValueSafe(person, "key"))
                                                        .emailAddress(getJsonValueSafe(person, "emailAddress"))
                                                        .build();
        return result;
    }

    public static Date fromTimeString(@Nullable String time) throws IllegalArgumentException {
        try {
            SimpleDateFormat format = new SimpleDateFormat(TIME_FORMAT);
            format.setLenient(false);
            return time != null ? format.parse(time) : null;
        } catch (ParseException e) {
            throw new IllegalArgumentException("Error parsing time: " + time, e);
        }
    }
}
