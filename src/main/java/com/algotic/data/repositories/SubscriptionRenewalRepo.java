package com.algotic.data.repositories;

import com.algotic.data.entities.SubscriptionRenewalDatas;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.stereotype.Repository;

@Repository
public interface SubscriptionRenewalRepo extends JpaRepository<SubscriptionRenewalDatas, String> {
    @Procedure(value = "Sp_SubscriptionRenewal")
    List<SubscriptionRenewalDatas> subscriptionRenewal(Integer limit, Integer offset);

    @Procedure(value = "Sp_SubscriptionRenewalcount")
    Object[] subscriptionrenewalCount();
}
