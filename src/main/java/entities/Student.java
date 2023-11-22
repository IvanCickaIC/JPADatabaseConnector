/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

/**
 *
 * @author ivanc
 */
@Entity
public class Student implements Serializable {
    @Id
//    @Column(name = "Id")
    private Long aisId;

    //    TOTO SOM PRIDAV
    private Long Id;
//    KRAJ
    private String name;
    @Column(unique = true)
    private String email;
    private String password;
    @Column(name = "study_year")
    private Integer year;
    private Integer term;
    @Column(name = "program")
    private String programme;
    @OneToOne(mappedBy="author")
    private ZaverecnaPraca thesis;

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getTerm() {
        return term;
    }

    public void setTerm(Integer term) {
        this.term = term;
    }

    public String getProgramme() {
        return programme;
    }

    public void setProgramme(String programme) {
        this.programme = programme;
    }

    public ZaverecnaPraca getThesis() {
        return thesis;
    }

    public void setThesis(ZaverecnaPraca zp) {
        this.thesis = zp;
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
        return "Student{" + "aisId=" + aisId + ", name=" + name + ", email=" + email + ", password=" + password + ", year=" + year + ", term=" + term + ", program=" + programme + ", zp=" + thesis + '}';
    }
 
}
