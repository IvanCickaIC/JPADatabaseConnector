/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package user;

import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import static sk.stuba.fei.uim.vsa.pr2.JAXRSApplicationConfiguration.log;

/**
 *
 * @author ivanc
 */
public class UserService implements AutoCloseable {
    
    private static UserService instance; 
    
    private EntityManagerFactory emf;
    
    public static UserService getInstance(){
        if(instance == null){
            instance = new UserService();
        }
        return instance;
    }
    
    
    private UserService(){
        emf = Persistence.createEntityManagerFactory("vsa-project-2");
    }
    public Optional<User> getUserByUsername(String username){
        EntityManager em = emf.createEntityManager();
        TypedQuery <User> q = em.createQuery("select u from User u where u.username = :username",User.class);
        q.setParameter("username", username);
        Optional <User> uop = q.getResultStream().findFirst();
        em.close();
        return uop;
    }
    
    public User deleteUserById(Long Id){
        EntityManager em = emf.createEntityManager();
        try{
        
        em.getTransaction().begin();
        TypedQuery <User> q = em.createQuery("select u from User u where u.aisId = :Id",User.class);
        q.setParameter("Id", Id);
        Optional <User> uop = q.getResultStream().findFirst();
        if (uop.isPresent()) {
            User user = uop.get();
            em.remove(user);
            em.getTransaction().commit();
            return user;
        }
        return null;
        }catch(Exception e){
            return null;
        }finally{
            em.close();
        }
    }
    
    public User save(User user){
        EntityManager em = emf.createEntityManager();
        try{
            em.getTransaction().begin();
            em.persist(user);
            em.getTransaction().commit();
            return user;
        }catch(Exception e){
            log.error(e.getMessage(), e);
            if(em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            return null;
        }finally{
            em.close();
        }
    }
    
    @Override
    public void close() throws Exception{
        emf.close();
    }
    
    
}
