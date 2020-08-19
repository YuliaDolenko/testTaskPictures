package com.test.taskAgileEngine.service.impl;

import com.test.taskAgileEngine.dto.TokenDto;
import com.test.taskAgileEngine.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.test.taskAgileEngine.constants.Constants.API_KEY;
import static com.test.taskAgileEngine.constants.Constants.BEARER;
import static com.test.taskAgileEngine.constants.Constants.PICTURES_ENDPOINT;

@Service
public class TokenServiceImpl implements TokenService {

    @Value("${urlAuth}")
    private String authRequest;

    @Value("${apiKey}")
    private String apiKey;

    private String token = "";

    private final RestTemplate restTemplate;

    @Autowired
    public TokenServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String getToken() {
        if (isValidToken()) {
            return token;
        }

        return getNewToken();
    }


    private boolean isValidToken() {
        HttpHeaders headers = new HttpHeaders() {{
            setContentType(MediaType.APPLICATION_JSON);
            set(AUTHORIZATION, BEARER + token);
        }};

        try {
            HttpEntity<String> entity = new HttpEntity<>(headers);
            HttpStatus httpStatus = restTemplate.exchange(PICTURES_ENDPOINT, HttpMethod.GET, entity, String.class).getStatusCode();

            return Objects.equals(httpStatus, HttpStatus.OK);
        } catch (Exception e) {
            return false;
        }
    }

    private String getNewToken() {
        try {

            Map<String, Object> request = new HashMap<String, Object>() {{
                put(API_KEY, apiKey);
            }};

            TokenDto tokenDto = restTemplate.postForEntity(authRequest, request, TokenDto.class).getBody();

            token = Objects.requireNonNull(tokenDto).getToken();
            return token;
        } catch (Exception e) {
            throw new RuntimeException("Something went wrong, invalid token exception");
        }
    }
}
