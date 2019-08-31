package org.zigmoi.ketchup.iam.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zigmoi.ketchup.project.entities.ProjectId;
import org.zigmoi.ketchup.iam.entities.User;


public interface UserRepository extends JpaRepository<User, String> {

    long countByEmail(String email);

//    boolean existsByUserNameAndProjectsExists(String userName, ProjectId projectId);
}