package com.spareparts.spareparts_backend.repo;

import com.spareparts.spareparts_backend.entity.Partner;
import com.spareparts.spareparts_backend.entity.PartnerSignedAgreement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PartnerSignedAgreementRepo extends JpaRepository<PartnerSignedAgreement, Integer> {

    List<PartnerSignedAgreement> findByPartnerOrderBySignedAtDesc(Partner partner);

    Optional<PartnerSignedAgreement> findTopByPartnerOrderBySignedAtDesc(Partner partner);


}
