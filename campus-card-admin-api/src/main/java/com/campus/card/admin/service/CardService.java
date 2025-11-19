package com.campus.card.admin.service;

import com.campus.card.admin.domain.Card;
import com.campus.card.admin.domain.CardType;
import com.campus.card.admin.domain.CardTx;
import com.campus.card.admin.repository.CardRepository;
import com.campus.card.admin.repository.CardTypeRepository;
import com.campus.card.admin.repository.CardTxRepository;
import com.campus.card.admin.repository.StudentRepository;
import com.campus.card.admin.repository.TeacherRepository;
import com.campus.card.common.result.PageResult;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CardService {
    private final CardRepository cardRepository;
    private final CardTypeRepository cardTypeRepository;
    private final CardTxRepository cardTxRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;

    public CardService(CardRepository cardRepository, CardTypeRepository cardTypeRepository, CardTxRepository cardTxRepository,
                       StudentRepository studentRepository, TeacherRepository teacherRepository) {
        this.cardRepository = cardRepository;
        this.cardTypeRepository = cardTypeRepository;
        this.cardTxRepository = cardTxRepository;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
    }

    // 卡种管理
    public List<CardType> listCardTypes() {
        return cardTypeRepository.findAll();
    }

    public CardType createCardType(CardType cardType) {
        return cardTypeRepository.save(cardType);
    }

    public CardType updateCardType(Long id, CardType body) {
        body.setId(id);
        return cardTypeRepository.save(body);
    }

    public void deleteCardType(Long id) {
        cardTypeRepository.deleteById(id);
    }

    // 卡片管理
    public PageResult<Card> pageList(int page, int size, String cardNo, String holderType, String holderId, String status) {
        List<Card> list = cardRepository.findAll();
        if (cardNo != null && !cardNo.isEmpty()) {
            list = list.stream().filter(c -> c.getCardNo() != null && c.getCardNo().contains(cardNo)).collect(java.util.stream.Collectors.toList());
        }
        if (holderType != null && !holderType.isEmpty()) {
            list = list.stream().filter(c -> holderType.equalsIgnoreCase(c.getHolderType())).collect(java.util.stream.Collectors.toList());
        }
        if (holderId != null && !holderId.isEmpty()) {
            list = list.stream().filter(c -> holderId.equals(c.getHolderId())).collect(java.util.stream.Collectors.toList());
        }
        if (status != null && !status.isEmpty()) {
            list = list.stream().filter(c -> status.equalsIgnoreCase(c.getStatus())).collect(java.util.stream.Collectors.toList());
        }
        int total = list.size();
        int from = Math.max(0, (page - 1) * size);
        int to = Math.min(total, from + size);
        List<Card> pageList = from >= total ? java.util.Collections.emptyList() : list.subList(from, to);
        return PageResult.of(total, pageList);
    }

    public Card issueCard(IssueCardReq req) {
        Card card = new Card();
        card.setCardNo(generateCardNo());
        card.setTypeId(req.getTypeId());
        card.setHolderType(req.getHolderType());
        card.setHolderId(req.getHolderId());
        card.setBalance(req.getInitialBalance() != null ? req.getInitialBalance() : BigDecimal.ZERO);
        card.setStatus("ACTIVE");
        card.setCreatedAt(LocalDateTime.now());
        card.setExpireAt(req.getExpireAt());
        Card saved = cardRepository.save(card);
        if (req.getInitialBalance() != null && req.getInitialBalance().compareTo(BigDecimal.ZERO) > 0) {
            CardTx tx = new CardTx();
            tx.setCardNo(saved.getCardNo());
            tx.setType("RECHARGE");
            tx.setAmount(req.getInitialBalance());
            tx.setBalanceAfter(req.getInitialBalance());
            tx.setMerchant("系统发卡");
            tx.setOccurredAt(LocalDateTime.now());
            tx.setNote(req.getNote());
            cardTxRepository.save(tx);
        }
        return saved;
    }

    public BalanceInfo getBalance(String cardNo) {
        Optional<Card> oc = cardRepository.findByCardNo(cardNo);
        if (!oc.isPresent()) {
            throw new IllegalArgumentException("卡片不存在");
        }
        Card card = oc.get();
        BalanceInfo info = new BalanceInfo();
        info.setCardNo(cardNo);
        info.setBalance(card.getBalance());
        info.setStatus(card.getStatus());
        info.setHolderType(card.getHolderType());
        info.setHolderId(card.getHolderId());
        String holderName = null;
        if ("STUDENT".equalsIgnoreCase(card.getHolderType())) {
            holderName = studentRepository.findByStudentNo(card.getHolderId()).map(s -> s.getName()).orElse(null);
        } else if ("TEACHER".equalsIgnoreCase(card.getHolderType())) {
            holderName = teacherRepository.findByTeacherNo(card.getHolderId()).map(t -> t.getName()).orElse(null);
        }
        info.setHolderName(holderName);
        String cardTypeName = card.getTypeId() != null ? cardTypeRepository.findById(card.getTypeId()).map(CardType::getName).orElse(null) : null;
        info.setCardTypeName(cardTypeName);
        List<CardTx> list = cardTxRepository.findByCardNoOrderByOccurredAtDesc(cardNo);
        info.setUpdatedAt(list != null && !list.isEmpty() ? list.get(0).getOccurredAt() : card.getCreatedAt());
        return info;
    }

    public List<CardTx> getTransactions(String cardNo, String type, LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime != null && endTime != null) {
            return cardTxRepository.findByCardNoAndOccurredAtBetweenOrderByOccurredAtDesc(cardNo, startTime, endTime);
        }
        if (type != null && !type.isEmpty()) {
            return cardTxRepository.findByCardNoAndTypeOrderByOccurredAtDesc(cardNo, type);
        }
        return cardTxRepository.findByCardNoOrderByOccurredAtDesc(cardNo);
    }

    private String generateCardNo() {
        return "C" + System.currentTimeMillis() + String.format("%03d", (int)(Math.random() * 1000));
    }

    @Data
    public static class IssueCardReq {
        private Long typeId;
        private String holderType;
        private String holderId;
        private BigDecimal initialBalance;
        private String note;
        private LocalDateTime expireAt; // 临时卡可选过期时间
    }

    @Data
    public static class BalanceInfo {
        private String cardNo;
        private BigDecimal balance;
        private String status;
        private String holderType;
        private String holderId;
        private String holderName;
        private String cardTypeName;
        private LocalDateTime updatedAt;
    }

    @Data
    public static class RechargeReq {
        private String cardNo;
        private BigDecimal amount;
        private String method;
        private String note;
    }

    @Data
    public static class RechargeResult {
        private boolean success;
        private BigDecimal balance;
    }

    public RechargeResult recharge(RechargeReq req) {
        if (req.getAmount() == null || req.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("充值金额必须为正数");
        }
        Optional<Card> oc = cardRepository.findByCardNo(req.getCardNo());
        if (!oc.isPresent()) {
            throw new IllegalArgumentException("卡片不存在");
        }
        Card card = oc.get();
        if (!"ACTIVE".equalsIgnoreCase(card.getStatus())) {
            throw new IllegalStateException("当前卡片状态不允许充值");
        }
        BigDecimal newBalance = (card.getBalance() != null ? card.getBalance() : BigDecimal.ZERO).add(req.getAmount());
        card.setBalance(newBalance);
        cardRepository.save(card);
        CardTx tx = new CardTx();
        tx.setCardNo(card.getCardNo());
        tx.setType("RECHARGE");
        tx.setAmount(req.getAmount());
        tx.setBalanceAfter(newBalance);
        tx.setMerchant(req.getMethod() != null ? req.getMethod() : "UNKNOWN");
        tx.setOccurredAt(LocalDateTime.now());
        tx.setNote(req.getNote());
        cardTxRepository.save(tx);
        RechargeResult result = new RechargeResult();
        result.setSuccess(true);
        result.setBalance(newBalance);
        return result;
    }

    @Data
    public static class ConsumeReq {
        private String cardNo;
        private BigDecimal amount; // 正数
        private String merchant;
        private String note;
    }

    @Data
    public static class ConsumeResult {
        private boolean success;
        private BigDecimal balance;
    }

    public ConsumeResult consume(ConsumeReq req) {
        if (req.getAmount() == null || req.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("消费金额必须为正数");
        }
        Optional<Card> oc = cardRepository.findByCardNo(req.getCardNo());
        if (!oc.isPresent()) {
            throw new IllegalArgumentException("卡片不存在");
        }
        Card card = oc.get();
        if (!"ACTIVE".equalsIgnoreCase(card.getStatus())) {
            throw new IllegalStateException("当前卡片状态不允许消费");
        }
        BigDecimal current = card.getBalance() != null ? card.getBalance() : BigDecimal.ZERO;
        if (current.compareTo(req.getAmount()) < 0) {
            throw new IllegalStateException("余额不足");
        }
        BigDecimal newBalance = current.subtract(req.getAmount());
        card.setBalance(newBalance);
        cardRepository.save(card);
        CardTx tx = new CardTx();
        tx.setCardNo(card.getCardNo());
        tx.setType("CONSUME");
        tx.setAmount(req.getAmount().negate()); // 消费记录为负值
        tx.setBalanceAfter(newBalance);
        tx.setMerchant(req.getMerchant());
        tx.setOccurredAt(LocalDateTime.now());
        tx.setNote(req.getNote());
        cardTxRepository.save(tx);
        ConsumeResult result = new ConsumeResult();
        result.setSuccess(true);
        result.setBalance(newBalance);
        return result;
    }

    @Data
    public static class LossReq {
        private String cardNo;
    }

    // 新增：冻结/解冻请求
    @Data
    public static class FreezeReq {
        private String cardNo;
        private String reason;
    }

    @Data
    public static class UnfreezeReq {
        private String cardNo;
    }

    public void reportLoss(String cardNo) {
        Optional<Card> oc = cardRepository.findByCardNo(cardNo);
        if (!oc.isPresent()) {
            throw new IllegalArgumentException("卡片不存在");
        }
        Card card = oc.get();
        if ("CANCELLED".equalsIgnoreCase(card.getStatus())) {
            throw new IllegalStateException("已注销卡不可挂失");
        }
        card.setStatus("LOST");
        cardRepository.save(card);
    }

    // 新增：冻结
    public void freeze(String cardNo) {
        Optional<Card> oc = cardRepository.findByCardNo(cardNo);
        if (!oc.isPresent()) {
            throw new IllegalArgumentException("卡片不存在");
        }
        Card card = oc.get();
        if ("CANCELLED".equalsIgnoreCase(card.getStatus())) {
            throw new IllegalStateException("已注销卡不可冻结");
        }
        // LOST 状态也允许冻结，统一置为 FROZEN
        card.setStatus("FROZEN");
        cardRepository.save(card);
    }

    public void unloss(String cardNo) {
        Optional<Card> oc = cardRepository.findByCardNo(cardNo);
        if (!oc.isPresent()) {
            throw new IllegalArgumentException("卡片不存在");
        }
        Card card = oc.get();
        if (!"LOST".equalsIgnoreCase(card.getStatus())) {
            throw new IllegalStateException("仅挂失状态可解挂");
        }
        card.setStatus("ACTIVE");
        cardRepository.save(card);
    }

    // 新增：解冻
    public void unfreeze(String cardNo) {
        Optional<Card> oc = cardRepository.findByCardNo(cardNo);
        if (!oc.isPresent()) {
            throw new IllegalArgumentException("卡片不存在");
        }
        Card card = oc.get();
        if (!"FROZEN".equalsIgnoreCase(card.getStatus())) {
            throw new IllegalStateException("仅冻结状态可解冻");
        }
        card.setStatus("ACTIVE");
        cardRepository.save(card);
    }

    @Data
    public static class CancelReq {
        private String cardNo;
        private boolean refund; // 是否退还余额
        private String note;
    }

    public void cancel(CancelReq req) {
        Optional<Card> oc = cardRepository.findByCardNo(req.getCardNo());
        if (!oc.isPresent()) {
            throw new IllegalArgumentException("卡片不存在");
        }
        Card card = oc.get();
        if ("CANCELLED".equalsIgnoreCase(card.getStatus())) {
            return; // 已注销
        }
        BigDecimal current = card.getBalance() != null ? card.getBalance() : BigDecimal.ZERO;
        card.setStatus("CANCELLED");
        card.setBalance(BigDecimal.ZERO);
        cardRepository.save(card);
        if (req.isRefund() && current.compareTo(BigDecimal.ZERO) > 0) {
            CardTx tx = new CardTx();
            tx.setCardNo(card.getCardNo());
            tx.setType("REFUND");
            tx.setAmount(current);
            tx.setBalanceAfter(BigDecimal.ZERO);
            tx.setMerchant("CARD_CENTER");
            tx.setOccurredAt(LocalDateTime.now());
            tx.setNote(req.getNote() != null ? req.getNote() : "注销退款");
            cardTxRepository.save(tx);
        }
    }

    @Data
    public static class BatchIssueReq {
        private List<IssueCardReq> items;
    }

    @Data
    public static class BatchIssueResult {
        private boolean success;
        private int count;
        private List<String> cardNos;
    }

    public BatchIssueResult batchIssue(BatchIssueReq req) {
        java.util.ArrayList<String> nos = new java.util.ArrayList<>();
        List<IssueCardReq> items = (req.getItems() != null ? req.getItems() : java.util.Collections.<IssueCardReq>emptyList());
        for (IssueCardReq item : items) {
            Card c = issueCard(item);
            nos.add(c.getCardNo());
        }
        BatchIssueResult r = new BatchIssueResult();
        r.setSuccess(true);
        r.setCount(nos.size());
        r.setCardNos(nos);
        return r;
    }

    @Data
    public static class ReplaceReq {
        private String oldCardNo;
        private String changeType; // SUPPLEMENT 或 TYPE_CHANGE
        private Long newCardTypeId; // 对于类型变更时必填
        private BigDecimal fee; // 工本费，可选
        private String note;
    }

    @Data
    public static class ReplaceResult {
        private boolean success;
        private String newCardNo;
    }

    public ReplaceResult replaceCard(ReplaceReq req) {
        Optional<Card> oc = cardRepository.findByCardNo(req.getOldCardNo());
        if (!oc.isPresent()) {
            throw new IllegalArgumentException("原卡不存在");
        }
        Card old = oc.get();
        if (!"ACTIVE".equalsIgnoreCase(old.getStatus()) && !"LOST".equalsIgnoreCase(old.getStatus())) {
            throw new IllegalStateException("当前卡片状态不支持换/补卡");
        }
        Card newCard = new Card();
        newCard.setCardNo(generateCardNo());
        Long targetTypeId = ("TYPE_CHANGE".equalsIgnoreCase(req.getChangeType()) && req.getNewCardTypeId() != null)
                ? req.getNewCardTypeId() : old.getTypeId();
        newCard.setTypeId(targetTypeId);
        newCard.setHolderType(old.getHolderType());
        newCard.setHolderId(old.getHolderId());
        newCard.setStatus("ACTIVE");
        newCard.setCreatedAt(LocalDateTime.now());
        BigDecimal carry = old.getBalance() != null ? old.getBalance() : BigDecimal.ZERO;
        BigDecimal fee = req.getFee() != null ? req.getFee() : BigDecimal.ZERO;
        if (fee.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("费用不能为负数");
        }
        BigDecimal newBalance = carry.subtract(fee);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("余额不足以扣除工本费");
        }
        newCard.setBalance(newBalance);
        Card savedNew = cardRepository.save(newCard);
        old.setStatus("CANCELLED");
        old.setBalance(BigDecimal.ZERO);
        cardRepository.save(old);
        if (fee.compareTo(BigDecimal.ZERO) > 0) {
            CardTx feeTx = new CardTx();
            feeTx.setCardNo(savedNew.getCardNo());
            feeTx.setType("CONSUME");
            feeTx.setAmount(fee);
            feeTx.setBalanceAfter(newBalance);
            feeTx.setMerchant("CARD_CENTER");
            feeTx.setOccurredAt(LocalDateTime.now());
            feeTx.setNote(req.getNote() != null ? req.getNote() : "换/补卡工本费");
            cardTxRepository.save(feeTx);
        }
        ReplaceResult result = new ReplaceResult();
        result.setSuccess(true);
        result.setNewCardNo(savedNew.getCardNo());
        return result;
    }
}