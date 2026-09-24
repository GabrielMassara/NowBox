<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import AppIcon from '../../components/AppIcon.vue'
import { ApiError } from '../../lib/http'
import { estadoService } from '../../services/estado.service'
import { unidadeService } from '../../services/unidade.service'
import type { EstadoEntity, UnidadeResponseDTO } from '../../types/api'

const TAMANHO_PAGINA = 10

const router = useRouter()

const unidades = ref<UnidadeResponseDTO[]>([])
const estados = ref<EstadoEntity[]>([])
const carregando = ref(false)
const erro = ref('')
const excluindoId = ref('')

const pagina = ref(0)
const totalPaginas = ref(0)
const totalElementos = ref(0)

const filtro = reactive({ nome: '', cnpj: '', cidade: '', idEstado: '' })

function formatarCnpj(cnpj: string) {
  return cnpj.replace(/^(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})$/, '$1.$2.$3/$4-$5')
}

async function carregarEstados() {
  try {
    const resultado = await estadoService.listar(0, 100)
    estados.value = resultado.content
  } catch {
    // A listagem de unidades ainda funciona sem os estados para o filtro.
  }
}

async function carregar() {
  carregando.value = true
  erro.value = ''

  try {
    const resultado = await unidadeService.listar(pagina.value, TAMANHO_PAGINA, {
      nome: filtro.nome.trim() || undefined,
      cnpj: filtro.cnpj.replace(/\D/g, '') || undefined,
      cidade: filtro.cidade.trim() || undefined,
      idEstado: filtro.idEstado || undefined,
    })
    unidades.value = resultado.content
    totalPaginas.value = resultado.totalPages
    totalElementos.value = resultado.totalElements
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível carregar as unidades.'
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
  filtro.cnpj = ''
  filtro.cidade = ''
  filtro.idEstado = ''
  buscar()
}

function irParaPagina(novaPagina: number) {
  if (novaPagina < 0 || novaPagina >= totalPaginas.value) return
  pagina.value = novaPagina
  carregar()
}

function novaUnidade() {
  router.push('/unidades/novo')
}

function editarUnidade(unidade: UnidadeResponseDTO) {
  router.push(`/unidades/${unidade.id}/editar`)
}

async function excluirUnidade(unidade: UnidadeResponseDTO) {
  if (!confirm(`Excluir a unidade "${unidade.nome}"?`)) return

  excluindoId.value = unidade.id
  try {
    await unidadeService.excluir(unidade.id)
    if (unidades.value.length === 1 && pagina.value > 0) pagina.value -= 1
    await carregar()
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível excluir a unidade.'
  } finally {
    excluindoId.value = ''
  }
}

onMounted(() => {
  carregarEstados()
  carregar()
})
</script>

<template>
  <div class="unidades">
    <div class="unidades__painel">
      <div class="unidades__toolbar">
        <form class="unidades__filtro" @submit.prevent="buscar">
          <div class="unidades__campo">
            <AppIcon name="search" :size="16" />
            <input v-model="filtro.nome" type="text" placeholder="Buscar por nome" aria-label="Buscar por nome" />
          </div>

          <div class="unidades__campo">
            <AppIcon name="search" :size="16" />
            <input
              v-model="filtro.cnpj"
              type="text"
              inputmode="numeric"
              maxlength="18"
              placeholder="CNPJ"
              aria-label="Buscar por CNPJ"
            />
          </div>

          <div class="unidades__campo">
            <AppIcon name="search" :size="16" />
            <input v-model="filtro.cidade" type="text" placeholder="Cidade" aria-label="Buscar por cidade" />
          </div>

          <div class="unidades__campo">
            <AppIcon name="map-pin" :size="16" />
            <select v-model="filtro.idEstado" aria-label="Filtrar por estado">
              <option value="">Todos os estados</option>
              <option v-for="estado in estados" :key="estado.id" :value="estado.id">{{ estado.nome }}</option>
            </select>
          </div>

          <button type="submit" class="btn">Filtrar</button>
          <button
            v-if="filtro.nome || filtro.cnpj || filtro.cidade || filtro.idEstado"
            type="button"
            class="btn"
            @click="limparFiltro"
          >
            Limpar
          </button>
        </form>

        <button type="button" class="btn btn--primary" @click="novaUnidade">
          <AppIcon name="plus" :size="16" />
          Nova unidade
        </button>
      </div>

      <div v-if="carregando" class="unidades__state">
        <AppIcon name="loader" :size="20" class="unidades__spinner" />
        <span>Carregando unidades...</span>
      </div>

      <div v-else-if="erro" class="unidades__state unidades__state--error">
        <AppIcon name="alert-circle" :size="20" />
        <span>{{ erro }}</span>
        <button type="button" class="btn" @click="carregar">Tentar novamente</button>
      </div>

      <div v-else-if="unidades.length === 0" class="unidades__state">
        <AppIcon name="building" :size="20" />
        <span>Nenhuma unidade encontrada.</span>
      </div>

      <table v-else class="unidades__table">
        <thead>
          <tr>
            <th>Nome</th>
            <th>CNPJ</th>
            <th>Cidade</th>
            <th>Estado</th>
            <th class="unidades__col-acoes">Ações</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="unidade in unidades" :key="unidade.id">
            <td class="unidades__nome" data-label="Nome">{{ unidade.nome }}</td>
            <td data-label="CNPJ">{{ formatarCnpj(unidade.cnpj) }}</td>
            <td data-label="Cidade">{{ unidade.cidade }}</td>
            <td data-label="Estado">{{ unidade.estado?.uf }}</td>
            <td class="unidades__col-acoes">
              <div class="unidades__acoes">
                <button
                  type="button"
                  class="unidades__acao-btn"
                  aria-label="Editar unidade"
                  title="Editar"
                  @click="editarUnidade(unidade)"
                >
                  <AppIcon name="pencil" :size="16" />
                </button>
                <button
                  type="button"
                  class="unidades__acao-btn unidades__acao-btn--perigo"
                  aria-label="Excluir unidade"
                  title="Excluir"
                  :disabled="excluindoId === unidade.id"
                  @click="excluirUnidade(unidade)"
                >
                  <AppIcon
                    :name="excluindoId === unidade.id ? 'loader' : 'trash'"
                    :size="16"
                    :class="{ 'unidades__spinner': excluindoId === unidade.id }"
                  />
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>

      <div v-if="!carregando && !erro && unidades.length > 0" class="unidades__paginacao">
        <span class="unidades__total">{{ totalElementos }} unidade(s) no total</span>

        <div class="unidades__paginacao-controles">
          <button
            type="button"
            class="unidades__pagina-btn"
            aria-label="Página anterior"
            :disabled="pagina === 0"
            @click="irParaPagina(pagina - 1)"
          >
            <AppIcon name="chevron-left" :size="16" />
          </button>

          <span class="unidades__pagina-atual">Página {{ pagina + 1 }} de {{ Math.max(totalPaginas, 1) }}</span>

          <button
            type="button"
            class="unidades__pagina-btn"
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
.unidades {
  padding: 22px 28px 40px;
}

.unidades__painel {
  background: var(--bg-surface);
  border: 1px solid var(--border-hairline);
  box-shadow: var(--shadow-card);
}

.unidades__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-hairline);
}

.unidades__filtro {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.unidades__campo {
  display: flex;
  align-items: center;
  gap: 8px;
  background: var(--bg-surface);
  border: 1px solid var(--border-hairline);
  padding: 8px 12px;
  color: var(--text-muted);
}

.unidades__campo input,
.unidades__campo select {
  border: none;
  outline: none;
  background: transparent;
  font: inherit;
  font-size: 13.5px;
  color: var(--text-primary);
  width: 150px;
}

.unidades__campo input::placeholder {
  color: var(--text-muted);
}

.unidades__filtro .btn {
  border-radius: 0;
}

.unidades__state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 48px 16px;
  text-align: center;
  color: var(--text-muted);
  font-size: 13.5px;
}

.unidades__state--error {
  color: var(--text-critical);
}

.unidades__spinner {
  animation: unidades-spin 0.8s linear infinite;
  color: var(--brand-500);
}

@keyframes unidades-spin {
  to {
    transform: rotate(360deg);
  }
}

.unidades__table {
  width: 100%;
  border-collapse: collapse;
}

.unidades__table th,
.unidades__table td {
  text-align: left;
  padding: 13px 20px;
  font-size: 13.5px;
}

.unidades__table thead th {
  background: var(--bg-surface-sunken);
  color: var(--text-muted);
  font-weight: 600;
  font-size: 12px;
  letter-spacing: 0.05em;
  text-transform: uppercase;
  border-bottom: 1px solid var(--border-hairline);
}

.unidades__table tbody tr {
  transition: background-color 0.15s ease;
}

.unidades__table tbody tr + tr td {
  border-top: 1px solid var(--border-hairline);
}

.unidades__table tbody tr:hover {
  background: var(--brand-050);
}

.unidades__nome {
  font-weight: 600;
}

.unidades__col-acoes {
  width: 1%;
  white-space: nowrap;
}

.unidades__acoes {
  display: flex;
  align-items: center;
  gap: 8px;
}

.unidades__acao-btn {
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

.unidades__acao-btn:hover:not(:disabled) {
  background: var(--bg-surface-sunken);
  color: var(--text-primary);
}

.unidades__acao-btn--perigo:hover:not(:disabled) {
  border-color: var(--status-critical);
  color: var(--text-critical);
}

.unidades__acao-btn:disabled {
  opacity: 0.6;
  cursor: default;
}

.unidades__paginacao {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 20px;
  border-top: 1px solid var(--border-hairline);
}

.unidades__total {
  font-size: 12.5px;
  color: var(--text-muted);
}

.unidades__paginacao-controles {
  display: flex;
  align-items: center;
  gap: 10px;
}

.unidades__pagina-atual {
  font-size: 12.5px;
  color: var(--text-secondary);
  white-space: nowrap;
}

.unidades__pagina-btn {
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

.unidades__pagina-btn:hover:not(:disabled) {
  background: var(--bg-surface-sunken);
}

.unidades__pagina-btn:disabled {
  opacity: 0.5;
  cursor: default;
}

@media (max-width: 720px) {
  .unidades {
    padding: 16px 16px 32px;
  }

  .unidades__toolbar {
    padding: 14px 16px;
  }

  .unidades__filtro {
    width: 100%;
  }

  .unidades__campo {
    flex: 1;
    min-width: 0;
  }

  .unidades__campo input,
  .unidades__campo select {
    width: 100%;
  }

  .unidades__toolbar > .btn--primary {
    width: 100%;
    justify-content: center;
  }

  .unidades__table thead {
    display: none;
  }

  .unidades__table,
  .unidades__table tbody,
  .unidades__table tr,
  .unidades__table td {
    display: block;
    width: 100%;
  }

  .unidades__table tbody tr {
    padding: 14px 16px;
  }

  .unidades__table tbody tr + tr {
    border-top: 1px solid var(--border-hairline);
  }

  .unidades__table tbody tr + tr td {
    border-top: none;
  }

  .unidades__table td {
    padding: 4px 0;
  }

  .unidades__table td[data-label]::before {
    content: attr(data-label);
    display: block;
    font-size: 11px;
    font-weight: 600;
    letter-spacing: 0.05em;
    text-transform: uppercase;
    color: var(--text-muted);
    margin-bottom: 2px;
  }

  .unidades__col-acoes {
    padding-top: 10px;
  }

  .unidades__acoes {
    justify-content: flex-end;
  }

  .unidades__paginacao {
    flex-wrap: wrap;
    justify-content: center;
    text-align: center;
    padding: 14px 16px;
  }
}
</style>
