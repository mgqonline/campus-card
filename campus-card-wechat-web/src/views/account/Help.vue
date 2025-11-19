<script setup lang="ts">
import { useRouter } from 'vue-router'
import { ref, onMounted } from 'vue'
import { getHelpArticles, type HelpArticle } from '@/api/modules/help'
const router = useRouter()
function goBack() { router.back() }
const articles = ref<HelpArticle[] | null>(null)
const loadError = ref('')
onMounted(async () => {
  try {
    articles.value = await getHelpArticles()
  } catch (e: any) {
    loadError.value = e?.message || '加载失败'
  }
})
</script>

<template>
  <div class="page">
    <div class="weui-panel weui-panel_access">
      <div class="weui-panel__hd">帮助中心</div>
      <div class="weui-panel__bd">
        <div v-if="loadError" class="weui-loadmore weui-loadmore_line"><span class="weui-loadmore__tips">{{ loadError }}</span></div>
        <div v-else-if="!articles" class="weui-loadmore"><i class="weui-loading"></i><span class="weui-loadmore__tips">加载中</span></div>

        <template v-else>
          <div class="weui-cells__title">常见问题</div>
          <div class="weui-cells">
            <div v-if="articles.length===0" class="weui-loadmore weui-loadmore_line"><span class="weui-loadmore__tips">暂无内容</span></div>
            <div v-for="a in articles" :key="a.id" class="weui-cell">
              <div class="weui-cell__bd">
                <p class="q">{{ a.title }}</p>
                <p class="a">{{ a.content }}</p>
              </div>
            </div>
          </div>

          <div class="weui-cells__title">联系我们</div>
          <div class="weui-cells">
            <div class="weui-cell">
              <div class="weui-cell__bd">
                <p>学校管理员：<span class="em">教务处信息中心</span></p>
                <p>联系电话：<span class="em">(010) 1234 5678</span></p>
                <p>邮件支持：<span class="em">support@school.example.com</span></p>
              </div>
            </div>
          </div>

          <div class="weui-form__opr-area" style="padding:12px;">
            <a href="javascript:;" class="weui-btn weui-btn_primary" @click="goBack">返回</a>
          </div>
        </template>
      </div>
    </div>
  </div>
  
</template>

<style scoped>
.q { font-weight:600; color:#333; }
.a { color:#666; font-size:13px; margin-top:4px; }
.tip { color:#666; font-size:13px; }
.em { color:#333; font-weight:600; }
</style>