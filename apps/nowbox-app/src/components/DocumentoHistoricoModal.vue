<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import AppIcon from './AppIcon.vue'
import DocumentoPreview from './DocumentoPreview.vue'
import { ApiError } from '../lib/http'
import { clienteService } from '../services/cliente.service'
import type { DocumentoHistoricoDTO, TipoDocumentoCliente } from '../types/api'

const TAMANHO_PAGINA = 8

const props = defineProps<{ idCliente: string; titulo: string; tipo?: TipoDocumentoCliente }>()
const emit = defineEmits<{ fechar: [] }>()

const TIPOS: { valor: TipoDocumentoCliente; rotulo: string; vazio: string }[] = [
  { valor: 'IDENTIDADE', rotulo: 'Identidade', vazio: 'Nenhum documento de identidade enviado ainda.' },
  { valor: 'COMPROVANTE_RESIDENCIA', rotulo: 'Comprovante de residência', vazio: 'Nenhum comprovante de residência enviado ainda.' },
]

const tipoSelecionado = ref<TipoDocumentoCliente>(props.tipo ?? 'IDENTIDADE')
const mensagemVazio = computed(() => TIPOS.find((t) => t.valor === tipoSelecionado.value)!.vazio)

const documentos = ref<DocumentoHistoricoDTO[]>([])
const carregando = ref(true)
const erro = ref('')
const baixandoId = ref('')
const abrindoId = ref('')

const pagina = ref(0)
const totalPaginas = ref(0)
const totalElementos = ref(0)

const visualizando = ref<DocumentoHistoricoDTO | null>(null)
const arquivo = ref<Blob | null>(null)

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
    const resultado = await clienteService.listarDocumentos(props.idCliente, pagina.value, TAMANHO_PAGINA, tipoSelecionado.value)
    documentos.value = resultado.content
    totalPaginas.value = resultado.totalPages
    totalElementos.value = resultado.totalElements
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível carregar os documentos.'
  } finally {
    carregando.value = false
  }
}

function selecionarTipo(tipo: TipoDocumentoCliente) {
  if (tipo === tipoSelecionado.value) return
  tipoSelecionado.value = tipo
  pagina.value = 0
  carregar()
}

function irParaPagina(novaPagina: number) {
  if (novaPagina < 0 || novaPagina >= totalPaginas.value) return
  pagina.value = novaPagina
  carregar()
}

async function visualizar(documento: DocumentoHistoricoDTO) {
  abrindoId.value = documento.id
  erro.value = ''

  try {
    const resultado = await clienteService.obterDocumentoHistorico(props.idCliente, documento.id)
    arquivo.value = resultado.blob
    visualizando.value = documento
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível abrir o documento.'
  } finally {
    abrindoId.value = ''
  }
}

function voltar() {
  visualizando.value = null
  arquivo.value = null
}

async function baixar(documento: DocumentoHistoricoDTO) {
  baixandoId.value = documento.id
  erro.value = ''

  try {
    await clienteService.baixarDocumentoHistorico(props.idCliente, documento.id, documento.nomeArquivo)
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível baixar o documento.'
  } finally {
    baixandoId.value = ''
  }
}

function aoPressionarTecla(evento: KeyboardEvent) {
  if (evento.key !== 'Escape') return
  if (visualizando.value) voltar()
  else emit('fechar')
}

onMounted(() => {
  window.addEventListener('keydown', aoPressionarTecla)
  carregar()
})

onBeforeUnmount(() => window.removeEventListener('keydown', aoPressionarTecla))
</script>

<template>
  <div class="historico__fundo" @click.self="emit('fechar')">
    <div class="historico" role="dialog" aria-modal="true" :aria-label="titulo">
      <header class="historico__cabecalho">
        <button v-if="visualizando" type="button" class="historico__fechar" aria-label="Voltar ao histórico" @click="voltar">
          <AppIcon name="arrow-left" :size="18" />
        </button>
        <h2 class="historico__titulo">{{ titulo }}</h2>
        <button type="button" class="historico__fechar" aria-label="Fechar" @click="emit('fechar')">
          <AppIcon name="close" :size="18" />
        </button>
      </header>

      <div v-if="!visualizando" class="historico__abas" role="tablist">
        <button
          v-for="t in TIPOS"
          :key="t.valor"
          type="button"
          role="tab"
          class="historico__aba"
          :class="{ 'historico__aba--ativa': t.valor === tipoSelecionado }"
          :aria-selected="t.valor === tipoSelecionado"
          @click="selecionarTipo(t.valor)"
        >
          {{ t.rotulo }}
        </button>
      </div>

      <div v-if="visualizando" class="historico__corpo">
        <DocumentoPreview :arquivo="arquivo" :nome="visualizando.nomeArquivo" />
        <p class="historico__nome">{{ visualizando.nomeArquivo }} · {{ formatarDataHora(visualizando.salvoEm) }}</p>
      </div>

      <div v-else-if="carregando" class="historico__estado">
        <AppIcon name="loader" :size="20" class="historico__spinner" />
        <span>Carregando documentos...</span>
      </div>

      <div v-else-if="erro && documentos.length === 0" class="historico__estado historico__estado--erro">
        <AppIcon name="alert-circle" :size="20" />
        <span>{{ erro }}</span>
        <button type="button" class="btn" @click="carregar">Tentar novamente</button>
      </div>

      <div v-else-if="documentos.length === 0" class="historico__estado">
        <AppIcon name="file-text" :size="20" />
        <span>{{ mensagemVazio }}</span>
      </div>

      <template v-else>
        <p v-if="erro" class="historico__aviso">{{ erro }}</p>

        <table class="historico__tabela">
          <thead>
            <tr>
              <th>Enviado em</th>
              <th>Arquivo</th>
              <th>Tamanho</th>
              <th class="historico__col-acoes">Ações</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="documento in documentos" :key="documento.id">
              <td data-label="Enviado em">
                {{ formatarDataHora(documento.salvoEm) }}
                <span v-if="documento.atual" class="badge badge--good">
                  <span class="badge__dot" />
                  Atual
                </span>
              </td>
              <td data-label="Arquivo" class="historico__arquivo">{{ documento.nomeArquivo }}</td>
              <td data-label="Tamanho">{{ formatarTamanho(documento.tamanho) }}</td>
              <td class="historico__col-acoes">
                <div class="historico__acoes">
                  <button
                    type="button"
                    class="historico__acao-btn"
                    aria-label="Visualizar documento"
                    title="Visualizar"
                    :disabled="abrindoId === documento.id"
                    @click="visualizar(documento)"
                  >
                    <AppIcon
                      :name="abrindoId === documento.id ? 'loader' : 'eye'"
                      :size="16"
                      :class="{ 'historico__spinner': abrindoId === documento.id }"
                    />
                  </button>
                  <button
                    type="button"
                    class="historico__acao-btn"
                    aria-label="Baixar documento"
                    title="Baixar"
                    :disabled="baixandoId === documento.id"
                    @click="baixar(documento)"
                  >
                    <AppIcon
                      :name="baixandoId === documento.id ? 'loader' : 'download'"
                      :size="16"
                      :class="{ 'historico__spinner': baixandoId === documento.id }"
                    />
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>

        <div class="historico__paginacao">
          <span class="historico__total">{{ totalElementos }} documento(s)</span>

          <div class="historico__paginacao-controles">
            <button
              type="button"
              class="historico__acao-btn"
              aria-label="Página anterior"
              :disabled="pagina === 0"
              @click="irParaPagina(pagina - 1)"
            >
              <AppIcon name="chevron-left" :size="16" />
            </button>
            <span class="historico__pagina-atual">Página {{ pagina + 1 }} de {{ Math.max(totalPaginas, 1) }}</span>
            <button
              type="button"
              class="historico__acao-btn"
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
.historico__fundo {
  position: fixed;
  inset: 0;
  z-index: 100;
  display: grid;
  place-items: center;
  padding: 16px;
  background: rgba(11, 11, 11, 0.45);
}

.historico {
  width: min(760px, 100%);
  max-height: 90vh;
  overflow: auto;
  background: var(--bg-surface);
  border: 1px solid var(--border-hairline);
  box-shadow: var(--shadow-card);
}

.historico__cabecalho {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-hairline);
}

.historico__titulo {
  flex: 1;
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.historico__fechar {
  width: 32px;
  height: 32px;
  display: grid;
  place-items: center;
  border: none;
  background: transparent;
  color: var(--text-secondary);
  cursor: pointer;
}

.historico__fechar:hover {
  color: var(--text-primary);
}

.historico__abas {
  display: flex;
  gap: 4px;
  padding: 0 20px;
  border-bottom: 1px solid var(--border-hairline);
}

.historico__aba {
  padding: 10px 12px;
  border: none;
  border-bottom: 2px solid transparent;
  background: transparent;
  color: var(--text-muted);
  font-size: 13.5px;
  font-weight: 600;
  cursor: pointer;
}

.historico__aba:hover {
  color: var(--text-primary);
}

.historico__aba--ativa {
  color: var(--text-primary);
  border-bottom-color: var(--brand-500);
}

.historico__corpo {
  padding: 20px;
}

.historico__nome {
  margin-top: 10px;
  font-size: 12.5px;
  color: var(--text-muted);
}

.historico__estado {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 40px 16px;
  text-align: center;
  color: var(--text-muted);
  font-size: 13.5px;
}

.historico__estado--erro {
  color: var(--text-critical);
}

.historico__aviso {
  margin: 0;
  padding: 10px 20px;
  color: var(--text-critical);
  font-size: 13px;
  border-bottom: 1px solid var(--border-hairline);
}

.historico__spinner {
  animation: historico-spin 0.8s linear infinite;
  color: var(--brand-500);
}

@keyframes historico-spin {
  to {
    transform: rotate(360deg);
  }
}

.historico__tabela {
  width: 100%;
  border-collapse: collapse;
}

.historico__tabela th,
.historico__tabela td {
  text-align: left;
  padding: 12px 20px;
  font-size: 13.5px;
}

.historico__tabela thead th {
  background: var(--bg-surface-sunken);
  color: var(--text-muted);
  font-weight: 600;
  font-size: 12px;
  letter-spacing: 0.05em;
  text-transform: uppercase;
  border-bottom: 1px solid var(--border-hairline);
}

.historico__tabela tbody tr + tr td {
  border-top: 1px solid var(--border-hairline);
}

.historico__tabela td .badge {
  margin-left: 8px;
}

.historico__arquivo {
  word-break: break-all;
}

.historico__col-acoes {
  width: 1%;
  white-space: nowrap;
}

.historico__acoes {
  display: flex;
  align-items: center;
  gap: 8px;
}

.historico__acao-btn {
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

.historico__acao-btn:hover:not(:disabled) {
  background: var(--bg-surface-sunken);
  color: var(--text-primary);
}

.historico__acao-btn:disabled {
  opacity: 0.5;
  cursor: default;
}

.historico__paginacao {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 20px;
  border-top: 1px solid var(--border-hairline);
}

.historico__total,
.historico__pagina-atual {
  font-size: 12.5px;
  color: var(--text-muted);
  white-space: nowrap;
}

.historico__paginacao-controles {
  display: flex;
  align-items: center;
  gap: 10px;
}

@media (max-width: 720px) {
  .historico__tabela thead {
    display: none;
  }

  .historico__tabela,
  .historico__tabela tbody,
  .historico__tabela tr,
  .historico__tabela td {
    display: block;
    width: 100%;
  }

  .historico__tabela tbody tr {
    padding: 12px 16px;
  }

  .historico__tabela tbody tr + tr {
    border-top: 1px solid var(--border-hairline);
  }

  .historico__tabela tbody tr + tr td {
    border-top: none;
  }

  .historico__tabela td {
    padding: 4px 0;
  }

  .historico__tabela td[data-label]::before {
    content: attr(data-label);
    display: block;
    font-size: 11px;
    font-weight: 600;
    letter-spacing: 0.05em;
    text-transform: uppercase;
    color: var(--text-muted);
    margin-bottom: 2px;
  }

  .historico__col-acoes {
    padding-top: 8px;
  }

  .historico__acoes {
    justify-content: flex-end;
  }

  .historico__paginacao {
    flex-wrap: wrap;
    justify-content: center;
    padding: 14px 16px;
  }
}
</style>
