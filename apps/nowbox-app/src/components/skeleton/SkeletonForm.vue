<script setup lang="ts">
withDefaults(defineProps<{ campos?: number }>(), { campos: 4 })
</script>

<template>
  <div class="skeleton-form" role="status" aria-busy="true" aria-label="Carregando">
    <div v-for="c in campos" :key="c" class="skeleton-form__campo">
      <span class="skeleton skeleton--rotulo"></span>
      <span class="skeleton skeleton--input"></span>
    </div>
  </div>
</template>

<style scoped>
.skeleton-form {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 20px 24px;
  padding: 24px 20px;
}

.skeleton-form__campo {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.skeleton--rotulo {
  width: 90px;
  height: 12px;
}

.skeleton--input {
  height: 40px;
}

@media (max-width: 720px) {
  .skeleton-form {
    grid-template-columns: 1fr;
  }
}
</style>
