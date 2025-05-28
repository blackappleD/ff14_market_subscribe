package com.ff14.market.task;

import cn.hutool.core.text.csv.*;
import cn.hutool.json.JSONUtil;
import com.ff14.market.enums.WorldLevel;
import com.ff14.market.po.FF14ItemPO;
import com.ff14.market.po.FF14WorldPO;
import com.ff14.market.repo.FF14ItemRepo;
import com.ff14.market.repo.FF14WorldRepo;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import cn.hutool.core.util.CharsetUtil;

/**
 * @author chentong
 * @version 1.0
 * @description: description
 * @date 2024/12/19 14:44
 */
@Slf4j
@Configuration
public class InitRunner implements ApplicationRunner {

	@Resource
	private FF14ItemRepo ff14ItemRepo;

	@Resource
	private FF14WorldRepo ff14WorldRepo;

	@Resource
	private ResourceLoader resourceLoader;

	@Value("${item.force-update:false}")
	private boolean itemUpdate;

	@Override
	public void run(ApplicationArguments args) {
		try {

			initItemData();
			initWorldData();
		} catch (IOException e) {
			log.error("初始化失败，读取初始化文件异常：{}", e.getMessage(), e);
		}

	}

	public void initWorldData() throws IOException {
		if (ff14WorldRepo.findById(1L).isPresent()) {
			return;
		}
		String regionStr = resourceLoader.getResource("classpath:init/region.json").getContentAsString(StandardCharsets.UTF_8);

		String worldsStr = resourceLoader.getResource("classpath:init/world.json").getContentAsString(StandardCharsets.UTF_8);

		List<Regin> dataCenters = JSONUtil.toList(regionStr, Regin.class);
		List<FF14WorldPO> worlds1 = dataCenters.stream().map(Regin::getRegion)
				.distinct()
				.map(r -> {
					FF14WorldPO world = new FF14WorldPO();
					world.setLevel(WorldLevel.REGION);
					world.setName(r);
					return world;
				}).toList();

		List<FF14WorldPO> worlds2 = dataCenters.stream()
				.map(Regin::getName)
				.distinct()
				.map(r -> {
					FF14WorldPO world = new FF14WorldPO();
					world.setLevel(WorldLevel.DATA_CENTER);
					world.setName(r);
					return world;
				}).toList();
		ff14WorldRepo.saveAll(worlds1);
		ff14WorldRepo.saveAll(worlds2);

		List<World> worldList = JSONUtil.toList(worldsStr, World.class);
		List<FF14WorldPO> world3 = worldList.stream().map(w -> {
			FF14WorldPO world = new FF14WorldPO();
			world.setWorldId(w.getId());
			world.setName(w.getName());
			world.setLevel(WorldLevel.WORLD);
			return world;
		}).toList();

		ff14WorldRepo.saveAll(world3);
		log.info("初始化世界数据完成！");
	}


	@Data
	public static class World {
		private Long id;
		private String name;
	}

	@Data
	public static class Regin {
		private String name;
		private String region;
	}


	public void initItemData() throws IOException {
		if (ff14ItemRepo.findById(1L).isPresent() && !itemUpdate) {
			return;
		}
	
		// 创建CSV读取配置
		CsvReadConfig csvReadConfig = new CsvReadConfig();
		// 设置跳过前4行header（第5行开始才是实际数据）
		csvReadConfig.setBeginLineNo(4);
		// 不将第一行作为标题行
		csvReadConfig.setContainsHeader(false);
	
		// 使用CsvUtil读取CSV文件
		CsvReader reader = CsvUtil.getReader(csvReadConfig);
		
		// 将InputStream转换为Reader对象
		InputStreamReader inputStreamReader = new InputStreamReader(
			resourceLoader.getResource("classpath:init/Item.csv").getInputStream(),
			CharsetUtil.CHARSET_UTF_8
		);
		
		// 读取CSV文件为CsvData
		CsvData csvData = reader.read(inputStreamReader);
		
		// 获取所有行
		List<CsvRow> rows = csvData.getRows();
		
		// 将CsvRow转换为FF14ItemPO
		List<FF14ItemPO> poList = rows.stream().map(row -> {
			List<String> rawList = row.getRawList();
			FF14ItemPO item = new FF14ItemPO();
			if (!rawList.isEmpty()) {
				// 第一列是key（#），对应FF14ItemPO的id
				String keyStr = rawList.get(0);
				if (keyStr != null && !keyStr.isEmpty()) {
					item.setId(Long.parseLong(keyStr));
				}
				
				// 第八列是Name，对应FF14ItemPO的name
				if (rawList.size() > 1) {
					String nameStr = rawList.get(1);
					if (nameStr != null) {
						item.setName(nameStr);
					}
				}
			}
			return item;
		}).filter(item -> item.getId() != null && item.getName() != null)
		  .collect(Collectors.toList());
	
		ff14ItemRepo.saveAll(poList);
		log.info("初始化items数据完成，共加载{}条数据", poList.size());
	}

	@Data
	public static class CsvItem {
		private String key;
		private String str;
	}

}
