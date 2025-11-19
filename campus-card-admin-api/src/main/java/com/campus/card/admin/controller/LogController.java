package com.campus.card.admin.controller;

import com.campus.card.admin.domain.OpLog;
import com.campus.card.admin.domain.DeviceOpLog;
import com.campus.card.admin.domain.DataChangeLog;
import com.campus.card.admin.repository.OpLogRepository;
import com.campus.card.admin.repository.DeviceOpLogRepository;
import com.campus.card.admin.repository.DataChangeLogRepository;
import com.campus.card.common.result.PageResult;
import com.campus.card.common.result.Result;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/logs")
public class LogController {
    private final OpLogRepository opRepo;
    private final DeviceOpLogRepository devRepo;
    private final DataChangeLogRepository changeRepo;

    public LogController(OpLogRepository opRepo, DeviceOpLogRepository devRepo, DataChangeLogRepository changeRepo) {
        this.opRepo = opRepo;
        this.devRepo = devRepo;
        this.changeRepo = changeRepo;
    }

    @GetMapping("/op")
    public Result<PageResult<OpLog>> opLogs(@RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "20") int size,
                                            @RequestParam(required = false) String keywords) {
        PageRequest pr = PageRequest.of(Math.max(page - 1, 0), Math.max(size, 1));
        if (keywords != null && !keywords.isEmpty()) {
            List<OpLog> all = opRepo.findAll();
            List<OpLog> filtered = all.stream()
                    .filter(l -> contains(l.getUri(), keywords) || contains(l.getAction(), keywords) || contains(l.getClientIp(), keywords))
                    .sorted((a,b) -> Long.compare(b.getId(), a.getId()))
                    .collect(Collectors.toList());
            int from = Math.min((page - 1) * size, filtered.size());
            int to = Math.min(from + size, filtered.size());
            return Result.ok(PageResult.of(filtered.size(), filtered.subList(from, to)));
        }
        Page<OpLog> pg = opRepo.findAll(pr);
        return Result.ok(PageResult.of((int) pg.getTotalElements(), pg.getContent()));
    }

    @GetMapping("/device")
    public Result<PageResult<DeviceOpLog>> deviceLogs(@RequestParam(defaultValue = "1") int page,
                                                      @RequestParam(defaultValue = "20") int size,
                                                      @RequestParam(required = false) String keywords) {
        PageRequest pr = PageRequest.of(Math.max(page - 1, 0), Math.max(size, 1));
        if (keywords != null && !keywords.isEmpty()) {
            List<DeviceOpLog> all = devRepo.findAll();
            List<DeviceOpLog> filtered = all.stream()
                    .filter(l -> contains(l.getAction(), keywords) || contains(String.valueOf(l.getDeviceId()), keywords) || contains(l.getDeviceCode(), keywords))
                    .sorted((a,b) -> Long.compare(b.getId(), a.getId()))
                    .collect(Collectors.toList());
            int from = Math.min((page - 1) * size, filtered.size());
            int to = Math.min(from + size, filtered.size());
            return Result.ok(PageResult.of(filtered.size(), filtered.subList(from, to)));
        }
        Page<DeviceOpLog> pg = devRepo.findAll(pr);
        return Result.ok(PageResult.of((int) pg.getTotalElements(), pg.getContent()));
    }

    @GetMapping("/data-change")
    public Result<PageResult<DataChangeLog>> dataChangeLogs(@RequestParam(defaultValue = "1") int page,
                                                            @RequestParam(defaultValue = "20") int size,
                                                            @RequestParam(required = false) String entity,
                                                            @RequestParam(required = false) String keywords) {
        PageRequest pr = PageRequest.of(Math.max(page - 1, 0), Math.max(size, 1));
        if ((entity != null && !entity.isEmpty()) || (keywords != null && !keywords.isEmpty())) {
            List<DataChangeLog> all = changeRepo.findAll();
            List<DataChangeLog> filtered = all.stream()
                    .filter(l -> entity == null || entity.isEmpty() || entity.equalsIgnoreCase(l.getEntity()))
                    .filter(l -> keywords == null || keywords.isEmpty() || contains(l.getChangedFields(), keywords) || contains(l.getRemark(), keywords))
                    .sorted((a,b) -> Long.compare(b.getId(), a.getId()))
                    .collect(Collectors.toList());
            int from = Math.min((page - 1) * size, filtered.size());
            int to = Math.min(from + size, filtered.size());
            return Result.ok(PageResult.of(filtered.size(), filtered.subList(from, to)));
        }
        Page<DataChangeLog> pg = changeRepo.findAll(pr);
        return Result.ok(PageResult.of((int) pg.getTotalElements(), pg.getContent()));
    }

    private boolean contains(String s, String kw) {
        if (s == null || kw == null) return false;
        return s.toLowerCase().contains(kw.toLowerCase());
    }
}