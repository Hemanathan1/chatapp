package com.example.demo.repository;

import com.example.demo.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("SELECT m FROM Message m WHERE " +
           "(m.sender = :user1 AND m.receiver = :user2) OR " +
           "(m.sender = :user2 AND m.receiver = :user1) " +
           "ORDER BY m.id ASC")
    List<Message> findConversation(
        @Param("user1") String user1,
        @Param("user2") String user2
    );

    List<Message> findByGroupIdOrderByIdAsc(Long groupId);

    default List<Message> findByGroupId(Long groupId) {
        return findByGroupIdOrderByIdAsc(groupId);
        
        
    }
    @Query("SELECT m FROM Message m WHERE " +
    	       "((m.sender = :user1 AND m.receiver = :user2) OR " +
    	       "(m.sender = :user2 AND m.receiver = :user1)) " +
    	       "AND LOWER(m.content) LIKE LOWER(CONCAT('%', :query, '%')) " +
    	       "ORDER BY m.id ASC")
    	List<Message> searchConversation(
    	    @Param("user1") String user1,
    	    @Param("user2") String user2,
    	    @Param("query") String query
    	);
}