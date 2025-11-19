package com.campus.card.common.result;

import lombok.Data;
import java.util.List;

@Data
public class PageResult<T> {
    private long total;
    private List<T> records;

    public static <T> PageResult<T> of(long total, List<T> records) {
        PageResult<T> pr = new PageResult<>();
        pr.setTotal(total);
        pr.setRecords(records);
        return pr;
    }
}