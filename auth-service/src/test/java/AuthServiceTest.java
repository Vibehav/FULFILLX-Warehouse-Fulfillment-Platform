import com.fulfillx.auth.dto.*;
import com.fulfillx.auth.entity.User;
import com.fulfillx.auth.enums.Role;
import com.fulfillx.auth.exception.EmailAlreadyExistsException;
import com.fulfillx.auth.repository.UserRepository;
import com.fulfillx.auth.security.JwtUtil;
import com.fulfillx.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User mockUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@fulfillx.com");
        registerRequest.setPassword("password123");
        registerRequest.setFullName("Test User");
        registerRequest.setRole(Role.SELLER);
        registerRequest.setTenantId("tenant-001");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@fulfillx.com");
        loginRequest.setPassword("password123");

        mockUser = User.builder()
                .id("user-001")
                .email("test@fulfillx.com")
                .password("hashedPassword")
                .fullName("Test User")
                .role(Role.SELLER)
                .tenantId("tenant-001")
                .active(true)
                .build();
    }

    // ✅ Test 1 — Successful Registration
    @Test
    void register_ShouldReturnAuthResponse_WhenValidRequest() {
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashedPassword");
        when(userRepository.save(any())).thenReturn(mockUser);
        when(jwtUtil.generateToken(any())).thenReturn("accessToken");
        when(jwtUtil.generateRefreshToken(any())).thenReturn("refreshToken");

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("accessToken", response.getAccessToken());
        assertEquals("refreshToken", response.getRefreshToken());
        assertEquals("test@fulfillx.com", response.getEmail());
        verify(userRepository, times(1)).save(any());
    }

    // ✅ Test 2 — Registration Fails if Email Exists
    @Test
    void register_ShouldThrowException_WhenEmailAlreadyExists() {
        when(userRepository.existsByEmail(any())).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class,
                () -> authService.register(registerRequest));

        verify(userRepository, never()).save(any());
    }

    // ✅ Test 3 — Successful Login
    @Test
    void login_ShouldReturnAuthResponse_WhenValidCredentials() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(mockUser));
        when(jwtUtil.generateToken(any())).thenReturn("accessToken");
        when(jwtUtil.generateRefreshToken(any())).thenReturn("refreshToken");

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("accessToken", response.getAccessToken());
        assertEquals("SELLER", response.getRole());
    }

    // ✅ Test 4 — Login Fails if User Not Found
    @Test
    void login_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> authService.login(loginRequest));
    }

    // ✅ Test 5 — Successful Logout
    @Test
    void logout_ShouldBlacklistToken() {
        String token = "validToken";
        doNothing().when(jwtUtil).blacklistToken(token);

        authService.logout(token);

        verify(jwtUtil, times(1)).blacklistToken(token);
    }

    // ✅ Test 6 — Refresh Token Fails if Invalid
    @Test
    void refresh_ShouldThrowException_WhenTokenInvalid() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("invalidToken");

        when(jwtUtil.isTokenValid(any())).thenReturn(false);

        assertThrows(RuntimeException.class,
                () -> authService.refresh(request));
    }
}