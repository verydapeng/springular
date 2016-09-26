package singasug;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GithubTokenResponse {

    private final String accessToken;

    @JsonCreator
    public GithubTokenResponse(@JsonProperty("access_token") String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    @Override
    public String toString() {
        return "GithubTokenResponse{" +
                "accessToken='" + accessToken + '\'' +
                '}';
    }
}
