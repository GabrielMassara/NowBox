<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import AppIcon from '../../components/AppIcon.vue'
import { ApiError } from '../../lib/http'
import { boxService } from '../../services/box.service'
import { unidadeStore } from '../../stores/unidade'
import type { BoxResponseDTO } from '../../types/api'

const TAMANHO_PAGINA = 10

const router = useRouter()

const boxes = ref<BoxResponseDTO[]>([])
const carregando = ref(false)
const erro = ref('')
const excluindoId = ref('')

const pagina = ref(0)
const totalPaginas = ref(0)
const totalElementos = ref(0)

const filtro = reactive({ numero: '', disponivel: '' })

const moeda = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })
const metragem = new Intl.NumberFormat('pt-BR', { maximumFractionDigits: 2 })

async function carregar() {
  const unidade = unidadeStore.state.selecionada
  if (!unidade) return

  carregando.value = true
  erro.value = ''

  try {
    const resultado = await boxService.listar(pagina.value, TAMANHO_PAGINA, {
      idUnidade: unidade.id,
      numero: filtro.numero.trim() || undefined,
      disponivel: filtro.disponivel === '' ? undefined : filtro.disponivel === 'true',
    })
    boxes.value = resultado.content
    totalPaginas.value = resultado.totalPages
    totalElementos.value = resultado.totalElements
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível carregar os boxes.'
  } finally {
    carregando.value = false
  }
}

function buscar() {
  pagina.value = 0
  carregar()
}

function limparFiltro() {
  filtro.numero = ''
  filtro.disponivel = ''
  buscar()
}

function irParaPagina(novaPagina: number) {
  if (novaPagina < 0 || novaPagina >= totalPaginas.value) return
  pagina.value = novaPagina
  carregar()
}

function novoBox() {
  router.push('/boxes/novo')
}

function cadastrarEmLote() {
  router.push('/boxes/lote')
}

function editarBox(box: BoxResponseDTO) {
  router.push(`/boxes/${box.id}/editar`)
}

async function excluirBox(box: BoxResponseDTO) {
  if (!confirm(`Excluir o box "${box.numero}"?`)) return

  excluindoId.value = box.id
  try {
    await boxService.excluir(box.id)
    if (boxes.value.length === 1 && pagina.value > 0) pagina.value -= 1
    await carregar()
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível excluir o box.'
  } finally {
    excluindoId.value = ''
  }
}

onMounted(carregar)
</script>

<template>
  <div class="boxes">
    <div class="boxes__painel">
      <div class="boxes__toolbar">
        <form class="boxes__filtro" @submit.prevent="buscar">
          <div class="boxes__campo">
            <AppIcon name="search" :size="16" />
            <input v-model="filtro.numero" type="text" placeholder="Número do box" aria-label="Buscar por número" />
          </div>

          <div class="boxes__campo">
            <AppIcon name="box" :size="16" />
            <select v-model="filtro.disponivel" aria-label="Filtrar por situação">
              <option value="">Todas as situações</option>
              <option value="true">Liberados</option>
              <option value="false">Bloqueados</option>
            </select>
          </div>

          <button type="submit" class="btn">Filtrar</button>
          <button v-if="filtro.numero || filtro.disponivel" type="button" class="btn" @click="limparFiltro">
            Limpar
          </button>
        </form>

        <div class="boxes__acoes-topo">
          <button type="button" class="btn" @click="cadastrarEmLote">
            <AppIcon name="layers" :size="16" />
            Cadastrar em lote
          </button>
          <button type="button" class="btn btn--primary" @click="novoBox">
            <AppIcon name="plus" :size="16" />
            Novo box
          </button>
        </div>
      </div>

      <div v-if="carregando" class="boxes__state">
        <AppIcon name="loader" :size="20" class="boxes__spinner" />
        <span>Carregando boxes...</span>
      </div>

      <div v-else-if="erro" class="boxes__state boxes__state--error">
        <AppIcon name="alert-circle" :size="20" />
        <span>{{ erro }}</span>
        <button type="button" class="btn" @click="carregar">Tentar novamente</button>
      </div>

      <div v-else-if="boxes.length === 0" class="boxes__state">
        <AppIcon name="box" :size="20" />
        <span>Nenhum box encontrado.</span>
      </div>

      <table v-else class="boxes__table">
        <thead>
          <tr>
            <th>Número</th>
            <th>Tamanho</th>
            <th>Dimensões</th>
            <th>Preço</th>
            <th>Situação</th>
            <th class="boxes__col-acoes">Ações</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="box in boxes" :key="box.id">
            <td class="boxes__nome" data-label="Número">{{ box.numero }}</td>
            <td data-label="Tamanho">{{ metragem.format(box.tamanho) }} m²</td>
            <td data-label="Dimensões">{{ box.dimensoes }}</td>
            <td data-label="Preço">{{ moeda.format(box.preco) }}</td>
            <td data-label="Situação">
              <span class="badge" :class="box.disponivel ? 'badge--good' : 'badge--warning'">
                <span class="badge__dot" />
                {{ box.disponivel ? 'Liberado' : 'Bloqueado' }}
              </span>
            </td>
            <td class="boxes__col-acoes">
              <div class="boxes__acoes">
                <button
                  type="button"
                  class="boxes__acao-btn"
                  aria-label="Editar box"
                  title="Editar"
                  @click="editarBox(box)"
                >
                  <AppIcon name="pencil" :size="16" />
                </button>
                <button
                  type="button"
                  class="boxes__acao-btn boxes__acao-btn--perigo"
                  aria-label="Excluir box"
                  title="Excluir"
                  :disabled="excluindoId === box.id"
                  @click="excluirBox(box)"
                >
                  <AppIcon
                    :name="excluindoId === box.id ? 'loader' : 'trash'"
                    :size="16"
                    :class="{ 'boxes__spinner': excluindoId === box.id }"
                  />
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>

      <div v-if="!carregando && !erro && boxes.length > 0" class="boxes__paginacao">
        <span class="boxes__total">{{ totalElementos }} box(es) no total</span>

        <div class="boxes__paginacao-controles">
          <button
            type="button"
            class="boxes__pagina-btn"
            aria-label="Página anterior"
            :disabled="pagina === 0"
            @click="irParaPagina(pagina - 1)"
          >
            <AppIcon name="chevron-left" :size="16" />
          </button>

          <span class="boxes__pagina-atual">Página {{ pagina + 1 }} de {{ Math.max(totalPaginas, 1) }}</span>

          <button
            type="button"
            class="boxes__pagina-btn"
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
.boxes {
  padding: 22px 28px 40px;
}

.boxes__painel {
  background: var(--bg-surface);
  border: 1px solid var(--border-hairline);
  box-shadow: var(--shadow-card);
}

.boxes__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-hairline);
}

.boxes__acoes-topo {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.boxes__filtro {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.boxes__campo {
  display: flex;
  align-items: center;
  gap: 8px;
  background: var(--bg-surface);
  border: 1px solid var(--border-hairline);
  padding: 8px 12px;
  color: var(--text-muted);
}

.boxes__campo input,
.boxes__campo select {
  border: none;
  outline: none;
  background: transparent;
  font: inherit;
  font-size: 13.5px;
  color: var(--text-primary);
  width: 160px;
}

.boxes__campo input::placeholder {
  color: var(--text-muted);
}

.boxes__filtro .btn {
  border-radius: 0;
}

.boxes__state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 48px 16px;
  text-align: center;
  color: var(--text-muted);
  font-size: 13.5px;
}

.boxes__state--error {
  color: var(--text-critical);
}

.boxes__spinner {
  animation: boxes-spin 0.8s linear infinite;
  color: var(--brand-500);
}

@keyframes boxes-spin {
  to {
    transform: rotate(360deg);
  }
}

.boxes__table {
  width: 100%;
  border-collapse: collapse;
}

.boxes__table th,
.boxes__table td {
  text-align: left;
  padding: 13px 20px;
  font-size: 13.5px;
}

.boxes__table thead th {
  background: var(--bg-surface-sunken);
  color: var(--text-muted);
  font-weight: 600;
  font-size: 12px;
  letter-spacing: 0.05em;
  text-transform: uppercase;
  border-bottom: 1px solid var(--border-hairline);
}

.boxes__table tbody tr {
  transition: background-color 0.15s ease;
}

.boxes__table tbody tr + tr td {
  border-top: 1px solid var(--border-hairline);
}

.boxes__table tbody tr:hover {
  background: var(--brand-050);
}

.boxes__nome {
  font-weight: 600;
}

.boxes__col-acoes {
  width: 1%;
  white-space: nowrap;
}

.boxes__acoes {
  display: flex;
  align-items: center;
  gap: 8px;
}

.boxes__acao-btn {
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

.boxes__acao-btn:hover:not(:disabled) {
  background: var(--bg-surface-sunken);
  color: var(--text-primary);
}

.boxes__acao-btn--perigo:hover:not(:disabled) {
  border-color: var(--status-critical);
  color: var(--text-critical);
}

.boxes__acao-btn:disabled {
  opacity: 0.6;
  cursor: default;
}

.boxes__paginacao {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 20px;
  border-top: 1px solid var(--border-hairline);
}

.boxes__total {
  font-size: 12.5px;
  color: var(--text-muted);
}

.boxes__paginacao-controles {
  display: flex;
  align-items: center;
  gap: 10px;
}

.boxes__pagina-atual {
  font-size: 12.5px;
  color: var(--text-secondary);
  white-space: nowrap;
}

.boxes__pagina-btn {
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

.boxes__pagina-btn:hover:not(:disabled) {
  background: var(--bg-surface-sunken);
}

.boxes__pagina-btn:disabled {
  opacity: 0.5;
  cursor: default;
}

@media (max-width: 720px) {
  .boxes {
    padding: 16px 16px 32px;
  }

  .boxes__toolbar {
    padding: 14px 16px;
  }

  .boxes__filtro {
    width: 100%;
  }

  .boxes__campo {
    flex: 1;
    min-width: 0;
  }

  .boxes__campo input,
  .boxes__campo select {
    width: 100%;
  }

  .boxes__acoes-topo {
    width: 100%;
  }

  .boxes__acoes-topo .btn {
    flex: 1;
    justify-content: center;
  }

  .boxes__table thead {
    display: none;
  }

  .boxes__table,
  .boxes__table tbody,
  .boxes__table tr,
  .boxes__table td {
    display: block;
    width: 100%;
  }

  .boxes__table tbody tr {
    padding: 14px 16px;
  }

  .boxes__table tbody tr + tr {
    border-top: 1px solid var(--border-hairline);
  }

  .boxes__table tbody tr + tr td {
    border-top: none;
  }

  .boxes__table td {
    padding: 4px 0;
  }

  .boxes__table td[data-label]::before {
    content: attr(data-label);
    display: block;
    font-size: 11px;
    font-weight: 600;
    letter-spacing: 0.05em;
    text-transform: uppercase;
    color: var(--text-muted);
    margin-bottom: 2px;
  }

  .boxes__col-acoes {
    padding-top: 10px;
  }

  .boxes__acoes {
    justify-content: flex-end;
  }

  .boxes__paginacao {
    flex-wrap: wrap;
    justify-content: center;
    text-align: center;
    padding: 14px 16px;
  }
}
</style>
