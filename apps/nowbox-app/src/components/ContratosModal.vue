<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'
import AppIcon from './AppIcon.vue'
import { ApiError } from '../lib/http'
import { contratoService } from '../services/contrato.service'
import type { AditivoResponseDTO } from '../types/api'

const TAMANHO_PAGINA = 8

const props = defineProps<{
  titulo: string
  idAluguel: string
}>()

const emit = defineEmits<{ fechar: [] }>()

const contratos = ref<AditivoResponseDTO[]>([])
const carregando = ref(true)
const erro = ref('')
const baixandoId = ref('')

const pagina = ref(0)
const totalPaginas = ref(0)
const totalElementos = ref(0)

const dataHora = new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short', timeStyle: 'short' })

function formatarDataHora(data: string) {
  return dataHora.format(new Date(data))
}

function formatarTamanho(bytes: number) {
  return bytes >= 1024 * 1024 ? `${(bytes / (1024 * 1024)).toFixed(1)} MB` : `${Math.max(1, Math.round(bytes / 1024))} KB`
}


async function carregar() {
  carregando.value = true
  erro.value = ''

  try {
    const resultado = await contratoService.listarAditivosPorAluguel(props.idAluguel, pagina.value, TAMANHO_PAGINA)
    contratos.value = resultado.content
    totalPaginas.value = resultado.totalPages
    totalElementos.value = resultado.totalElements
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível carregar os aditivos.'
  } finally {
    carregando.value = false
  }
}

function irParaPagina(novaPagina: number) {
  if (novaPagina < 0 || novaPagina >= totalPaginas.value) return
  pagina.value = novaPagina
  carregar()
}

async function baixar(contrato: AditivoResponseDTO) {
  baixandoId.value = contrato.id
  erro.value = ''

  try {
    await contratoService.baixarAditivo(contrato.id, contrato.nomeArquivo)
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível baixar o aditivo.'
  } finally {
    baixandoId.value = ''
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
  <div class="contratos__fundo" @click.self="emit('fechar')">
    <div class="contratos" role="dialog" aria-modal="true" :aria-label="titulo">
      <header class="contratos__cabecalho">
        <h2 class="contratos__titulo">{{ titulo }}</h2>
        <button type="button" class="contratos__fechar" aria-label="Fechar" @click="emit('fechar')">
          <AppIcon name="close" :size="18" />
        </button>
      </header>

      <div v-if="carregando" class="contratos__estado">
        <AppIcon name="loader" :size="20" class="contratos__spinner" />
        <span>Carregando aditivos...</span>
      </div>

      <div v-else-if="erro && contratos.length === 0" class="contratos__estado contratos__estado--erro">
        <AppIcon name="alert-circle" :size="20" />
        <span>{{ erro }}</span>
        <button type="button" class="btn" @click="carregar">Tentar novamente</button>
      </div>

      <div v-else-if="contratos.length === 0" class="contratos__estado">
        <AppIcon name="file-text" :size="20" />
        <span>Nenhum aditivo gerado. Um aditivo é gerado em segundo plano sempre que o aluguel é alterado, e o contrato original é mantido.</span>
        <button type="button" class="btn" @click="carregar">Atualizar</button>
      </div>

      <template v-else>
        <p v-if="erro" class="contratos__aviso">{{ erro }}</p>

        <table class="contratos__tabela">
          <thead>
            <tr>
              <th>Gerado em</th>
              <th>Alterações</th>
              <th>Arquivo</th>
              <th>Tamanho</th>
              <th class="contratos__col-acoes">Baixar</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="contrato in contratos" :key="contrato.id">
              <td data-label="Gerado em">
                {{ formatarDataHora(contrato.salvoEm) }}
              </td>
              <td data-label="Alterações" class="contratos__descricao">{{ contrato.descricao || '-' }}</td>
              <td data-label="Arquivo" class="contratos__arquivo">{{ contrato.nomeArquivo }}</td>
              <td data-label="Tamanho">{{ formatarTamanho(contrato.tamanho) }}</td>
              <td class="contratos__col-acoes">
                <button
                  type="button"
                  class="contratos__acao-btn"
                  aria-label="Baixar aditivo"
                  title="Baixar"
                  :disabled="baixandoId === contrato.id"
                  @click="baixar(contrato)"
                >
                  <AppIcon
                    :name="baixandoId === contrato.id ? 'loader' : 'download'"
                    :size="16"
                    :class="{ 'contratos__spinner': baixandoId === contrato.id }"
                  />
                </button>
              </td>
            </tr>
          </tbody>
        </table>

        <div class="contratos__paginacao">
          <span class="contratos__total">{{ totalElementos }} aditivo(s)</span>

          <div class="contratos__paginacao-controles">
            <button
              type="button"
              class="contratos__acao-btn"
              aria-label="Página anterior"
              :disabled="pagina === 0"
              @click="irParaPagina(pagina - 1)"
            >
              <AppIcon name="chevron-left" :size="16" />
            </button>
            <span class="contratos__pagina-atual">Página {{ pagina + 1 }} de {{ Math.max(totalPaginas, 1) }}</span>
            <button
              type="button"
              class="contratos__acao-btn"
              aria-label="Próxima página"
              :disabled="pagina + 1 >= totalPaginas"
              @click="irParaPagina(pagina + 1)"
            >
              <AppIcon name="chevron-right" :size="16" />
            </button>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<style scoped>
.contratos__fundo {
  position: fixed;
  inset: 0;
  z-index: 100;
  display: grid;
  place-items: center;
  padding: 16px;
  background: rgba(11, 11, 11, 0.45);
}

.contratos {
  width: min(760px, 100%);
  max-height: 90vh;
  overflow: auto;
  background: var(--bg-surface);
  border: 1px solid var(--border-hairline);
  box-shadow: var(--shadow-card);
}

.contratos__cabecalho {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-hairline);
}

.contratos__titulo {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.contratos__fechar {
  width: 32px;
  height: 32px;
  display: grid;
  place-items: center;
  border: none;
  background: transparent;
  color: var(--text-secondary);
  cursor: pointer;
}

.contratos__fechar:hover {
  color: var(--text-primary);
}

.contratos__estado {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 40px 16px;
  text-align: center;
  color: var(--text-muted);
  font-size: 13.5px;
}

.contratos__estado--erro {
  color: var(--text-critical);
}

.contratos__aviso {
  margin: 0;
  padding: 10px 20px;
  color: var(--text-critical);
  font-size: 13px;
  border-bottom: 1px solid var(--border-hairline);
}

.contratos__spinner {
  animation: contratos-spin 0.8s linear infinite;
  color: var(--brand-500);
}

@keyframes contratos-spin {
  to {
    transform: rotate(360deg);
  }
}

.contratos__tabela {
  width: 100%;
  border-collapse: collapse;
}

.contratos__tabela th,
.contratos__tabela td {
  text-align: left;
  padding: 12px 20px;
  font-size: 13.5px;
}

.contratos__tabela thead th {
  background: var(--bg-surface-sunken);
  color: var(--text-muted);
  font-weight: 600;
  font-size: 12px;
  letter-spacing: 0.05em;
  text-transform: uppercase;
  border-bottom: 1px solid var(--border-hairline);
}

.contratos__tabela tbody tr + tr td {
  border-top: 1px solid var(--border-hairline);
}


.contratos__descricao {
  white-space: pre-line;
}

.contratos__arquivo {
  word-break: break-all;
}

.contratos__col-acoes {
  width: 1%;
  white-space: nowrap;
}

.contratos__acao-btn {
  width: 32px;
  height: 32px;
  display: grid;
  place-items: center;
  border: 1px solid var(--border-hairline);
  background: var(--bg-surface);
  color: var(--text-secondary);
  cursor: pointer;
  transition: background-color 0.15s ease, color 0.15s ease;
}

.contratos__acao-btn:hover:not(:disabled) {
  background: var(--bg-surface-sunken);
  color: var(--text-primary);
}

.contratos__acao-btn:disabled {
  opacity: 0.5;
  cursor: default;
}

.contratos__paginacao {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 20px;
  border-top: 1px solid var(--border-hairline);
}

.contratos__total,
.contratos__pagina-atual {
  font-size: 12.5px;
  color: var(--text-muted);
  white-space: nowrap;
}

.contratos__paginacao-controles {
  display: flex;
  align-items: center;
  gap: 10px;
}

@media (max-width: 720px) {
  .contratos__tabela thead {
    display: none;
  }

  .contratos__tabela,
  .contratos__tabela tbody,
  .contratos__tabela tr,
  .contratos__tabela td {
    display: block;
    width: 100%;
  }

  .contratos__tabela tbody tr {
    padding: 12px 16px;
  }

  .contratos__tabela tbody tr + tr {
    border-top: 1px solid var(--border-hairline);
  }

  .contratos__tabela tbody tr + tr td {
    border-top: none;
  }

  .contratos__tabela td {
    padding: 4px 0;
  }

  .contratos__tabela td[data-label]::before {
    content: attr(data-label);
    display: block;
    font-size: 11px;
    font-weight: 600;
    letter-spacing: 0.05em;
    text-transform: uppercase;
    color: var(--text-muted);
    margin-bottom: 2px;
  }

  .contratos__col-acoes {
    padding-top: 8px;
    text-align: right;
  }

  .contratos__paginacao {
    flex-wrap: wrap;
    justify-content: center;
    padding: 14px 16px;
  }
}
</style>
