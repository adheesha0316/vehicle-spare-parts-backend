package com.spareparts.spareparts_backend.repo;

import com.spareparts.spareparts_backend.entity.User;
import com.spareparts.spareparts_backend.entity.Partner;
import com.spareparts.spareparts_backend.enums.PartnerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PartnerRepo extends JpaRepository<Partner, Integer> {
    // Find partner by linked user
    Optional<Partner> findByUser(User user);

    Optional<Partner> findByUser_UserId(Integer userId);

    // Approved partners
    List<Partner> findAllByStatus(PartnerStatus status);

    // Pending approval
    List<Partner> findAllByStatusOrderByCreatedAtDesc(PartnerStatus status);

    // Find by shop name (optional search)
    List<Partner> findByShopNameContainingIgnoreCase(String shopName);

    // Find by branch
    List<Partner> findByBranchNameContainingIgnoreCase(String branchName);

    // Agreement validation helpers
    List<Partner> findAllByAgreementAcceptedFalse();

    List<Partner> findAllByAgreedAgreement_AgreementId(Integer agreementId);

    // Admin cleanup / auditing
    boolean existsByUser_UserId(Integer userId);

}
