<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import AppIcon from '../../components/AppIcon.vue'
import { ApiError } from '../../lib/http'
import { carregarTodas } from '../../lib/paginacao'
import { aluguelService } from '../../services/aluguel.service'
import { boxService } from '../../services/box.service'
import { clienteService } from '../../services/cliente.service'
import { unidadeStore } from '../../stores/unidade'
import type { AluguelResponseDTO, BoxResponseDTO, ClienteResponseDTO } from '../../types/api'

const TAMANHO_PAGINA = 10

const router = useRouter()

const alugueis = ref<AluguelResponseDTO[]>([])
const boxes = ref<BoxResponseDTO[]>([])
const clientes = ref<ClienteResponseDTO[]>([])
const carregando = ref(true)
const erro = ref('')
const excluindoId = ref('')

const pagina = ref(0)
const totalPaginas = ref(0)
const totalElementos = ref(0)

const filtro = reactive({ idBox: '', idCliente: '', status: '' })

const moeda = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })

function formatarData(data?: string) {
  return data ? new Date(data).toLocaleDateString('pt-BR') : '—'
}

async function carregarOpcoes() {
  const unidade = unidadeStore.state.selecionada
  if (!unidade) return

  boxes.value = await carregarTodas((p, t) => boxService.listar(p, t, { idUnidade: unidade.id }))
  if (boxes.value.length > 0) filtro.idBox = boxes.value[0].id

  try {
    clientes.value = await carregarTodas((p, t) => clienteService.listar(p, t))
  } catch {
    // A listagem de aluguéis ainda funciona sem as opções do filtro de cliente.
  }
}

async function carregar() {
  if (!filtro.idBox) return

  carregando.value = true
  erro.value = ''

  try {
    const resultado = await aluguelService.listar(pagina.value, TAMANHO_PAGINA, {
      idBox: filtro.idBox,
      idCliente: filtro.idCliente || undefined,
      status: filtro.status === '' ? undefined : filtro.status === 'true',
    })
    alugueis.value = resultado.content
    totalPaginas.value = resultado.totalPages
    totalElementos.value = resultado.totalElements
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível carregar os aluguéis.'
  } finally {
    carregando.value = false
  }
}

function buscar() {
  pagina.value = 0
  carregar()
}

function limparFiltro() {
  filtro.idCliente = ''
  filtro.status = ''
  buscar()
}

function irParaPagina(novaPagina: number) {
  if (novaPagina < 0 || novaPagina >= totalPaginas.value) return
  pagina.value = novaPagina
  carregar()
}

function novoAluguel() {
  router.push('/alugueis/novo')
}

function editarAluguel(aluguel: AluguelResponseDTO) {
  router.push(`/alugueis/${aluguel.id}/editar`)
}

async function excluirAluguel(aluguel: AluguelResponseDTO) {
  if (!confirm(`Excluir o aluguel do box "${aluguel.box?.numero}" para "${aluguel.cliente?.nome}"?`)) return

  excluindoId.value = aluguel.id
  try {
    await aluguelService.excluir(aluguel.id)
    if (alugueis.value.length === 1 && pagina.value > 0) pagina.value -= 1
    await carregar()
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível excluir o aluguel.'
  } finally {
    excluindoId.value = ''
  }
}

async function iniciar() {
  carregando.value = true
  erro.value = ''

  try {
    await carregarOpcoes()
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível carregar os boxes da unidade.'
    carregando.value = false
    return
  }

  if (filtro.idBox) {
    await carregar()
  } else {
    carregando.value = false
  }
}

onMounted(iniciar)
</script>

<template>
  <div class="alugueis">
    <div class="alugueis__painel">
      <div class="alugueis__toolbar">
        <form class="alugueis__filtro" @submit.prevent="buscar">
          <div class="alugueis__campo">
            <AppIcon name="box" :size="16" />
            <select v-model="filtro.idBox" aria-label="Box" :disabled="boxes.length === 0">
              <option v-for="box in boxes" :key="box.id" :value="box.id">Box {{ box.numero }}</option>
            </select>
          </div>

          <div class="alugueis__campo">
            <AppIcon name="users" :size="16" />
            <select v-model="filtro.idCliente" aria-label="Filtrar por cliente">
              <option value="">Todos os clientes</option>
              <option v-for="cliente in clientes" :key="cliente.id" :value="cliente.id">{{ cliente.nome }}</option>
            </select>
          </div>

          <div class="alugueis__campo">
            <AppIcon name="check-circle" :size="16" />
            <select v-model="filtro.status" aria-label="Filtrar por situação">
              <option value="">Todas as situações</option>
              <option value="true">Ativos</option>
              <option value="false">Inativos</option>
            </select>
          </div>

          <button type="submit" class="btn" :disabled="!filtro.idBox">Filtrar</button>
          <button v-if="filtro.idCliente || filtro.status" type="button" class="btn" @click="limparFiltro">
            Limpar
          </button>
        </form>

        <button type="button" class="btn btn--primary" @click="novoAluguel">
          <AppIcon name="plus" :size="16" />
          Novo aluguel
        </button>
      </div>

      <div v-if="carregando" class="alugueis__state">
        <AppIcon name="loader" :size="20" class="alugueis__spinner" />
        <span>Carregando aluguéis...</span>
      </div>

      <div v-else-if="erro" class="alugueis__state alugueis__state--error">
        <AppIcon name="alert-circle" :size="20" />
        <span>{{ erro }}</span>
        <button type="button" class="btn" @click="iniciar">Tentar novamente</button>
      </div>

      <div v-else-if="boxes.length === 0" class="alugueis__state">
        <AppIcon name="box" :size="20" />
        <span>A unidade selecionada ainda não tem boxes. Cadastre um box para registrar aluguéis.</span>
      </div>

      <div v-else-if="alugueis.length === 0" class="alugueis__state">
        <AppIcon name="bag" :size="20" />
        <span>Nenhum aluguel encontrado para este box.</span>
      </div>

      <table v-else class="alugueis__table">
        <thead>
          <tr>
            <th>Box</th>
            <th>Cliente</th>
            <th>Valor</th>
            <th>Situação</th>
            <th>Cadastrado em</th>
            <th class="alugueis__col-acoes">Ações</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="aluguel in alugueis" :key="aluguel.id">
            <td class="alugueis__nome" data-label="Box">{{ aluguel.box?.numero }}</td>
            <td data-label="Cliente">{{ aluguel.cliente?.nome }}</td>
            <td data-label="Valor">{{ moeda.format(aluguel.valor) }}</td>
            <td data-label="Situação">
              <span class="badge" :class="aluguel.status ? 'badge--good' : 'badge--warning'">
                <span class="badge__dot" />
                {{ aluguel.status ? 'Ativo' : 'Inativo' }}
              </span>
            </td>
            <td data-label="Cadastrado em">{{ formatarData(aluguel.createdAt) }}</td>
            <td class="alugueis__col-acoes">
              <div class="alugueis__acoes">
                <button
                  type="button"
                  class="alugueis__acao-btn"
                  aria-label="Editar aluguel"
                  title="Editar"
                  @click="editarAluguel(aluguel)"
                >
                  <AppIcon name="pencil" :size="16" />
                </button>
                <button
                  type="button"
                  class="alugueis__acao-btn alugueis__acao-btn--perigo"
                  aria-label="Excluir aluguel"
                  title="Excluir"
                  :disabled="excluindoId === aluguel.id"
                  @click="excluirAluguel(aluguel)"
                >
                  <AppIcon
                    :name="excluindoId === aluguel.id ? 'loader' : 'trash'"
                    :size="16"
                    :class="{ 'alugueis__spinner': excluindoId === aluguel.id }"
                  />
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>

      <div v-if="!carregando && !erro && alugueis.length > 0" class="alugueis__paginacao">
        <span class="alugueis__total">{{ totalElementos }} aluguel(éis) no total</span>

        <div class="alugueis__paginacao-controles">
          <button
            type="button"
            class="alugueis__pagina-btn"
            aria-label="Página anterior"
            :disabled="pagina === 0"
            @click="irParaPagina(pagina - 1)"
          >
            <AppIcon name="chevron-left" :size="16" />
          </button>

          <span class="alugueis__pagina-atual">Página {{ pagina + 1 }} de {{ Math.max(totalPaginas, 1) }}</span>

          <button
            type="button"
            class="alugueis__pagina-btn"
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
.alugueis {
  padding: 22px 28px 40px;
}

.alugueis__painel {
  background: var(--bg-surface);
  border: 1px solid var(--border-hairline);
  box-shadow: var(--shadow-card);
}

.alugueis__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-hairline);
}

.alugueis__filtro {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.alugueis__campo {
  display: flex;
  align-items: center;
  gap: 8px;
  background: var(--bg-surface);
  border: 1px solid var(--border-hairline);
  padding: 8px 12px;
  color: var(--text-muted);
}

.alugueis__campo input,
.alugueis__campo select {
  border: none;
  outline: none;
  background: transparent;
  font: inherit;
  font-size: 13.5px;
  color: var(--text-primary);
  width: 160px;
}

.alugueis__campo input::placeholder {
  color: var(--text-muted);
}

.alugueis__filtro .btn {
  border-radius: 0;
}

.alugueis__state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 48px 16px;
  text-align: center;
  color: var(--text-muted);
  font-size: 13.5px;
}

.alugueis__state--error {
  color: var(--text-critical);
}

.alugueis__spinner {
  animation: alugueis-spin 0.8s linear infinite;
  color: var(--brand-500);
}

@keyframes alugueis-spin {
  to {
    transform: rotate(360deg);
  }
}

.alugueis__table {
  width: 100%;
  border-collapse: collapse;
}

.alugueis__table th,
.alugueis__table td {
  text-align: left;
  padding: 13px 20px;
  font-size: 13.5px;
}

.alugueis__table thead th {
  background: var(--bg-surface-sunken);
  color: var(--text-muted);
  font-weight: 600;
  font-size: 12px;
  letter-spacing: 0.05em;
  text-transform: uppercase;
  border-bottom: 1px solid var(--border-hairline);
}

.alugueis__table tbody tr {
  transition: background-color 0.15s ease;
}

.alugueis__table tbody tr + tr td {
  border-top: 1px solid var(--border-hairline);
}

.alugueis__table tbody tr:hover {
  background: var(--brand-050);
}

.alugueis__nome {
  font-weight: 600;
}

.alugueis__col-acoes {
  width: 1%;
  white-space: nowrap;
}

.alugueis__acoes {
  display: flex;
  align-items: center;
  gap: 8px;
}

.alugueis__acao-btn {
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

.alugueis__acao-btn:hover:not(:disabled) {
  background: var(--bg-surface-sunken);
  color: var(--text-primary);
}

.alugueis__acao-btn--perigo:hover:not(:disabled) {
  border-color: var(--status-critical);
  color: var(--text-critical);
}

.alugueis__acao-btn:disabled {
  opacity: 0.6;
  cursor: default;
}

.alugueis__paginacao {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 20px;
  border-top: 1px solid var(--border-hairline);
}

.alugueis__total {
  font-size: 12.5px;
  color: var(--text-muted);
}

.alugueis__paginacao-controles {
  display: flex;
  align-items: center;
  gap: 10px;
}

.alugueis__pagina-atual {
  font-size: 12.5px;
  color: var(--text-secondary);
  white-space: nowrap;
}

.alugueis__pagina-btn {
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

.alugueis__pagina-btn:hover:not(:disabled) {
  background: var(--bg-surface-sunken);
}

.alugueis__pagina-btn:disabled {
  opacity: 0.5;
  cursor: default;
}

@media (max-width: 720px) {
  .alugueis {
    padding: 16px 16px 32px;
  }

  .alugueis__toolbar {
    padding: 14px 16px;
  }

  .alugueis__filtro {
    width: 100%;
  }

  .alugueis__campo {
    flex: 1;
    min-width: 0;
  }

  .alugueis__campo input,
  .alugueis__campo select {
    width: 100%;
  }

  .alugueis__toolbar > .btn--primary {
    width: 100%;
    justify-content: center;
  }

  .alugueis__table thead {
    display: none;
  }

  .alugueis__table,
  .alugueis__table tbody,
  .alugueis__table tr,
  .alugueis__table td {
    display: block;
    width: 100%;
  }

  .alugueis__table tbody tr {
    padding: 14px 16px;
  }

  .alugueis__table tbody tr + tr {
    border-top: 1px solid var(--border-hairline);
  }

  .alugueis__table tbody tr + tr td {
    border-top: none;
  }

  .alugueis__table td {
    padding: 4px 0;
  }

  .alugueis__table td[data-label]::before {
    content: attr(data-label);
    display: block;
    font-size: 11px;
    font-weight: 600;
    letter-spacing: 0.05em;
    text-transform: uppercase;
    color: var(--text-muted);
    margin-bottom: 2px;
  }

  .alugueis__col-acoes {
    padding-top: 10px;
  }

  .alugueis__acoes {
    justify-content: flex-end;
  }

  .alugueis__paginacao {
    flex-wrap: wrap;
    justify-content: center;
    text-align: center;
    padding: 14px 16px;
  }
}
</style>
