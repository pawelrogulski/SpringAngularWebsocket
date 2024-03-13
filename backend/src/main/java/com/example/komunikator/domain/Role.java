package com.example.komunikator.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Collection;

@Getter
@Setter
@Entity
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;
    @JsonIgnore //many-to-many relationship caused a stack overflow error because the JSON serialization included a user who possessed a role that belonged to that user, and so on
    @ManyToMany(mappedBy = "roles")
    private Collection<User> users;
}
