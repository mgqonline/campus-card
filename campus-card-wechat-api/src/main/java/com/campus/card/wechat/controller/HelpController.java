package com.campus.card.wechat.controller;

import com.campus.card.common.result.Result;
import com.campus.card.wechat.model.HelpArticle;
import com.campus.card.wechat.repository.HelpArticleRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/help")
public class HelpController {

    private final HelpArticleRepository repo;

    public HelpController(HelpArticleRepository repo) { this.repo = repo; }

    /** 帮助中心文章列表（首次访问自动填充默认内容） */
    @GetMapping("/articles")
    public Result<List<HelpArticle>> articles() {
        if (repo.count() == 0) {
            HelpArticle a1 = new HelpArticle();
            a1.setTitle("如何绑定子女？");
            a1.setContent("在个人中心中绑定微信或手动输入手机号，完成后即可在学生信息中查看子女资料。");
            a1.setCategory("FAQ");
            HelpArticle a2 = new HelpArticle();
            a2.setTitle("考勤提醒没有显示？");
            a2.setContent("若提醒列表为空，可能暂无数据。请稍后再试，或联系学校管理员确认设备上传状态。");
            a2.setCategory("FAQ");
            HelpArticle a3 = new HelpArticle();
            a3.setTitle("充值记录在哪里查看？");
            a3.setContent("进入在线充值页面，右上角点击账单图标即可查看充值记录。");
            a3.setCategory("FAQ");
            repo.save(a1); repo.save(a2); repo.save(a3);
        }
        return Result.ok(repo.findAll());
    }
}