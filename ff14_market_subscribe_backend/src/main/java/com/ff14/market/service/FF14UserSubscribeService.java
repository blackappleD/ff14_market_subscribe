package com.ff14.market.service;

import com.ff14.market.dto.UserSubscribeReqDTO;
import com.ff14.market.dto.UserSubscribeResDTO;
import com.ff14.market.mapper.FF14UserSubscribeMapper;
import com.ff14.market.po.FF14UserPO;
import com.ff14.market.po.FF14UserSubPO;
import com.ff14.market.po.FF14WorldPO;
import com.ff14.market.repo.FF14UserSubscribeRepo;
import com.ff14.market.util.AdminUtil;
import com.ff14.market.util.AuthUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author chentong
 * @version 1.0
 * @description: description
 * @date 2024/12/19 11:24
 */
@Service
public class FF14UserSubscribeService {

	@Resource
	private FF14UserSubscribeRepo ff14UserSubscribeRepo;

	@Resource
	private FF14UserService ff14UserService;

	@Resource
	private FF14WorldService ff14WorldService;

	@Resource
	private FF14UserSubscribeMapper ff14UserSubscribeMapper;

	public List<UserSubscribeResDTO> get() {

		FF14UserPO user = ff14UserService.findById(AuthUtil.getLoginId());
		List<FF14UserSubPO> byUser = ff14UserSubscribeRepo.findByUser(user);
		return byUser.stream()
				.map(po -> ff14UserSubscribeMapper.po2resDto(po))
				.toList();
	}

	public void modify(List<UserSubscribeReqDTO> req) {

		FF14UserPO currentUser = AdminUtil.getCurrentUser();
		List<FF14UserSubPO> byUser = ff14UserSubscribeRepo.findByUser(currentUser);
		Map<FF14WorldPO, FF14UserSubPO> subMap = byUser.stream()
				.collect(Collectors.toMap(FF14UserSubPO::getWorld, Function.identity()));

		List<Long> reqWorldIds = req.stream().map(dto -> dto.getWorld().getId()).toList();

		List<FF14UserSubPO> removeWorldSubs = byUser.stream()
				.filter(sub -> !reqWorldIds.contains(sub.getWorld().getId()))
				.toList();
		ff14UserSubscribeRepo.deleteAll(removeWorldSubs);
		req.forEach(dto -> {
			FF14WorldPO world = ff14WorldService.findById(dto.getWorld().getId());
			FF14UserSubPO po;
			if (subMap.containsKey(world)) {
				po = subMap.get(world);
				ff14UserSubscribeMapper.reqDto2po(dto, po);
			} else {
				po = ff14UserSubscribeMapper.reqDto2po(dto);
				po.setUser(currentUser);
			}
			ff14UserSubscribeRepo.save(po);

		});

	}

	public List<FF14UserSubPO> findByUser(FF14UserPO user) {
		return ff14UserSubscribeRepo.findByUser(user);
	}

	public List<FF14UserSubPO> findAll() {
		return ff14UserSubscribeRepo.findAll();
	}

}
