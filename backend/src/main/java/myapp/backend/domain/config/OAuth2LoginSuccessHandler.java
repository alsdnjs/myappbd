package myapp.backend.domain.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import myapp.backend.domain.auth.vo.UserVO;
import myapp.backend.domain.auth.mapper.UserMapper;
import myapp.backend.domain.auth.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtService jwtService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String registrationId;
        String snsId = null;  // 반드시 초기화

        if (attributes.containsKey("response")) { // 네이버 로그인 처리
            Map<String, Object> responseData = (Map<String, Object>) attributes.get("response");
            snsId = (String) responseData.get("id");  // 네이버 user id 가져오기
            registrationId = "naver";

        } else if (attributes.containsKey("sub")) { // 구글 로그인 처리
            snsId = (String) attributes.get("sub");
            registrationId = "google";

        } else { // 카카오 로그인 처리
            snsId = String.valueOf(attributes.get("id"));
            registrationId = "kakao";
        }

        // snsId가 정상적으로 할당됐는지 체크 (안전장치)
        if (snsId == null) {
            throw new IllegalStateException("snsId가 할당되지 않았습니다.");
        }

        // DB에서 유저 조회 (이미 CustomOAuth2UserService에서 처리됨)
        UserVO user = userMapper.findBySnsIdAndSnsType(snsId, registrationId);

        // 디버깅 로그 추가
        System.out.println("=== 로그인 성공 핸들러 디버깅 ===");
        System.out.println("SNS ID: " + snsId);
        System.out.println("SNS Type: " + registrationId);
        System.out.println("DB 조회 전 User ID: " + user.getUser_id());
        
        // DB에서 직접 user_id 조회하여 강제 설정
        Integer dbUserId = userMapper.getUserIdBySnsInfo(snsId, registrationId);
        System.out.println("DB에서 직접 조회한 User ID: " + dbUserId);
        if (dbUserId != null && dbUserId > 0) {
            user.setUser_id(dbUserId);
            System.out.println("강제 설정 후 User ID: " + user.getUser_id());
        } else {
            System.out.println("DB에서 user_id를 가져올 수 없음!");
        }
        
        System.out.println("최종 User ID: " + user.getUser_id());
        System.out.println("Username: " + user.getUsername());
        System.out.println("================================");

        // 세션 저장
        HttpSession session = request.getSession();
        session.setAttribute("username", user.getUsername());
        session.setAttribute("user_id", user.getUser_id());
        session.setAttribute("loginStatus", "ok");

        // JWT 토큰 생성 및 응답 헤더에 추가
        String jwtToken = jwtService.generateToken(user);
        response.setHeader("Authorization", "Bearer " + jwtToken);

        // 리다이렉트
        response.sendRedirect("http://localhost:3000");
    }
}
