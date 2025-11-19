package com.campus.card.admin.controller;

import com.campus.card.admin.domain.Menu;
import com.campus.card.admin.repository.MenuRepository;
import com.campus.card.admin.security.RequiresPermission;
import com.campus.card.common.result.Result;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/menus")
public class MenuController {
    private final MenuRepository menuRepository;

    public MenuController(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    @GetMapping
    @RequiresPermission({"system:menu:list"})
    public Result<List<Menu>> list() {
        return Result.ok(menuRepository.findAll());
    }

    @PostMapping
    @RequiresPermission({"system:menu:create"})
    public Result<Menu> create(@RequestBody CreateMenuReq req) {
        Menu m = new Menu();
        m.setName(req.getName());
        m.setPath(req.getPath());
        m.setSortOrder(req.getSortOrder() == null ? 0 : req.getSortOrder());
        m.setStatus(1);
        return Result.ok(menuRepository.save(m));
    }

    @Data
    public static class CreateMenuReq {
        private String name;
        private String path;
        private Integer sortOrder;
    }
}