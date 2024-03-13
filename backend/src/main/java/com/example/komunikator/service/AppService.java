package com.example.komunikator.service;

import com.example.komunikator.domain.*;
import com.example.komunikator.repository.ConversationRepo;
import com.example.komunikator.repository.MessageRepo;
import com.example.komunikator.repository.RoleRepo;
import com.example.komunikator.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityExistsException;
import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class AppService {
    private final RoleRepo roleRepository;
    private final UserRepo userRepository;
    private final ConversationRepo conversationRepository;
    private final MessageRepo messageRepository;


    public Role addRole(String role){
        String[] possibleRoles = {"ROLE_USER"}; //list of all possible roles
        if(!Arrays.asList(possibleRoles).contains(role)){
            throw new IllegalStateException("Role not implemented");
        }
        Role foundRole = roleRepository.findByName(role);
        if(foundRole==null){ //add to DB if not exists yet
            Role newRole = new Role();
            newRole.setName(role);
            roleRepository.save(newRole);
            foundRole = roleRepository.findByName(role);
        }
        return foundRole;
    }

    public User addUser(User user){
        if(user.getUsername().contains(" ")){throw new IllegalStateException("Forbidden sign");}
        if(user.getPassword().contains(" ")){throw new IllegalStateException("Forbidden sign");}
        if(user.getUsername().equals("")){throw new IllegalStateException("Forbidden sign");}
        if(user.getPassword().equals("")){throw new IllegalStateException("Forbidden sign");}
        if(!userRepository.findByUsername(user.getUsername()).equals(Optional.empty())){throw new EntityExistsException("Login in use");}

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Collection<Role> defaultRole = new ArrayList<>();
        defaultRole.add(addRole("ROLE_USER")); //new user is created with default role USER
        user.setRoles(defaultRole);
        return userRepository.save(user);
    }


    public void addRoleToUser(String username, String role){
        User user = userRepository.findByUsername(username).get();
        Collection<Role> roles = user.getRoles();
        roles.add(addRole(role));
        user.setRoles(roles);
        userRepository.save(user);
    }

    public List<User> getAllUsers(){
        return userRepository.findAll();
    }
    public User getUserByUsername(String username){
        return userRepository.findByUsername(username).get();
    }
    public User getUserById(int id){
        return userRepository.findById(id).get();
    }

    public int startConversation(int user1id, int user2id){
        if(user1id==user2id){
            throw new IllegalArgumentException("Użytkownik nie może nawiązać konwersacji sam ze sobą");
        }
        int conversationId = findConversation(user1id, user2id);
        if(conversationId==-1){ //if users don't have conversation then method creates one and returns it id
            Collection<User> users = new ArrayList<>();
            users.add(userRepository.findById(user1id).get());
            users.add(userRepository.findById(user2id).get());
            Conversation conversation = new Conversation();
            conversation.setUsers(users);
            conversationRepository.save(conversation);
            return conversation.getId();
        }
        return conversationId; //if conversation already exists then return it id
    }

    @Transactional
    public int findConversation(int user1id, int user2id){ //return id of conversation between two users
        List<Conversation> conversations = conversationRepository.findConversationByUserId(new int[]{user1id, user2id});
        if(conversations.isEmpty()){
            return -1;
        }
        else{
            return conversations.get(0).getId();
        }
    }

    public Conversation getConversationById(int id){
        return conversationRepository.findById(id).get();
    }

    public List<String> sortMessagesByUsername(Conversation conversation){ // return in chronological order of messages of two users
        List<String> messages = new ArrayList<>();
        if(conversation.getMessages()!=null){
            for(Message message : conversation.getMessages()){
                //adds username prefix to messages
                messages.add(getUserById(message.getFromId()).getUsername()+": "+message.getText());
            }
        }
        return messages;
    }

    public void addMessage(String text, int fromId, int conversationId){
        Message message = new Message();
        message.setText(text);
        message.setFromId(fromId);
        message.setConversation(conversationRepository.findById(conversationId).get());
        messageRepository.save(message);
    }

    public String findRecipientUsername(int conversationId, String username){
        Conversation conversation = conversationRepository.findById(conversationId).get();
        return conversation
                .getUsers()
                .stream()
                .filter(user -> !user.getUsername().equals(username))
                .collect(Collectors.toList())
                .get(0)
                .getUsername();
    }

    public String getPrincipalUsername(Object principal){
        if (principal instanceof MyUserDetails) {return  ((MyUserDetails)principal).getUsername();}
        else{
            return null;
        }
    }
}