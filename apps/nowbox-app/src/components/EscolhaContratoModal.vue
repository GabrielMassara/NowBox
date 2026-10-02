<script setup lang="ts">
import { onBeforeUnmount, onMounted } from 'vue'
import AppIcon from './AppIcon.vue'

withDefaults(
  defineProps<{
    titulo: string
    assinadoDisponivel: boolean
    documento?: 'contrato' | 'distrato'
  }>(),
  { documento: 'contrato' },
)

const emit = defineEmits<{ escolher: [versao: 'modelo' | 'assinado']; fechar: [] }>()

function aoPressionarTecla(evento: KeyboardEvent) {
  if (evento.key === 'Escape') emit('fechar')
}

onMounted(() => window.addEventListener('keydown', aoPressionarTecla))
onBeforeUnmount(() => window.removeEventListener('keydown', aoPressionarTecla))
</script>

<template>
  <div class="escolha__fundo" @click.self="emit('fechar')">
    <div class="escolha" role="dialog" aria-modal="true" :aria-label="titulo">
      <header class="escolha__cabecalho">
        <h2 class="escolha__titulo">{{ titulo }}</h2>
        <button type="button" class="escolha__fechar" aria-label="Fechar" @click="emit('fechar')">
          <AppIcon name="close" :size="18" />
        </button>
      </header>

      <p class="escolha__mensagem">Qual versão do {{ documento }} você quer baixar?</p>

      <div class="escolha__opcoes">
        <button type="button" class="escolha__opcao" @click="emit('escolher', 'modelo')">
          <AppIcon name="file-text" :size="20" />
          <span class="escolha__opcao-titulo">Modelo em branco</span>
          <span class="escolha__opcao-descricao">Documento gerado pelo sistema, sem assinaturas.</span>
        </button>

        <button type="button" class="escolha__opcao" :disabled="!assinadoDisponivel" @click="emit('escolher', 'assinado')">
          <AppIcon name="clipboard-check" :size="20" />
          <span class="escolha__opcao-titulo">{{ documento === 'contrato' ? 'Contrato' : 'Distrato' }} assinado</span>
          <span class="escolha__opcao-descricao">
            {{ assinadoDisponivel ? 'Arquivo enviado com as assinaturas.' : 'Ainda não foi enviado.' }}
          </span>
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.escolha__fundo {
  position: fixed;
  inset: 0;
  z-index: 100;
  display: grid;
  place-items: center;
  padding: 16px;
  background: rgba(11, 11, 11, 0.45);
}

.escolha {
  width: min(520px, 100%);
  background: var(--bg-surface);
  border: 1px solid var(--border-hairline);
  box-shadow: var(--shadow-card);
}

.escolha__cabecalho {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-hairline);
}

.escolha__titulo {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.escolha__fechar {
  width: 32px;
  height: 32px;
  display: grid;
  place-items: center;
  border: none;
  background: transparent;
  color: var(--text-secondary);
  cursor: pointer;
}

.escolha__fechar:hover {
  color: var(--text-primary);
}

.escolha__mensagem {
  margin: 0;
  padding: 16px 20px 0;
  font-size: 13.5px;
  color: var(--text-secondary);
}

.escolha__opcoes {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  padding: 16px 20px 20px;
}

.escolha__opcao {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 6px;
  padding: 14px;
  text-align: left;
  font: inherit;
  color: var(--text-primary);
  background: var(--bg-surface);
  border: 1px solid var(--border-hairline);
  cursor: pointer;
  transition: background-color 0.15s ease, border-color 0.15s ease;
}

.escolha__opcao:hover:not(:disabled) {
  background: var(--bg-surface-sunken);
  border-color: var(--brand-500);
}

.escolha__opcao:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.escolha__opcao-titulo {
  font-size: 14px;
  font-weight: 600;
}

.escolha__opcao-descricao {
  font-size: 12.5px;
  color: var(--text-muted);
}

@media (max-width: 520px) {
  .escolha__opcoes {
    grid-template-columns: 1fr;
  }
}
</style>
