package singasug;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.web.util.UriTemplate;

import java.net.URI;

@ConfigurationProperties(prefix = "github")
public class GithubConfig {

    private String clientSecret;
    private String clientId;
    private UriTemplate accessTokenUri;
    private UriTemplate userUri;
    private String authoriseUri;

    public URI getUserUri(String accessToken) {
        return userUri.expand(accessToken);
    }

    public void setUserUri(String userUri) {
        this.userUri = new UriTemplate(userUri);
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public URI getAccessTokenUri(String code) {
        return accessTokenUri.expand(code);
    }

    public void setAccessTokenUri(String accessTokenUri) {
        this.accessTokenUri = new UriTemplate(accessTokenUri);
    }

    public String getAuthoriseUri() {
        return authoriseUri;
    }

    public void setAuthoriseUri(String authoriseUri) {
        this.authoriseUri = authoriseUri;
    }
}
