<script setup>
import DefaultTheme from 'vitepress/theme'

const { Layout } = DefaultTheme
</script>

<template>
  <Layout>
    <template #home-features-before>
      <section class="sig">
        <div class="sig-card">
          <span class="sig-label">LEARNING PATH · 11 DEMOS</span>
          <h3 class="sig-title">从第一次对话到知识型客服</h3>
          <div class="al-path">
            <div class="al-track"></div>
            <span class="al-st al-mil"><b>01</b><i>基础对话</i></span>
            <span class="al-st"><b>02</b><i>流式</i></span>
            <span class="al-st"><b>03</b></span>
            <span class="al-st"><b>04</b></span>
            <span class="al-st al-mil"><b>05</b><i>工具调用</i></span>
            <span class="al-st"><b>06</b></span>
            <span class="al-st"><b>07</b></span>
            <span class="al-st al-mil"><b>08</b><i>RAG</i></span>
            <span class="al-st"><b>09</b></span>
            <span class="al-st al-mil"><b>10</b><i>综合客服</i></span>
            <span class="al-st al-mil"><b>11</b><i>知识型客服</i></span>
          </div>
          <p class="sig-note">11 个 Demo 沿着一条线爬升：先会对话，再会调工具、查知识库，最后把这几样组合成能用的客服应用——终点不是 API，是场景。</p>
        </div>
      </section>
    </template>
    <template #home-features-after>
      <div class="sig-more"><a href="https://ibqy.github.io/" target="_blank" rel="noopener">← ibqy.github.io · 探索全部项目</a></div>
    </template>
  </Layout>
</template>
