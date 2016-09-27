package singasug;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplate;

import java.net.URI;
import java.security.Principal;
import java.util.Collections;

@SpringBootApplication
@EnableConfigurationProperties(GithubConfig.class)
@Controller
public class SpringularApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringularApplication.class, args);
    }

    @GetMapping("/me")
    @ResponseBody
    Object me(Principal principal) {
        return principal;
    }

    @GetMapping("/githubLogin")
    String githubLogin() {
        return "redirect:" + githubConfig.getAuthoriseUri();
    }

    RestTemplate restTemplate = new RestTemplate();

    @Autowired
    GithubConfig githubConfig;

    @GetMapping("/callback")
    Object callback(@RequestParam String code) {

        GithubTokenResponse response = restTemplate.postForEntity(githubConfig.getAccessTokenUri(code),
                null, GithubTokenResponse.class).getBody();

        GithubUser githubUser = restTemplate.getForObject(
                githubConfig.getUserUri(response.getAccessToken()),
                GithubUser.class);

        SecurityContextHolder.getContext().setAuthentication(new AbstractAuthenticationToken(Collections.emptyList()) {

            {
                setAuthenticated(true);
                setDetails(githubUser);
            }

            @Override
            public Object getCredentials() { return null; }

            @Override
            public Object getPrincipal() { return githubUser.getUsername(); }

        });

        return "redirect:/";
    }

    @Bean
    WebSecurityConfigurerAdapter webSecurityConfigurerAdapter() throws Exception {
        return new WebSecurityConfigurerAdapter() {
            @Override
            protected void configure(HttpSecurity http) throws Exception {
                http
                        .httpBasic().disable()

                        .authorizeRequests()
                        .mvcMatchers("/me").authenticated()
                        .mvcMatchers("/callback").permitAll()

                        .and().logout().logoutSuccessHandler((req, resp, auth) -> {}).permitAll()
                        .and().csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
            }
        };
    }

}

class GithubTokenResponse {

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

class GithubUser {

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

    @Override
    public String toString() {
        return "GithubUser{" +
                "username='" + username + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}

@ConfigurationProperties(prefix = "github")
class GithubConfig {

    private UriTemplate accessTokenUri;
    private UriTemplate userUri;
    private String authoriseUri;

    public URI getUserUri(String accessToken) {
        return userUri.expand(accessToken);
    }

    public void setUserUri(String userUri) {
        this.userUri = new UriTemplate(userUri);
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
