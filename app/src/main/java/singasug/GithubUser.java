package singasug;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Collections;

public class GithubUser {

    private String username;
    private String name;
    private String avatarUrl;

    @JsonCreator
    public GithubUser(@JsonProperty("login") String username,
                      @JsonProperty("name") String name,
                      @JsonProperty("avatar_url") String avatarUrl) {
        this.username = username;
        this.name = name;
        this.avatarUrl = avatarUrl;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }

    Authentication toAuthentication() {
        AbstractAuthenticationToken token = new AbstractAuthenticationToken(Collections.emptyList()) {
            @Override
            public Object getCredentials() {
                return null;
            }

            @Override
            public Object getPrincipal() {
                return username;
            }
        };

        token.setDetails(GithubUser.this);
        token.isAuthenticated();
        return token;
    }

    @Override
    public String toString() {
        return "GithubUser{" +
                "username='" + username + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
