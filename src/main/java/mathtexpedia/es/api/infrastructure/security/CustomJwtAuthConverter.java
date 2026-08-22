package mathtexpedia.es.api.infrastructure.security;

import mathtexpedia.es.api.domain.security.UserProfile;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;

public class CustomJwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final KeycloakRoleConverter roleConverter = new KeycloakRoleConverter();

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = roleConverter.convert(jwt);

        String email = jwt.getClaimAsString("email");

        String role = authorities.stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("ROLE_USER");

        UserProfile userProfile = new UserProfile(email, role);

        return new UserProfileAuthenticationToken(userProfile, jwt, authorities);
    }
}
