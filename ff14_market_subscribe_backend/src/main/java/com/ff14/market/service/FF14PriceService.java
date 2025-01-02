package com.ff14.market.service;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.ff14.market.dto.ItemPriceInfo;
import com.ff14.market.dto.ItemPriceInfoGroupByWorld;
import com.ff14.market.po.FF14UserPO;
import com.ff14.market.po.FF14UserSubPO;
import com.ff14.market.task.ScheduleTask;
import com.ff14.market.util.AdminUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author chentong
 * @version 1.0
 * @description: description
 * @date 2024/12/27 17:23
 */
@Service
public class FF14PriceService {


	// 占位符： 大区 itemid
	private static final String UNIVERSAL_URI = "https://universalis.app/api/v2/{}/{}?listings=50&entries=20&noGst=1";


	@Resource
	private FF14UserSubscribeService ff14UserSubscribeService;

	public List<ItemPriceInfoGroupByWorld> subscribeItemPriceOnTime() {
		return subscribeItemPrice(AdminUtil.getCurrentUser());

	}

	public List<ItemPriceInfoGroupByWorld> subscribeItemPrice(FF14UserPO user) {
		List<FF14UserSubPO> userSubscribeList = ff14UserSubscribeService.findByUser(user);

		return userSubscribeList.stream().map(userSubscribe -> {
			ItemPriceInfoGroupByWorld data = new ItemPriceInfoGroupByWorld();
			List<ItemPriceInfo> itemPriceInfos = userSubscribe.getItems()
					.stream()
					.map(itemSup -> {
						ItemPriceInfo itemPriceInfo = requestItemPriceInfo(itemSup.getItem().getId(),
								itemSup.getItem().getName(), userSubscribe.getWorld().getName());
						itemPriceInfo.setTotal(itemPriceInfo.getTotal() + itemPriceInfo.getTax());
						if (itemSup.getNotifyThreshold() == null) {
							itemPriceInfo.setLowerThreshold(false);
						} else {
							itemPriceInfo.setLowerThreshold(itemPriceInfo.getPricePerUnit() <= itemSup.getNotifyThreshold());
						}
						return itemPriceInfo;
					}).toList();
			data.setWorldName(userSubscribe.getWorld().getName());
			data.setItemPriceInfoList(itemPriceInfos);
			return data;
		}).toList();
	}


	public ItemPriceInfo requestItemPriceInfo(Long itemId, String itemName, String worldName) {

		String url = CharSequenceUtil.format(UNIVERSAL_URI, worldName, itemId);
		try (HttpResponse response = HttpUtil.createGet(url)
				.execute()) {
			if (response.getStatus() == 200) {

				String body = response.body();
				List<ItemPriceInfo> listings = JSONUtil.toBean(body, ScheduleTask.Listings.class).getListings();
				if (!listings.isEmpty()) {
					ItemPriceInfo first = listings.getFirst();
					first.setId(String.valueOf(itemId));
					first.setName(itemName);
					return first;
				}
			} else {
				// todo notify
				return null;
			}
		}
		return null;
	}

}
