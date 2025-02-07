package com.ff14.market.service;

import com.ff14.market.dto.SubscribeGroupResDTO;
import com.ff14.market.dto.UserSubscribeGroupReqDTO;
import com.ff14.market.mapper.FF14SubscribeGroupMapper;
import com.ff14.market.po.FF14SubscribeGroupPO;
import com.ff14.market.po.FF14UserPO;
import com.ff14.market.repo.FF14UserSubscribeRepo;
import com.ff14.market.util.AdminUtil;
import com.ff14.market.util.AuthUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author chentong
 * @version 1.0
 * @description: description
 * @date 2024/12/19 11:24
 */
@Service
public class FF14SubscribeGroupService {

	@Resource
	private FF14UserSubscribeRepo ff14UserSubscribeRepo;

	@Resource
	private FF14UserService ff14UserService;

	@Resource
	private FF14SubscribeGroupMapper ff14SubscribeGroupMapper;

	public List<SubscribeGroupResDTO> get() {

		FF14UserPO user = ff14UserService.findById(AuthUtil.getLoginId());
		List<FF14SubscribeGroupPO> byUser = ff14UserSubscribeRepo.findByUser(user);
		return byUser.stream()
				.map(po -> ff14SubscribeGroupMapper.po2resDto(po))
				.toList();
	}

	public void modify(List<UserSubscribeGroupReqDTO> req) {

		FF14UserPO currentUser = AdminUtil.getCurrentUser();
		List<FF14SubscribeGroupPO> userSubGroup = ff14UserSubscribeRepo.findByUser(currentUser);
		Map<Long, FF14SubscribeGroupPO> userSubGroupMap = userSubGroup.stream()
				.collect(Collectors.toMap(FF14SubscribeGroupPO::getId, Function.identity()));

		List<Long> reqGroupIds = req.stream().map(UserSubscribeGroupReqDTO::getId).toList();

		List<FF14SubscribeGroupPO> removeSubGroups = userSubGroup.stream()
				.filter(sub -> !reqGroupIds.contains(sub.getId()))
				.toList();
		ff14UserSubscribeRepo.deleteAll(removeSubGroups);
		req.forEach(dto -> {
			FF14SubscribeGroupPO po;
			if (Objects.nonNull(dto.getId()) && userSubGroupMap.containsKey(dto.getId())) {
				po = userSubGroupMap.get(dto.getId());
				ff14SubscribeGroupMapper.reqDto2po(dto, po);
			} else {
				po = ff14SubscribeGroupMapper.reqDto2po(dto);
				po.setUser(currentUser);
			}
			ff14UserSubscribeRepo.save(po);

		});

	}

	public List<FF14SubscribeGroupPO> findByUser(FF14UserPO user) {
		return ff14UserSubscribeRepo.findByUser(user);
	}

	public List<FF14SubscribeGroupPO> findAll() {
		return ff14UserSubscribeRepo.findAll();
	}

}
