/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fei.uim.vsa.pr2.api;

import ErrorMessages.ErrorMessage;
import auth.Permission;
import auth.Secured;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import entities.ThesisAssignmentRequest;
import entities.ZaverecnaPraca;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import lombok.NoArgsConstructor;
import sk.stuba.fei.uim.vsa.pr1.ThesisService;
import user.User;

/**
 *
 * @author ivanc
 */

@Path("/")
public class ThesisResource {
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
    
    @POST
    @Path("/theses")
    @Secured({Permission.TEACHER_PERM})
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createTheses(ZaverecnaPraca thesis, @Context SecurityContext secourity) throws JsonProcessingException {
         try{
            if(thesis.getRegistrationNumber() == null || thesis.getTitle() == null || thesis.getType() == null){
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(createErrorMessage(400,"required args: registrationNumber,title,type","Bad Request",printStackTrace()))
                        .build();
            }
            User user = (User) secourity.getUserPrincipal();
             
            ZaverecnaPraca zp = thesisService.makeThesisAssignment(user.getAisId(),thesis.getTitle(),thesis.getType().toString(),thesis.getDescription());
            
            if(zp == null){
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).
                        entity(createErrorMessage(500,"RegistrationNumber must be unique","Internal server Error",printStackTrace()))
                        .build();
            }

            //TOTO DORABEM
            zp.setRegistrationNumber(thesis.getRegistrationNumber());
//            thesis.setId(zp.getId());
            ZaverecnaPraca zp1 = thesisService.updateThesis(zp);
//            zp1.setSupervisor(null);
            
            
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode studentNode = objectMapper.valueToTree(zp1);
//            studentNode.remove("id");
//            studentNode.remove("department");
//            studentNode.remove("supervisor");
//            studentNode.remove("author");
//            studentNode.remove("publishedOn");
//            studentNode.remove("deadline");
//            studentNode.remove("status");
             Date date = zp1.getPublishedOn();
             SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
             String formattedDate = dateFormat.format(date);
             studentNode.put("publishedOn", formattedDate);

             Date date1 = zp1.getDeadline();
             SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
             String formattedDate1 = dateFormat1.format(date1);
             studentNode.put("deadline", formattedDate1);
             //KRAJ
            
            String json = objectMapper.writeValueAsString(studentNode);
            return Response.status(Response.Status.CREATED).entity(json).build();
            
        }catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(createErrorMessage(500,e.getMessage(),"Internal server Error",e.getStackTrace().toString())).build();        
        }
    }
    
    @GET
    @Path("/theses")
    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllTheses() throws JsonProcessingException {
        try{
            List<ZaverecnaPraca> getTheses = thesisService.getTheses();
            ObjectMapper objectMapper = new ObjectMapper();
            ArrayNode thesesArray = objectMapper.createArrayNode();
            for (ZaverecnaPraca theses : getTheses) {
                Long author = null;
                Long supervisor = null;
                if(theses.getAuthor() != null){
                    author = theses.getAuthor().getAisId();
                }
                if(theses.getSupervisor() != null){
                    supervisor = theses.getSupervisor().getAisId();
                }
                
                theses.setSupervisor(null);
                theses.setAuthor(null);
                
                ObjectNode thesesNode = objectMapper.valueToTree(theses);
      
                if(supervisor == null){
                    thesesNode.remove("supervisor");
                }else{
                    thesesNode.put("supervisor", supervisor);
                }
                if(author == null){
                    thesesNode.remove("author");
                }else{
                    thesesNode.put("author", author);
                }
                Date date = theses.getPublishedOn();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String formattedDate = dateFormat.format(date);
                thesesNode.put("publishedOn", formattedDate);
                
                Date date1 = theses.getDeadline();
                SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
                String formattedDate1 = dateFormat1.format(date1);
                thesesNode.put("deadline", formattedDate1);
            
                thesesArray.add(thesesNode);
        }

        String json = objectMapper.writeValueAsString(thesesArray);
        
        return Response.ok(json).build();
        }catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(createErrorMessage(500,e.getMessage(),"Internal server Error",e.getStackTrace().toString())).build();        
        }
    }
    
    
    @GET
    @Path("/theses/{id}")
    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    public Response getThesisById(@PathParam("id") Long id) throws JsonProcessingException {
        try{
            
            ZaverecnaPraca getTheses = thesisService.getThesis(id);
            if(getTheses == null){
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(createErrorMessage(404,"Thesis With id="+id+" not exist","Not found",printStackTrace()))
                        .build();
            }
            ObjectMapper objectMapper = new ObjectMapper();
                
                Long author = null;
                Long supervisor = null;
                
                if(getTheses.getAuthor() != null){
                    author = getTheses.getAuthor().getAisId();
                }
                if(getTheses.getSupervisor() != null){
                    supervisor = getTheses.getSupervisor().getAisId();
                }
                getTheses.setSupervisor(null);
                getTheses.setAuthor(null);
                ObjectNode thesesNode = objectMapper.valueToTree(getTheses);
                if(supervisor == null){
                    thesesNode.remove("supervisor");
                }else{
                    thesesNode.put("supervisor", supervisor);
                }
            
                if(author == null){
                    thesesNode.remove("author");
                }else{
                    thesesNode.put("author", author);
                }
                Date date = getTheses.getPublishedOn();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String formattedDate = dateFormat.format(date);
                thesesNode.put("publishedOn", formattedDate);
                
                Date date1 = getTheses.getDeadline();
                SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
                String formattedDate1 = dateFormat1.format(date1);
                thesesNode.put("deadline", formattedDate1);


        String json = objectMapper.writeValueAsString(thesesNode);
        
        return Response.ok(json).build();
        }catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(createErrorMessage(500,e.getMessage(),"Internal server Error",e.getStackTrace().toString())).build();        
        }
    }
    
    @DELETE
    @Path("/theses/{id}")
    @Secured({Permission.TEACHER_PERM})
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteTheses(@PathParam("id") Long id, @Context SecurityContext secourity) throws JsonProcessingException {
        try{
            
            User user = (User) secourity.getUserPrincipal();
            ZaverecnaPraca tmp = thesisService.getThesis(id);
            if(user.getAisId().equals(tmp.getSupervisor().getAisId())){
                ZaverecnaPraca getTheses = thesisService.deleteThesis(id);
                
                if(getTheses != null){
                    ObjectMapper objectMapper = new ObjectMapper();
                    Long author = null;
                    Long supervisor = null;
                    if(getTheses.getAuthor() != null){
                        author = getTheses.getAuthor().getAisId();
                    }
                    if(getTheses.getSupervisor() != null){
                        supervisor = getTheses.getSupervisor().getAisId();
                    }
                    
                    getTheses.setSupervisor(null);
                    getTheses.setAuthor(null);
                    ObjectNode thesesNode = objectMapper.valueToTree(getTheses);
                    if(supervisor == null){
                        thesesNode.remove("supervisor");
                    }else{
                        thesesNode.put("supervisor", supervisor);
                    }
                    if(author == null){
                        thesesNode.remove("author");
                    }else{
                        thesesNode.put("author", author);
                    }
                    Date date = getTheses.getPublishedOn();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String formattedDate = dateFormat.format(date);
                thesesNode.put("publishedOn", formattedDate);
                
                Date date1 = getTheses.getDeadline();
                SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
                String formattedDate1 = dateFormat1.format(date1);
                thesesNode.put("deadline", formattedDate1);

                    String json = objectMapper.writeValueAsString(thesesNode);
                    return Response.ok(json).build();
                } else {
                    return Response.status(Response.Status.NOT_FOUND)
                        .entity(createErrorMessage(404,"Thesis With id="+id+" not exist","Not Found",printStackTrace()))
                        .build();
                }
            }
            else{
                return Response.status(Response.Status.FORBIDDEN)
                    .entity(createErrorMessage(403,"Only teacher that assign thesis can delete","Forbidden",printStackTrace()))
                    .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(createErrorMessage(500,e.getMessage(),"Internal server Error",e.getStackTrace().toString())).build();        
        }
    }
    
    
    
    @POST
    @Path("/theses/{id}/assign")
    @Secured
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response assignThesisById(ThesisAssignmentRequest request,@PathParam("id") Long id,@Context SecurityContext secourity) throws JsonProcessingException {
        try{
            Long studentId = request.getStudentId();
            User user = (User) secourity.getUserPrincipal();
            
            ZaverecnaPraca getTheses = thesisService.getThesis(id);
            if(getTheses == null){
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(createErrorMessage(404,"Thesis With id="+id+" not exist","Not Found",printStackTrace()))
                        .build();
            }
            
            if(user.getPermissions().contains(Permission.STUDENT_PERM)){
                getTheses = thesisService.assignThesis(id, user.getAisId());
            }else if(user.getAisId().equals(getTheses.getSupervisor().getAisId())){
                getTheses =thesisService.assignThesis(id,studentId);
            }else{
                return Response.status(Response.Status.FORBIDDEN)
                    .entity(createErrorMessage(403,"You dont have permission","Forbidden",printStackTrace()))
                    .build();
            }
            ObjectMapper objectMapper = new ObjectMapper();
                
                Long author = null;
                Long supervisor = null;
                
                if(getTheses.getAuthor() != null){
                    author = getTheses.getAuthor().getAisId();
                }
                if(getTheses.getSupervisor() != null){
                    supervisor = getTheses.getSupervisor().getAisId();
                }
                getTheses.setSupervisor(null);
                getTheses.setAuthor(null);
                ObjectNode thesesNode = objectMapper.valueToTree(getTheses);
                if(supervisor == null){
                    thesesNode.remove("supervisor");
                }else{
                    thesesNode.put("supervisor", supervisor);
                }
                if(author == null){
                    thesesNode.remove("author");
                }else{
                    thesesNode.put("author", author);
                }
                Date date = getTheses.getPublishedOn();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String formattedDate = dateFormat.format(date);
                thesesNode.put("publishedOn", formattedDate);
                
                Date date1 = getTheses.getDeadline();
                SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
                String formattedDate1 = dateFormat1.format(date1);
                thesesNode.put("deadline", formattedDate1);
            
//                thesesArray.add(thesesNode);
//        }

        String json = objectMapper.writeValueAsString(thesesNode);
        
            return Response.ok(json).build();
        }catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(createErrorMessage(500,e.getMessage(),"Internal server Error",e.getStackTrace().toString())).build();        
        }
    }
    
    
    @POST
    @Path("/theses/{id}/submit")
    @Secured
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response submitThesisById(ThesisAssignmentRequest request,@PathParam("id") Long id,@Context SecurityContext secourity) throws JsonProcessingException {
        try{
            Long studentId = request.getStudentId();
            User user = (User) secourity.getUserPrincipal();

            ZaverecnaPraca getTheses = thesisService.getThesis(id);
            if(getTheses == null){
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(createErrorMessage(404,"Thesis With id="+id+" not exist","Not Found",printStackTrace()))
                        .build();
            }
            
            if(user.getPermissions().contains(Permission.STUDENT_PERM) && getTheses.getAuthor().getAisId().equals(user.getAisId())){
                getTheses = thesisService.submitThesis(getTheses.getId());
            }else if(studentId.equals(getTheses.getAuthor().getAisId())){
                getTheses =thesisService.submitThesis(getTheses.getId());
            }else{
                return Response.status(Response.Status.FORBIDDEN)
                    .entity(createErrorMessage(403,"You dont have permission","Forbidden",printStackTrace()))
                    .build();
            }
            ObjectMapper objectMapper = new ObjectMapper();

                
                Long author = null;
                Long supervisor = null;
//                
                if(getTheses.getAuthor() != null){
                    author = getTheses.getAuthor().getAisId();
                }
                if(getTheses.getSupervisor() != null){
                    supervisor = getTheses.getSupervisor().getAisId();
                }
                getTheses.setSupervisor(null);
                getTheses.setAuthor(null);
                ObjectNode thesesNode = objectMapper.valueToTree(getTheses);
                if(supervisor == null){
                    thesesNode.remove("supervisor");
                }else{
                    thesesNode.put("supervisor", supervisor);
                }

                if(author == null){
                    thesesNode.remove("author");
                }else{
                    thesesNode.put("author", author);
                }
                Date date = getTheses.getPublishedOn();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String formattedDate = dateFormat.format(date);
                thesesNode.put("publishedOn", formattedDate);
                
                Date date1 = getTheses.getDeadline();
                SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
                String formattedDate1 = dateFormat1.format(date1);
                thesesNode.put("deadline", formattedDate1);


        String json = objectMapper.writeValueAsString(thesesNode);
        
            return Response.ok(json).build();
        }catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(createErrorMessage(500,e.getMessage(),"Internal server Error",e.getStackTrace().toString())).build();        
        }
    }
    
    
    
    @POST
    @Path("/search/theses")
    @Secured
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchThesis(ThesisAssignmentRequest request,@PathParam("id") Long id) throws JsonProcessingException {
        try{
            
            
            ArrayList<ZaverecnaPraca> getTheses = new ArrayList();
            if(request.getStudentId() != null){
                getTheses.add(thesisService.getThesisByStudent(request.getStudentId()));
            }else if(request.getTeacherId() != null){
 
                List<ZaverecnaPraca> z = thesisService.getThesesByTeacher(request.getTeacherId());
                for(ZaverecnaPraca tmp: z ){
                     getTheses.add(tmp);
                 }
            }else{
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(createErrorMessage(404,"Thesis not found","Not Found",printStackTrace()))
                        .build();
            }
            
            ObjectMapper objectMapper = new ObjectMapper();
            ArrayNode thesesArray = objectMapper.createArrayNode();
            for (ZaverecnaPraca theses : getTheses) {
                
                Long author = null;
                Long supervisor = null;
//                
                if(theses.getAuthor() != null){
                    author = theses.getAuthor().getAisId();
                }
                if(theses.getSupervisor() != null){
                    supervisor = theses.getSupervisor().getAisId();
                }
                theses.setSupervisor(null);
                theses.setAuthor(null);
                ObjectNode thesesNode = objectMapper.valueToTree(theses);
                if(supervisor == null){
                    thesesNode.remove("supervisor");
                }else{
                    thesesNode.put("supervisor", supervisor);
                }
                if(author == null){
                    thesesNode.remove("author");
                }else{
                    thesesNode.put("author", author);
                }
                Date date = theses.getPublishedOn();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String formattedDate = dateFormat.format(date);
                thesesNode.put("publishedOn", formattedDate);
                
                Date date1 = theses.getDeadline();
                SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
                String formattedDate1 = dateFormat1.format(date1);
                thesesNode.put("deadline", formattedDate1);
            
                thesesArray.add(thesesNode);
        }

        String json = objectMapper.writeValueAsString(thesesArray);
        
            return Response.ok(json).build();
        }catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(createErrorMessage(500,e.getMessage(),"Internal server Error",e.getStackTrace().toString())).build();        
        }
    }
    
}
