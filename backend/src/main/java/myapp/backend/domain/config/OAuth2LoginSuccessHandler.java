package myapp.backend.domain.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import myapp.backend.domain.auth.mapper.UserMapper;
import myapp.backend.domain.auth.service.JwtService;
import myapp.backend.domain.auth.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Map;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtService jwtService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        if (response.isCommitted()) {
            logger.debug("Response has already been committed. Unable to redirect.");
            return;
        }

        DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String registrationId;
        String snsId = null;

        if (attributes.containsKey("response")) {
            Map<String, Object> responseData = (Map<String, Object>) attributes.get("response");
            snsId = (String) responseData.get("id");
            registrationId = "naver";

        } else if (attributes.containsKey("sub")) {
            snsId = (String) attributes.get("sub");
            registrationId = "google";

        } else {
            snsId = String.valueOf(attributes.get("id"));
            registrationId = "kakao";
        }

        if (snsId == null) {
            throw new IllegalStateException("snsId가 할당되지 않았습니다.");
        }

        UserVO user = userMapper.findBySnsIdAndSnsType(snsId, registrationId);

        Integer dbUserId = userMapper.getUserIdBySnsInfo(snsId, registrationId);
        if (dbUserId != null && dbUserId > 0) {
            user.setUser_id(dbUserId);
        }

        if (user == null || user.getUser_id() == 0) {
             logger.error("로그인 후 사용자 정보를 찾거나 ID를 가져올 수 없습니다.");
             getRedirectStrategy().sendRedirect(request, response, "/login?error=user_processing_failed");
             return;
        }

        String jwtToken = jwtService.generateToken(user);
        
        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:3000/")
                .queryParam("token", jwtToken)
                .build().toUriString();
        
        // 디버깅 로그 강화
        logger.info("Generated JWT and redirecting to: " + targetUrl);

        // 부모 클래스의 리다이렉트 전략 사용
        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
