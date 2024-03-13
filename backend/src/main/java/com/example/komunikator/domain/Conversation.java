package com.example.komunikator.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Collection;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "conversations")
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "conversation",
            joinColumns = @JoinColumn(name="conversation_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Collection<User> users;
    @OneToMany(mappedBy = "conversation")
    private Collection<Message> messages;
}
