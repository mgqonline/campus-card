package com.campus.card.admin.service;

import com.campus.card.admin.domain.Semester;
import com.campus.card.admin.repository.SemesterRepository;
import com.campus.card.common.result.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SemesterService {
    private final SemesterRepository semesterRepository;

    public SemesterService(SemesterRepository semesterRepository) {
        this.semesterRepository = semesterRepository;
    }

    public PageResult<Semester> pageList(int page, int size, String name, Long schoolId) {
        List<Semester> all = (schoolId != null) ? semesterRepository.findBySchoolId(schoolId) : semesterRepository.findAll();
        List<Semester> filtered = all.stream()
                .filter(s -> name == null || s.getName().toLowerCase().contains(name.toLowerCase()))
                .sorted(Comparator.comparing(Semester::getStartDate, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .collect(Collectors.toList());
        int total = filtered.size();
        int fromIndex = Math.max(0, (page - 1) * size);
        int toIndex = Math.min(total, fromIndex + size);
        List<Semester> records = fromIndex < toIndex ? filtered.subList(fromIndex, toIndex) : new ArrayList<>();
        return PageResult.of(total, records);
    }

    public Optional<Semester> findById(Long id) {
        return semesterRepository.findById(id);
    }

    @Transactional
    public Semester create(Semester body) {
        if (body.getSchoolId() == null) throw new IllegalArgumentException("schoolId 必填");
        if (body.getCode() == null) throw new IllegalArgumentException("code 必填");
        if (semesterRepository.existsBySchoolIdAndCode(body.getSchoolId(), body.getCode())) {
            throw new IllegalArgumentException("同一学校下学期编码已存在");
        }
        body.setId(null);
        body.setStatus(body.getStatus() == null ? 1 : body.getStatus());
        body.setCreateTime(LocalDateTime.now());
        body.setUpdateTime(LocalDateTime.now());
        return semesterRepository.save(body);
    }

    @Transactional
    public Semester update(Long id, Semester body) {
        Optional<Semester> os = semesterRepository.findById(id);
        if (!os.isPresent()) throw new IllegalArgumentException("学期不存在");
        Semester origin = os.get();
        // 编码唯一性校验（仅当修改了编码时）
        if (body.getCode() != null && !body.getCode().equals(origin.getCode())) {
            if (semesterRepository.existsBySchoolIdAndCode(origin.getSchoolId(), body.getCode())) {
                throw new IllegalArgumentException("同一学校下学期编码已存在");
            }
            origin.setCode(body.getCode());
        }
        if (body.getName() != null) origin.setName(body.getName());
        if (body.getStartDate() != null) origin.setStartDate(body.getStartDate());
        if (body.getEndDate() != null) origin.setEndDate(body.getEndDate());
        if (body.getStatus() != null) origin.setStatus(body.getStatus());
        origin.setUpdateTime(LocalDateTime.now());
        return semesterRepository.save(origin);
    }

    public void delete(Long id) {
        semesterRepository.deleteById(id);
    }

    @Transactional
    public Semester enable(Long id) {
        Optional<Semester> os = semesterRepository.findById(id);
        if (!os.isPresent()) throw new IllegalArgumentException("学期不存在");
        Semester s = os.get();
        s.setStatus(1);
        s.setUpdateTime(LocalDateTime.now());
        return semesterRepository.save(s);
    }

    @Transactional
    public Semester disable(Long id) {
        Optional<Semester> os = semesterRepository.findById(id);
        if (!os.isPresent()) throw new IllegalArgumentException("学期不存在");
        Semester s = os.get();
        s.setStatus(0);
        s.setUpdateTime(LocalDateTime.now());
        return semesterRepository.save(s);
    }

    @Transactional
    public Semester setCurrent(Long id) {
        Optional<Semester> os = semesterRepository.findById(id);
        if (!os.isPresent()) throw new IllegalArgumentException("学期不存在");
        Semester s = os.get();
        Long schoolId = s.getSchoolId();
        // 取消原当前学期
        semesterRepository.findBySchoolIdAndCurrentTrue(schoolId).ifPresent(curr -> {
            if (!curr.getId().equals(s.getId())) {
                curr.setCurrent(false);
                curr.setUpdateTime(LocalDateTime.now());
                semesterRepository.save(curr);
            }
        });
        s.setCurrent(true);
        s.setUpdateTime(LocalDateTime.now());
        return semesterRepository.save(s);
    }

    public Optional<Semester> getCurrent(Long schoolId) {
        return semesterRepository.findBySchoolIdAndCurrentTrue(schoolId);
    }

    public List<Semester> listHistory(Long schoolId, LocalDate start, LocalDate end) {
        if (schoolId == null) throw new IllegalArgumentException("schoolId 必填");
        if (start != null && end != null) {
            return semesterRepository.findBySchoolIdAndStartDateGreaterThanEqualAndEndDateLessThanEqual(schoolId, start, end);
        }
        return semesterRepository.findBySchoolId(schoolId);
    }
}