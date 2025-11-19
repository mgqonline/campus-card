<script setup lang="ts">
// 关于我们为静态展示页，无需复杂逻辑
</script>

<template>
  <div class="page">
    <div class="weui-panel weui-panel_access">
      <div class="weui-panel__hd">关于我们</div>
      <div class="weui-panel__bd">
        <div class="weui-cells__title">公司简介</div>
        <div class="weui-cells">
          <div class="weui-cell">
            <div class="weui-cell__bd">
              <p>我们专注于校园数字化，提供家长端和学校端一体化服务，连接校园卡、考勤设备与移动应用，提升校园信息化体验。</p>
            </div>
          </div>
        </div>

        <div class="weui-cells__title">主要业务</div>
        <div class="weui-cells">
          <div class="weui-cell">
            <div class="weui-cell__bd">
              <p class="b">考勤管理</p>
              <p class="d">支持进出校实时记录、迟到早退提醒、照片查看与统计分析。</p>
            </div>
          </div>
          <div class="weui-cell">
            <div class="weui-cell__bd">
              <p class="b">校园卡消费与充值</p>
              <p class="d">提供消费明细、月度统计、在线充值与账单记录查询。</p>
            </div>
          </div>
          <div class="weui-cell">
            <div class="weui-cell__bd">
              <p class="b">通知与家校沟通</p>
              <p class="d">基于消息提醒中心，提供异常到校、缺勤等场景的及时通知。</p>
            </div>
          </div>
          <div class="weui-cell">
            <div class="weui-cell__bd">
              <p class="b">学生信息与家长资料</p>
              <p class="d">家长端可查看子女基本信息、班级与卡号状态，支持资料维护。</p>
            </div>
          </div>
        </div>

        <div class="weui-cells__title">我们的优势</div>
        <div class="weui-cells">
          <div class="weui-cell">
            <div class="weui-cell__bd">
              <p>系统稳定易用，快速集成校园现有设备；注重隐私安全；界面遵循 WeUI 规范，移动端体验友好。</p>
            </div>
          </div>
        </div>

        <div class="weui-cells__title">联系方式</div>
        <div class="weui-cells">
          <div class="weui-cell">
            <div class="weui-cell__bd">
              <p>商务合作：<span class="em">business@school.example.com</span></p>
              <p>技术支持：<span class="em">support@school.example.com</span></p>
              <p>联系电话：<span class="em">(010) 1234 5678</span></p>
            </div>
          </div>
        </div>

        <div class="weui-form__opr-area" style="padding:12px;">
          <a href="/account/profile" class="weui-btn weui-btn_default">返回个人中心</a>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.b { font-weight:600; color:#333; }
.d { color:#666; font-size:13px; margin-top:4px; }
.em { color:#333; font-weight:600; }
</style>