package com.ff14.market.service;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.extra.mail.MailAccount;
import cn.hutool.extra.mail.MailUtil;
import com.ff14.market.dto.ItemPriceInfo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author chentong
 * @version 1.0
 * @description: description
 * @date 2024/12/17 14:33
 */
@Slf4j
@Component
public class FF14MailService {

	@Resource
	private MailAccount mailAccount;

	public void sendPriceSubscriptions(List<ItemPriceInfo> itemPriceInfos, String email) {

		if (!Validator.isEmail(email)) {
			log.warn("不合法的邮箱地址：{}", email);
			return;
		}
		// 创建HTML表格
		StringBuilder htmlContent = new StringBuilder();
		htmlContent.append("<html><body>");
		htmlContent.append("<table border='1' style='border-collapse: collapse; width: 100%;'>");

		// 添加表头
		htmlContent.append("<tr style='background-color: #f2f2f2;'>");
		htmlContent.append("<th style='padding: 10px; text-align: left;'>物品名称</th>");
		htmlContent.append("<th style='padding: 10px; text-align: left;'>最低价格</th>");
		htmlContent.append("<th style='padding: 10px; text-align: left;'>所在区服</th>");
		htmlContent.append("<th style='padding: 10px; text-align: left;'>品质</th>");
		htmlContent.append("<th style='padding: 10px; text-align: left;'>数量</th>");
		htmlContent.append("</tr>");

		// 添加数据行
		for (ItemPriceInfo item : itemPriceInfos) {
			htmlContent.append("<tr>");
			htmlContent.append(String.format("<td style='padding: 10px;'>%s</td>", item.getName()));
			htmlContent.append(CharSequenceUtil.format("<td style='padding: 10px;{}'>{}</td>", item.isLowerThreshold() ? "color: red;" : "", item.getPricePerUnit()));
			htmlContent.append(String.format("<td style='padding: 10px;'>%s</td>", item.getWorldName()));
			htmlContent.append(String.format("<td style='padding: 10px;'>%s</td>", item.isHq() ? "hq" : "nq"));
			htmlContent.append(String.format("<td style='padding: 10px;'>%s</td>", item.getQuantity()));
			htmlContent.append("</tr>");
		}

		htmlContent.append("</table>");
		htmlContent.append("</body></html>");
		// 发送邮件
		try {
			String result = MailUtil.send(mailAccount, email, "ff14订阅物品价格信息", htmlContent.toString(), true);
			System.out.println("邮件发送成功！" + result);
		} catch (Exception e) {
			System.err.println("邮件发送失败：" + e.getMessage());
		}

	}

}
