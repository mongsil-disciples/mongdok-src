package com.web.mongdok.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class KakaoAPI {
	
	@Value("${spring.kakao.redirect}")
	private String redirect;
	
    public Map<String, String> getAccessToken(String authorize_code) {
        String accessToken = "";
        String refreshToken = "";
        String reqURL = "https://kauth.kakao.com/oauth/token"; // 토큰 받기
        
        try {
            URL url = new URL(reqURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
            StringBuilder sb = new StringBuilder();
            sb.append("grant_type=authorization_code");
//            sb.append("&client_id=APPKEY");
            sb.append("&client_id=4da2e6372fc055ada48d1942fd63ddcf");
//            sb.append("&redirect_uri=http://localhost:8080/join");
            sb.append("&redirect_uri=" + redirect);
            sb.append("&code=" + authorize_code);
            
            bw.write(sb.toString());
            bw.flush();

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = "";
            String result = "";

            while ((line = br.readLine()) != null) {
                result += line;
            }

            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(result);

            accessToken = element.getAsJsonObject().get("access_token").getAsString();
            refreshToken = element.getAsJsonObject().get("refresh_token").getAsString();
            
//            System.out.println("access_token: " + access_Token);
//            System.out.println("refresh_token: " + refresh_Token);
            br.close();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);
        return tokens;
    }

    public Map<String, String> getUserInfo(String accessToken, String refreshToken) {

        Map<String, String> userInfo = new HashMap<>();
        String reqURL = "https://kapi.kakao.com/v2/user/me"; // 사용자 정보 가져오기

        try {
            URL url = new URL(reqURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");

            conn.setRequestProperty("Authorization", "Bearer " + accessToken);

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String line = "";
            String result = "";

            while ((line = br.readLine()) != null) {
                result += line;
            }
//			System.out.println("result: " + result);

			JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(result);
           
            String id = element.getAsJsonObject().get("id").getAsString();
            String email = null;
            
			userInfo.put("id", id);
            userInfo.put("refresh_token", refreshToken);
            userInfo.put("access_token", accessToken);
            
            
        } catch (IOException e) {
            e.printStackTrace();
        }

        return userInfo;
    }

    // 토큰 정보 보기 (-401: 토큰 값이 잘못되었거나 만료되어 유효하지 않은 경우로 토큰 갱신 필요)
	public String auth(String accessToken) {
		
        String reqURL = "https://kapi.kakao.com/v1/user/access_token_info"; // 토큰 정보 보기
		String result = "";
		
        try {
            URL url = new URL(reqURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            conn.setRequestProperty("Authorization", "Bearer " + accessToken);

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            
            // 응답이 401이라면 토큰 갱신하기
            // 이외의 에러는 로그아웃 처리
            
            String line = "";

            while ((line = br.readLine()) != null) {
                result += line;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
	}
	
	// 토큰 갱신하기
	public Map<String, String> freshToken(String refreshToken) {
		
		String accessToken = "";
        String newRefreshToken = "";
        String reqURL = "https://kauth.kakao.com/oauth/token"; // 토큰 받기

        try {
            URL url = new URL(reqURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
            StringBuilder sb = new StringBuilder();
            sb.append("grant_type=refresh_token");
//            sb.append("&client_id=APPKEY");
            sb.append("&client_id=4da2e6372fc055ada48d1942fd63ddcf");
            sb.append("&refresh_token=" + refreshToken);
            sb.append("&redirect_uri=http://localhost:3000/kakaologin");
            
            bw.write(sb.toString());
            bw.flush();

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = "";
            String result = "";

            while ((line = br.readLine()) != null) {
                result += line;
            }

            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(result);

            accessToken = element.getAsJsonObject().get("access_token").getAsString();
            newRefreshToken = element.getAsJsonObject().get("refresh_token").getAsString();

            br.close();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);
        return tokens;
	}

	public String unlink(String accessToken) {
		
        String reqURL = "https://kapi.kakao.com/v1/user/unlink"; // 연결 끊기
		String result = "";
		
        try {
            URL url = new URL(reqURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");

            conn.setRequestProperty("Authorization", "Bearer " + accessToken);

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            
            String line = "";

            while ((line = br.readLine()) != null) {
                result += line;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
	}

	public boolean unlinkByAdmin(String kakaoId) {
		String reqURL = "https://kapi.kakao.com/v1/user/unlink"; // 연결 끊기
		String result = "";
		
        try {
            URL url = new URL(reqURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "KakaoAK " + "c4d7b7fd679f0d3dc5a5c63ab697ce89");
			conn.setDoOutput(true);
			
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
            StringBuilder sb = new StringBuilder();
            sb.append("target_id_type=user_id");
            sb.append("&target_id=" + kakaoId);
            
            bw.write(sb.toString());
            bw.flush();
            
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            
            
            String line = null;

//            while ((line = br.readLine()) != null) {
//                result += line;
//            }
            
            if((line = br.readLine()) != null)
            	return true;
            return false;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
	}
}