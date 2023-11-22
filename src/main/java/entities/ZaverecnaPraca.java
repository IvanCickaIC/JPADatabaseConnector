/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

import Enums.Status;
import Enums.Typ;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.SequenceGenerator;
import javax.persistence.Temporal;

/**
 *
 * @author ivanc
 */
@Entity
public class ZaverecnaPraca implements Serializable{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;   
    @Column(unique = true)
    private String registrationNumber;
    private String title;
    private String description;
    private String department;
    
    @ManyToOne(optional = false)
    private Pedagog supervisor;
    
    @OneToOne
    @JoinColumn(nullable=true)
    private Student author;
    
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date publishedOn;
    
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date deadline;
    
    @Enumerated(EnumType.STRING)
    private Typ type;
    
    @Enumerated(EnumType.STRING)
    private Status status;
    
    
//    @PrePersist
//    private void generateregNum() {
//        this.registrationNumber = "FEI-"+Id;
//    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }
    
    
    

    public Long getId() {
        return id;
    }

    public void setId(Long Id) {
        this.id = Id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String name) {
        this.title = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String workplace) {
        this.department = workplace;
    }

    public Date getPublishedOn() {
        return publishedOn;
    }

    public void setPublishedOn(Date beginDate) {
        this.publishedOn = beginDate;
    }

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public Student getAuthor() {
        return author;
    }

    public void setAuthor(Student student) {
        this.author = student;
    }

    public Pedagog getSupervisor() {
        return supervisor;
    }

    public void setSupervisor(Pedagog pedagog) {
        this.supervisor = pedagog;
    }

    public Typ getType() {
        return type;
    }

    public void setType(Typ typ) {
        this.type = typ;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "ZaverecnaPraca{" + "Id=" + id + ", regNum=" + registrationNumber + ", name=" + title + ", description=" + description + ", workplace=" + department + ", date=" + publishedOn + ", deadline=" + deadline + ", student=" + author + ", pedagog=" + supervisor + ", typ=" + type + ", status=" + status + '}';
    }
        
}
