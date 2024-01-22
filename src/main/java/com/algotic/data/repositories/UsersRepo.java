package com.algotic.data.repositories;

import com.algotic.data.entities.Users;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersRepo extends JpaRepository<Users, String> {
    @Query(
            value =
                    "Select * from Users s where s.PhoneNumber =:phoneNumber and s.Status='Active' and s.IsDeleted= false limit 1",
            nativeQuery = true)
    Users findByPhoneNumber(String phoneNumber);

    @Query(
            value = "Select * from Users s where s.Email =:email and s.Status='Active' and s.IsDeleted= false limit 1",
            nativeQuery = true)
    Users findByEmail(String email);

    @Query(value = "Select * from Users s where s.Email =:email  limit 1", nativeQuery = true)
    Users findForStatus(String email);

    @Query(value = "Select * from Users s where s.ID =:userId and s.IsDeleted= false limit 1", nativeQuery = true)
    Users findByID(String userId);

    @Query(
            value =
                    "Select * from Users s where s.Role='customer' and  s.IsDeleted= false order by s.CreatedAt desc limit :limit offset :offset",
            nativeQuery = true)
    List<Users> findUsers(Integer limit, Integer offset);

    Users findByIdAndIsDeleted(String userId, boolean isDeleted);

    @Query(
            value =
                    "Select count(1) from Users u where  u.Status  <> 'CREATED' and u.`Role` <> 'Admin' and u.IsDeleted= false ",
            nativeQuery = true)
    Integer getUsersCount();

    @Query(
            value = "Select * from Users u where u.ID=:userId and u.IsDeleted=false and u.Role='customer'",
            nativeQuery = true)
    Users findByRole(String userId);

    @Query(
            value = "Select count(1) from Users u where ( u.Email  LIKE  '%@minditsystems%' OR "
                    + "u.Email Like '%@mailinator.com%'   or "
                    + "u.Email Like'%@Mailinator.co.in%' or u.Email Like '%@minditSystems.co.in%') and "
                    + " u.Status  <> 'CREATED' and u.`Role` <> 'Admin' and u.IsDeleted= false",
            nativeQuery = true)
    Integer getSubscriberCount();
}
