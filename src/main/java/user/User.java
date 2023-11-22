/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package user;

import auth.Permission;
import java.io.Serializable;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;

/**
 * 
 * @author ivanc
 */
@Entity
//@AllArgsConstructor
@Table(name ="vsa_user") 
public class User implements Principal, Serializable {
    //47 min: ---> nvm preco dav tam volaco ...
    @Id
    @GeneratedValue
    private Long aisId;
    private String username;
    private String password;
    
    @ElementCollection
    private List<Permission> permissions;

    public Long getAisId() {
        return aisId;
    }

    public void setAisId(Long aisId) {
        this.aisId = aisId;
    }
    
    public User(){
        permissions = new ArrayList<>();
    }
    public User(Long aisId,String username,String password){
        this();
        this.aisId = aisId;
        this.username = username;
        this.password = password;
    }
    
    public void addPermission(Permission permission){
        this.permissions.add(permission);
    }

    public List<Permission> getPermissions() {
        return permissions;
    }

    public String getName(){
        return username;
    }
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
}
