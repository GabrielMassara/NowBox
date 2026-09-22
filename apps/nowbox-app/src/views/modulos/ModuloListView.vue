<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import AppIcon from '../../components/AppIcon.vue'
import { ApiError } from '../../lib/http'
import { moduloService } from '../../services/modulo.service'
import { sessaoService } from '../../services/sessao.service'
import type { ModuloResponseDTO, SessaoResponseDTO } from '../../types/api'

const TAMANHO_PAGINA = 10

const router = useRouter()

const modulos = ref<ModuloResponseDTO[]>([])
const sessoes = ref<SessaoResponseDTO[]>([])
const carregando = ref(false)
const erro = ref('')
const excluindoId = ref('')

const pagina = ref(0)
const totalPaginas = ref(0)
const totalElementos = ref(0)

const filtro = reactive({ nome: '', rota: '', idSessao: '' })

async function carregarSessoes() {
  try {
    const resultado = await sessaoService.listar(0, 100)
    sessoes.value = resultado.content
  } catch {
    // A listagem de módulos ainda funciona sem as sessões para o filtro.
  }
}

async function carregar() {
  carregando.value = true
  erro.value = ''

  try {
    const resultado = await moduloService.listar(pagina.value, TAMANHO_PAGINA, {
      nome: filtro.nome.trim() || undefined,
      rota: filtro.rota.trim() || undefined,
      idSessao: filtro.idSessao || undefined,
    })
    modulos.value = resultado.content
    totalPaginas.value = resultado.totalPages
    totalElementos.value = resultado.totalElements
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível carregar os módulos.'
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
  filtro.idSessao = ''
  buscar()
}

function irParaPagina(novaPagina: number) {
  if (novaPagina < 0 || novaPagina >= totalPaginas.value) return
  pagina.value = novaPagina
  carregar()
}

function novoModulo() {
  router.push('/modulos/novo')
}

function editarModulo(modulo: ModuloResponseDTO) {
  router.push(`/modulos/${modulo.id}/editar`)
}

async function excluirModulo(modulo: ModuloResponseDTO) {
  if (!confirm(`Excluir o módulo "${modulo.nome}"?`)) return

  excluindoId.value = modulo.id
  try {
    await moduloService.excluir(modulo.id)
    if (modulos.value.length === 1 && pagina.value > 0) pagina.value -= 1
    await carregar()
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível excluir o módulo.'
  } finally {
    excluindoId.value = ''
  }
}

onMounted(() => {
  carregarSessoes()
  carregar()
})
</script>

<template>
  <div class="modulos">
    <div class="modulos__painel">
      <div class="modulos__toolbar">
        <form class="modulos__filtro" @submit.prevent="buscar">
          <div class="modulos__campo">
            <AppIcon name="search" :size="16" />
            <input v-model="filtro.nome" type="text" placeholder="Buscar por nome" aria-label="Buscar por nome" />
          </div>

          <div class="modulos__campo">
            <AppIcon name="folder" :size="16" />
            <input v-model="filtro.rota" type="text" placeholder="Buscar por rota" aria-label="Buscar por rota" />
          </div>

          <div class="modulos__campo">
            <AppIcon name="layers" :size="16" />
            <select v-model="filtro.idSessao" aria-label="Filtrar por sessão">
              <option value="">Todas as sessões</option>
              <option v-for="sessao in sessoes" :key="sessao.id" :value="sessao.id">{{ sessao.nome }}</option>
            </select>
          </div>

          <button type="submit" class="btn">Filtrar</button>
          <button v-if="filtro.nome || filtro.rota || filtro.idSessao" type="button" class="btn" @click="limparFiltro">
            Limpar
          </button>
        </form>

        <button type="button" class="btn btn--primary" @click="novoModulo">
          <AppIcon name="plus" :size="16" />
          Novo módulo
        </button>
      </div>

      <div v-if="carregando" class="modulos__state">
        <AppIcon name="loader" :size="20" class="modulos__spinner" />
        <span>Carregando módulos...</span>
      </div>

      <div v-else-if="erro" class="modulos__state modulos__state--error">
        <AppIcon name="alert-circle" :size="20" />
        <span>{{ erro }}</span>
        <button type="button" class="btn" @click="carregar">Tentar novamente</button>
      </div>

      <div v-else-if="modulos.length === 0" class="modulos__state">
        <AppIcon name="layers" :size="20" />
        <span>Nenhum módulo encontrado.</span>
      </div>

      <table v-else class="modulos__table">
        <thead>
          <tr>
            <th>Nome</th>
            <th>Rota</th>
            <th>Sessão</th>
            <th class="modulos__col-acoes">Ações</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="modulo in modulos" :key="modulo.id">
            <td class="modulos__nome" data-label="Nome">{{ modulo.nome }}</td>
            <td data-label="Rota"><span class="modulos__rota">{{ modulo.rota }}</span></td>
            <td data-label="Sessão">{{ modulo.sessao?.nome }}</td>
            <td class="modulos__col-acoes">
              <div class="modulos__acoes">
                <button
                  type="button"
                  class="modulos__acao-btn"
                  aria-label="Editar módulo"
                  title="Editar"
                  @click="editarModulo(modulo)"
                >
                  <AppIcon name="pencil" :size="16" />
                </button>
                <button
                  type="button"
                  class="modulos__acao-btn modulos__acao-btn--perigo"
                  aria-label="Excluir módulo"
                  title="Excluir"
                  :disabled="excluindoId === modulo.id"
                  @click="excluirModulo(modulo)"
                >
                  <AppIcon
                    :name="excluindoId === modulo.id ? 'loader' : 'trash'"
                    :size="16"
                    :class="{ 'modulos__spinner': excluindoId === modulo.id }"
                  />
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>

      <div v-if="!carregando && !erro && modulos.length > 0" class="modulos__paginacao">
        <span class="modulos__total">{{ totalElementos }} módulo(s) no total</span>

        <div class="modulos__paginacao-controles">
          <button
            type="button"
            class="modulos__pagina-btn"
            aria-label="Página anterior"
            :disabled="pagina === 0"
            @click="irParaPagina(pagina - 1)"
          >
            <AppIcon name="chevron-left" :size="16" />
          </button>

          <span class="modulos__pagina-atual">Página {{ pagina + 1 }} de {{ Math.max(totalPaginas, 1) }}</span>

          <button
            type="button"
            class="modulos__pagina-btn"
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
.modulos {
  padding: 22px 28px 40px;
}

.modulos__painel {
  background: var(--bg-surface);
  border: 1px solid var(--border-hairline);
  box-shadow: var(--shadow-card);
}

.modulos__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-hairline);
}

.modulos__filtro {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.modulos__campo {
  display: flex;
  align-items: center;
  gap: 8px;
  background: var(--bg-surface);
  border: 1px solid var(--border-hairline);
  padding: 8px 12px;
  color: var(--text-muted);
}

.modulos__campo input,
.modulos__campo select {
  border: none;
  outline: none;
  background: transparent;
  font: inherit;
  font-size: 13.5px;
  color: var(--text-primary);
  width: 170px;
}

.modulos__campo input::placeholder {
  color: var(--text-muted);
}

.modulos__filtro .btn {
  border-radius: 0;
}

.modulos__state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 48px 16px;
  text-align: center;
  color: var(--text-muted);
  font-size: 13.5px;
}

.modulos__state--error {
  color: var(--text-critical);
}

.modulos__spinner {
  animation: modulos-spin 0.8s linear infinite;
  color: var(--brand-500);
}

@keyframes modulos-spin {
  to {
    transform: rotate(360deg);
  }
}

.modulos__table {
  width: 100%;
  border-collapse: collapse;
}

.modulos__table th,
.modulos__table td {
  text-align: left;
  padding: 13px 20px;
  font-size: 13.5px;
}

.modulos__table thead th {
  background: var(--bg-surface-sunken);
  color: var(--text-muted);
  font-weight: 600;
  font-size: 12px;
  letter-spacing: 0.05em;
  text-transform: uppercase;
  border-bottom: 1px solid var(--border-hairline);
}

.modulos__table tbody tr {
  transition: background-color 0.15s ease;
}

.modulos__table tbody tr + tr td {
  border-top: 1px solid var(--border-hairline);
}

.modulos__table tbody tr:hover {
  background: var(--brand-050);
}

.modulos__nome {
  font-weight: 600;
}

.modulos__rota {
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 12.5px;
  color: var(--text-secondary);
}

.modulos__col-acoes {
  width: 1%;
  white-space: nowrap;
}

.modulos__acoes {
  display: flex;
  align-items: center;
  gap: 8px;
}

.modulos__acao-btn {
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

.modulos__acao-btn:hover:not(:disabled) {
  background: var(--bg-surface-sunken);
  color: var(--text-primary);
}

.modulos__acao-btn--perigo:hover:not(:disabled) {
  border-color: var(--status-critical);
  color: var(--text-critical);
}

.modulos__acao-btn:disabled {
  opacity: 0.6;
  cursor: default;
}

.modulos__paginacao {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 20px;
  border-top: 1px solid var(--border-hairline);
}

.modulos__total {
  font-size: 12.5px;
  color: var(--text-muted);
}

.modulos__paginacao-controles {
  display: flex;
  align-items: center;
  gap: 10px;
}

.modulos__pagina-atual {
  font-size: 12.5px;
  color: var(--text-secondary);
  white-space: nowrap;
}

.modulos__pagina-btn {
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

.modulos__pagina-btn:hover:not(:disabled) {
  background: var(--bg-surface-sunken);
}

.modulos__pagina-btn:disabled {
  opacity: 0.5;
  cursor: default;
}

@media (max-width: 720px) {
  .modulos {
    padding: 16px 16px 32px;
  }

  .modulos__toolbar {
    padding: 14px 16px;
  }

  .modulos__filtro {
    width: 100%;
  }

  .modulos__campo {
    flex: 1;
    min-width: 0;
  }

  .modulos__campo input,
  .modulos__campo select {
    width: 100%;
  }

  .modulos__toolbar > .btn--primary {
    width: 100%;
    justify-content: center;
  }

  .modulos__table thead {
    display: none;
  }

  .modulos__table,
  .modulos__table tbody,
  .modulos__table tr,
  .modulos__table td {
    display: block;
    width: 100%;
  }

  .modulos__table tbody tr {
    padding: 14px 16px;
  }

  .modulos__table tbody tr + tr {
    border-top: 1px solid var(--border-hairline);
  }

  .modulos__table tbody tr + tr td {
    border-top: none;
  }

  .modulos__table td {
    padding: 4px 0;
  }

  .modulos__table td[data-label]::before {
    content: attr(data-label);
    display: block;
    font-size: 11px;
    font-weight: 600;
    letter-spacing: 0.05em;
    text-transform: uppercase;
    color: var(--text-muted);
    margin-bottom: 2px;
  }

  .modulos__col-acoes {
    padding-top: 10px;
  }

  .modulos__acoes {
    justify-content: flex-end;
  }

  .modulos__paginacao {
    flex-wrap: wrap;
    justify-content: center;
    text-align: center;
    padding: 14px 16px;
  }
}
</style>
