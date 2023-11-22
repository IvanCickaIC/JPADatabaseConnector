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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import entities.Student;
import entities.ZaverecnaPraca;
import java.io.PrintWriter;
import java.io.StringWriter;
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
public class StudentResource {
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
    @Path("/students")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createStudent(Student student) throws JsonProcessingException {
        try{
            //ak povinie parametre nisa splnenie
            if(student.getAisId() == null || student.getName() == null || student.getEmail() == null || student.getPassword() == null){
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(createErrorMessage(400,"required args: name,password,email,aisId","Bad Request",printStackTrace()))
                        .build();
            }
            Student createdStudent = thesisService.createStudent(student.getAisId(), student.getName(), student.getEmail());
            if(createdStudent == null){
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).
                        entity(createErrorMessage(500,"Ais id and email must be unique","Internal server Error",printStackTrace()))
                        .build();
            }
            
            //Add user 
            UserService service = UserService.getInstance();
            User u = new User(student.getAisId(),student.getEmail(),hashPassword(student.getPassword()));
            u.addPermission(Permission.STUDENT_PERM);
            service.save(u);
            
            Student createdStudent1 = thesisService.updateStudent(student);
            
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode studentNode = objectMapper.valueToTree(createdStudent1);
            studentNode.remove("thesis");
            studentNode.remove("password");
            studentNode.put("Id", createdStudent1.getAisId());
            
            ObjectNode jsonNode = objectMapper.createObjectNode();
            jsonNode.put("Id", createdStudent1.getAisId());
            jsonNode.setAll(studentNode);
            String json = objectMapper.writeValueAsString(jsonNode);
            
            
            return Response.status(Response.Status.CREATED).entity(json).build();
        }catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(createErrorMessage(500,e.getMessage(),"Internal server Error",e.getStackTrace().toString())).build();        
        }
        
    }
    
    
    @GET
    @Path("/students")
    @Secured()
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllStudents() throws JsonProcessingException {
        try {
            List<Student> getStudents = thesisService.getStudents();
            

            ObjectMapper objectMapper = new ObjectMapper();
            ArrayNode studentArray = objectMapper.createArrayNode();
            
            for (Student student : getStudents) {
                Long t = null;
                if(student.getThesis() != null){
                     t = student.getThesis().getId();
                }
                
                student.setThesis(null);
                ObjectNode studentNode = objectMapper.valueToTree(student);
     
                if(t == null){
                    studentNode.remove("thesis");
                }else{
                    studentNode.put("thesis", t );
                }
                studentNode.remove("password");
                studentNode.put("Id", student.getAisId());
            
                ObjectNode jsonNode = objectMapper.createObjectNode();
                jsonNode.put("Id", student.getAisId());
                jsonNode.setAll(studentNode);
                
                studentArray.add(jsonNode);
            }

            String json = objectMapper.writeValueAsString(studentArray);
            
            return Response.ok(json).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(createErrorMessage(500,e.getMessage(),"Internal server Error",e.getStackTrace().toString())).build();        

        }
    }
    
    @GET
    @Path("/students/{id}")
    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStudent(@PathParam("id") Long id) throws JsonProcessingException {

        try {
            Student getStudent = thesisService.getStudent(id);

            
            if (getStudent == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(createErrorMessage(404,"Student With id="+id+" not exist","Not found",printStackTrace()))
                        .build();
            }
            //dodav aby sa neciklilo
            ObjectMapper objectMapper = new ObjectMapper();
            Long t = null;
            if(getStudent.getThesis() != null){
                t = getStudent.getThesis().getId();
            }
                
            getStudent.setThesis(null);
            //
            
            ObjectNode studentNode = objectMapper.valueToTree(getStudent);
            if(t == null){
                studentNode.remove("thesis");
            }else{
                studentNode.remove("thesis");
                studentNode.put("thesis", t);
            }
            
            studentNode.remove("password");
            studentNode.put("Id", getStudent.getAisId());
            
            ObjectNode jsonNode = objectMapper.createObjectNode();
            jsonNode.put("Id", getStudent.getAisId());
            jsonNode.setAll(studentNode);
            String json = objectMapper.writeValueAsString(jsonNode);
            
            
            return Response.ok(json).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(createErrorMessage(500,e.getMessage(),"Internal server Error",e.getStackTrace().toString())).build();        
        }
    }
    
    @DELETE
    @Path("/students/{id}")
    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteStudent(@PathParam("id") Long id,@Context SecurityContext secourity) throws JsonProcessingException {
        try{
            User user = (User) secourity.getUserPrincipal();
            if(user.getAisId().equals(id) || user.getPermissions().contains(Permission.TEACHER_PERM)){
                Student deleted = thesisService.deleteStudent(id);
                if(deleted != null){
                    ObjectMapper objectMapper = new ObjectMapper();
                    Long t = null;
                    if(deleted.getThesis() != null){
                        t = deleted.getThesis().getId();
                    }
                    deleted.setThesis(null);
                    ObjectNode studentNode = objectMapper.valueToTree(deleted);
                    if(t == null){
                        studentNode.remove("thesis");
                    }else{
                        studentNode.remove("thesis");
                        studentNode.put("thesis", t);
                    }

                    studentNode.remove("password");
                    studentNode.put("Id", deleted.getAisId());

                    ObjectNode jsonNode = objectMapper.createObjectNode();
                    jsonNode.put("Id", deleted.getAisId());
                    jsonNode.setAll(studentNode);
                    String json = objectMapper.writeValueAsString(jsonNode);
                    UserService.getInstance().deleteUserById(deleted.getAisId());
                    
                    return Response.ok(json).build();
                } else {
                    return Response.status(Response.Status.NOT_FOUND)
                        .entity(createErrorMessage(404,"Student With "+id+" not exist","Not found",printStackTrace()))
                        .build();
                }
            }else{
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(createErrorMessage(403,"Only teacher and student with same id can delete","Forbidden",printStackTrace()))
                    .build();
        }    
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(createErrorMessage(500,e.getMessage(),"Internal server Error",e.getStackTrace().toString())).build();        
        }
    }    
}
