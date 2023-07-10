package com.example.demo.controller;

import com.example.demo.service.CalendarService;
import com.example.demo.service.KakaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Controller
@RequiredArgsConstructor
public class KakaoContoller {

	private final KakaoService kakaoService;

	@GetMapping("/")
	public String home() {return "index";}

	@RequestMapping("/login-callback")
	public RedirectView loginCallback(@RequestParam("code") String code) {
	   return kakaoService.loginCallback(code);
	}
	
	@RequestMapping("/authorize")
    public RedirectView goKakaoOAuth() {
		return kakaoService.goKakaoOAuth();
    }
	
	@RequestMapping("/message")
    public String message() {
       return kakaoService.message();
    }

	@RequestMapping("/calendar")
	public String calendar() {
		String redirectUrl = "https://accounts.google.com/o/oauth2/v2/auth?" +
				"scope=https://www.googleapis.com/auth/calendar.events&"+
				"access_type=online&"+
				"response_type=code&"+
				"redirect_uri=http://localhost:8080/callback&"+
				"client_id=";
		return "redirect:"+redirectUrl;
	}

	@RequestMapping("/callback")
	public String callback(@RequestParam(required = false) String code,
						   @RequestParam(required = false) String scope){
		return "redirect:"+CalendarService.getAccessTokenJsonData(code);
	}

	@RequestMapping("/token")
	public String token() throws GeneralSecurityException, IOException {
		String accessToken = CalendarService.readAccessTokenFromFile();
		return "redirect:"+CalendarService.setEvent(accessToken);
	}
}
