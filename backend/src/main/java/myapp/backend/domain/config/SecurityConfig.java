package myapp.backend.domain.config;

import myapp.backend.domain.config.OAuth2LoginSuccessHandler;
import myapp.backend.domain.auth.service.CustomOAuth2UserService;
import myapp.backend.domain.auth.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Autowired
    private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // 개발용: CSRF 비활성화
            .authorizeHttpRequests(authorize -> authorize
                // 리소스 및 OAuth 로그인 관련 경로는 모두 허용
                .requestMatchers("/", "/css/**", "/js/**", "/images/**", "/api/auth/**", "/login/oauth2/**").permitAll()
                // /api/user, /api/logout 등은 인증된 사용자만 접근 가능
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                // 로그인 성공 시 프론트엔드(React 등) 주소로 리다이렉트
                // .defaultSuccessUrl("http://localhost:3000", true) // successHandler와 중복되므로 제거
                .failureUrl("/login?error")
                .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                .successHandler(oAuth2LoginSuccessHandler)
            )
            .logout(logout -> logout
                .logoutUrl("/api/logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .logoutSuccessHandler((request, response, authentication) -> {
                    response.setStatus(200);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"message\":\"로그아웃 성공\", \"isAuthenticated\":false}");
                })
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
