package myapp.backend.domain.auth.vo;

import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collections;
import java.util.Map;

@Getter
public class UserPrincipal extends User {
    private final int userId;
    private final String snsType;
    private final String snsId;

    public UserPrincipal(String username, int userId, String snsType, String snsId) {
        super(username, "", Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        this.userId = userId;
        this.snsType = snsType;
        this.snsId = snsId;
    }

    public Map<String, Object> getCustomAttributes() {
        return Map.of(
            "user_id", userId,
            "username", getUsername(),
            "sns_type", snsType,
            "sns_id", snsId
        );
    }
} 