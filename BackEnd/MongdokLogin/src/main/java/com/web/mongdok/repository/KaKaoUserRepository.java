package com.web.mongdok.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.web.mongdok.entity.User;

import io.lettuce.core.dynamic.annotation.Param;


@Repository
public interface KaKaoUserRepository extends CrudRepository<User, String> {

	List<User> findAll();

	@Query(value = "select * from user where kakao_id=:kakaoId", nativeQuery = true)
	Optional<User> findByKakaoId(@Param("kakaoId") String kakaoId);

	User findByEmail(String email);

	User findByUserName(String nickname);

	boolean deleteByKakaoId(String kakaoId);
}