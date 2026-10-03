<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'
import AppIcon from './AppIcon.vue'
import DocumentoPreview from './DocumentoPreview.vue'
import { ApiError } from '../lib/http'
import { clienteService } from '../services/cliente.service'
import type { TipoDocumentoCliente } from '../types/api'

const props = defineProps<{ idCliente: string; titulo: string; tipo?: TipoDocumentoCliente }>()
const emit = defineEmits<{ fechar: [] }>()

const arquivo = ref<Blob | null>(null)
const nomeArquivo = ref<string>()
const carregando = ref(true)
const erro = ref('')

async function carregar() {
  carregando.value = true
  erro.value = ''

  try {
    const documento = await clienteService.obterDocumento(props.idCliente, props.tipo ?? 'IDENTIDADE')
    arquivo.value = documento.blob
    nomeArquivo.value = documento.nomeArquivo
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível carregar o documento.'
  } finally {
    carregando.value = false
  }
}

function aoPressionarTecla(evento: KeyboardEvent) {
  if (evento.key === 'Escape') emit('fechar')
}

onMounted(() => {
  window.addEventListener('keydown', aoPressionarTecla)
  carregar()
})
onBeforeUnmount(() => window.removeEventListener('keydown', aoPressionarTecla))
</script>

<template>
  <div class="documento-modal__fundo" @click.self="emit('fechar')">
    <div class="documento-modal" role="dialog" aria-modal="true" :aria-label="titulo">
      <header class="documento-modal__cabecalho">
        <h2 class="documento-modal__titulo">{{ titulo }}</h2>
        <button type="button" class="documento-modal__fechar" aria-label="Fechar" @click="emit('fechar')">
          <AppIcon name="close" :size="18" />
        </button>
      </header>

      <div v-if="carregando" class="documento-modal__estado">
        <AppIcon name="loader" :size="20" class="documento-modal__spinner" />
        <span>Carregando documento...</span>
      </div>

      <div v-else-if="erro" class="documento-modal__estado documento-modal__estado--erro">
        <AppIcon name="alert-circle" :size="20" />
        <span>{{ erro }}</span>
        <button type="button" class="btn" @click="carregar">Tentar novamente</button>
      </div>

      <div v-else class="documento-modal__corpo">
        <DocumentoPreview :arquivo="arquivo" :nome="nomeArquivo" />
        <p v-if="nomeArquivo" class="documento-modal__nome">{{ nomeArquivo }}</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.documento-modal__fundo {
  position: fixed;
  inset: 0;
  z-index: 100;
  display: grid;
  place-items: center;
  padding: 16px;
  background: rgba(11, 11, 11, 0.45);
}

.documento-modal {
  width: min(760px, 100%);
  max-height: 90vh;
  overflow: auto;
  background: var(--bg-surface);
  border: 1px solid var(--border-hairline);
  box-shadow: var(--shadow-card);
}

.documento-modal__cabecalho {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-hairline);
}

.documento-modal__titulo {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.documento-modal__fechar {
  width: 32px;
  height: 32px;
  display: grid;
  place-items: center;
  border: none;
  background: transparent;
  color: var(--text-secondary);
  cursor: pointer;
}

.documento-modal__fechar:hover {
  color: var(--text-primary);
}

.documento-modal__corpo {
  padding: 20px;
}

.documento-modal__nome {
  margin-top: 10px;
  font-size: 12.5px;
  color: var(--text-muted);
}

.documento-modal__estado {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 40px 16px;
  text-align: center;
  color: var(--text-muted);
  font-size: 13.5px;
}

.documento-modal__estado--erro {
  color: var(--text-critical);
}

.documento-modal__spinner {
  animation: documento-modal-spin 0.8s linear infinite;
  color: var(--brand-500);
}

@keyframes documento-modal-spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
