package myapp.backend.domain.auth.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import myapp.backend.domain.auth.service.JwtService;
import myapp.backend.domain.auth.vo.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractTokenFromRequest(request);
        System.out.println("[JwtAuthenticationFilter] 요청에서 추출한 토큰: " + token);

        if (StringUtils.hasText(token)) {
            boolean valid = jwtService.validateToken(token);
            System.out.println("[JwtAuthenticationFilter] 토큰 유효성 검사 결과: " + valid);

            if (valid) {
                try {
                    String username = jwtService.extractUsername(token);
                    Integer userId = jwtService.extractUserId(token);
                    String snsType = jwtService.extractSnsType(token);
                    String snsId = jwtService.extractSnsId(token);

                    System.out.println("[JwtAuthenticationFilter] 토큰에서 추출한 username: " + username);
                    System.out.println("[JwtAuthenticationFilter] 토큰에서 추출한 userId: " + userId);

                    if (username != null && userId != null) {
                        UserPrincipal userPrincipal = new UserPrincipal(username, userId, snsType, snsId);

                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userPrincipal,
                                null,
                                userPrincipal.getAuthorities()
                        );

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        System.out.println("[JwtAuthenticationFilter] SecurityContext에 인증 정보 설정 완료");
                    }
                } catch (Exception e) {
                    System.err.println("[JwtAuthenticationFilter] JWT 처리 중 예외 발생:");
                    e.printStackTrace();
                }
            }
        } else {
            System.out.println("[JwtAuthenticationFilter] 토큰이 없거나 비어있음");
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
