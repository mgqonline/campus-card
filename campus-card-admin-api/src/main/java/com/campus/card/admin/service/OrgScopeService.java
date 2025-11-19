package com.campus.card.admin.service;

import com.campus.card.admin.domain.DataScope;
import com.campus.card.admin.domain.User;
import com.campus.card.admin.domain.Clazz;
import com.campus.card.admin.repository.DataScopeRepository;
import com.campus.card.admin.repository.ClazzRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrgScopeService {
    private final DataScopeRepository dataScopeRepository;
    private final ClazzRepository clazzRepository;

    public OrgScopeService(DataScopeRepository dataScopeRepository, ClazzRepository clazzRepository) {
        this.dataScopeRepository = dataScopeRepository;
        this.clazzRepository = clazzRepository;
    }

    public static class EffectiveScope {
        public Set<Long> schoolIds = new HashSet<>();
        public Set<Long> gradeIds = new HashSet<>();
        public Set<Long> classIds = new HashSet<>();
        public Set<Long> studentIds = new HashSet<>();
        public boolean personalOnly;
    }

    private Set<Long> parseIds(String csv) {
        if (csv == null || csv.trim().isEmpty()) return Collections.emptySet();
        Set<Long> set = new HashSet<>();
        for (String s : csv.split(",")) {
            try {
                String t = s.trim();
                if (!t.isEmpty()) set.add(Long.parseLong(t));
            } catch (Exception ignored) {}
        }
        return set;
    }

    public EffectiveScope resolveForUser(User user) {
        EffectiveScope eff = new EffectiveScope();
        if (user == null) return eff;
        // 用户级配置
        List<DataScope> userScopes = dataScopeRepository.findBySubjectTypeAndSubjectId("USER", user.getId());
        // 角色级配置
        Set<Long> roleIds = Optional.ofNullable(user.getRoles()).orElse(Collections.emptySet()).stream()
                .filter(Objects::nonNull)
                .map(r -> r.getId())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        List<DataScope> roleScopes = new ArrayList<>();
        for (Long rid : roleIds) {
            roleScopes.addAll(dataScopeRepository.findBySubjectTypeAndSubjectId("ROLE", rid));
        }
        // 合并逻辑：并集
        for (DataScope ds : concat(userScopes, roleScopes)) {
            String type = Optional.ofNullable(ds.getScopeType()).orElse("");
            if ("SCHOOL".equalsIgnoreCase(type)) {
                eff.schoolIds.addAll(parseIds(ds.getSchoolIds()));
            } else if ("GRADE".equalsIgnoreCase(type)) {
                eff.gradeIds.addAll(parseIds(ds.getGradeIds()));
            } else if ("CLASS".equalsIgnoreCase(type)) {
                eff.classIds.addAll(parseIds(ds.getClassIds()));
            } else if ("PERSONAL".equalsIgnoreCase(type)) {
                eff.studentIds.addAll(parseIds(ds.getStudentIds()));
                eff.personalOnly = true;
            }
        }
        // 将学校/年级范围转换成班级范围，便于应用到记录查询
        if (!eff.gradeIds.isEmpty()) {
            for (Long gid : eff.gradeIds) {
                List<Clazz> byGrade = clazzRepository.findByGradeId(gid);
                eff.classIds.addAll(byGrade.stream().map(Clazz::getId).collect(Collectors.toSet()));
            }
        }
        if (!eff.schoolIds.isEmpty()) {
            for (Long sid : eff.schoolIds) {
                List<Clazz> bySchool = clazzRepository.findBySchoolId(sid);
                eff.classIds.addAll(bySchool.stream().map(Clazz::getId).collect(Collectors.toSet()));
            }
        }
        return eff;
    }

    private List<DataScope> concat(List<DataScope> a, List<DataScope> b) {
        List<DataScope> list = new ArrayList<>();
        if (a != null) list.addAll(a);
        if (b != null) list.addAll(b);
        return list;
    }
}