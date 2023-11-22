/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package auth;

import java.io.IOException;
import java.security.Principal;
import java.util.Base64;
import java.util.Optional;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import sk.stuba.fei.uim.vsa.pr2.BCryptService;
import static sk.stuba.fei.uim.vsa.pr2.JAXRSApplicationConfiguration.log;
import user.User;
import user.UserService;

/**
 *
 * @author ivanc
 */

@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {
    
    @Override
    public void filter(ContainerRequestContext request)throws IOException{
        String authHeader = request.getHeaderString(HttpHeaders.AUTHORIZATION);
        if(authHeader == null || authHeader.isEmpty() || !authHeader.contains("Basic")){
            reject(request);
            return;
        }
        String[] credentials = extractFromAuthHeader(authHeader);
        log.info("Recieved credentials: " + credentials[0] + ", "+credentials[1]);
        
        UserService userService = UserService.getInstance();  
        Optional <User> userOptional =userService.getUserByUsername(credentials[0]);
        
        //ak nebude robit zameniv na Object equals 32:02
        if(!userOptional.isPresent() || !BCryptService.verify(credentials[1],userOptional.get().getPassword())){
            reject(request);
            return;
        }
        final SecurityContext securityContext = request.getSecurityContext();
        BasicSecurityContext context = new BasicSecurityContext(userOptional.get());
        context.setSecure(securityContext.isSecure());
        request.setSecurityContext(context);
            
    }
    
    private void reject(ContainerRequestContext request){
        request.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                    .header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"VSA\"")
                .build());
            return;
    }
    
    private String[] extractFromAuthHeader(String authHeader){
        return new String(Base64.getDecoder().decode(authHeader.replace("Basic","").trim())).split(":");
    
    }
    
    
}
