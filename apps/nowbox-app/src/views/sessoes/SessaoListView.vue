<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import AppIcon from '../../components/AppIcon.vue'
import { ApiError } from '../../lib/http'
import { sessaoService } from '../../services/sessao.service'
import type { SessaoResponseDTO } from '../../types/api'

const TAMANHO_PAGINA = 10

const router = useRouter()

const sessoes = ref<SessaoResponseDTO[]>([])
const carregando = ref(false)
const erro = ref('')
const excluindoId = ref('')

const pagina = ref(0)
const totalPaginas = ref(0)
const totalElementos = ref(0)

const filtro = reactive({ nome: '', rota: '' })

async function carregar() {
  carregando.value = true
  erro.value = ''

  try {
    const resultado = await sessaoService.listar(pagina.value, TAMANHO_PAGINA, {
      nome: filtro.nome.trim() || undefined,
      rota: filtro.rota.trim() || undefined,
    })
    sessoes.value = resultado.content
    totalPaginas.value = resultado.page.totalPages
    totalElementos.value = resultado.page.totalElements
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível carregar as sessões.'
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
  filtro.rota = ''
  buscar()
}

function irParaPagina(novaPagina: number) {
  if (novaPagina < 0 || novaPagina >= totalPaginas.value) return
  pagina.value = novaPagina
  carregar()
}

function novaSessao() {
  router.push('/sessoes/nova')
}

function editarSessao(sessao: SessaoResponseDTO) {
  router.push(`/sessoes/${sessao.id}/editar`)
}

async function excluirSessao(sessao: SessaoResponseDTO) {
  if (!confirm(`Excluir a sessão "${sessao.nome}"?`)) return

  excluindoId.value = sessao.id
  try {
    await sessaoService.excluir(sessao.id)
    if (sessoes.value.length === 1 && pagina.value > 0) pagina.value -= 1
    await carregar()
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível excluir a sessão.'
  } finally {
    excluindoId.value = ''
  }
}

carregar()
</script>

<template>
  <div class="sessoes">
    <div class="sessoes__painel">
      <div class="sessoes__toolbar">
        <form class="sessoes__filtro" @submit.prevent="buscar">
          <div class="sessoes__campo">
            <AppIcon name="search" :size="16" />
            <input v-model="filtro.nome" type="text" placeholder="Buscar por nome" aria-label="Buscar por nome" />
          </div>

          <div class="sessoes__campo">
            <AppIcon name="folder" :size="16" />
            <input v-model="filtro.rota" type="text" placeholder="Buscar por rota" aria-label="Buscar por rota" />
          </div>

          <button type="submit" class="btn">Filtrar</button>
          <button v-if="filtro.nome || filtro.rota" type="button" class="btn" @click="limparFiltro">Limpar</button>
        </form>

        <button type="button" class="btn btn--primary" @click="novaSessao">
          <AppIcon name="plus" :size="16" />
          Nova sessão
        </button>
      </div>

      <div v-if="carregando" class="sessoes__state">
        <AppIcon name="loader" :size="20" class="sessoes__spinner" />
        <span>Carregando sessões...</span>
      </div>

      <div v-else-if="erro" class="sessoes__state sessoes__state--error">
        <AppIcon name="alert-circle" :size="20" />
        <span>{{ erro }}</span>
        <button type="button" class="btn" @click="carregar">Tentar novamente</button>
      </div>

      <div v-else-if="sessoes.length === 0" class="sessoes__state">
        <AppIcon name="folder" :size="20" />
        <span>Nenhuma sessão encontrada.</span>
      </div>

      <table v-else class="sessoes__table">
        <thead>
          <tr>
            <th>Nome</th>
            <th>Rota</th>
            <th class="sessoes__col-acoes">Ações</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="sessao in sessoes" :key="sessao.id">
            <td class="sessoes__nome" data-label="Nome">{{ sessao.nome }}</td>
            <td data-label="Rota"><span class="sessoes__rota">{{ sessao.rota }}</span></td>
            <td class="sessoes__col-acoes">
              <div class="sessoes__acoes">
                <button
                  type="button"
                  class="sessoes__acao-btn"
                  aria-label="Editar sessão"
                  title="Editar"
                  @click="editarSessao(sessao)"
                >
                  <AppIcon name="pencil" :size="16" />
                </button>
                <button
                  type="button"
                  class="sessoes__acao-btn sessoes__acao-btn--perigo"
                  aria-label="Excluir sessão"
                  title="Excluir"
                  :disabled="excluindoId === sessao.id"
                  @click="excluirSessao(sessao)"
                >
                  <AppIcon :name="excluindoId === sessao.id ? 'loader' : 'trash'" :size="16" :class="{ 'sessoes__spinner': excluindoId === sessao.id }" />
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>

      <div v-if="!carregando && !erro && sessoes.length > 0" class="sessoes__paginacao">
        <span class="sessoes__total">{{ totalElementos }} sessão(ões) no total</span>

        <div class="sessoes__paginacao-controles">
          <button
            type="button"
            class="sessoes__pagina-btn"
            aria-label="Página anterior"
            :disabled="pagina === 0"
            @click="irParaPagina(pagina - 1)"
          >
            <AppIcon name="chevron-left" :size="16" />
          </button>

          <span class="sessoes__pagina-atual">Página {{ pagina + 1 }} de {{ Math.max(totalPaginas, 1) }}</span>

          <button
            type="button"
            class="sessoes__pagina-btn"
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
.sessoes {
  padding: 22px 28px 40px;
}

.sessoes__painel {
  background: var(--bg-surface);
  border: 1px solid var(--border-hairline);
  box-shadow: var(--shadow-card);
}

.sessoes__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-hairline);
}

.sessoes__filtro {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.sessoes__campo {
  display: flex;
  align-items: center;
  gap: 8px;
  background: var(--bg-surface);
  border: 1px solid var(--border-hairline);
  padding: 8px 12px;
  color: var(--text-muted);
}

.sessoes__campo input {
  border: none;
  outline: none;
  background: transparent;
  font: inherit;
  font-size: 13.5px;
  color: var(--text-primary);
  width: 170px;
}

.sessoes__campo input::placeholder {
  color: var(--text-muted);
}

.sessoes__filtro .btn {
  border-radius: 0;
}

.sessoes__state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 48px 16px;
  text-align: center;
  color: var(--text-muted);
  font-size: 13.5px;
}

.sessoes__state--error {
  color: var(--text-critical);
}

.sessoes__spinner {
  animation: sessoes-spin 0.8s linear infinite;
  color: var(--brand-500);
}

@keyframes sessoes-spin {
  to {
    transform: rotate(360deg);
  }
}

.sessoes__table {
  width: 100%;
  border-collapse: collapse;
}

.sessoes__table th,
.sessoes__table td {
  text-align: left;
  padding: 13px 20px;
  font-size: 13.5px;
}

.sessoes__table thead th {
  background: var(--bg-surface-sunken);
  color: var(--text-muted);
  font-weight: 600;
  font-size: 12px;
  letter-spacing: 0.05em;
  text-transform: uppercase;
  border-bottom: 1px solid var(--border-hairline);
}

.sessoes__table tbody tr {
  transition: background-color 0.15s ease;
}

.sessoes__table tbody tr + tr td {
  border-top: 1px solid var(--border-hairline);
}

.sessoes__table tbody tr:hover {
  background: var(--brand-050);
}

.sessoes__nome {
  font-weight: 600;
}

.sessoes__rota {
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 12.5px;
  color: var(--text-secondary);
}

.sessoes__col-acoes {
  width: 1%;
  white-space: nowrap;
}

.sessoes__acoes {
  display: flex;
  align-items: center;
  gap: 8px;
}

.sessoes__acao-btn {
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

.sessoes__acao-btn:hover:not(:disabled) {
  background: var(--bg-surface-sunken);
  color: var(--text-primary);
}

.sessoes__acao-btn--perigo:hover:not(:disabled) {
  border-color: var(--status-critical);
  color: var(--text-critical);
}

.sessoes__acao-btn:disabled {
  opacity: 0.6;
  cursor: default;
}

.sessoes__paginacao {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 20px;
  border-top: 1px solid var(--border-hairline);
}

.sessoes__total {
  font-size: 12.5px;
  color: var(--text-muted);
}

.sessoes__paginacao-controles {
  display: flex;
  align-items: center;
  gap: 10px;
}

.sessoes__pagina-atual {
  font-size: 12.5px;
  color: var(--text-secondary);
  white-space: nowrap;
}

.sessoes__pagina-btn {
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

.sessoes__pagina-btn:hover:not(:disabled) {
  background: var(--bg-surface-sunken);
}

.sessoes__pagina-btn:disabled {
  opacity: 0.5;
  cursor: default;
}

@media (max-width: 720px) {
  .sessoes {
    padding: 16px 16px 32px;
  }

  .sessoes__toolbar {
    padding: 14px 16px;
  }

  .sessoes__filtro {
    width: 100%;
  }

  .sessoes__campo {
    flex: 1;
    min-width: 0;
  }

  .sessoes__campo input {
    width: 100%;
  }

  .sessoes__toolbar > .btn--primary {
    width: 100%;
    justify-content: center;
  }

  .sessoes__table thead {
    display: none;
  }

  .sessoes__table,
  .sessoes__table tbody,
  .sessoes__table tr,
  .sessoes__table td {
    display: block;
    width: 100%;
  }

  .sessoes__table tbody tr {
    padding: 14px 16px;
  }

  .sessoes__table tbody tr + tr {
    border-top: 1px solid var(--border-hairline);
  }

  .sessoes__table tbody tr + tr td {
    border-top: none;
  }

  .sessoes__table td {
    padding: 4px 0;
  }

  .sessoes__table td[data-label]::before {
    content: attr(data-label);
    display: block;
    font-size: 11px;
    font-weight: 600;
    letter-spacing: 0.05em;
    text-transform: uppercase;
    color: var(--text-muted);
    margin-bottom: 2px;
  }

  .sessoes__col-acoes {
    padding-top: 10px;
  }

  .sessoes__acoes {
    justify-content: flex-end;
  }

  .sessoes__paginacao {
    flex-wrap: wrap;
    justify-content: center;
    text-align: center;
    padding: 14px 16px;
  }
}
</style>
