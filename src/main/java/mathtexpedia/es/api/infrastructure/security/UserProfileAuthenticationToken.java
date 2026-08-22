package mathtexpedia.es.api.infrastructure.security;

import mathtexpedia.es.api.domain.security.UserProfile;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;

public class UserProfileAuthenticationToken extends AbstractAuthenticationToken {

    private final UserProfile principal;
    private final Jwt jwt;

    public UserProfileAuthenticationToken(UserProfile principal, Jwt jwt,
                                          Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.jwt = jwt;
        setAuthenticated(true);
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    @Override
    public Object getCredentials() {
        return jwt;
    }
}
