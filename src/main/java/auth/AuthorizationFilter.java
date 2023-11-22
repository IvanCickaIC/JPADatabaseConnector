/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package auth;

import ErrorMessages.ErrorMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import user.User;

/**
 *
 * @author ivanc
 */


@Secured
@Provider
@Priority(Priorities.AUTHORIZATION)
public class AuthorizationFilter implements ContainerRequestFilter{
    
    @Context
    private ResourceInfo resourceInfo;
    
    public String createErrorMessage(Integer code,String message, String type, String trace) throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    ErrorMessage errorMessage = new ErrorMessage();
    errorMessage.setCode(code);
    errorMessage.setMessage(message);
    ErrorMessage.Error error = new ErrorMessage.Error();
    error.setType(type);
    error.setTrace(trace);
    errorMessage.setError(error);
    ObjectNode node = objectMapper.valueToTree(errorMessage);
    node.remove("id");
    String json = objectMapper.writeValueAsString(node);
    return json;
}
   public String printStackTrace(){
        Throwable t = new Throwable();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        String stackTrace = sw.toString();
        return stackTrace;
   }
    
    @Override
    public void filter(ContainerRequestContext request)throws IOException{
        User user = (User) request.getSecurityContext().getUserPrincipal();
        Method resourceMethod = resourceInfo.getResourceMethod();
        Set<Permission> permissions = extractPermissionFromMethod(resourceMethod);
        
        //dodav som kontrolu za empty aj null lebo mi nerobi bez toho 
//        System.out.println("---->  "+permissions);
        if(permissions != null && !permissions.isEmpty()){
//            System.out.println("---->  "+permissions);
            if(user.getPermissions().stream().noneMatch(permissions::contains)){
            request.abortWith(Response.status(Response.Status.FORBIDDEN)
                    .entity(createErrorMessage(403,"You dont have acces to this","Forbidden",printStackTrace()))
                    .build());
            return;
        }
        }
        
    }
    
    
    private Set<Permission> extractPermissionFromMethod(Method method){
        if(method == null){
            return new HashSet<>();
        }
        Secured secured = method.getAnnotation(Secured.class);
        if(secured == null){
            return new HashSet<>();
        }
        return new HashSet<>(Arrays.asList(secured.value()));
    }
}
