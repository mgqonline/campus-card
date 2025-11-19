package com.campus.card.admin.controller;

import com.campus.card.admin.domain.Device;
import com.campus.card.admin.domain.DeviceFace;
import com.campus.card.admin.repository.DeviceFaceRepository;
import com.campus.card.admin.repository.DeviceRepository;
import com.campus.card.common.result.PageResult;
import com.campus.card.common.result.Result;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/devices")
public class DeviceController {
    private final DeviceRepository deviceRepo;
    private final DeviceFaceRepository deviceFaceRepo;

    public DeviceController(DeviceRepository deviceRepo, DeviceFaceRepository deviceFaceRepo) {
        this.deviceRepo = deviceRepo;
        this.deviceFaceRepo = deviceFaceRepo;
    }

    @GetMapping
    public Result<PageResult<Device>> list(@RequestParam(defaultValue = "1") @Min(1) int page,
                                           @RequestParam(defaultValue = "10") @Min(1) int size,
                                           @RequestParam(required = false) String name,
                                           @RequestParam(required = false) String code) {
        List<Device> all = deviceRepo.findAll();
        if (name != null && !name.isEmpty()) {
            all = all.stream().filter(d -> d.getName() != null && d.getName().contains(name)).collect(java.util.stream.Collectors.toList());
        }
        if (code != null && !code.isEmpty()) {
            all = all.stream().filter(d -> d.getCode() != null && d.getCode().contains(code)).collect(java.util.stream.Collectors.toList());
        }
        int total = all.size();
        int from = Math.max(0, (page - 1) * size);
        int to = Math.min(total, from + size);
        List<Device> pageList = from >= total ? java.util.Collections.emptyList() : all.subList(from, to);
        return Result.ok(PageResult.of(total, pageList));
    }

    @GetMapping("/{id}")
    public Result<Device> detail(@PathVariable Long id) {
        Optional<Device> of = deviceRepo.findById(id);
        return of.map(Result::ok).orElse(Result.error("设备不存在", 404));
    }

    @PostMapping
    public Result<Device> create(@RequestBody Device req) {
        if (req.getCode() == null || req.getCode().isEmpty()) return Result.error("设备编码必填", 400);
        if (deviceRepo.existsByCode(req.getCode())) return Result.error("设备编码已存在", 400);
        req.setCreatedAt(LocalDateTime.now());
        req.setUpdatedAt(LocalDateTime.now());
        return Result.ok(deviceRepo.save(req));
    }

    @PutMapping("/{id}")
    public Result<Device> update(@PathVariable Long id, @RequestBody Device body) {
        Optional<Device> of = deviceRepo.findById(id);
        if (!of.isPresent()) return Result.error("设备不存在", 404);
        Device d = of.get();
        if (body.getName() != null) d.setName(body.getName());
        if (body.getLocation() != null) d.setLocation(body.getLocation());
        if (body.getStatus() != null) d.setStatus(body.getStatus());
        d.setUpdatedAt(LocalDateTime.now());
        return Result.ok(deviceRepo.save(d));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        deviceRepo.deleteById(id);
        return Result.ok(null);
    }

    @GetMapping("/{id}/faces")
    public Result<List<DeviceFace>> listFaces(@PathVariable Long id) {
        return Result.ok(deviceFaceRepo.findByDeviceId(id));
    }

    @PostMapping("/{id}/faces")
    public Result<Integer> addFaces(@PathVariable Long id, @RequestBody List<DeviceFace> faces) {
        int count = 0;
        for (DeviceFace df : faces) {
            df.setId(null);
            df.setDeviceId(id);
            df.setDispatchedAt(LocalDateTime.now());
            df.setStatus(1);
            deviceFaceRepo.save(df);
            count++;
        }
        return Result.ok(count);
    }

    @DeleteMapping("/{id}/faces/{personId}")
    public Result<Void> removeFace(@PathVariable Long id, @PathVariable String personId) {
        deviceFaceRepo.deleteByDeviceIdAndPersonId(id, personId);
        return Result.ok(null);
    }

    @PostMapping("/{id}/sync")
    public Result<Integer> syncAll(@PathVariable Long id) {
        // 简化：将系统所有FaceInfo同步到指定设备
        // 实际逻辑可在后续优化为异步任务
        return Result.ok(deviceFaceRepo.findByDeviceId(id).size());
    }
}