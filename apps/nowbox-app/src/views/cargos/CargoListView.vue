<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import AppIcon from '../../components/AppIcon.vue'
import { ApiError } from '../../lib/http'
import { cargoService } from '../../services/cargo.service'
import { unidadeService } from '../../services/unidade.service'
import type { CargoResponseDTO, UnidadeEntity } from '../../types/api'

const TAMANHO_PAGINA = 10

const router = useRouter()

const cargos = ref<CargoResponseDTO[]>([])
const unidades = ref<UnidadeEntity[]>([])
const carregando = ref(false)
const erro = ref('')
const excluindoId = ref('')

const pagina = ref(0)
const totalPaginas = ref(0)
const totalElementos = ref(0)

const filtro = reactive({ nome: '', idUnidade: '' })

async function carregarUnidades() {
  try {
    const resultado = await unidadeService.listar(0, 100)
    unidades.value = resultado.content
  } catch {
    // A listagem de cargos ainda funciona sem as unidades para o filtro.
  }
}

async function carregar() {
  carregando.value = true
  erro.value = ''

  try {
    const resultado = await cargoService.listar(pagina.value, TAMANHO_PAGINA, {
      nome: filtro.nome.trim() || undefined,
      idUnidade: filtro.idUnidade || undefined,
    })
    cargos.value = resultado.content
    totalPaginas.value = resultado.totalPages
    totalElementos.value = resultado.totalElements
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível carregar os cargos.'
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
  filtro.idUnidade = ''
  buscar()
}

function irParaPagina(novaPagina: number) {
  if (novaPagina < 0 || novaPagina >= totalPaginas.value) return
  pagina.value = novaPagina
  carregar()
}

function novoCargo() {
  router.push('/cargos/novo')
}

function editarCargo(cargo: CargoResponseDTO) {
  router.push(`/cargos/${cargo.id}/editar`)
}

async function excluirCargo(cargo: CargoResponseDTO) {
  if (!confirm(`Excluir o cargo "${cargo.nome}"?`)) return

  excluindoId.value = cargo.id
  try {
    await cargoService.excluir(cargo.id)
    if (cargos.value.length === 1 && pagina.value > 0) pagina.value -= 1
    await carregar()
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível excluir o cargo.'
  } finally {
    excluindoId.value = ''
  }
}

onMounted(() => {
  carregarUnidades()
  carregar()
})
</script>

<template>
  <div class="cargos">
    <div class="cargos__painel">
      <div class="cargos__toolbar">
        <form class="cargos__filtro" @submit.prevent="buscar">
          <div class="cargos__campo">
            <AppIcon name="search" :size="16" />
            <input v-model="filtro.nome" type="text" placeholder="Buscar por nome" aria-label="Buscar por nome" />
          </div>

          <div class="cargos__campo">
            <AppIcon name="building" :size="16" />
            <select v-model="filtro.idUnidade" aria-label="Filtrar por unidade">
              <option value="">Todas as unidades</option>
              <option v-for="unidade in unidades" :key="unidade.id" :value="unidade.id">{{ unidade.nome }}</option>
            </select>
          </div>

          <button type="submit" class="btn">Filtrar</button>
          <button v-if="filtro.nome || filtro.idUnidade" type="button" class="btn" @click="limparFiltro">
            Limpar
          </button>
        </form>

        <button type="button" class="btn btn--primary" @click="novoCargo">
          <AppIcon name="plus" :size="16" />
          Novo cargo
        </button>
      </div>

      <div v-if="carregando" class="cargos__state">
        <AppIcon name="loader" :size="20" class="cargos__spinner" />
        <span>Carregando cargos...</span>
      </div>

      <div v-else-if="erro" class="cargos__state cargos__state--error">
        <AppIcon name="alert-circle" :size="20" />
        <span>{{ erro }}</span>
        <button type="button" class="btn" @click="carregar">Tentar novamente</button>
      </div>

      <div v-else-if="cargos.length === 0" class="cargos__state">
        <AppIcon name="tag" :size="20" />
        <span>Nenhum cargo encontrado.</span>
      </div>

      <table v-else class="cargos__table">
        <thead>
          <tr>
            <th>Nome</th>
            <th>Unidade</th>
            <th class="cargos__col-acoes">Ações</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="cargo in cargos" :key="cargo.id">
            <td class="cargos__nome" data-label="Nome">{{ cargo.nome }}</td>
            <td data-label="Unidade">{{ cargo.unidade?.nome }}</td>
            <td class="cargos__col-acoes">
              <div class="cargos__acoes">
                <button
                  type="button"
                  class="cargos__acao-btn"
                  aria-label="Editar cargo"
                  title="Editar"
                  @click="editarCargo(cargo)"
                >
                  <AppIcon name="pencil" :size="16" />
                </button>
                <button
                  type="button"
                  class="cargos__acao-btn cargos__acao-btn--perigo"
                  aria-label="Excluir cargo"
                  title="Excluir"
                  :disabled="excluindoId === cargo.id"
                  @click="excluirCargo(cargo)"
                >
                  <AppIcon
                    :name="excluindoId === cargo.id ? 'loader' : 'trash'"
                    :size="16"
                    :class="{ 'cargos__spinner': excluindoId === cargo.id }"
                  />
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>

      <div v-if="!carregando && !erro && cargos.length > 0" class="cargos__paginacao">
        <span class="cargos__total">{{ totalElementos }} cargo(s) no total</span>

        <div class="cargos__paginacao-controles">
          <button
            type="button"
            class="cargos__pagina-btn"
            aria-label="Página anterior"
            :disabled="pagina === 0"
            @click="irParaPagina(pagina - 1)"
          >
            <AppIcon name="chevron-left" :size="16" />
          </button>

          <span class="cargos__pagina-atual">Página {{ pagina + 1 }} de {{ Math.max(totalPaginas, 1) }}</span>

          <button
            type="button"
            class="cargos__pagina-btn"
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
.cargos {
  padding: 22px 28px 40px;
}

.cargos__painel {
  background: var(--bg-surface);
  border: 1px solid var(--border-hairline);
  box-shadow: var(--shadow-card);
}

.cargos__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-hairline);
}

.cargos__filtro {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.cargos__campo {
  display: flex;
  align-items: center;
  gap: 8px;
  background: var(--bg-surface);
  border: 1px solid var(--border-hairline);
  padding: 8px 12px;
  color: var(--text-muted);
}

.cargos__campo input,
.cargos__campo select {
  border: none;
  outline: none;
  background: transparent;
  font: inherit;
  font-size: 13.5px;
  color: var(--text-primary);
  width: 170px;
}

.cargos__campo input::placeholder {
  color: var(--text-muted);
}

.cargos__filtro .btn {
  border-radius: 0;
}

.cargos__state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 48px 16px;
  text-align: center;
  color: var(--text-muted);
  font-size: 13.5px;
}

.cargos__state--error {
  color: var(--text-critical);
}

.cargos__spinner {
  animation: cargos-spin 0.8s linear infinite;
  color: var(--brand-500);
}

@keyframes cargos-spin {
  to {
    transform: rotate(360deg);
  }
}

.cargos__table {
  width: 100%;
  border-collapse: collapse;
}

.cargos__table th,
.cargos__table td {
  text-align: left;
  padding: 13px 20px;
  font-size: 13.5px;
}

.cargos__table thead th {
  background: var(--bg-surface-sunken);
  color: var(--text-muted);
  font-weight: 600;
  font-size: 12px;
  letter-spacing: 0.05em;
  text-transform: uppercase;
  border-bottom: 1px solid var(--border-hairline);
}

.cargos__table tbody tr {
  transition: background-color 0.15s ease;
}

.cargos__table tbody tr + tr td {
  border-top: 1px solid var(--border-hairline);
}

.cargos__table tbody tr:hover {
  background: var(--brand-050);
}

.cargos__nome {
  font-weight: 600;
}

.cargos__col-acoes {
  width: 1%;
  white-space: nowrap;
}

.cargos__acoes {
  display: flex;
  align-items: center;
  gap: 8px;
}

.cargos__acao-btn {
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

.cargos__acao-btn:hover:not(:disabled) {
  background: var(--bg-surface-sunken);
  color: var(--text-primary);
}

.cargos__acao-btn--perigo:hover:not(:disabled) {
  border-color: var(--status-critical);
  color: var(--text-critical);
}

.cargos__acao-btn:disabled {
  opacity: 0.6;
  cursor: default;
}

.cargos__paginacao {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 20px;
  border-top: 1px solid var(--border-hairline);
}

.cargos__total {
  font-size: 12.5px;
  color: var(--text-muted);
}

.cargos__paginacao-controles {
  display: flex;
  align-items: center;
  gap: 10px;
}

.cargos__pagina-atual {
  font-size: 12.5px;
  color: var(--text-secondary);
  white-space: nowrap;
}

.cargos__pagina-btn {
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

.cargos__pagina-btn:hover:not(:disabled) {
  background: var(--bg-surface-sunken);
}

.cargos__pagina-btn:disabled {
  opacity: 0.5;
  cursor: default;
}

@media (max-width: 720px) {
  .cargos {
    padding: 16px 16px 32px;
  }

  .cargos__toolbar {
    padding: 14px 16px;
  }

  .cargos__filtro {
    width: 100%;
  }

  .cargos__campo {
    flex: 1;
    min-width: 0;
  }

  .cargos__campo input,
  .cargos__campo select {
    width: 100%;
  }

  .cargos__toolbar > .btn--primary {
    width: 100%;
    justify-content: center;
  }

  .cargos__table thead {
    display: none;
  }

  .cargos__table,
  .cargos__table tbody,
  .cargos__table tr,
  .cargos__table td {
    display: block;
    width: 100%;
  }

  .cargos__table tbody tr {
    padding: 14px 16px;
  }

  .cargos__table tbody tr + tr {
    border-top: 1px solid var(--border-hairline);
  }

  .cargos__table tbody tr + tr td {
    border-top: none;
  }

  .cargos__table td {
    padding: 4px 0;
  }

  .cargos__table td[data-label]::before {
    content: attr(data-label);
    display: block;
    font-size: 11px;
    font-weight: 600;
    letter-spacing: 0.05em;
    text-transform: uppercase;
    color: var(--text-muted);
    margin-bottom: 2px;
  }

  .cargos__col-acoes {
    padding-top: 10px;
  }

  .cargos__acoes {
    justify-content: flex-end;
  }

  .cargos__paginacao {
    flex-wrap: wrap;
    justify-content: center;
    text-align: center;
    padding: 14px 16px;
  }
}
</style>
