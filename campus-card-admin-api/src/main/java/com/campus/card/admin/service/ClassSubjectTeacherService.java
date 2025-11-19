package com.campus.card.admin.service;

import com.campus.card.admin.domain.ClassSubjectTeacher;
import com.campus.card.admin.domain.Clazz;
import com.campus.card.admin.domain.Subject;
import com.campus.card.admin.domain.Teacher;
import com.campus.card.admin.repository.ClassSubjectTeacherRepository;
import com.campus.card.admin.repository.ClazzRepository;
import com.campus.card.admin.repository.SubjectRepository;
import com.campus.card.admin.repository.TeacherRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ClassSubjectTeacherService {
    private final ClassSubjectTeacherRepository repository;
    private final ClazzRepository clazzRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherRepository teacherRepository;

    public ClassSubjectTeacherService(ClassSubjectTeacherRepository repository,
                                      ClazzRepository clazzRepository,
                                      SubjectRepository subjectRepository,
                                      TeacherRepository teacherRepository) {
        this.repository = repository;
        this.clazzRepository = clazzRepository;
        this.subjectRepository = subjectRepository;
        this.teacherRepository = teacherRepository;
    }

    public List<ClassSubjectTeacher> listByClass(Long classId) {
        return repository.findByClassId(classId);
    }

    @Transactional
    public ClassSubjectTeacher assign(Long classId, Long subjectId, Long teacherId) {
        Clazz c = clazzRepository.findById(classId).orElseThrow(() -> new IllegalArgumentException("班级不存在"));
        Subject s = subjectRepository.findById(subjectId).orElseThrow(() -> new IllegalArgumentException("学科不存在"));
        Teacher t = teacherRepository.findById(teacherId).orElseThrow(() -> new IllegalArgumentException("教师不存在"));
        Optional<ClassSubjectTeacher> existing = repository.findByClassIdAndSubjectId(classId, subjectId);
        ClassSubjectTeacher rel = existing.orElseGet(ClassSubjectTeacher::new);
        rel.setClassId(classId);
        rel.setSubjectId(subjectId);
        rel.setTeacherId(teacherId);
        if (rel.getId() == null) {
            rel.setCreateTime(LocalDateTime.now());
        }
        rel.setUpdateTime(LocalDateTime.now());
        return repository.save(rel);
    }

    @Transactional
    public void remove(Long id) {
        repository.deleteById(id);
    }

    @Transactional
    public void removeByClassAndSubject(Long classId, Long subjectId) {
        Optional<ClassSubjectTeacher> existing = repository.findByClassIdAndSubjectId(classId, subjectId);
        existing.ifPresent(rel -> repository.deleteById(rel.getId()));
    }
}