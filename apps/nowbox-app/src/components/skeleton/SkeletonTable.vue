<script setup lang="ts">
withDefaults(defineProps<{ colunas?: number; linhas?: number }>(), { colunas: 5, linhas: 8 })
</script>

<template>
  <div class="skeleton-table" role="status" aria-busy="true" aria-label="Carregando">
    <div v-for="l in linhas" :key="l" class="skeleton-table__linha" :style="{ '--colunas': colunas }">
      <span
        v-for="c in colunas"
        :key="c"
        class="skeleton"
        :class="{ 'skeleton--curto': c === colunas && colunas > 1 }"
        :style="{ width: c === 1 ? '70%' : undefined }"
      ></span>
    </div>
  </div>
</template>

<style scoped>
.skeleton-table__linha {
  display: grid;
  grid-template-columns: repeat(var(--colunas), 1fr);
  align-items: center;
  gap: 40px;
  padding: 18px 20px;
}

.skeleton-table__linha + .skeleton-table__linha {
  border-top: 1px solid var(--border-hairline);
}

.skeleton--curto {
  width: 40px;
  justify-self: end;
}

@media (max-width: 720px) {
  .skeleton-table__linha {
    grid-template-columns: 1fr 1fr;
    gap: 14px 20px;
    padding: 16px;
  }
}
</style>
