package myapp.backend.domain.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import myapp.backend.domain.auth.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private JwtService jwtService;

    // 기존 세션 기반 사용자 정보 반환 (POST 요청)
    @PostMapping("/user")
    public Map<String, Object> getCurrentUser(Authentication authentication) {
        Map<String, Object> userInfo = new HashMap<>();

        if (authentication != null && authentication.getPrincipal() instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            Map<String, Object> attributes = oauth2User.getAttributes();

            if (attributes.containsKey("response")) { // 네이버
                Map<String, Object> response = (Map<String, Object>) attributes.get("response");
                userInfo.put("email", response.get("email"));
                userInfo.put("name", response.get("nickname"));
                userInfo.put("profileImg", response.get("profile_image"));
                userInfo.put("provider", "naver");
            } else if (attributes.containsKey("kakao_account")) { // 카카오
                Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
                Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
                userInfo.put("email", kakaoAccount.get("email"));
                userInfo.put("name", profile.get("nickname"));
                userInfo.put("profileImg", profile.get("profile_image_url"));
                userInfo.put("provider", "kakao");
            } else { // 구글
                userInfo.put("email", attributes.get("email"));
                userInfo.put("name", attributes.get("name"));
                userInfo.put("profileImg", attributes.get("picture"));
                userInfo.put("provider", "google");
            }
            userInfo.put("isAuthenticated", true);
        } else {
            userInfo.put("isAuthenticated", false);
            userInfo.put("message", "User not authenticated or not an OAuth2 user.");
        }
        return userInfo;
    }

    // JWT 토큰 기반 사용자 정보 반환 (새로운 엔드포인트)
    @PostMapping("/user/token")
    public Map<String, Object> getCurrentUserByToken(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> userInfo = new HashMap<>();

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            
            if (jwtService.validateToken(token)) {
                try {
                    userInfo.put("user_id", jwtService.extractUserId(token));
                    userInfo.put("sns_type", jwtService.extractSnsType(token));
                    userInfo.put("sns_id", jwtService.extractSnsId(token));
                    userInfo.put("username", jwtService.extractUsername(token));
                    userInfo.put("isAuthenticated", true);
                } catch (Exception e) {
                    userInfo.put("isAuthenticated", false);
                    userInfo.put("message", "Invalid token");
                }
            } else {
                userInfo.put("isAuthenticated", false);
                userInfo.put("message", "Invalid or expired token");
            }
        } else {
            userInfo.put("isAuthenticated", false);
            userInfo.put("message", "No token provided");
        }
        
        return userInfo;
    }

    // **로그아웃 컨트롤러는 삭제 권장!**
    // 로그아웃은 SecurityConfig의 logout 설정에 맡기세요.
}
