<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import AppIcon from './AppIcon.vue'

// Confirmação para ações irreversíveis: só habilita o botão depois que a pessoa digita a palavra exigida.
const props = withDefaults(
  defineProps<{
    titulo: string
    mensagem: string
    // Pontos de atenção exibidos em lista, como as consequências da ação.
    avisos?: string[]
    palavra?: string
    textoConfirmar?: string
    processando?: boolean
    erro?: string
  }>(),
  { avisos: () => [], palavra: 'confirmar', textoConfirmar: 'Confirmar', processando: false, erro: '' },
)

const emit = defineEmits<{ confirmar: []; cancelar: [] }>()

const digitado = ref('')
const campo = ref<HTMLInputElement | null>(null)

const liberado = computed(() => digitado.value.trim().toLowerCase() === props.palavra.toLowerCase())

function confirmar() {
  if (!liberado.value || props.processando) return
  emit('confirmar')
}

function cancelar() {
  if (!props.processando) emit('cancelar')
}

function aoPressionarTecla(evento: KeyboardEvent) {
  if (evento.key === 'Escape') cancelar()
}

onMounted(() => {
  window.addEventListener('keydown', aoPressionarTecla)
  campo.value?.focus()
})

onBeforeUnmount(() => window.removeEventListener('keydown', aoPressionarTecla))
</script>

<template>
  <div class="confirmacao__fundo" @click.self="cancelar">
    <form class="confirmacao" role="alertdialog" aria-modal="true" :aria-label="titulo" @submit.prevent="confirmar">
      <header class="confirmacao__cabecalho">
        <span class="confirmacao__icone"><AppIcon name="alert-circle" :size="22" /></span>
        <div>
          <h2 class="confirmacao__titulo">{{ titulo }}</h2>
          <p class="confirmacao__mensagem">{{ mensagem }}</p>
        </div>
      </header>

      <ul v-if="avisos.length > 0" class="confirmacao__avisos">
        <li v-for="aviso in avisos" :key="aviso">{{ aviso }}</li>
      </ul>

      <div class="confirmacao__campo">
        <label for="confirmacao-palavra">
          Para continuar, digite <strong>{{ palavra }}</strong> abaixo
        </label>
        <input
          id="confirmacao-palavra"
          ref="campo"
          v-model="digitado"
          type="text"
          autocomplete="off"
          autocapitalize="off"
          spellcheck="false"
          :placeholder="palavra"
          :disabled="processando"
        />
      </div>

      <p v-if="erro" class="confirmacao__erro">
        <AppIcon name="alert-circle" :size="15" />
        {{ erro }}
      </p>

      <footer class="confirmacao__acoes">
        <button type="button" class="btn" :disabled="processando" @click="cancelar">Cancelar</button>
        <button type="submit" class="btn confirmacao__confirmar" :disabled="!liberado || processando">
          <AppIcon v-if="processando" name="loader" :size="15" class="confirmacao__spinner" />
          {{ processando ? 'Processando...' : textoConfirmar }}
        </button>
      </footer>
    </form>
  </div>
</template>

<style scoped>
.confirmacao__fundo {
  position: fixed;
  inset: 0;
  z-index: 110;
  display: grid;
  place-items: center;
  padding: 16px;
  background: rgba(11, 11, 11, 0.5);
}

.confirmacao {
  width: min(480px, 100%);
  max-height: 90vh;
  overflow: auto;
  display: flex;
  flex-direction: column;
  gap: 18px;
  padding: 24px;
  background: var(--bg-surface);
  border: 1px solid var(--border-hairline);
  border-top: 3px solid var(--status-critical);
  box-shadow: var(--shadow-card);
}

.confirmacao__cabecalho {
  display: flex;
  gap: 14px;
  align-items: flex-start;
}

.confirmacao__icone {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: color-mix(in srgb, var(--status-critical) 12%, transparent);
  color: var(--text-critical);
}

.confirmacao__titulo {
  margin: 0;
  font-size: 17px;
  font-weight: 700;
}

.confirmacao__mensagem {
  margin: 6px 0 0;
  font-size: 13.5px;
  line-height: 1.5;
  color: var(--text-secondary);
}

.confirmacao__avisos {
  margin: 0;
  padding: 12px 16px 12px 32px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 13px;
  color: var(--text-secondary);
  background: var(--bg-surface-sunken);
  border: 1px solid var(--border-hairline);
}

.confirmacao__campo {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.confirmacao__campo label {
  font-size: 13px;
  color: var(--text-secondary);
}

.confirmacao__campo strong {
  color: var(--text-primary);
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
}

.confirmacao__campo input {
  padding: 10px 12px;
  font: inherit;
  font-size: 14px;
  color: var(--text-primary);
  background: var(--bg-surface);
  border: 1px solid var(--border-hairline);
  outline: none;
}

.confirmacao__campo input:focus {
  border-color: var(--status-critical);
}

.confirmacao__erro {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 0;
  font-size: 13px;
  color: var(--text-critical);
}

.confirmacao__acoes {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.confirmacao__confirmar {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: var(--status-critical);
  border-color: var(--status-critical);
  color: #fff;
}

.confirmacao__confirmar:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.confirmacao__spinner {
  animation: confirmacao-spin 0.8s linear infinite;
}

@keyframes confirmacao-spin {
  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 520px) {
  .confirmacao {
    padding: 20px 16px;
  }

  .confirmacao__acoes {
    flex-direction: column-reverse;
  }

  .confirmacao__acoes .btn {
    width: 100%;
    justify-content: center;
  }
}
</style>
