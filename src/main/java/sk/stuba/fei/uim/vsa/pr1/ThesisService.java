/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fei.uim.vsa.pr1;

import Enums.Status;
import Enums.Typ;
import entities.Pedagog;
import entities.Student;
import entities.ZaverecnaPraca;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import static org.eclipse.persistence.expressions.ExpressionOperator.currentDate;
import sk.stuba.fei.uim.vsa.pr2.BCryptService;

/**
 *
 * @author ivanc
 *
 * @param <S> Trieda reprezentujúca študenta (student)
 * @param <T> Trieda reprezentujúca pedagóga (teacher)
 * @param <A> Trieda reprezentujúca záverečnú prácu (assignment)
 */

public class ThesisService extends AbstractThesisService<Student,Pedagog,ZaverecnaPraca> { 
    
    public ThesisService() {
        super();
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
 
    @Override
    public Student createStudent(Long aisId, String name, String email) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("vsa-project-2");
        EntityManager em = emf.createEntityManager();
        
        try{
            em.createQuery("select s from Student s where s.email = :email")
                    .setParameter("email", email).getSingleResult();
            em.close();
            return null;
        } catch (Exception e){
            em.getTransaction().begin();
            Student s = new Student();
            s.setAisId(aisId);
            s.setName(name);
            s.setEmail(email);
            em.persist(s);
            em.getTransaction().commit();
            em.close();
            return s;
        }
    }

    @Override
    public Student getStudent(Long id) {
        if(id == null ){
            throw new IllegalArgumentException("Id is null");
        }
        
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("vsa-project-2");
        EntityManager em = emf.createEntityManager();
        
        try {
            Student s = em.createQuery("select s from Student s where s.aisId = :id",Student.class)
                    .setParameter("id", id).getSingleResult();
            em.close();
            return s;
        } catch (Exception e) {
            em.close();
            return null;
        }        
    }


    @Override
    public Student updateStudent(Student student) {
        if((student == null) ||(student.getAisId() == null)){
            throw new IllegalArgumentException("null element");
        }
        
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("vsa-project-2");
        EntityManager em = emf.createEntityManager();
        try {    
            Student s = em.createQuery("select s from Student s where s.aisId = :id",Student.class)
                    .setParameter("id", student.getAisId()).getSingleResult();
            
            s.setId(s.getAisId()); //TOTO SOM DODAV
            s.setEmail(student.getEmail());
            s.setName(student.getName());
            s.setProgramme(student.getProgramme());
            s.setTerm(student.getTerm());
//            s.setPassword(BCryptService.hash(student.getPassword()));
            s.setPassword(hashPassword(student.getPassword())); //TOTO SOM DODAV
            s.setYear(student.getYear());
            s.setThesis(student.getThesis());
            
            em.getTransaction().begin();
            em.getTransaction().commit();
            em.close();
            return s;
        } catch (Exception e) {
            em.close();
            return null;
        }
        
    }

    @Override
    public List<Student> getStudents() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("vsa-project-2");
        EntityManager em = emf.createEntityManager();
        List<Student> s = new ArrayList();
        try {
            s = em.createQuery("select s from Student s",Student.class).getResultList();
            em.close();
            return s;
        } catch (Exception e) {
            em.close();
            return s;
        }
    }

    @Override
    public Student deleteStudent(Long id) {
        if(id == null){
            throw new IllegalArgumentException("Id is null");
        }
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("vsa-project-2");
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        try {
            Student s = em.createQuery("select s from Student s where s.aisId = :id",Student.class)
                    .setParameter("id", id).getSingleResult();
            em.remove(s);
            em.getTransaction().commit();
            em.close();
            return s;
        } catch (Exception e) {
            em.close();
            return null;
        }
    }

    @Override
    public Pedagog createTeacher(Long aisId, String name, String email, String department) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("vsa-project-2");
        EntityManager em = emf.createEntityManager();

        try {
            em.createQuery("select p from Pedagog p where p.email = :email")
                    .setParameter("email", email).getSingleResult();
            em.close();
            return null;
        } catch (Exception e) {
            
            em.getTransaction().begin();
            Pedagog p = new Pedagog();
            p.setEmail(email);
            p.setInstitute(department);
            p.setAisId(aisId);
            p.setName(name);
            em.persist(p);
            em.getTransaction().commit();
            em.close();
            return p;
        }
    }

    @Override
    public Pedagog getTeacher(Long id) {
        if(id == null){
            throw new IllegalArgumentException("Id is null");
        }
        
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("vsa-project-2");
        EntityManager em = emf.createEntityManager();
        try {
            Pedagog p = em.createQuery("select p from Pedagog p where p.aisId = :id",Pedagog.class)
                    .setParameter("id", id).getSingleResult();
            em.close();
            return p;
        } catch (Exception e) {
            em.close();
            return null;
        }    }

    
    @Override
    public Pedagog updateTeacher(Pedagog teacher) {
        if((teacher == null) ||(teacher.getAisId() == null)){
            throw new IllegalArgumentException("null element");
        }
        
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("vsa-project-2");
        EntityManager em = emf.createEntityManager();
        try {    
            Pedagog p = em.createQuery("select p from Pedagog p where p.aisId = :id",Pedagog.class)
                    .setParameter("id", teacher.getAisId()).getSingleResult();

            p.setId(teacher.getAisId()); //TOTO SOM DODAV
            p.setDepartment(teacher.getDepartment());
            p.setEmail(teacher.getEmail());
            p.setAisId(teacher.getAisId());
            p.setInstitute(teacher.getInstitute());
            p.setName(teacher.getName());
            p.setTheses(teacher.getTheses());
            
            em.getTransaction().begin();
            em.getTransaction().commit();
            em.close();
            return p;
        } catch (Exception e) {
            em.close();
            return null;
        }
    }

    @Override
    public List<Pedagog> getTeachers() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("vsa-project-2");
        EntityManager em = emf.createEntityManager();
        List<Pedagog> p = new ArrayList();
        try {
            p = em.createQuery("select p from Pedagog p",Pedagog.class).getResultList();
            em.close();
            return p;
        } catch (Exception e) {
            em.close();
            return p;
        }    }

    @Override
    public Pedagog deleteTeacher(Long id) {
        if(id == null){
            throw new IllegalArgumentException("Id is null");
        }
        
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("vsa-project-2");
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        try {
            Pedagog p = em.createQuery("select p from Pedagog p where p.aisId = :id",Pedagog.class)
                    .setParameter("id", id).getSingleResult();
//            System.out.println("-------->"+p);
//            List<ZaverecnaPraca> zp = em.createQuery("select zp from ZaverecnaPraca zp where zp.pedagog.Id = :id",ZaverecnaPraca.class)
//                    .setParameter("id", id).getResultList();
            List<ZaverecnaPraca> zp = em.createQuery("select zp from ZaverecnaPraca zp where zp.supervisor.aisId = :id",ZaverecnaPraca.class)
                    .setParameter("id", id).getResultList();
//            System.out.println("-------->");
            
            for(ZaverecnaPraca tmp : zp){
                em.remove(tmp);
            }
            em.remove(p);
            em.getTransaction().commit();
            em.close();
            return p;
        } catch (Exception e) {
            em.close();
            return null;
        }    
    }

    @Override
    public ZaverecnaPraca makeThesisAssignment(Long supervisor, String title, String type, String description) {
        if(supervisor == null){
            throw new IllegalArgumentException("Id is null");
        }    
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("vsa-project-2");
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        try {
            Pedagog p =em.createQuery("select p from Pedagog p where p.aisId = :id",Pedagog.class)
                    .setParameter("id", supervisor).getSingleResult();
            Typ t = Typ.valueOf(type);
            ZaverecnaPraca zp = new ZaverecnaPraca();
            Date nowDate = new Date();
            LocalDate endLocalDate = LocalDate.now().plusMonths(3);
            Date dateDate = Date.from(endLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

            zp.setTitle(title);
            zp.setType(t);
            zp.setDepartment(p.getInstitute());
            zp.setPublishedOn(nowDate);
            zp.setDeadline(dateDate);
            zp.setDescription(description);
            zp.setSupervisor(p);
            zp.setStatus(Status.FREE_TO_TAKE);
            
            em.persist(zp);
            em.getTransaction().commit();
            em.close();
            return zp;
        } catch (Exception e) {
            System.out.println("----------->"+e);
            em.close();
            return null;
        }    
    }

    @Override
    public ZaverecnaPraca assignThesis(Long thesisId, Long studentId) {
        if((thesisId == null)||(studentId == null)){
            throw new IllegalArgumentException("null element");
        } 
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("vsa-project-2");
        EntityManager em = emf.createEntityManager();
        
        Date date = new Date();
        ZaverecnaPraca tmp = em.find(ZaverecnaPraca.class, thesisId);
        if((tmp.getStatus() == Status.SUBMITTED)||(tmp.getStatus() == Status.IN_PROGRESS)){
            throw new IllegalStateException("Theme is taken ");
        }
        if((tmp.getDeadline().compareTo(date)<0)){
            throw new IllegalStateException("Date expire");
        }
        
        em.getTransaction().begin();
        try {
            
            ZaverecnaPraca zp = em.createQuery("select zp from ZaverecnaPraca zp where zp.id = :id",ZaverecnaPraca.class)
                    .setParameter("id", thesisId).getSingleResult();
            Student s = em.createQuery("select s from Student s where s.aisId = :id",Student.class)
                    .setParameter("id", studentId).getSingleResult();
            if(s.getThesis() != null){
                throw new IllegalStateException("Student has allready been assigned thesis");
            }
            
            zp.setAuthor(s);
            zp.setStatus(Status.IN_PROGRESS);
            em.getTransaction().commit();
            em.close();
            return zp;
        } catch (Exception e) {
            em.close();
            return null;
        }    
        
    }

    @Override
    public ZaverecnaPraca submitThesis(Long thesisId) {
        if((thesisId == null)){
            throw new IllegalArgumentException("null element");
        } 
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("vsa-project-2");
        EntityManager em = emf.createEntityManager();
        
        Date date = new Date();
        ZaverecnaPraca tmp = em.find(ZaverecnaPraca.class, thesisId);
        if((tmp.getStatus() == Status.SUBMITTED)||(tmp.getStatus() == Status.FREE_TO_TAKE)||(tmp.getDeadline().compareTo(date)<0)){
            throw new IllegalStateException("Theme is taken or date expired ");
        }
        em.getTransaction().begin();
        try {
            ZaverecnaPraca zp = em.createQuery("select zp from ZaverecnaPraca zp where zp.id = :id",ZaverecnaPraca.class)
                    .setParameter("id", thesisId).getSingleResult();
            
            zp.setStatus(Status.SUBMITTED);
            em.getTransaction().commit();
            em.close();
            return zp;
        } catch (Exception e) {
            em.close();
            return null;
        }    
    }

    @Override
    public ZaverecnaPraca deleteThesis(Long id) {
        if(id == null){
            throw new IllegalArgumentException("Id is null");
        }
        
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("vsa-project-2");
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        try {
            ZaverecnaPraca zp = em.createQuery("select zp from ZaverecnaPraca zp where zp.id = :id",ZaverecnaPraca.class)
                    .setParameter("id", id).getSingleResult();
            em.remove(zp);
            em.getTransaction().commit();
            em.close();
            return zp;
        } catch (Exception e) {
            em.close();
            return null;
        }    
    }

    @Override
    public List<ZaverecnaPraca> getTheses() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("vsa-project-2");
        EntityManager em = emf.createEntityManager();
        List<ZaverecnaPraca> zp = new ArrayList();
        try {
            zp = em.createQuery("select zp from ZaverecnaPraca zp",ZaverecnaPraca.class).getResultList();
            em.close();
            return zp;
        } catch (Exception e) {
            em.close();
            return zp;
        }        
}

    @Override
    public List<ZaverecnaPraca> getThesesByTeacher(Long teacherId) {
        if(teacherId == null){
            throw new IllegalArgumentException("Id is null");
        }
        
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("vsa-project-2");
        EntityManager em = emf.createEntityManager();
        List<ZaverecnaPraca> zp = new ArrayList();
        try {
            zp = em.createQuery("select zp from ZaverecnaPraca zp where zp.supervisor.aisId = :teacherId",ZaverecnaPraca.class)
                    .setParameter("teacherId", teacherId).getResultList();
            em.close();
            return zp;
        } catch (Exception e) {
            em.close();
            return zp;
        }            
    }

    @Override
    public ZaverecnaPraca getThesisByStudent(Long studentId) {
        if(studentId == null){
            throw new IllegalArgumentException("Id is null");
        }
        
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("vsa-project-2");
        EntityManager em = emf.createEntityManager();
        
        try {
            ZaverecnaPraca zp = em.createQuery("select zp from ZaverecnaPraca zp where zp.author.aisId = :studentId",ZaverecnaPraca.class)
                    .setParameter("studentId", studentId).getSingleResult();
            em.close();
            return zp;
        } catch (Exception e) {
            em.close();
            return null;
        }            
    }

    @Override
    public ZaverecnaPraca getThesis(Long id) {
        if(id == null){
            throw new IllegalArgumentException("Id is null");
        }
        
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("vsa-project-2");
        EntityManager em = emf.createEntityManager();
        
        try {
            ZaverecnaPraca zp = em.createQuery("select zp from ZaverecnaPraca zp where zp.id = :Id",ZaverecnaPraca.class)
                    .setParameter("Id", id).getSingleResult();
            em.close();
            return zp;
        } catch (Exception e) {
            em.close();
            return null;
        }            
    }

    @Override
    public ZaverecnaPraca updateThesis(ZaverecnaPraca thesis) {
        if((thesis == null)||(thesis.getId() == null)){
            throw new IllegalArgumentException("Id is null");
        }
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("vsa-project-2");
        EntityManager em = emf.createEntityManager();
        
        em.getTransaction().begin();
        System.out.println("-------------------->"+thesis.getId());
        try {
            ZaverecnaPraca zp = em.createQuery("select zp from ZaverecnaPraca zp where zp.id = :Id",ZaverecnaPraca.class)
                    .setParameter("Id", thesis.getId()).getSingleResult();
            
            
            
            if(thesis.getPublishedOn() != null){
                zp.setPublishedOn(thesis.getPublishedOn());
            }
            if(thesis.getDeadline() != null){
                zp.setDeadline(thesis.getDeadline());
            }
            zp.setTitle(thesis.getTitle());
            zp.setDescription(thesis.getDescription());
            
            zp.setSupervisor(thesis.getSupervisor());
            if(thesis.getStatus() != null){
                zp.setStatus(thesis.getStatus());
            }
            zp.setAuthor(thesis.getAuthor());
            zp.setType(thesis.getType());
            zp.setRegistrationNumber(thesis.getRegistrationNumber());
            zp.setDepartment(thesis.getDepartment());
            
            
            em.getTransaction().commit();
            em.close();
            return zp;
        } catch (Exception e) {
            System.out.println("---------------->"+e);
            em.close();
            return null;
        }           
    }
}
