package com.example.demo.service;

import com.example.demo.common.Const;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpSession;

@RequiredArgsConstructor
@Service
public class KakaoService {
	
	private final HttpSession httpSession;	
	private final HttpCallService httpCallService;
	private String accessToken = "access_token";

	@Value("${rest-api-key}")
	private String REST_API_KEY;
	
	@Value("${redirect-uri}")
	private String REDIRECT_URI;	
	
	@Value("${authorize-uri}")
	private String AUTHORIZE_URI;		
	
	@Value("${token-uri}")
	public String TOKEN_URI;			
	
	@Value("${client-secret}")
	private String CLIENT_SECRET;	
	
	@Value("${kakao-api-host}")
	private String KAKAO_API_HOST;
	
	public RedirectView goKakaoOAuth() {
	   
	   String uri = AUTHORIZE_URI +
			   "?redirect_uri=" + REDIRECT_URI
			   + "&response_type=code&client_id=" + REST_API_KEY
			   + "&scope=talk_message";
			   
       return new RedirectView(uri);
	}	
	
	public RedirectView loginCallback(String code) {	
		String param = "grant_type=authorization_code&client_id="+REST_API_KEY+"&redirect_uri="+REDIRECT_URI+"&client_secret="+CLIENT_SECRET+"&code="+code;
		String rtn = httpCallService.Call(Const.POST, TOKEN_URI, Const.EMPTY, param);
        httpSession.setAttribute("token", token(rtn, new JsonParser()));
		return new RedirectView("/index.html");
	}
	
	public String message() {	
		String uri = KAKAO_API_HOST + "/v2/api/talk/memo/send?template_id=95748";
		return httpCallService.CallwithToken(Const.POST, uri, accessToken);
	}

	public String token(String rtn, JsonParser parser) {
		JsonElement element = parser.parse(rtn);
		return element.getAsJsonObject().get("access_token").getAsString();
	}
}
