package com.ff14.market.service;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.ff14.market.dto.ItemDTO;
import com.ff14.market.dto.ItemPriceInfo;
import com.ff14.market.dto.SubscribePriceGroup;
import com.ff14.market.po.FF14ItemPO;
import com.ff14.market.po.FF14ItemSubPO;
import com.ff14.market.po.FF14UserPO;
import com.ff14.market.po.FF14SubscribeGroupPO;
import com.ff14.market.util.AdminUtil;
import jakarta.annotation.Resource;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author chentong
 * @version 1.0
 * @description: description
 * @date 2024/12/27 17:23
 */
@Service
public class FF14PriceService {


	// 占位符： 大区 itemid
	private static final String UNIVERSAL_URI = "https://universalis.app/api/v2/{}/{}?listings=5&entries=20&noGst=1&hq={}";


	@Resource
	private FF14SubscribeGroupService ff14SubscribeGroupService;

	public List<SubscribePriceGroup> subscribeItemPriceOnTime() {
		return subscribeItemPrice(AdminUtil.getCurrentUser());

	}

	public List<SubscribePriceGroup> subscribeItemPrice(FF14UserPO user) {
		List<FF14SubscribeGroupPO> userSubscribeList = ff14SubscribeGroupService.findByUser(user);

		return userSubscribeList.stream().map(userSubscribe -> {
			SubscribePriceGroup data = new SubscribePriceGroup();
			data.setId(userSubscribe.getId());
			data.setWorldName(userSubscribe.getWorld().getName());
			data.setItemPriceGroups(requestItemPriceInfo(userSubscribe.getItems(), userSubscribe.getWorld().getName()));
			return data;
		}).toList();
	}

	public List<SubscribePriceGroup.ItemPriceGroup> requestItemPriceInfo(List<FF14ItemSubPO> itemSubs, String worldName) {

		List<SubscribePriceGroup.ItemPriceGroup> list = ListUtil.list(false);
		List<FF14ItemSubPO> hqItemSubs = itemSubs.stream().filter(FF14ItemSubPO::getHq).toList();
		List<FF14ItemSubPO> notHqItemSubs = itemSubs.stream().filter(ff14ItemSubPO -> !ff14ItemSubPO.getHq()).toList();

		Map<String, String> itemIdNameMap = itemSubs.stream()
				.collect(Collectors.toMap(item -> String.valueOf(item.getItem().getId()),
						item -> item.getItem().getName()));

		if (hqItemSubs.size() == 1) {
			list.add(SubscribePriceGroup.ItemPriceGroup.builder()
					.id(hqItemSubs.getFirst().getItem().getId())
					.name(hqItemSubs.getFirst().getItem().getName())
					.itemPriceInfoList(requestItemPriceInfo(hqItemSubs.getFirst(), worldName))
					.build());
		} else {
			String hqItemIdStr = itemSubs.stream()
					.filter(FF14ItemSubPO::getHq)
					.map(itemSub -> String.valueOf(itemSub.getItem().getId()))
					.collect(Collectors.joining(","));
			String hqUrl = CharSequenceUtil.format(UNIVERSAL_URI, worldName, hqItemIdStr, true);
			list.addAll(request(itemIdNameMap, hqUrl));
		}
		if (notHqItemSubs.size() == 1) {
			list.add(SubscribePriceGroup.ItemPriceGroup.builder()
					.id(notHqItemSubs.getFirst().getItem().getId())
					.name(notHqItemSubs.getFirst().getItem().getName())
					.itemPriceInfoList(requestItemPriceInfo(notHqItemSubs.getFirst(), worldName))
					.build());
		} else {
			String notHqItemIdStr = itemSubs.stream()
					.filter(itemSub -> !itemSub.getHq())
					.map(itemSub -> String.valueOf(itemSub.getItem().getId()))
					.collect(Collectors.joining(","));

			String nqUrl = CharSequenceUtil.format(UNIVERSAL_URI, worldName, notHqItemIdStr, "");
			list.addAll(request(itemIdNameMap, nqUrl));
		}
		return list;

	}


	private List<SubscribePriceGroup.ItemPriceGroup> request(Map<String, String> itemIdNameMap, String hqUrl) {
		try (HttpResponse response = HttpUtil.createGet(hqUrl)
				.execute()) {
			if (response.getStatus() == 200) {

				String body = response.body();
				Map<String, Listings> itemListingsMap = JSONUtil.toBean(body, BatchItemsPrice.class).getItems();
				return itemListingsMap.entrySet().stream().map(entry -> {
					SubscribePriceGroup.ItemPriceGroup itemPriceGroup = new SubscribePriceGroup.ItemPriceGroup();
					itemPriceGroup.setId(Long.valueOf(entry.getKey()));
					itemPriceGroup.setName(itemIdNameMap.get(entry.getKey()));
					itemPriceGroup.setItemPriceInfoList(entry.getValue().getListings());
					return itemPriceGroup;
				}).toList();
			}
			return new ArrayList<>();
		}
	}

	public List<ItemPriceInfo> requestItemPriceInfo(FF14ItemSubPO itemSub, String worldName) {

		String url = CharSequenceUtil.format(UNIVERSAL_URI, worldName, itemSub.getItem().getId(), itemSub.getHq());
		try (HttpResponse response = HttpUtil.createGet(url)
				.execute()) {
			if (response.getStatus() == 200) {

				String body = response.body();
				return JSONUtil.toBean(body, Listings.class).getListings();
			} else {
				return null;
			}
		}
	}

	@Data
	public static class BatchItemsPrice {
		private List<Long> itemIds;

		// itemId, listings
		private Map<String, Listings> items;
	}

	@Data
	public static class Rows {
		public List<ItemDTO> rows;
	}

	@Data
	public static class Listings {
		public List<ItemPriceInfo> listings;
	}

}
