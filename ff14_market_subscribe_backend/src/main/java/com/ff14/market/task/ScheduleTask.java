package com.ff14.market.task;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.ff14.market.dto.ItemDTO;
import com.ff14.market.dto.ItemPriceInfo;
import com.ff14.market.po.FF14UserSubPO;
import com.ff14.market.service.FF14MailService;
import com.ff14.market.service.FF14UserSubscribeService;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author chentong
 * @version 1.0
 * @description: description
 * @date 2024/12/17 12:59
 */
@Slf4j
@Component
public class ScheduleTask {

	@Resource
	private FF14MailService ff14MailService;

	@Resource
	private FF14UserSubscribeService ff14UserSubscribeService;

	// 占位符： 大区 itemid
	private static final String UNIVERSAL_URI = "https://universalis.app/api/v2/{}/{}?listings=50&entries=20&noGst=1";

	@Scheduled(cron = "0 0/30 * * * ? ")
	public void ff14Task() {

		LocalDateTime now = LocalDateTime.now();
		LocalDateTime beginOfDay = LocalDateTimeUtil.beginOfDay(now);
		if (now.isAfter(beginOfDay.plusHours(23)) || now.isBefore(beginOfDay.plusHours(8))) {
			log.info("当前时间：{}，处于免打扰时间段，不执行推送任务！", now);
			return;
		}
		List<FF14UserSubPO> userSubscribeList = ff14UserSubscribeService.findAll();

		userSubscribeList.forEach(userSubscribe -> {
			List<ItemPriceInfo> itemPriceInfos = userSubscribe.getItems()
					.stream()
					.map(itemSup -> {
						ItemPriceInfo itemPriceInfo = requestItemPriceInfo(itemSup.getItem().getId(),
								itemSup.getItem().getName(), userSubscribe.getWorld().getName());
						if (itemSup.getNotifyThreshold() == null) {
							itemPriceInfo.setLowerThreshold(false);
						} else {
							itemPriceInfo
									.setLowerThreshold(itemPriceInfo.getPricePerUnit() <= itemSup.getNotifyThreshold());
						}
						return itemPriceInfo;

					})
					.collect(Collectors.toList());
			if (itemPriceInfos.isEmpty()) {
				return;
			}
			ff14MailService.sendPriceSubscriptions(itemPriceInfos, userSubscribe.getUser().getEmail());
		});

	}

	public ItemPriceInfo requestItemPriceInfo(Long itemId, String itemName, String worldName) {

		String url = CharSequenceUtil.format(UNIVERSAL_URI, worldName, itemId);
		try (HttpResponse response = HttpUtil.createGet(url)
				.execute()) {
			if (response.getStatus() == 200) {

				String body = response.body();
				List<ItemPriceInfo> listings = JSONUtil.toBean(body, Listings.class).getListings();
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

	@Data
	public static class Rows {
		public List<ItemDTO> rows;
	}

	@Data
	public static class Listings {
		public List<ItemPriceInfo> listings;
	}

}
