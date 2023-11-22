/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package auth;

import java.security.Principal;
import javax.ws.rs.core.SecurityContext;
import user.User;

/**
 *
 * @author ivanc
 */
public class BasicSecurityContext implements SecurityContext {
    
    private final User user;
    private boolean secure;

    public void setSecure(boolean secure) {
        this.secure = secure;
    }
    
    public BasicSecurityContext(User user){
        this.user = user;
    }
    
    @Override
    public Principal getUserPrincipal() {
        // implementation for getUserPrincipal method
        return user;
    }

    @Override
    public boolean isUserInRole(String role) {
        // implementation for isUserInRole method
        return true;
    }

    @Override
    public boolean isSecure() {
        // implementation for isSecure method
        return secure;
    }

    @Override
    public String getAuthenticationScheme() {
        // implementation for getAuthenticationScheme method
        return "Basic";
    }

}
