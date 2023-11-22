/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;

/**
 *
 * @author ivanc
 */
@Entity
public class Pedagog implements Serializable{
    @Id
    private Long aisId;

//    TOTO SOM PRIDAV
    private Long Id;
//    KRAJ

    private String name;
    @Column(unique = true)
    private String email;
    private String password;
    private String institute;
    private String department;
    @OneToMany(mappedBy="supervisor")
    private List<ZaverecnaPraca> theses;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    
    
    public Long getAisId() {
        return aisId;
    }

    public void setAisId(Long aisId) {
        this.aisId = aisId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getInstitute() {
        return institute;
    }

    public void setInstitute(String institute) {
        this.institute = institute;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public List<ZaverecnaPraca> getTheses() {
        return theses;
    }

    public void setTheses(List<ZaverecnaPraca> zpList) {
        this.theses = zpList;
    }

    //    TOTO SOM PRIDAV
    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }
//    KRAJ

    @Override
    public String toString() {
        return "Pedagog{" + "Id=" + aisId + ", name=" + name + ", email=" + email + ", institute=" + institute + ", department=" + department + ", zpList=" + theses + '}';
    }
    
    
    
    
}
