package com.ff14.market.controller;

import com.ff14.market.dto.ItemDTO;
import com.ff14.market.service.FF14ItemService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ff14/item")
public class FF14ItemController {

    @Resource
    private FF14ItemService ff14ItemService;

    @GetMapping("/search")
    public List<ItemDTO> searchItems(@RequestParam String name) {
        return ff14ItemService.searchItems(name);
    }
}