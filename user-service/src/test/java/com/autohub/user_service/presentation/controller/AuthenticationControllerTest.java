package com.autohub.user_service.presentation.controller;

import com.autohub.user_service.infrastructure.security.JwtUtil;
import com.autohub.user_service.presentation.dto.auth.AuthenticationRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(AuthenticationController.class)
public class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    public void createAuthenticationToken_ShouldReturnJwtOnValidCredentials() throws Exception {
        String email = "user@example.com";
        String password = "password123";
        String jwt = "test-jwt-token";

        AuthenticationRequest request = new AuthenticationRequest(email, password);
        UserDetails userDetails = Mockito.mock(UserDetails.class);

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(new UsernamePasswordAuthenticationToken(email, password,
                Collections.singletonList(new SimpleGrantedAuthority("USER"))));
        SecurityContextHolder.setContext(securityContext);


        Mockito.when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);

        Mockito.when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
        Mockito.when(jwtUtil.generateToken(eq(userDetails))).thenReturn(jwt);

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwt").value(jwt));
    }

    @Test
    public void createAuthenticationToken_ShouldReturnErrorOnInvalidCredentials() throws Exception {
        String email = "user@example.com";
        String password = "invalidpassword";

        AuthenticationRequest request = new AuthenticationRequest(email, password);

        Mockito.doThrow(new BadCredentialsException("Invalid credentials"))
                .when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@example.com\",\"password\":\"invalidpassword\"}"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    public void createAuthenticationToken_ShouldReturnError_OnInvalidRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"invalidemail\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isNotEmpty());
    }
}
