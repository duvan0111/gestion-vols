package com.bluesky.gestionvols.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "clients")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Client {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    private String lastName;
    
    private String firstName;
    
    private String email;
    
    private String phone;
    
    @Column(unique = true)
    private String numPassport;
    
    private String sex;
    
    @OneToMany(mappedBy = "client")
    private List<Ticket> tickets;
}
