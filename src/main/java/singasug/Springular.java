package singasug;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.security.Principal;

@SpringBootApplication
@EnableConfigurationProperties(GithubConfig.class)
@Controller
public class Springular {

    private static final Logger logger = LoggerFactory.getLogger(Springular.class);

    public static void main(String[] args) {
        SpringApplication.run(Springular.class, args);
    }

    RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/me")
    @ResponseBody
    Object me(Principal principal) {
        if (principal != null) {
            return principal;
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @Autowired
    GithubConfig githubConfig;

    @GetMapping("/githubLogin")
    Object githubLogin() {
        return "redirect:" + githubConfig.getAuthoriseUri();
    }

    @GetMapping("/callback")
    Object callback(@RequestParam String code) {
        GithubTokenResponse response = restTemplate.postForEntity(githubConfig.getAccessTokenUri(code), null, GithubTokenResponse.class).getBody();
        logger.info("tokenResponse: {}", response);
        GithubUser user = restTemplate.getForEntity(githubConfig.getUserUri(response.getAccessToken()), GithubUser.class).getBody();
        logger.info("user: {}");
        SecurityContextHolder.getContext().setAuthentication(user.toAuthentication());

        return "redirect:/";
    }

    @Bean
    WebSecurityConfigurerAdapter webSecurityConfigurerAdapter() throws Exception {
        return new WebSecurityConfigurerAdapter() {
            @Override
            protected void configure(HttpSecurity http) throws Exception {
                http
                        .authorizeRequests()
                        .mvcMatchers("/me").permitAll()
                        .mvcMatchers("/callback").permitAll()
                        .and().logout().logoutSuccessHandler((request, response, authentication) -> {}).permitAll()
                        .and().csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
            }

        };
    }
}
