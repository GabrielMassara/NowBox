<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import AppIcon from '../../components/AppIcon.vue'
import { ApiError } from '../../lib/http'
import { moduloService } from '../../services/modulo.service'
import { operacaoService } from '../../services/operacao.service'
import type { ModuloResponseDTO, OperacaoResponseDTO } from '../../types/api'

const TAMANHO_PAGINA = 10

const router = useRouter()

const operacoes = ref<OperacaoResponseDTO[]>([])
const modulos = ref<ModuloResponseDTO[]>([])
const carregando = ref(false)
const erro = ref('')
const excluindoId = ref('')

const pagina = ref(0)
const totalPaginas = ref(0)
const totalElementos = ref(0)

const filtro = reactive({ nome: '', codigo: '', idModulo: '' })

async function carregarModulos() {
  try {
    const resultado = await moduloService.listar(0, 100)
    modulos.value = resultado.content
  } catch {
    // A listagem de operações ainda funciona sem os módulos para o filtro.
  }
}

async function carregar() {
  carregando.value = true
  erro.value = ''

  try {
    const resultado = await operacaoService.listar(pagina.value, TAMANHO_PAGINA, {
      nome: filtro.nome.trim() || undefined,
      codigo: filtro.codigo.trim() || undefined,
      idModulo: filtro.idModulo || undefined,
    })
    operacoes.value = resultado.content
    totalPaginas.value = resultado.totalPages
    totalElementos.value = resultado.totalElements
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível carregar as operações.'
  } finally {
    carregando.value = false
  }
}

function buscar() {
  pagina.value = 0
  carregar()
}

function limparFiltro() {
  filtro.nome = ''
  filtro.codigo = ''
  filtro.idModulo = ''
  buscar()
}

function irParaPagina(novaPagina: number) {
  if (novaPagina < 0 || novaPagina >= totalPaginas.value) return
  pagina.value = novaPagina
  carregar()
}

function novaOperacao() {
  router.push('/operacoes-sistema/nova')
}

function editarOperacao(operacao: OperacaoResponseDTO) {
  router.push(`/operacoes-sistema/${operacao.id}/editar`)
}

async function excluirOperacao(operacao: OperacaoResponseDTO) {
  if (!confirm(`Excluir a operação "${operacao.nome}"?`)) return

  excluindoId.value = operacao.id
  try {
    await operacaoService.excluir(operacao.id)
    if (operacoes.value.length === 1 && pagina.value > 0) pagina.value -= 1
    await carregar()
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível excluir a operação.'
  } finally {
    excluindoId.value = ''
  }
}

onMounted(() => {
  carregarModulos()
  carregar()
})
</script>

<template>
  <div class="operacoes">
    <div class="operacoes__painel">
      <div class="operacoes__toolbar">
        <form class="operacoes__filtro" @submit.prevent="buscar">
          <div class="operacoes__campo">
            <AppIcon name="search" :size="16" />
            <input v-model="filtro.nome" type="text" placeholder="Buscar por nome" aria-label="Buscar por nome" />
          </div>

          <div class="operacoes__campo">
            <AppIcon name="folder" :size="16" />
            <input v-model="filtro.codigo" type="text" placeholder="Buscar por código" aria-label="Buscar por código" />
          </div>

          <div class="operacoes__campo">
            <AppIcon name="settings" :size="16" />
            <select v-model="filtro.idModulo" aria-label="Filtrar por módulo">
              <option value="">Todos os módulos</option>
              <option v-for="modulo in modulos" :key="modulo.id" :value="modulo.id">{{ modulo.nome }}</option>
            </select>
          </div>

          <button type="submit" class="btn">Filtrar</button>
          <button v-if="filtro.nome || filtro.codigo || filtro.idModulo" type="button" class="btn" @click="limparFiltro">
            Limpar
          </button>
        </form>

        <button type="button" class="btn btn--primary" @click="novaOperacao">
          <AppIcon name="plus" :size="16" />
          Nova operação
        </button>
      </div>

      <div v-if="carregando" class="operacoes__state">
        <AppIcon name="loader" :size="20" class="operacoes__spinner" />
        <span>Carregando operações...</span>
      </div>

      <div v-else-if="erro" class="operacoes__state operacoes__state--error">
        <AppIcon name="alert-circle" :size="20" />
        <span>{{ erro }}</span>
        <button type="button" class="btn" @click="carregar">Tentar novamente</button>
      </div>

      <div v-else-if="operacoes.length === 0" class="operacoes__state">
        <AppIcon name="settings" :size="20" />
        <span>Nenhuma operação encontrada.</span>
      </div>

      <table v-else class="operacoes__table">
        <thead>
          <tr>
            <th>Nome</th>
            <th>Código</th>
            <th>Módulo</th>
            <th class="operacoes__col-acoes">Ações</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="operacao in operacoes" :key="operacao.id">
            <td class="operacoes__nome" data-label="Nome">{{ operacao.nome }}</td>
            <td data-label="Código"><span class="operacoes__codigo">{{ operacao.codigo }}</span></td>
            <td data-label="Módulo">{{ operacao.modulo?.nome }}</td>
            <td class="operacoes__col-acoes">
              <div class="operacoes__acoes">
                <button
                  type="button"
                  class="operacoes__acao-btn"
                  aria-label="Editar operação"
                  title="Editar"
                  @click="editarOperacao(operacao)"
                >
                  <AppIcon name="pencil" :size="16" />
                </button>
                <button
                  type="button"
                  class="operacoes__acao-btn operacoes__acao-btn--perigo"
                  aria-label="Excluir operação"
                  title="Excluir"
                  :disabled="excluindoId === operacao.id"
                  @click="excluirOperacao(operacao)"
                >
                  <AppIcon
                    :name="excluindoId === operacao.id ? 'loader' : 'trash'"
                    :size="16"
                    :class="{ 'operacoes__spinner': excluindoId === operacao.id }"
                  />
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>

      <div v-if="!carregando && !erro && operacoes.length > 0" class="operacoes__paginacao">
        <span class="operacoes__total">{{ totalElementos }} operação(ões) no total</span>

        <div class="operacoes__paginacao-controles">
          <button
            type="button"
            class="operacoes__pagina-btn"
            aria-label="Página anterior"
            :disabled="pagina === 0"
            @click="irParaPagina(pagina - 1)"
          >
            <AppIcon name="chevron-left" :size="16" />
          </button>

          <span class="operacoes__pagina-atual">Página {{ pagina + 1 }} de {{ Math.max(totalPaginas, 1) }}</span>

          <button
            type="button"
            class="operacoes__pagina-btn"
            aria-label="Próxima página"
            :disabled="pagina + 1 >= totalPaginas"
            @click="irParaPagina(pagina + 1)"
          >
            <AppIcon name="chevron-right" :size="16" />
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.operacoes {
  padding: 22px 28px 40px;
}

.operacoes__painel {
  background: var(--bg-surface);
  border: 1px solid var(--border-hairline);
  box-shadow: var(--shadow-card);
}

.operacoes__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-hairline);
}

.operacoes__filtro {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.operacoes__campo {
  display: flex;
  align-items: center;
  gap: 8px;
  background: var(--bg-surface);
  border: 1px solid var(--border-hairline);
  padding: 8px 12px;
  color: var(--text-muted);
}

.operacoes__campo input,
.operacoes__campo select {
  border: none;
  outline: none;
  background: transparent;
  font: inherit;
  font-size: 13.5px;
  color: var(--text-primary);
  width: 170px;
}

.operacoes__campo input::placeholder {
  color: var(--text-muted);
}

.operacoes__filtro .btn {
  border-radius: 0;
}

.operacoes__state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 48px 16px;
  text-align: center;
  color: var(--text-muted);
  font-size: 13.5px;
}

.operacoes__state--error {
  color: var(--text-critical);
}

.operacoes__spinner {
  animation: operacoes-spin 0.8s linear infinite;
  color: var(--brand-500);
}

@keyframes operacoes-spin {
  to {
    transform: rotate(360deg);
  }
}

.operacoes__table {
  width: 100%;
  border-collapse: collapse;
}

.operacoes__table th,
.operacoes__table td {
  text-align: left;
  padding: 13px 20px;
  font-size: 13.5px;
}

.operacoes__table thead th {
  background: var(--bg-surface-sunken);
  color: var(--text-muted);
  font-weight: 600;
  font-size: 12px;
  letter-spacing: 0.05em;
  text-transform: uppercase;
  border-bottom: 1px solid var(--border-hairline);
}

.operacoes__table tbody tr {
  transition: background-color 0.15s ease;
}

.operacoes__table tbody tr + tr td {
  border-top: 1px solid var(--border-hairline);
}

.operacoes__table tbody tr:hover {
  background: var(--brand-050);
}

.operacoes__nome {
  font-weight: 600;
}

.operacoes__codigo {
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 12.5px;
  color: var(--text-secondary);
}

.operacoes__col-acoes {
  width: 1%;
  white-space: nowrap;
}

.operacoes__acoes {
  display: flex;
  align-items: center;
  gap: 8px;
}

.operacoes__acao-btn {
  width: 32px;
  height: 32px;
  display: grid;
  place-items: center;
  border: 1px solid var(--border-hairline);
  background: var(--bg-surface);
  color: var(--text-secondary);
  cursor: pointer;
  transition: background-color 0.15s ease, color 0.15s ease, border-color 0.15s ease;
}

.operacoes__acao-btn:hover:not(:disabled) {
  background: var(--bg-surface-sunken);
  color: var(--text-primary);
}

.operacoes__acao-btn--perigo:hover:not(:disabled) {
  border-color: var(--status-critical);
  color: var(--text-critical);
}

.operacoes__acao-btn:disabled {
  opacity: 0.6;
  cursor: default;
}

.operacoes__paginacao {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 20px;
  border-top: 1px solid var(--border-hairline);
}

.operacoes__total {
  font-size: 12.5px;
  color: var(--text-muted);
}

.operacoes__paginacao-controles {
  display: flex;
  align-items: center;
  gap: 10px;
}

.operacoes__pagina-atual {
  font-size: 12.5px;
  color: var(--text-secondary);
  white-space: nowrap;
}

.operacoes__pagina-btn {
  width: 30px;
  height: 30px;
  display: grid;
  place-items: center;
  border: 1px solid var(--border-hairline);
  background: var(--bg-surface);
  color: var(--text-secondary);
  cursor: pointer;
  transition: background-color 0.15s ease;
}

.operacoes__pagina-btn:hover:not(:disabled) {
  background: var(--bg-surface-sunken);
}

.operacoes__pagina-btn:disabled {
  opacity: 0.5;
  cursor: default;
}

@media (max-width: 720px) {
  .operacoes {
    padding: 16px 16px 32px;
  }

  .operacoes__toolbar {
    padding: 14px 16px;
  }

  .operacoes__filtro {
    width: 100%;
  }

  .operacoes__campo {
    flex: 1;
    min-width: 0;
  }

  .operacoes__campo input,
  .operacoes__campo select {
    width: 100%;
  }

  .operacoes__toolbar > .btn--primary {
    width: 100%;
    justify-content: center;
  }

  .operacoes__table thead {
    display: none;
  }

  .operacoes__table,
  .operacoes__table tbody,
  .operacoes__table tr,
  .operacoes__table td {
    display: block;
    width: 100%;
  }

  .operacoes__table tbody tr {
    padding: 14px 16px;
  }

  .operacoes__table tbody tr + tr {
    border-top: 1px solid var(--border-hairline);
  }

  .operacoes__table tbody tr + tr td {
    border-top: none;
  }

  .operacoes__table td {
    padding: 4px 0;
  }

  .operacoes__table td[data-label]::before {
    content: attr(data-label);
    display: block;
    font-size: 11px;
    font-weight: 600;
    letter-spacing: 0.05em;
    text-transform: uppercase;
    color: var(--text-muted);
    margin-bottom: 2px;
  }

  .operacoes__col-acoes {
    padding-top: 10px;
  }

  .operacoes__acoes {
    justify-content: flex-end;
  }

  .operacoes__paginacao {
    flex-wrap: wrap;
    justify-content: center;
    text-align: center;
    padding: 14px 16px;
  }
}
</style>
