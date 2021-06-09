package com.alexmegremis.planningpoker.integration.jira;

import com.alexmegremis.planningpoker.PokerService;
import com.alexmegremis.planningpoker.SessionDTO;
import com.google.gson.*;
import com.vaadin.spring.annotation.SpringComponent;
import lombok.AccessLevel;
import lombok.Setter;
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

import javax.annotation.PostConstruct;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;

import static org.springframework.web.reactive.function.client.WebClient.ResponseSpec;

@Slf4j
@SpringComponent
@Scope (value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class JiraServiceImpl implements JiraService {

    public static final String TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    @Value ("${jira.hostname}")
    @Setter(AccessLevel.PRIVATE)
    private String jiraHostname;
    @Value ("${jira.port}")
    @Setter(AccessLevel.PRIVATE)
    private String jiraPort;
    @Value ("${jira.scheme}")
    @Setter(AccessLevel.PRIVATE)
    private String jiraScheme;
    @Value ("${jira.issueAPI}")
    @Setter(AccessLevel.PRIVATE)
    private String issueAPI;
    @Value ("${jira.uac.fieldname}")
    @Setter(AccessLevel.PRIVATE)
    private String fieldNameUAC;
    @Value ("${jira.user}")
    @Setter(AccessLevel.PRIVATE)
    private String user;
    @Value ("${jira.pass}")
    @Setter(AccessLevel.PRIVATE)
    private String pass;

    @PostConstruct
    private void externalInit() {
        getEnvVarAndLog("JIRA_HOSTNAME", this :: setJiraHostname);
        getEnvVarAndLog("JIRA_PORT", this :: setJiraPort);
        getEnvVarAndLog("JIRA_SCHEME", this :: setJiraScheme);
        getEnvVarAndLog("JIRA_ISSUEAPI", this :: setIssueAPI);
        getEnvVarAndLog("JIRA_UAC_FIELDNAME", this :: setFieldNameUAC);
        getEnvVarAndLog("JIRA_USER", this :: setUser);
        getEnvVarAndLog("JIRA_PASS", this :: setPass);
    }
    private void getEnvVarAndLog(final String varName, final Consumer<String> setter) {
        String result = System.getenv(varName);
        log.info(">>> Finding env var value for {}, found {}", varName, result);
        if(StringUtils.hasLength(result)) {
            log.info(">>> Setting override for {} with value {}", varName, result);
            setter.accept(result);
        }
    }


    @Override
    public void getIssueByKey(final PokerService pokerService, final SessionDTO session, final String issueKey) {

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance()
                                                                        .scheme(jiraScheme)
                                                                        .host(jiraHostname)
                                                                        .port(jiraPort)
                                                                        .queryParam("fields", "description,summary,created,updated,creator,assignee,labels," + fieldNameUAC);

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

    @Override
    public JiraIssueDTO createJiraIssueDTO(final String jsonBody) {
        JiraIssueDTO result;

        JsonObject root = JsonParser.parseString(jsonBody).getAsJsonObject();

        final JsonObject fields = root.getAsJsonObject("fields");

        Calendar created = parseTimestamp(fields, "created");
        Calendar updated = parseTimestamp(fields, "updated");

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
                             .updated(updated)
                             .labels(labels)
                             .build();

        return result;
    }

    private Calendar parseTimestamp(final JsonObject fields, final String timestampName) {
        final String timestampAsText = getJsonValueSafe(fields, timestampName);
        Calendar     result          = null;
        if (StringUtils.hasLength(timestampAsText)) {
            result = Calendar.getInstance();
            result.setTime(fromTimeString(timestampAsText));
        }
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
            SimpleDateFormat format = new SimpleDateFormat(JiraServiceImpl.TIME_FORMAT);
            format.setLenient(false);
            return time != null ? format.parse(time) : null;
        } catch (ParseException e) {
            throw new IllegalArgumentException("Error parsing time: " + time, e);
        }
    }
}
