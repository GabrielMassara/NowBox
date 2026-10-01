<script setup lang="ts">
import { onBeforeUnmount, ref, watch } from 'vue'

const props = defineProps<{ arquivo: Blob | null; nome?: string }>()

const url = ref('')

function liberar() {
  if (url.value) URL.revokeObjectURL(url.value)
  url.value = ''
}

watch(
  () => props.arquivo,
  (arquivo) => {
    liberar()
    if (arquivo) url.value = URL.createObjectURL(arquivo)
  },
  { immediate: true },
)

onBeforeUnmount(liberar)
</script>

<template>
  <div v-if="arquivo && url" class="documento-preview">
    <iframe v-if="arquivo.type === 'application/pdf'" :src="url" :title="nome ?? 'Documento'" class="documento-preview__pdf" />
    <img v-else :src="url" :alt="nome ?? 'Documento'" class="documento-preview__imagem" />
  </div>
</template>

<style scoped>
.documento-preview {
  background: var(--bg-surface-sunken);
  border: 1px solid var(--border-hairline);
  display: grid;
  place-items: center;
  overflow: hidden;
}

.documento-preview__imagem {
  max-width: 100%;
  max-height: 420px;
  object-fit: contain;
  display: block;
}

.documento-preview__pdf {
  width: 100%;
  height: 480px;
  border: none;
  display: block;
}
</style>
