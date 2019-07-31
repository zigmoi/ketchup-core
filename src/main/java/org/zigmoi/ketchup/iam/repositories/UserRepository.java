package org.zigmoi.ketchup.iam.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zigmoi.ketchup.iam.entities.User;


public interface UserRepository extends JpaRepository<User, String> {

    public long countByEmail(String email);

}