package com.campus.card.admin.service;

import com.campus.card.admin.domain.Campus;
import com.campus.card.admin.domain.School;
import com.campus.card.admin.domain.SchoolConfig;
import com.campus.card.admin.domain.SchoolLogo;
import com.campus.card.admin.repository.CampusRepository;
import com.campus.card.admin.repository.SchoolConfigRepository;
import com.campus.card.admin.repository.SchoolLogoRepository;
import com.campus.card.admin.repository.SchoolRepository;
import com.campus.card.common.result.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SchoolService {
    private final SchoolRepository schoolRepository;
    private final CampusRepository campusRepository;
    private final SchoolConfigRepository schoolConfigRepository;
    private final SchoolLogoRepository schoolLogoRepository;

    public PageResult<School> pageList(int page, int size, String name, String code) {
        List<School> list = schoolRepository.findAll();
        if (name != null && !name.isEmpty()) {
            list = list.stream().filter(s -> s.getName() != null && s.getName().contains(name)).collect(Collectors.toList());
        }
        if (code != null && !code.isEmpty()) {
            list = list.stream().filter(s -> s.getCode() != null && s.getCode().contains(code)).collect(Collectors.toList());
        }
        int total = list.size();
        int p = Math.max(page, 1);
        int sz = Math.max(size, 1);
        int from = Math.max(0, (p - 1) * sz);
        int to = Math.min(total, from + sz);
        List<School> pageList = from >= total ? java.util.Collections.emptyList() : list.subList(from, to);
        return PageResult.of(total, pageList);
    }

    public Optional<School> findById(Long id) {
        return schoolRepository.findById(id);
    }

    public School create(School school) {
        if (school.getStatus() == null) school.setStatus(1);
        school.setCreateTime(LocalDateTime.now());
        school.setUpdateTime(LocalDateTime.now());
        return schoolRepository.save(school);
    }

    public School update(Long id, School body) {
        body.setId(id);
        body.setUpdateTime(LocalDateTime.now());
        return schoolRepository.save(body);
    }

    public void delete(Long id) {
        schoolRepository.deleteById(id);
    }

    public School enable(Long id) {
        Optional<School> os = schoolRepository.findById(id);
        if (!os.isPresent()) throw new IllegalArgumentException("学校不存在");
        School s = os.get();
        s.setStatus(1);
        s.setUpdateTime(LocalDateTime.now());
        return schoolRepository.save(s);
    }

    public School disable(Long id) {
        Optional<School> os = schoolRepository.findById(id);
        if (!os.isPresent()) throw new IllegalArgumentException("学校不存在");
        School s = os.get();
        s.setStatus(0);
        s.setUpdateTime(LocalDateTime.now());
        return schoolRepository.save(s);
    }

    // 校区管理
    public List<Campus> listCampuses(Long schoolId) {
        return campusRepository.findBySchoolId(schoolId);
    }

    public PageResult<Campus> pageCampuses(Long schoolId, int page, int size, String name) {
        PageRequest pr = PageRequest.of(Math.max(page - 1, 0), Math.max(size, 1));
        Page<Campus> pg;
        if (schoolId != null && name != null && !name.isEmpty()) {
            pg = campusRepository.findBySchoolIdAndNameContainingIgnoreCase(schoolId, name, pr);
        } else if (schoolId != null) {
            pg = campusRepository.findBySchoolId(schoolId, pr);
        } else if (name != null && !name.isEmpty()) {
            pg = campusRepository.findByNameContainingIgnoreCase(name, pr);
        } else {
            pg = campusRepository.findAll(pr);
        }
        return PageResult.of((int) pg.getTotalElements(), pg.getContent());
    }

    public Campus addCampus(Long schoolId, Campus campus) {
        if (schoolId == null || !schoolRepository.existsById(schoolId)) throw new IllegalArgumentException("学校不存在");
        if (campus.getName() == null || campus.getName().trim().isEmpty()) throw new IllegalArgumentException("校区名称不能为空");
        if (campus.getCode() == null || campus.getCode().trim().isEmpty()) throw new IllegalArgumentException("校区编码不能为空");
        String name = campus.getName().trim();
        String code = campus.getCode().trim();
        if (campusRepository.existsBySchoolIdAndNameIgnoreCase(schoolId, name)) throw new IllegalStateException("该学校下校区名称已存在");
        if (campusRepository.existsByCode(code)) throw new IllegalStateException("校区编码已存在");
        campus.setSchoolId(schoolId);
        if (campus.getStatus() == null) campus.setStatus(1);
        campus.setName(name);
        campus.setCode(code);
        campus.setCreateTime(LocalDateTime.now());
        campus.setUpdateTime(LocalDateTime.now());
        return campusRepository.save(campus);
    }

    public Campus updateCampus(Long schoolId, Long campusId, Campus body) {
        Campus origin = campusRepository.findById(campusId).orElseThrow(() -> new IllegalArgumentException("校区不存在"));
        if (!origin.getSchoolId().equals(schoolId)) throw new IllegalArgumentException("校区不属于该学校");
        if (body.getSchoolId() != null && !body.getSchoolId().equals(origin.getSchoolId())) {
            throw new IllegalStateException("禁止变更校区所属学校");
        }
        if (body.getName() != null) {
            String newName = body.getName().trim();
            if (!newName.equalsIgnoreCase(origin.getName()) && campusRepository.existsBySchoolIdAndNameIgnoreCase(origin.getSchoolId(), newName)) {
                throw new IllegalStateException("该学校下校区名称已存在");
            }
            origin.setName(newName);
        }
        if (body.getCode() != null) {
            String newCode = body.getCode().trim();
            if (!newCode.equalsIgnoreCase(origin.getCode()) && campusRepository.existsByCode(newCode)) {
                throw new IllegalStateException("校区编码已存在");
            }
            origin.setCode(newCode);
        }
        if (body.getAddress() != null) origin.setAddress(body.getAddress());
        if (body.getStatus() != null) origin.setStatus(body.getStatus());
        origin.setUpdateTime(LocalDateTime.now());
        return campusRepository.save(origin);
    }

    public void deleteCampus(Long campusId) {
        Campus origin = campusRepository.findById(campusId).orElseThrow(() -> new IllegalArgumentException("校区不存在"));
        if (origin.getStatus() != null && origin.getStatus() == 1) throw new IllegalStateException("请先禁用该校区后再删除");
        campusRepository.deleteById(campusId);
    }

    public Campus enableCampus(Long schoolId, Long campusId) {
        Campus origin = campusRepository.findById(campusId).orElseThrow(() -> new IllegalArgumentException("校区不存在"));
        if (!origin.getSchoolId().equals(schoolId)) throw new IllegalArgumentException("校区不属于该学校");
        origin.setStatus(1);
        origin.setUpdateTime(LocalDateTime.now());
        return campusRepository.save(origin);
    }

    public Campus disableCampus(Long schoolId, Long campusId) {
        Campus origin = campusRepository.findById(campusId).orElseThrow(() -> new IllegalArgumentException("校区不存在"));
        if (!origin.getSchoolId().equals(schoolId)) throw new IllegalArgumentException("校区不属于该学校");
        origin.setStatus(0);
        origin.setUpdateTime(LocalDateTime.now());
        return campusRepository.save(origin);
    }

    // 学校参数配置
    public List<SchoolConfig> listConfigs(Long schoolId) {
        return schoolConfigRepository.findBySchoolId(schoolId);
    }

    @Transactional
    public SchoolConfig setConfig(Long schoolId, String key, String value) {
        Optional<SchoolConfig> cfgOpt = schoolConfigRepository.findBySchoolIdAndKey(schoolId, key);
        SchoolConfig cfg = cfgOpt.orElseGet(SchoolConfig::new);
        cfg.setSchoolId(schoolId);
        cfg.setKey(key);
        cfg.setValue(value);
        return schoolConfigRepository.save(cfg);
    }

    // 学校Logo
    public Optional<SchoolLogo> getLogo(Long schoolId) {
        return schoolLogoRepository.findById(schoolId);
    }

    public SchoolLogo setLogo(Long schoolId, String url) {
        SchoolLogo logo = schoolLogoRepository.findById(schoolId).orElseGet(SchoolLogo::new);
        logo.setSchoolId(schoolId);
        logo.setLogoUrl(url);
        logo.setUpdatedAt(LocalDateTime.now());
        return schoolLogoRepository.save(logo);
    }
}