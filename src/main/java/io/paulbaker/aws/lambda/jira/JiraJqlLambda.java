package io.paulbaker.aws.lambda.jira;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.util.concurrent.Promise;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Base64.getDecoder;

/**
 * Created by Paul N. Baker on 03/16/2018
 */
@SuppressWarnings("unused")
public class JiraJqlLambda implements RequestStreamHandler {

    private ObjectMapper objectMapper;

    public JiraJqlLambda() {
        objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        SearchRequest searchRequest = objectMapper.readValue(inputStream, SearchRequest.class);

        List<Issue> issues = getJiraIssues(searchRequest);

        objectMapper.writeValue(outputStream, issues);
    }

    private List<Issue> getJiraIssues(SearchRequest searchRequest) {
        JiraRestClient jiraRestClient = toJiraRestClient(searchRequest);
        SearchRestClient searchClient = jiraRestClient.getSearchClient();
        return searchRequest.getJqlItems().stream()
                .map(searchClient::searchJql)
                .map(Promise::claim)
                .map(SearchResult::getIssues)
                .flatMap(JiraJqlLambda::toStream)
                .collect(Collectors.toList());
    }

    private static <T> Stream<T> toStream(Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    private static JiraRestClient toJiraRestClient(SearchRequest searchRequest) {
        URI host = toURI(searchRequest);
        String[] authTuple = getAuthTuple(searchRequest.getBasicAuth());
        String username = authTuple[0];
        String password = authTuple[1];
        JiraRestClientFactory jiraRestClientFactory = new AsynchronousJiraRestClientFactory();
        return jiraRestClientFactory.createWithBasicHttpAuthentication(host, username, password);
    }

    private static URI toURI(SearchRequest searchRequest) {
        String uriString = searchRequest.getProtocol() + "://" + searchRequest.getHost();
        try {
            return new URL(uriString).toURI();
        } catch (URISyntaxException | MalformedURLException e) {
            System.err.println("URI String: " + uriString);
            throw new IllegalArgumentException(e);
        }
    }

    private static String[] getAuthTuple(String basicAuth) {
        Pattern pattern = Pattern.compile("(?:Basic\\s+)(.*)");
        Matcher matcher = pattern.matcher(basicAuth);
        if (!matcher.find()) {
            throw new IllegalArgumentException("There was no credentials to be pulled from the basic auth header.");
        }
        String encodedAuth = matcher.group(1);
        String decodedAuth = new String(getDecoder().decode(encodedAuth));

        return decodedAuth.split(":", 2);
    }
}
