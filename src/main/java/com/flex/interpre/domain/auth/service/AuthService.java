package com.flex.interpre.domain.auth.service;

import com.flex.interpre.domain.auth.dto.response.GoogleUserInfo;
import com.flex.interpre.domain.user.entity.User;
import com.flex.interpre.domain.user.repository.UserRepository;

import com.flex.interpre.global.property.GoogleProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final GoogleProperty googleProperty;
    private final WebClient webClient;

    private final UserRepository userRepository;

    public void login(String code, User.Role role) {

        String accessToken = getAccessToken(code);

        GoogleUserInfo userInfo = getUserInfo(accessToken);

        User user = userRepository.findByGoogleId(userInfo.getId()).orElseGet(()->from(userInfo,role));
        //TODO: JWT 토큰 생성
    }




    private String getAccessToken(String code){
        return webClient.post()
                .uri("https://oauth2.googleapis.com/token")
                .header(HttpHeaders.CONTENT_TYPE,MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .bodyValue("code="+code
                        +"&client_id="+googleProperty.getClientId()
                        +"&client_secret="+googleProperty.getClientSecret()
                        +"&redirect_uri="+googleProperty.getRedirectUrl()
                        +"&grant_type=authorization_code")
                .retrieve()
                .bodyToMono(Map.class)
                .map(res -> (String) res.get("access_token"))
                .block();
    }

    private GoogleUserInfo getUserInfo(String accessToken){

        return webClient.get()
                .uri("https://www.googleapis.com/oauth2/v2/userinfo")
                .header(HttpHeaders.AUTHORIZATION,"Bearer "+accessToken)
                .retrieve()
                .bodyToMono(GoogleUserInfo.class)
                .block();
    }

    private User from(GoogleUserInfo userInfo, User.Role role){

        boolean approved = true;
        if (role == User.Role.COMPANY) approved = false;

        return userRepository.save(User.builder()
                        .email(userInfo.getEmail())
                        .googleId(userInfo.getId())
                        .role(role)
                        .approved(approved)
                .build());
    }
}
