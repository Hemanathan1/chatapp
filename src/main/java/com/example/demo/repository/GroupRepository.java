package com.example.demo.repository;

import com.example.demo.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GroupRepository extends JpaRepository<Group, Long> {

    @Query("SELECT g FROM Group g WHERE :username MEMBER OF g.members")
    List<Group> findGroupsByMember(@Param("username") String username);
}