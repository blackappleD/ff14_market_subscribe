package com.ff14.market.service;

import cn.hutool.extra.spring.SpringUtil;
import com.ff14.market.dto.SubscribeCfgReqDTO;
import com.ff14.market.dto.SubscribeCfgResDTO;
import com.ff14.market.exception.FF14Exception;
import com.ff14.market.mapper.FF14SubscribeCfgMapper;
import com.ff14.market.po.FF14SubscribeCfgPO;
import com.ff14.market.po.FF14UserPO;
import com.ff14.market.repo.FF14SubscribeCfgRepo;
import com.ff14.market.repo.FF14UserRepo;
import com.ff14.market.util.AdminUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * @author chentong
 * @version 1.0
 * @description: description
 * @date 2025/1/20 9:00
 */
@Service
public class FF14SubscribeCfgService {

	@Resource
	private FF14SubscribeCfgRepo ff14SubscribeCfgRepo;

	@Resource
	private FF14SubscribeCfgMapper ff14SubscribeCfgMapper;

	public void modifyNotify(Boolean notify) {

		Optional<FF14SubscribeCfgPO> byUser = ff14SubscribeCfgRepo.findByUser(AdminUtil.getCurrentUser());
		if (byUser.isPresent()) {
			FF14SubscribeCfgPO ff14SubscribeCfg = byUser.get();
			ff14SubscribeCfg.setNotify(notify);

			ff14SubscribeCfgRepo.save(ff14SubscribeCfg);
		}
	}

	public SubscribeCfgResDTO get() {

		FF14SubscribeCfgPO byUser = findByUser(AdminUtil.getCurrentUser());
		return ff14SubscribeCfgMapper.po2dto(byUser);
	}

	public FF14SubscribeCfgPO findByUser(FF14UserPO user) {

		return ff14SubscribeCfgRepo.findByUser(user).orElseThrow(() -> new FF14Exception("订阅配置不存在"));
	}

	public void update(SubscribeCfgReqDTO dto) {
		FF14SubscribeCfgPO subscribeCfg = findById(dto.getId());
		ff14SubscribeCfgMapper.dto2po(dto, subscribeCfg);
		ff14SubscribeCfgRepo.save(subscribeCfg);
	}

	public FF14SubscribeCfgPO findById(Long id) {
		return ff14SubscribeCfgRepo.findById(id).orElseThrow(() -> new FF14Exception("订阅配置不存在"));
	}

	@PostConstruct
	public void test() {
		FF14UserRepo userRepo = SpringUtil.getBean(FF14UserRepo.class);
		List<FF14UserPO> users = userRepo.findAll();


		List<FF14SubscribeCfgPO> list = users.stream().map(user -> {
			FF14SubscribeCfgPO cfg = new FF14SubscribeCfgPO();
			cfg.setUser(user);
			return cfg;
		}).toList();

		FF14SubscribeCfgRepo repo = SpringUtil.getBean(FF14SubscribeCfgRepo.class);
		repo.saveAll(list);

	}

}
