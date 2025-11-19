package com.campus.card.admin.service;

import com.campus.card.admin.domain.Card;
import com.campus.card.admin.domain.CardTx;
import com.campus.card.admin.domain.Clazz;
import com.campus.card.admin.domain.StudentInfo;
import com.campus.card.admin.repository.CardRepository;
import com.campus.card.admin.repository.CardTxRepository;
import com.campus.card.admin.repository.ClazzRepository;
import com.campus.card.admin.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {
    private final CardRepository cardRepository;
    private final CardTxRepository cardTxRepository;
    private final StudentRepository studentRepository;
    private final ClazzRepository clazzRepository;

    public ReportService(CardRepository cardRepository,
                         CardTxRepository cardTxRepository,
                         StudentRepository studentRepository,
                         ClazzRepository clazzRepository) {
        this.cardRepository = cardRepository;
        this.cardTxRepository = cardTxRepository;
        this.studentRepository = studentRepository;
        this.clazzRepository = clazzRepository;
    }

    public StatsResp dailyConsume(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        List<CardTx> txs = cardTxRepository.findByTypeAndOccurredAtBetweenOrderByOccurredAtAsc("CONSUME", start, end);
        return aggregateStats(txs);
    }

    public MonthlyResp monthlyConsume(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        List<CardTx> txs = cardTxRepository.findByTypeAndOccurredAtBetweenOrderByOccurredAtAsc("CONSUME", start, end);
        Map<Integer, List<CardTx>> byDay = new HashMap<>();
        for (CardTx tx : txs) {
            int d = tx.getOccurredAt().getDayOfMonth();
            byDay.computeIfAbsent(d, k -> new ArrayList<>()).add(tx);
        }
        List<DayStat> series = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        int count = 0;
        for (int d = 1; d <= startDate.lengthOfMonth(); d++) {
            List<CardTx> list = byDay.getOrDefault(d, Collections.emptyList());
            StatsResp s = aggregateStats(list);
            series.add(new DayStat(d, s.getTotalAmount(), s.getTxCount()));
            total = total.add(s.getTotalAmount());
            count += s.getTxCount();
        }
        MonthlyResp resp = new MonthlyResp();
        resp.setYear(year);
        resp.setMonth(month);
        resp.setTotalAmount(total);
        resp.setTxCount(count);
        resp.setSeries(series);
        return resp;
    }

    public ClassConsumeResp classConsume(Long classId, LocalDate startDate, LocalDate endDate) {
        if (classId == null) throw new IllegalArgumentException("classId 必填");
        Clazz clazz = clazzRepository.findById(classId).orElse(null);
        List<StudentInfo> students = studentRepository.findByClassId(classId);
        Set<String> studentNos = students.stream().map(StudentInfo::getStudentNo).filter(Objects::nonNull).collect(Collectors.toSet());
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : LocalDate.now().minusDays(30).atStartOfDay();
        LocalDateTime end = endDate != null ? endDate.atTime(LocalTime.MAX) : LocalDateTime.now();
        BigDecimal total = BigDecimal.ZERO;
        int count = 0;
        List<StudentStat> breakdown = new ArrayList<>();
        for (StudentInfo s : students) {
            String sno = s.getStudentNo();
            if (sno == null) continue;
            List<Card> cards = cardRepository.findByHolderTypeAndHolderId("STUDENT", sno);
            BigDecimal personTotal = BigDecimal.ZERO;
            int personCount = 0;
            for (Card card : cards) {
                List<CardTx> txs = cardTxRepository.findByCardNoAndOccurredAtBetweenOrderByOccurredAtDesc(card.getCardNo(), start, end);
                for (CardTx tx : txs) {
                    if (!"CONSUME".equalsIgnoreCase(tx.getType())) continue;
                    personTotal = personTotal.add(tx.getAmount().abs());
                    personCount++;
                }
            }
            if (personCount > 0) {
                breakdown.add(new StudentStat(s.getId(), s.getName(), s.getStudentNo(), personTotal, personCount));
            }
            total = total.add(personTotal);
            count += personCount;
        }
        breakdown.sort((a, b) -> b.getTotalAmount().compareTo(a.getTotalAmount()));
        ClassConsumeResp resp = new ClassConsumeResp();
        resp.setClassId(classId);
        resp.setClassName(clazz != null ? clazz.getName() : null);
        resp.setStudentCount(students.size());
        resp.setTotalAmount(total);
        resp.setTxCount(count);
        resp.setBreakdown(breakdown);
        return resp;
    }

    public PersonalResp personalConsume(Long studentId, String studentNo, LocalDate startDate, LocalDate endDate) {
        StudentInfo s;
        if (studentId != null) {
            s = studentRepository.findById(studentId).orElseThrow(() -> new IllegalArgumentException("学生不存在"));
        } else if (studentNo != null && !studentNo.isEmpty()) {
            s = studentRepository.findByStudentNo(studentNo).orElseThrow(() -> new IllegalArgumentException("学生不存在"));
        } else {
            throw new IllegalArgumentException("studentId 或 studentNo 必填其一");
        }
        String sno = s.getStudentNo();
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : LocalDate.now().minusDays(30).atStartOfDay();
        LocalDateTime end = endDate != null ? endDate.atTime(LocalTime.MAX) : LocalDateTime.now();
        List<Card> cards = cardRepository.findByHolderTypeAndHolderId("STUDENT", sno);
        Map<LocalDate, List<CardTx>> daily = new TreeMap<>();
        BigDecimal total = BigDecimal.ZERO;
        int count = 0;
        for (Card card : cards) {
            List<CardTx> txs = cardTxRepository.findByCardNoAndOccurredAtBetweenOrderByOccurredAtDesc(card.getCardNo(), start, end);
            for (CardTx tx : txs) {
                if (!"CONSUME".equalsIgnoreCase(tx.getType())) continue;
                total = total.add(tx.getAmount().abs());
                count++;
                LocalDate d = tx.getOccurredAt().toLocalDate();
                daily.computeIfAbsent(d, k -> new ArrayList<>()).add(tx);
            }
        }
        List<DayStat> series = daily.entrySet().stream()
                .map(e -> {
                    StatsResp s1 = aggregateStats(e.getValue());
                    return new DayStat(e.getKey().getDayOfMonth(), s1.getTotalAmount(), s1.getTxCount());
                })
                .collect(Collectors.toList());
        PersonalResp resp = new PersonalResp();
        resp.setStudentId(s.getId());
        resp.setStudentNo(s.getStudentNo());
        resp.setStudentName(s.getName());
        resp.setTotalAmount(total);
        resp.setTxCount(count);
        resp.setSeries(series);
        return resp;
    }

    public List<RankingItem> ranking(LocalDate startDate, LocalDate endDate, int limit) {
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : LocalDate.now().minusDays(30).atStartOfDay();
        LocalDateTime end = endDate != null ? endDate.atTime(LocalTime.MAX) : LocalDateTime.now();
        List<CardTx> txs = cardTxRepository.findByTypeAndOccurredAtBetweenOrderByOccurredAtAsc("CONSUME", start, end);
        Map<String, BigDecimal> byCard = new HashMap<>();
        for (CardTx tx : txs) {
            byCard.merge(tx.getCardNo(), tx.getAmount().abs(), BigDecimal::add);
        }
        Map<Long, RankingItem> byStudent = new HashMap<>();
        for (Map.Entry<String, BigDecimal> e : byCard.entrySet()) {
            String cardNo = e.getKey();
            BigDecimal amt = e.getValue();
            Optional<Card> oc = cardRepository.findByCardNo(cardNo);
            if (!oc.isPresent()) continue;
            Card card = oc.get();
            if (!"STUDENT".equalsIgnoreCase(card.getHolderType())) continue;
            String sno = card.getHolderId();
            Optional<StudentInfo> os = studentRepository.findByStudentNo(sno);
            if (!os.isPresent()) continue;
            StudentInfo si = os.get();
            RankingItem item = byStudent.computeIfAbsent(si.getId(), k -> new RankingItem(si.getId(), si.getName(), si.getStudentNo(), BigDecimal.ZERO));
            item.setTotalAmount(item.getTotalAmount().add(amt));
        }
        List<RankingItem> list = new ArrayList<>(byStudent.values());
        list.sort((a, b) -> b.getTotalAmount().compareTo(a.getTotalAmount()));
        return list.subList(0, Math.min(limit <= 0 ? 10 : limit, list.size()));
    }

    public List<DayStat> trend(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : LocalDate.now().minusDays(30).atStartOfDay();
        LocalDateTime end = endDate != null ? endDate.atTime(LocalTime.MAX) : LocalDateTime.now();
        List<CardTx> txs = cardTxRepository.findByTypeAndOccurredAtBetweenOrderByOccurredAtAsc("CONSUME", start, end);
        Map<LocalDate, List<CardTx>> daily = new TreeMap<>();
        for (CardTx tx : txs) {
            LocalDate d = tx.getOccurredAt().toLocalDate();
            daily.computeIfAbsent(d, k -> new ArrayList<>()).add(tx);
        }
        List<DayStat> series = new ArrayList<>();
        for (Map.Entry<LocalDate, List<CardTx>> e : daily.entrySet()) {
            StatsResp s = aggregateStats(e.getValue());
            series.add(new DayStat(e.getKey().getDayOfMonth(), s.getTotalAmount(), s.getTxCount()));
        }
        return series;
    }

    private StatsResp aggregateStats(List<CardTx> txs) {
        BigDecimal total = BigDecimal.ZERO;
        int count = 0;
        for (CardTx tx : txs) {
            if (!"CONSUME".equalsIgnoreCase(tx.getType())) continue;
            total = total.add(tx.getAmount().abs());
            count++;
        }
        StatsResp resp = new StatsResp();
        resp.setTotalAmount(total);
        resp.setTxCount(count);
        return resp;
    }

    public static class StatsResp {
        private BigDecimal totalAmount;
        private int txCount;
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        public int getTxCount() { return txCount; }
        public void setTxCount(int txCount) { this.txCount = txCount; }
    }

    public static class DayStat {
        private int day;
        private BigDecimal totalAmount;
        private int txCount;
        public DayStat() {}
        public DayStat(int day, BigDecimal totalAmount, int txCount) { this.day = day; this.totalAmount = totalAmount; this.txCount = txCount; }
        public int getDay() { return day; }
        public void setDay(int day) { this.day = day; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        public int getTxCount() { return txCount; }
        public void setTxCount(int txCount) { this.txCount = txCount; }
    }

    public static class MonthlyResp extends StatsResp {
        private int year;
        private int month;
        private List<DayStat> series;
        public int getYear() { return year; }
        public void setYear(int year) { this.year = year; }
        public int getMonth() { return month; }
        public void setMonth(int month) { this.month = month; }
        public List<DayStat> getSeries() { return series; }
        public void setSeries(List<DayStat> series) { this.series = series; }
    }

    public static class ClassConsumeResp extends StatsResp {
        private Long classId;
        private String className;
        private int studentCount;
        private List<StudentStat> breakdown;
        public Long getClassId() { return classId; }
        public void setClassId(Long classId) { this.classId = classId; }
        public String getClassName() { return className; }
        public void setClassName(String className) { this.className = className; }
        public int getStudentCount() { return studentCount; }
        public void setStudentCount(int studentCount) { this.studentCount = studentCount; }
        public List<StudentStat> getBreakdown() { return breakdown; }
        public void setBreakdown(List<StudentStat> breakdown) { this.breakdown = breakdown; }
    }

    public static class StudentStat {
        private Long studentId;
        private String studentName;
        private String studentNo;
        private BigDecimal totalAmount;
        private int txCount;
        public StudentStat() {}
        public StudentStat(Long studentId, String studentName, String studentNo, BigDecimal totalAmount, int txCount) {
            this.studentId = studentId; this.studentName = studentName; this.studentNo = studentNo; this.totalAmount = totalAmount; this.txCount = txCount;
        }
        public Long getStudentId() { return studentId; }
        public void setStudentId(Long studentId) { this.studentId = studentId; }
        public String getStudentName() { return studentName; }
        public void setStudentName(String studentName) { this.studentName = studentName; }
        public String getStudentNo() { return studentNo; }
        public void setStudentNo(String studentNo) { this.studentNo = studentNo; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        public int getTxCount() { return txCount; }
        public void setTxCount(int txCount) { this.txCount = txCount; }
    }

    public static class PersonalResp extends StatsResp {
        private Long studentId;
        private String studentNo;
        private String studentName;
        private List<DayStat> series;
        public Long getStudentId() { return studentId; }
        public void setStudentId(Long studentId) { this.studentId = studentId; }
        public String getStudentNo() { return studentNo; }
        public void setStudentNo(String studentNo) { this.studentNo = studentNo; }
        public String getStudentName() { return studentName; }
        public void setStudentName(String studentName) { this.studentName = studentName; }
        public List<DayStat> getSeries() { return series; }
        public void setSeries(List<DayStat> series) { this.series = series; }
    }

    public static class RankingItem {
        private Long studentId;
        private String studentName;
        private String studentNo;
        private BigDecimal totalAmount;
        public RankingItem() {}
        public RankingItem(Long studentId, String studentName, String studentNo, BigDecimal totalAmount) {
            this.studentId = studentId; this.studentName = studentName; this.studentNo = studentNo; this.totalAmount = totalAmount;
        }
        public Long getStudentId() { return studentId; }
        public void setStudentId(Long studentId) { this.studentId = studentId; }
        public String getStudentName() { return studentName; }
        public void setStudentName(String studentName) { this.studentName = studentName; }
        public String getStudentNo() { return studentNo; }
        public void setStudentNo(String studentNo) { this.studentNo = studentNo; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    }
}