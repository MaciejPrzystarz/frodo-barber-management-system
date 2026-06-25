package pl.frodo.barber.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String targetUrl = resolveTargetUrl(authentication);
        response.sendRedirect(request.getContextPath() + targetUrl);
    }

    private String resolveTargetUrl(Authentication authentication) {
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String role = authority.getAuthority();

            if (role.equals("ROLE_ADMIN")) {
                return "/admin/dashboard";
            }
            if (role.equals("ROLE_BARBER")) {
                return "/barber/dashboard";
            }
            if (role.equals("ROLE_CLIENT")) {
                return "/client/dashboard";
            }
        }
        return "/";
    }
}
