package com.campus.card.wechat.bootstrap;

import com.campus.card.wechat.model.AttendanceEvent;
import com.campus.card.wechat.model.Student;
import com.campus.card.wechat.repository.AttendanceEventRepository;
import com.campus.card.wechat.repository.StudentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * 启动时初始化示例数据：班级 301 的学生和最近 7 天的考勤事件
 */
@Component
public class AttendanceDataSeeder implements CommandLineRunner {
    private final StudentRepository studentRepository;
    private final AttendanceEventRepository attendanceRepository;

    public AttendanceDataSeeder(StudentRepository studentRepository,
                                AttendanceEventRepository attendanceRepository) {
        this.studentRepository = studentRepository;
        this.attendanceRepository = attendanceRepository;
    }

    @Override
    public void run(String... args) {
        try {
            seedStudents();
            seedAttendanceEvents();
        } catch (Exception ex) {
            // 保守处理：初始化失败不影响服务启动
        }
    }

    private void seedStudents() {
        // 若班级 301 一个学生都没有，则注入示例学生
        boolean hasClass301 = studentRepository.findAll().stream().anyMatch(s -> Objects.equals(s.getClassId(), 301L));
        if (hasClass301) return;

        List<Student> list = new ArrayList<>();
        list.add(newStudent(2001L, "张三", 301L, "三年级", "62220001", "已采集"));
        list.add(newStudent(2002L, "李四", 301L, "三年级", "62220002", "已采集"));
        list.add(newStudent(2003L, "王五", 301L, "三年级", "62220003", "未采集"));
        list.add(newStudent(2004L, "赵六", 301L, "三年级", "62220004", "已采集"));
        list.add(newStudent(2005L, "钱七", 301L, "三年级", "62220005", "已采集"));
        studentRepository.saveAll(list);
    }

    private Student newStudent(Long id, String name, Long classId, String grade, String cardNo, String faceStatus) {
        Student s = new Student();
        s.setId(id);
        s.setName(name);
        s.setClassId(classId);
        s.setGrade(grade);
        s.setCardNo(cardNo);
        s.setFaceStatus(faceStatus);
        s.setStudentNo("S" + id);
        return s;
    }

    private void seedAttendanceEvents() {
        // 为班级 301 的学生补充最近 7 天的进校/离校事件（若已存在则跳过）
        List<Student> students = studentRepository.findAll();
        List<Student> classStudents = new ArrayList<>();
        for (Student s : students) {
            if (Objects.equals(s.getClassId(), 301L)) {
                classStudents.add(s);
            }
        }
        if (classStudents.isEmpty()) return;

        LocalDate today = LocalDate.now();
        for (Student s : classStudents) {
            for (int i = 0; i < 7; i++) {
                LocalDate day = today.minusDays(i);
                LocalDateTime inTime = day.atTime(LocalTime.of(8, 0)).plusMinutes((i * 7 + s.getId()) % 20); // 轻微扰动
                LocalDateTime outTime = day.atTime(LocalTime.of(17, 0)).minusMinutes((i * 5 + s.getId()) % 15);

                // 若该日已存在进校事件则跳过生成，避免重复
                org.springframework.data.domain.Page<AttendanceEvent> page = attendanceRepository
                        .findByChildIdAndTimeBetweenOrderByTimeDesc(s.getId(), day.atStartOfDay(), day.atTime(LocalTime.MAX), org.springframework.data.domain.PageRequest.of(0, 1));
                if (!page.getContent().isEmpty()) continue;

                AttendanceEvent in = new AttendanceEvent();
                in.setChildId(s.getId());
                in.setType("进校");
                in.setGate("南门");
                in.setTime(inTime);
                in.setPhotoUrl(null);
                // 简单规则：超过 8:10 视为迟到
                boolean late = inTime.isAfter(day.atTime(8, 10));
                in.setLate(late);
                in.setEarlyLeave(false);

                AttendanceEvent out = new AttendanceEvent();
                out.setChildId(s.getId());
                out.setType("离校");
                out.setGate("南门");
                out.setTime(outTime);
                out.setPhotoUrl(null);
                // 简单规则：早于 16:50 视为早退
                boolean early = outTime.isBefore(day.atTime(16, 50));
                out.setEarlyLeave(early);
                out.setLate(false);

                attendanceRepository.save(in);
                attendanceRepository.save(out);
            }
        }
    }
}