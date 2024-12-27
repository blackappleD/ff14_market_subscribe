package com.ff14.market.repo;

import com.ff14.market.po.FF14UserPO;
import com.ff14.market.po.FF14UserSubPO;
import com.ff14.market.po.FF14WorldPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

/**
 * @author chentong
 * @version 1.0
 * @description: description
 * @date 2024/12/19 9:57
 */
public interface FF14UserSubscribeRepo extends JpaRepository<FF14UserSubPO, Long>, JpaSpecificationExecutor<FF14UserSubPO> {

	Optional<FF14UserSubPO> findByUserAndWorld(FF14UserPO user, FF14WorldPO world);

	List<FF14UserSubPO> findByUser(FF14UserPO user);

	void deleteByUserAndWorld(FF14UserPO user, FF14WorldPO world);

}
