/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
*/

package sk.stuba.fei.uim.vsa.pr2.api;

import ErrorMessages.ErrorMessage;
import auth.Permission;
import auth.Secured;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import entities.Pedagog;
import entities.Student;
import entities.ZaverecnaPraca;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import sk.stuba.fei.uim.vsa.pr1.ThesisService;
import sk.stuba.fei.uim.vsa.pr2.BCryptService;
import user.User;
import user.UserService;

/**
 *
 * @author ivanc
 */

@Path("/")
public class TeacherResource {
    ThesisService thesisService = new ThesisService();
    
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

    private String hashPassword(String password) {
        byte[] bytes = null;
        try {
            bytes = Base64.getDecoder().decode(password);
        } catch (Exception e) {
            bytes = password.getBytes();
        }
        String pass = new String(bytes);
        return BCryptService.hash(pass);
    }
    
    @POST
    @Path("/teachers")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createTeacher(Pedagog pedagog) throws JsonProcessingException {
        try{
            if(pedagog.getAisId() == null || pedagog.getEmail() == null || pedagog.getName() == null || pedagog.getPassword() == null){
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(createErrorMessage(400,"required args: name,password,email,aisId","Bad Request",printStackTrace()))
                        .build();
            }
            
            Pedagog createPedagog = thesisService.createTeacher(pedagog.getAisId(), pedagog.getName(), pedagog.getEmail(),pedagog.getDepartment());
            if(createPedagog == null){
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).
                        entity(createErrorMessage(500,"Ais id and email must be unique","Internal server Error",printStackTrace()))
                        .build();
            }
            
            UserService service = UserService.getInstance();
            User u = new User(pedagog.getAisId(),pedagog.getEmail(),hashPassword(pedagog.getPassword()));
            u.addPermission(Permission.TEACHER_PERM);
            service.save(u);
            
            Pedagog createdTeacher1 = thesisService.updateTeacher(pedagog);
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode studentNode = objectMapper.valueToTree(createdTeacher1);
            studentNode.remove("theses");
            studentNode.remove("password");
            studentNode.put("Id", createdTeacher1.getAisId());
            
            ObjectNode jsonNode = objectMapper.createObjectNode();
            jsonNode.put("Id", createdTeacher1.getAisId());
            jsonNode.setAll(studentNode);
            String json = objectMapper.writeValueAsString(jsonNode);
            
            return Response.status(Response.Status.CREATED).entity(json).build();
            
        }catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(createErrorMessage(500,e.getMessage(),"Internal server Error",e.getStackTrace().toString())).build();        
        }
    }
    
    
    
    @GET
    @Path("/teachers")
    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllTeachers() throws JsonProcessingException {
        try{
            List<Pedagog> getTeacher = thesisService.getTeachers();
            
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode teachersArray = objectMapper.createArrayNode();
        for (Pedagog teacher : getTeacher) {
            //lem aby nezaciklilo
            ArrayNode t = objectMapper.createArrayNode();
            if(teacher.getTheses() != null){
                for(ZaverecnaPraca zp :teacher.getTheses())
                t.add(zp.getId());
            }
            teacher.setTheses(null);
            //kraj
            ObjectNode teacherNode = objectMapper.valueToTree(teacher);
           
            if(t == null || t.isEmpty()){
                teacherNode.remove("theses");
            }else{
                teacherNode.set("theses", t);
            }
            teacherNode.remove("password");
            teacherNode.put("Id", teacher.getAisId());
            
            ObjectNode jsonNode = objectMapper.createObjectNode();
            jsonNode.put("Id", teacher.getAisId());
            jsonNode.setAll(teacherNode);

//            String json = objectMapper.writeValueAsString(teacherNode);
            teachersArray.add(jsonNode);
        }

        String json = objectMapper.writeValueAsString(teachersArray);
        
        return Response.ok(json).build();
        }catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(createErrorMessage(500,e.getMessage(),"Internal server Error",e.getStackTrace().toString())).build();        
        }
    }
    
    @GET
    @Path("/teachers/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    public Response getPedagog(@PathParam("id") Long id) throws JsonProcessingException {
        try{
            Pedagog getTeacher = thesisService.getTeacher(id);
            if(getTeacher == null){
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(createErrorMessage(404,"Teacher With id="+id+" not exist","Not found",printStackTrace()))
                        .build();
            }
            
            ObjectMapper objectMapper = new ObjectMapper();
            //lem aby sa nezaciklilo
            ArrayNode t = objectMapper.createArrayNode();
            if(getTeacher.getTheses() != null){
                for(ZaverecnaPraca zp :getTeacher.getTheses())
                t.add(zp.getId());
            }
            getTeacher.setTheses(null);
            //kraj
            ObjectNode teacherNode = objectMapper.valueToTree(getTeacher);
           
            if(t == null || t.isEmpty()){
                teacherNode.remove("theses");
            }else{
                    teacherNode.set("theses", t);
            }

            
            teacherNode.remove("password");
            teacherNode.put("Id", getTeacher.getAisId());

            ObjectNode jsonNode = objectMapper.createObjectNode();
            jsonNode.put("Id", getTeacher.getAisId());
            jsonNode.setAll(teacherNode);
                
            
            String json = objectMapper.writeValueAsString(jsonNode);
            return Response.ok(json).build();
        }catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(createErrorMessage(500,e.getMessage(),"Internal server Error",e.getStackTrace().toString())).build();        
        }
    }
    
    @DELETE
    @Path("/teachers/{id}")
    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteTeacher(@PathParam("id") Long id, @Context SecurityContext secourity) throws JsonProcessingException {
        try{
            
            User user = (User) secourity.getUserPrincipal();
            if(user.getAisId().equals(id)){
                Pedagog getTeacher = thesisService.deleteTeacher(id);
                
                if(getTeacher != null){
                    ObjectMapper objectMapper = new ObjectMapper();
                    //lem aby sa nezaciklilo
                    ArrayNode t = objectMapper.createArrayNode();
                    if(getTeacher.getTheses() != null){
                        for(ZaverecnaPraca zp :getTeacher.getTheses())
                        t.add(zp.getId());
                    }
                    getTeacher.setTheses(null);
                    //kraj
                    ObjectNode teacherNode = objectMapper.valueToTree(getTeacher);

                    if(t == null || t.isEmpty()){
                        teacherNode.remove("theses");
                    }else{
                            teacherNode.set("theses", t);
                    }


                    teacherNode.remove("password");
                    teacherNode.put("Id", getTeacher.getAisId());

                    ObjectNode jsonNode = objectMapper.createObjectNode();
                    jsonNode.put("Id", getTeacher.getAisId());
                    jsonNode.setAll(teacherNode);


                    String json = objectMapper.writeValueAsString(jsonNode);
                    
                    UserService.getInstance().deleteUserById(getTeacher.getAisId());
                    return Response.ok(json).build();
                } else {
                     return Response.status(Response.Status.NOT_FOUND)
                        .entity(createErrorMessage(404,"Teacher With "+id+" not exist","Not Found",printStackTrace()))
                        .build();
                }
            }
            else{
                return Response.status(Response.Status.FORBIDDEN)
                    .entity(createErrorMessage(403,"Only teacher can delete","Forbidden",printStackTrace()))
                    .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(createErrorMessage(500,e.getMessage(),"Internal Server Error",e.getStackTrace().toString())).build();        
        }
    }
}
