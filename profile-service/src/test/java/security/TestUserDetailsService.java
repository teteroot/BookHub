package security;

import com.bookhub.profileservice.enums.UserRole;
import com.bookhub.profileservice.security.GatewayUserDetails;
import lombok.Getter;
import org.jspecify.annotations.NullMarked;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.UUID;


@TestComponent
public class TestUserDetailsService implements UserDetailsService {
    @Getter
    private final UUID userId = UUID.randomUUID();

    @Override
    @NullMarked
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return new GatewayUserDetails(userId, UserRole.AUTHOR);
    }

}
