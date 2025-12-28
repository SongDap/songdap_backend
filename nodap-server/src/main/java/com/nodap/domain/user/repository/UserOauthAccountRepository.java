package com.nodap.domain.user.repository;

import com.nodap.domain.user.entity.Provider;
import com.nodap.domain.user.entity.UserOauthAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * OAuth 계정 Repository
 */
@Repository
public interface UserOauthAccountRepository extends JpaRepository<UserOauthAccount, Long> {

    /**
     * Provider와 ProviderId로 OAuth 계정 조회
     */
    Optional<UserOauthAccount> findByProviderAndProviderId(Provider provider, String providerId);

    /**
     * Provider와 ProviderId로 User와 함께 조회 (N+1 방지)
     */
    @Query("SELECT oa FROM UserOauthAccount oa JOIN FETCH oa.user WHERE oa.provider = :provider AND oa.providerId = :providerId")
    Optional<UserOauthAccount> findByProviderAndProviderIdWithUser(
            @Param("provider") Provider provider, 
            @Param("providerId") String providerId);

    /**
     * 사용자 ID로 OAuth 계정 조회
     */
    Optional<UserOauthAccount> findByUserId(Long userId);

    /**
     * 해당 Provider로 가입 여부 확인
     */
    boolean existsByProviderAndProviderId(Provider provider, String providerId);
}



