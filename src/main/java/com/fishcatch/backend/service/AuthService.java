package com.fishcatch.backend.service;

import com.fishcatch.backend.dto.AuthResponse;
import com.fishcatch.backend.dto.GoogleAuthRequest;
import com.fishcatch.backend.dto.LoginRequest;
import com.fishcatch.backend.dto.RegisterRequest;
import com.fishcatch.backend.model.CrewMember;
import com.fishcatch.backend.model.KasbonTransaction;
import com.fishcatch.backend.model.Trip;
import com.fishcatch.backend.model.User;
import com.fishcatch.backend.model.Vessel;
import com.fishcatch.backend.repository.CrewMemberRepository;
import com.fishcatch.backend.repository.KasbonTransactionRepository;
import com.fishcatch.backend.repository.TripRepository;
import com.fishcatch.backend.repository.UserRepository;
import com.fishcatch.backend.repository.VesselRepository;
import com.fishcatch.backend.security.CurrentUserContext;
import com.fishcatch.backend.security.JwtService;
import com.fishcatch.backend.security.PasswordHasher;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final VesselRepository vesselRepository;
    private final CrewMemberRepository crewMemberRepository;
    private final KasbonTransactionRepository kasbonRepository;
    private final TripRepository tripRepository;
    private final PasswordHasher passwordHasher;
    private final JwtService jwtService;
    private final CurrentUserContext currentUserContext;

    private final RestClient restClient = RestClient.create();

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new IllegalStateException("An account with this email already exists");
        }

        User user = User.builder()
                .name(request.name().trim())
                .email(normalizedEmail)
                .passwordHash(passwordHasher.hash(request.password()))
                .build();
        User saved = userRepository.save(user);
        claimOrphanDataIfFirstUser(saved);

        String token = jwtService.generateToken(saved);
        return new AuthResponse(token, saved);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (user.getPasswordHash() == null
                || !passwordHasher.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        claimOrphanDataIfFirstUser(user);
        String token = jwtService.generateToken(user);
        return new AuthResponse(token, user);
    }

    @Transactional
    public AuthResponse authenticateWithGoogle(GoogleAuthRequest request) {
        String email;
        String name;
        String googleId;
        String avatarUrl;

        if (request.credential() != null && !request.credential().isBlank()) {
            Map<String, Object> tokenInfo = verifyGoogleIdToken(request.credential().trim());
            email = String.valueOf(tokenInfo.get("email")).toLowerCase();
            name = tokenInfo.get("name") != null
                    ? String.valueOf(tokenInfo.get("name"))
                    : email.split("@")[0];
            googleId = String.valueOf(tokenInfo.get("sub"));
            avatarUrl = tokenInfo.get("picture") != null
                    ? String.valueOf(tokenInfo.get("picture"))
                    : null;
        } else if (request.accessToken() != null && !request.accessToken().isBlank()) {
            Map<String, Object> userInfo = fetchGoogleUserInfo(request.accessToken().trim());
            email = String.valueOf(userInfo.get("email")).toLowerCase();
            name = userInfo.get("name") != null
                    ? String.valueOf(userInfo.get("name"))
                    : email.split("@")[0];
            googleId = String.valueOf(userInfo.get("sub"));
            avatarUrl = userInfo.get("picture") != null
                    ? String.valueOf(userInfo.get("picture"))
                    : null;
        } else {
            throw new IllegalArgumentException("Valid Google OAuth token is required");
        }

        User user = userRepository.findByGoogleId(googleId)
                .or(() -> userRepository.findByEmailIgnoreCase(email))
                .map(existing -> {
                    existing.setGoogleId(googleId);
                    if (avatarUrl != null && !avatarUrl.isBlank()) {
                        existing.setAvatarUrl(avatarUrl);
                    }
                    if (existing.getName() == null || existing.getName().isBlank()) {
                        existing.setName(name);
                    }
                    return userRepository.save(existing);
                })
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .name(name)
                                .email(email)
                                .googleId(googleId)
                                .avatarUrl(avatarUrl)
                                .build()
                ));

        claimOrphanDataIfFirstUser(user);
        String token = jwtService.generateToken(user);
        return new AuthResponse(token, user);
    }

    @Transactional(readOnly = true)
    public User getCurrentUser() {
        return currentUserContext.getCurrentUser();
    }

    private void claimOrphanDataIfFirstUser(User user) {
        if (userRepository.count() == 1) {
            List<Vessel> orphanVessels = vesselRepository.findByUserIsNull();
            for (Vessel v : orphanVessels) {
                v.setUser(user);
            }
            vesselRepository.saveAll(orphanVessels);

            List<CrewMember> orphanCrew = crewMemberRepository.findByUserIsNull();
            for (CrewMember c : orphanCrew) {
                c.setUser(user);
            }
            crewMemberRepository.saveAll(orphanCrew);

            List<KasbonTransaction> orphanKasbon = kasbonRepository.findByUserIsNull();
            for (KasbonTransaction k : orphanKasbon) {
                k.setUser(user);
            }
            kasbonRepository.saveAll(orphanKasbon);

            List<Trip> orphanTrips = tripRepository.findByUserIsNull();
            for (Trip t : orphanTrips) {
                t.setUser(user);
            }
            tripRepository.saveAll(orphanTrips);
        }
    }

    private Map<String, Object> verifyGoogleIdToken(String idToken) {
        try {
            String url = "https://oauth2.googleapis.com/tokeninfo?id_token="
                    + URLEncoder.encode(idToken, StandardCharsets.UTF_8);
            Map<String, Object> body = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            if (body == null || !body.containsKey("email")) {
                throw new IllegalArgumentException("Invalid Google ID token");
            }
            return body;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to verify Google ID token", e);
        }
    }

    private Map<String, Object> fetchGoogleUserInfo(String accessToken) {
        try {
            Map<String, Object> body = restClient.get()
                    .uri("https://www.googleapis.com/oauth2/v3/userinfo")
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            if (body == null || !body.containsKey("email")) {
                throw new IllegalArgumentException("Invalid Google access token");
            }
            return body;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to fetch Google user profile", e);
        }
    }
}
