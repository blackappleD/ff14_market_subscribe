package com.ff14.market.repo;

import com.ff14.market.po.CursorOrderPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

/**
 * @author chentong
 * @version 1.0
 * @description: description
 * @date 2025/2/12 16:58
 */
public interface CursorOrderRepo extends JpaRepository<CursorOrderPO, Long>, JpaSpecificationExecutor<CursorOrderPO> {

	Optional<CursorOrderPO> findByOrderId(String orderId);

}
