<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import AppIcon from '../../components/AppIcon.vue'
import { ApiError } from '../../lib/http'
import { carregarTodas } from '../../lib/paginacao'
import { cargoService } from '../../services/cargo.service'
import { permissaoService } from '../../services/permissao.service'
import type { CargoResponseDTO, PermissaoResponseDTO } from '../../types/api'

const TAMANHO_PAGINA = 10

const router = useRouter()

const permissoes = ref<PermissaoResponseDTO[]>([])
const cargos = ref<CargoResponseDTO[]>([])
const carregando = ref(false)
const erro = ref('')
const excluindoId = ref('')

const pagina = ref(0)
const totalPaginas = ref(0)
const totalElementos = ref(0)

const filtro = reactive({ idCargo: '' })

async function carregarCargos() {
  try {
    cargos.value = await carregarTodas((p, t) => cargoService.listar(p, t))
  } catch {
    // A listagem de permissões ainda funciona sem os cargos para o filtro.
  }
}

async function carregar() {
  carregando.value = true
  erro.value = ''

  try {
    const resultado = await permissaoService.listar(pagina.value, TAMANHO_PAGINA, {
      idCargo: filtro.idCargo || undefined,
    })
    permissoes.value = resultado.content
    totalPaginas.value = resultado.totalPages
    totalElementos.value = resultado.totalElements
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível carregar as permissões.'
  } finally {
    carregando.value = false
  }
}

function buscar() {
  pagina.value = 0
  carregar()
}

function limparFiltro() {
  filtro.idCargo = ''
  buscar()
}

function irParaPagina(novaPagina: number) {
  if (novaPagina < 0 || novaPagina >= totalPaginas.value) return
  pagina.value = novaPagina
  carregar()
}

function gerenciarPermissoes() {
  router.push('/permissoes/nova')
}

function editarPermissoes(permissao: PermissaoResponseDTO) {
  router.push(`/permissoes/cargo/${permissao.cargo.id}/editar`)
}

async function excluirPermissao(permissao: PermissaoResponseDTO) {
  if (!confirm(`Remover a permissão "${permissao.operacao.nome}" do cargo "${permissao.cargo.nome}"?`)) return

  excluindoId.value = permissao.id
  try {
    await permissaoService.excluir(permissao.id)
    if (permissoes.value.length === 1 && pagina.value > 0) pagina.value -= 1
    await carregar()
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível excluir a permissão.'
  } finally {
    excluindoId.value = ''
  }
}

onMounted(() => {
  carregarCargos()
  carregar()
})
</script>

<template>
  <div class="permissoes">
    <div class="permissoes__painel">
      <div class="permissoes__toolbar">
        <form class="permissoes__filtro" @submit.prevent="buscar">
          <div class="permissoes__campo">
            <AppIcon name="tag" :size="16" />
            <select v-model="filtro.idCargo" aria-label="Filtrar por cargo">
              <option value="">Todos os cargos</option>
              <option v-for="cargo in cargos" :key="cargo.id" :value="cargo.id">
                {{ cargo.nome }} — {{ cargo.unidade?.nome }}
              </option>
            </select>
          </div>

          <button type="submit" class="btn">Filtrar</button>
          <button v-if="filtro.idCargo" type="button" class="btn" @click="limparFiltro">Limpar</button>
        </form>

        <button type="button" class="btn btn--primary" @click="gerenciarPermissoes">
          <AppIcon name="plus" :size="16" />
          Gerenciar permissões
        </button>
      </div>

      <div v-if="carregando" class="permissoes__state">
        <AppIcon name="loader" :size="20" class="permissoes__spinner" />
        <span>Carregando permissões...</span>
      </div>

      <div v-else-if="erro" class="permissoes__state permissoes__state--error">
        <AppIcon name="alert-circle" :size="20" />
        <span>{{ erro }}</span>
        <button type="button" class="btn" @click="carregar">Tentar novamente</button>
      </div>

      <div v-else-if="permissoes.length === 0" class="permissoes__state">
        <AppIcon name="shield" :size="20" />
        <span>Nenhuma permissão encontrada.</span>
      </div>

      <table v-else class="permissoes__table">
        <thead>
          <tr>
            <th>Cargo</th>
            <th>Sessão</th>
            <th>Módulo</th>
            <th>Operação</th>
            <th class="permissoes__col-acoes">Ações</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="permissao in permissoes" :key="permissao.id">
            <td class="permissoes__nome" data-label="Cargo">
              {{ permissao.cargo?.nome }}
              <span class="permissoes__sub">{{ permissao.cargo?.unidade?.nome }}</span>
            </td>
            <td data-label="Sessão">{{ permissao.operacao?.modulo?.sessao?.nome }}</td>
            <td data-label="Módulo">{{ permissao.operacao?.modulo?.nome }}</td>
            <td data-label="Operação">
              {{ permissao.operacao?.nome }}
              <span class="permissoes__codigo">{{ permissao.operacao?.codigo }}</span>
            </td>
            <td class="permissoes__col-acoes">
              <div class="permissoes__acoes">
                <button
                  type="button"
                  class="permissoes__acao-btn"
                  aria-label="Editar permissões do cargo"
                  title="Editar permissões do cargo"
                  @click="editarPermissoes(permissao)"
                >
                  <AppIcon name="pencil" :size="16" />
                </button>
                <button
                  type="button"
                  class="permissoes__acao-btn permissoes__acao-btn--perigo"
                  aria-label="Excluir permissão"
                  title="Excluir"
                  :disabled="excluindoId === permissao.id"
                  @click="excluirPermissao(permissao)"
                >
                  <AppIcon
                    :name="excluindoId === permissao.id ? 'loader' : 'trash'"
                    :size="16"
                    :class="{ 'permissoes__spinner': excluindoId === permissao.id }"
                  />
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>

      <div v-if="!carregando && !erro && permissoes.length > 0" class="permissoes__paginacao">
        <span class="permissoes__total">{{ totalElementos }} permissão(ões) no total</span>

        <div class="permissoes__paginacao-controles">
          <button
            type="button"
            class="permissoes__pagina-btn"
            aria-label="Página anterior"
            :disabled="pagina === 0"
            @click="irParaPagina(pagina - 1)"
          >
            <AppIcon name="chevron-left" :size="16" />
          </button>

          <span class="permissoes__pagina-atual">Página {{ pagina + 1 }} de {{ Math.max(totalPaginas, 1) }}</span>

          <button
            type="button"
            class="permissoes__pagina-btn"
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
.permissoes {
  padding: 22px 28px 40px;
}

.permissoes__painel {
  background: var(--bg-surface);
  border: 1px solid var(--border-hairline);
  box-shadow: var(--shadow-card);
}

.permissoes__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-hairline);
}

.permissoes__filtro {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.permissoes__campo {
  display: flex;
  align-items: center;
  gap: 8px;
  background: var(--bg-surface);
  border: 1px solid var(--border-hairline);
  padding: 8px 12px;
  color: var(--text-muted);
}

.permissoes__campo input,
.permissoes__campo select {
  border: none;
  outline: none;
  background: transparent;
  font: inherit;
  font-size: 13.5px;
  color: var(--text-primary);
  width: 170px;
}

.permissoes__campo input::placeholder {
  color: var(--text-muted);
}

.permissoes__filtro .btn {
  border-radius: 0;
}

.permissoes__state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 48px 16px;
  text-align: center;
  color: var(--text-muted);
  font-size: 13.5px;
}

.permissoes__state--error {
  color: var(--text-critical);
}

.permissoes__spinner {
  animation: permissoes-spin 0.8s linear infinite;
  color: var(--brand-500);
}

@keyframes permissoes-spin {
  to {
    transform: rotate(360deg);
  }
}

.permissoes__table {
  width: 100%;
  border-collapse: collapse;
}

.permissoes__table th,
.permissoes__table td {
  text-align: left;
  padding: 13px 20px;
  font-size: 13.5px;
}

.permissoes__table thead th {
  background: var(--bg-surface-sunken);
  color: var(--text-muted);
  font-weight: 600;
  font-size: 12px;
  letter-spacing: 0.05em;
  text-transform: uppercase;
  border-bottom: 1px solid var(--border-hairline);
}

.permissoes__table tbody tr {
  transition: background-color 0.15s ease;
}

.permissoes__table tbody tr + tr td {
  border-top: 1px solid var(--border-hairline);
}

.permissoes__table tbody tr:hover {
  background: var(--brand-050);
}

.permissoes__nome {
  font-weight: 600;
}

.permissoes__codigo {
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 12.5px;
  color: var(--text-secondary);
}

.permissoes__sub {
  display: block;
  font-size: 12px;
  font-weight: 400;
  color: var(--text-muted);
}

.permissoes__col-acoes {
  width: 1%;
  white-space: nowrap;
}

.permissoes__acoes {
  display: flex;
  align-items: center;
  gap: 8px;
}

.permissoes__acao-btn {
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

.permissoes__acao-btn:hover:not(:disabled) {
  background: var(--bg-surface-sunken);
  color: var(--text-primary);
}

.permissoes__acao-btn--perigo:hover:not(:disabled) {
  border-color: var(--status-critical);
  color: var(--text-critical);
}

.permissoes__acao-btn:disabled {
  opacity: 0.6;
  cursor: default;
}

.permissoes__paginacao {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 20px;
  border-top: 1px solid var(--border-hairline);
}

.permissoes__total {
  font-size: 12.5px;
  color: var(--text-muted);
}

.permissoes__paginacao-controles {
  display: flex;
  align-items: center;
  gap: 10px;
}

.permissoes__pagina-atual {
  font-size: 12.5px;
  color: var(--text-secondary);
  white-space: nowrap;
}

.permissoes__pagina-btn {
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

.permissoes__pagina-btn:hover:not(:disabled) {
  background: var(--bg-surface-sunken);
}

.permissoes__pagina-btn:disabled {
  opacity: 0.5;
  cursor: default;
}

@media (max-width: 720px) {
  .permissoes {
    padding: 16px 16px 32px;
  }

  .permissoes__toolbar {
    padding: 14px 16px;
  }

  .permissoes__filtro {
    width: 100%;
  }

  .permissoes__campo {
    flex: 1;
    min-width: 0;
  }

  .permissoes__campo input,
  .permissoes__campo select {
    width: 100%;
  }

  .permissoes__toolbar > .btn--primary {
    width: 100%;
    justify-content: center;
  }

  .permissoes__table thead {
    display: none;
  }

  .permissoes__table,
  .permissoes__table tbody,
  .permissoes__table tr,
  .permissoes__table td {
    display: block;
    width: 100%;
  }

  .permissoes__table tbody tr {
    padding: 14px 16px;
  }

  .permissoes__table tbody tr + tr {
    border-top: 1px solid var(--border-hairline);
  }

  .permissoes__table tbody tr + tr td {
    border-top: none;
  }

  .permissoes__table td {
    padding: 4px 0;
  }

  .permissoes__table td[data-label]::before {
    content: attr(data-label);
    display: block;
    font-size: 11px;
    font-weight: 600;
    letter-spacing: 0.05em;
    text-transform: uppercase;
    color: var(--text-muted);
    margin-bottom: 2px;
  }

  .permissoes__col-acoes {
    padding-top: 10px;
  }

  .permissoes__acoes {
    justify-content: flex-end;
  }

  .permissoes__paginacao {
    flex-wrap: wrap;
    justify-content: center;
    text-align: center;
    padding: 14px 16px;
  }
}
</style>
