package com.ff14.market.service;

import com.ff14.market.dto.ItemDTO;
import com.ff14.market.exception.FF14Exception;
import com.ff14.market.mapper.FF14ItemMapper;
import com.ff14.market.po.FF14ItemPO;
import com.ff14.market.repo.FF14ItemRepo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author chentong
 * @version 1.0
 * @description: description
 * @date 2024/12/19 9:58
 */
@Service
public class FF14ItemService {

	@Resource
	private FF14ItemRepo ff14ItemRepo;

	@Resource
	private FF14ItemMapper ff14ItemMapper;

	public List<ItemDTO> searchItems(String name) {
		List<FF14ItemPO> items = ff14ItemRepo.findByNameContaining(name);
		return items.stream()
				.map(ff14ItemMapper::po2dto)
				.collect(Collectors.toList());
	}

	public FF14ItemPO findById(Long id) {
		return ff14ItemRepo.findById(id).orElseThrow(() -> new FF14Exception("物品不存在"));
	}

}
