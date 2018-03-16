package io.paulbaker.aws.lambda.jira;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by Paul N. Baker on 03/16/2018
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@SuppressWarnings("unused")
public class SearchRequest {

    @JsonProperty("protocol")
    private String protocol;

    @JsonProperty("host")
    private String host;

    @JsonProperty("authorization")
    private String basicAuth;

    @JsonProperty("jql_items")
    private List<String> jqlItems;
}
