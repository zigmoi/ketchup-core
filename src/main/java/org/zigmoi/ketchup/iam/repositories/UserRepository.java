package org.zigmoi.ketchup.iam.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zigmoi.ketchup.iam.entities.User;

import java.util.List;


public interface UserRepository extends JpaRepository<User, String> {

    long countByEmail(String email);

    List<User> findAllByUserNameEndsWith(String tenantId);

//    boolean existsByUserNameAndProjectsExists(String userName, ProjectId projectId);
}