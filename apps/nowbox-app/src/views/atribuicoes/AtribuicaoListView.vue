<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import AppIcon from '../../components/AppIcon.vue'
import { ApiError } from '../../lib/http'
import { carregarTodas } from '../../lib/paginacao'
import { atribuicaoService } from '../../services/atribuicao.service'
import { cargoService } from '../../services/cargo.service'
import { usuarioService } from '../../services/usuario.service'
import type { AtribuicaoResponseDTO, CargoResponseDTO, UsuarioResponseDTO } from '../../types/api'

const TAMANHO_PAGINA = 10

const router = useRouter()

const atribuicoes = ref<AtribuicaoResponseDTO[]>([])
const usuarios = ref<UsuarioResponseDTO[]>([])
const cargos = ref<CargoResponseDTO[]>([])
const carregando = ref(false)
const erro = ref('')
const excluindoId = ref('')

const pagina = ref(0)
const totalPaginas = ref(0)
const totalElementos = ref(0)

const filtro = reactive({ idUsuario: '', idCargo: '' })

async function carregarOpcoesFiltro() {
  try {
    ;[usuarios.value, cargos.value] = await Promise.all([
      carregarTodas((p, t) => usuarioService.listar(p, t)),
      carregarTodas((p, t) => cargoService.listar(p, t)),
    ])
  } catch {
    // A listagem de atribuições ainda funciona sem as opções dos filtros.
  }
}

async function carregar() {
  carregando.value = true
  erro.value = ''

  try {
    const resultado = await atribuicaoService.listar(pagina.value, TAMANHO_PAGINA, {
      idUsuario: filtro.idUsuario || undefined,
      idCargo: filtro.idCargo || undefined,
    })
    atribuicoes.value = resultado.content
    totalPaginas.value = resultado.totalPages
    totalElementos.value = resultado.totalElements
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível carregar as atribuições.'
  } finally {
    carregando.value = false
  }
}

function buscar() {
  pagina.value = 0
  carregar()
}

function limparFiltro() {
  filtro.idUsuario = ''
  filtro.idCargo = ''
  buscar()
}

function irParaPagina(novaPagina: number) {
  if (novaPagina < 0 || novaPagina >= totalPaginas.value) return
  pagina.value = novaPagina
  carregar()
}

function novaAtribuicao() {
  router.push('/atribuicoes/nova')
}

function editarAtribuicao(atribuicao: AtribuicaoResponseDTO) {
  router.push(`/atribuicoes/${atribuicao.id}/editar`)
}

async function excluirAtribuicao(atribuicao: AtribuicaoResponseDTO) {
  if (!confirm(`Remover o cargo "${atribuicao.cargo?.nome}" de "${atribuicao.usuario?.nome}"?`)) return

  excluindoId.value = atribuicao.id
  try {
    await atribuicaoService.excluir(atribuicao.id)
    if (atribuicoes.value.length === 1 && pagina.value > 0) pagina.value -= 1
    await carregar()
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível excluir a atribuição.'
  } finally {
    excluindoId.value = ''
  }
}

onMounted(() => {
  carregarOpcoesFiltro()
  carregar()
})
</script>

<template>
  <div class="atribuicoes">
    <div class="atribuicoes__painel">
      <div class="atribuicoes__toolbar">
        <form class="atribuicoes__filtro" @submit.prevent="buscar">
          <div class="atribuicoes__campo">
            <AppIcon name="user-circle" :size="16" />
            <select v-model="filtro.idUsuario" aria-label="Filtrar por usuário">
              <option value="">Todos os usuários</option>
              <option v-for="usuario in usuarios" :key="usuario.id" :value="usuario.id">{{ usuario.nome }}</option>
            </select>
          </div>

          <div class="atribuicoes__campo">
            <AppIcon name="tag" :size="16" />
            <select v-model="filtro.idCargo" aria-label="Filtrar por cargo">
              <option value="">Todos os cargos</option>
              <option v-for="cargo in cargos" :key="cargo.id" :value="cargo.id">
                {{ cargo.nome }} — {{ cargo.unidade?.nome }}
              </option>
            </select>
          </div>

          <button type="submit" class="btn">Filtrar</button>
          <button v-if="filtro.idUsuario || filtro.idCargo" type="button" class="btn" @click="limparFiltro">
            Limpar
          </button>
        </form>

        <button type="button" class="btn btn--primary" @click="novaAtribuicao">
          <AppIcon name="plus" :size="16" />
          Nova atribuição
        </button>
      </div>

      <div v-if="carregando" class="atribuicoes__state">
        <AppIcon name="loader" :size="20" class="atribuicoes__spinner" />
        <span>Carregando atribuições...</span>
      </div>

      <div v-else-if="erro" class="atribuicoes__state atribuicoes__state--error">
        <AppIcon name="alert-circle" :size="20" />
        <span>{{ erro }}</span>
        <button type="button" class="btn" @click="carregar">Tentar novamente</button>
      </div>

      <div v-else-if="atribuicoes.length === 0" class="atribuicoes__state">
        <AppIcon name="clipboard-check" :size="20" />
        <span>Nenhuma atribuição encontrada.</span>
      </div>

      <table v-else class="atribuicoes__table">
        <thead>
          <tr>
            <th>Usuário</th>
            <th>E-mail</th>
            <th>Cargo</th>
            <th>Unidade</th>
            <th class="atribuicoes__col-acoes">Ações</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="atribuicao in atribuicoes" :key="atribuicao.id">
            <td class="atribuicoes__nome" data-label="Usuário">{{ atribuicao.usuario?.nome }}</td>
            <td data-label="E-mail">{{ atribuicao.usuario?.email }}</td>
            <td data-label="Cargo">{{ atribuicao.cargo?.nome }}</td>
            <td data-label="Unidade">{{ atribuicao.cargo?.unidade?.nome }}</td>
            <td class="atribuicoes__col-acoes">
              <div class="atribuicoes__acoes">
                <button
                  type="button"
                  class="atribuicoes__acao-btn"
                  aria-label="Editar atribuição"
                  title="Editar"
                  @click="editarAtribuicao(atribuicao)"
                >
                  <AppIcon name="pencil" :size="16" />
                </button>
                <button
                  type="button"
                  class="atribuicoes__acao-btn atribuicoes__acao-btn--perigo"
                  aria-label="Excluir atribuição"
                  title="Excluir"
                  :disabled="excluindoId === atribuicao.id"
                  @click="excluirAtribuicao(atribuicao)"
                >
                  <AppIcon
                    :name="excluindoId === atribuicao.id ? 'loader' : 'trash'"
                    :size="16"
                    :class="{ 'atribuicoes__spinner': excluindoId === atribuicao.id }"
                  />
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>

      <div v-if="!carregando && !erro && atribuicoes.length > 0" class="atribuicoes__paginacao">
        <span class="atribuicoes__total">{{ totalElementos }} atribuição(ões) no total</span>

        <div class="atribuicoes__paginacao-controles">
          <button
            type="button"
            class="atribuicoes__pagina-btn"
            aria-label="Página anterior"
            :disabled="pagina === 0"
            @click="irParaPagina(pagina - 1)"
          >
            <AppIcon name="chevron-left" :size="16" />
          </button>

          <span class="atribuicoes__pagina-atual">Página {{ pagina + 1 }} de {{ Math.max(totalPaginas, 1) }}</span>

          <button
            type="button"
            class="atribuicoes__pagina-btn"
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
.atribuicoes {
  padding: 22px 28px 40px;
}

.atribuicoes__painel {
  background: var(--bg-surface);
  border: 1px solid var(--border-hairline);
  box-shadow: var(--shadow-card);
}

.atribuicoes__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-hairline);
}

.atribuicoes__filtro {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.atribuicoes__campo {
  display: flex;
  align-items: center;
  gap: 8px;
  background: var(--bg-surface);
  border: 1px solid var(--border-hairline);
  padding: 8px 12px;
  color: var(--text-muted);
}

.atribuicoes__campo input,
.atribuicoes__campo select {
  border: none;
  outline: none;
  background: transparent;
  font: inherit;
  font-size: 13.5px;
  color: var(--text-primary);
  width: 220px;
}

.atribuicoes__campo input::placeholder {
  color: var(--text-muted);
}

.atribuicoes__filtro .btn {
  border-radius: 0;
}

.atribuicoes__state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 48px 16px;
  text-align: center;
  color: var(--text-muted);
  font-size: 13.5px;
}

.atribuicoes__state--error {
  color: var(--text-critical);
}

.atribuicoes__spinner {
  animation: atribuicoes-spin 0.8s linear infinite;
  color: var(--brand-500);
}

@keyframes atribuicoes-spin {
  to {
    transform: rotate(360deg);
  }
}

.atribuicoes__table {
  width: 100%;
  border-collapse: collapse;
}

.atribuicoes__table th,
.atribuicoes__table td {
  text-align: left;
  padding: 13px 20px;
  font-size: 13.5px;
}

.atribuicoes__table thead th {
  background: var(--bg-surface-sunken);
  color: var(--text-muted);
  font-weight: 600;
  font-size: 12px;
  letter-spacing: 0.05em;
  text-transform: uppercase;
  border-bottom: 1px solid var(--border-hairline);
}

.atribuicoes__table tbody tr {
  transition: background-color 0.15s ease;
}

.atribuicoes__table tbody tr + tr td {
  border-top: 1px solid var(--border-hairline);
}

.atribuicoes__table tbody tr:hover {
  background: var(--brand-050);
}

.atribuicoes__nome {
  font-weight: 600;
}

.atribuicoes__col-acoes {
  width: 1%;
  white-space: nowrap;
}

.atribuicoes__acoes {
  display: flex;
  align-items: center;
  gap: 8px;
}

.atribuicoes__acao-btn {
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

.atribuicoes__acao-btn:hover:not(:disabled) {
  background: var(--bg-surface-sunken);
  color: var(--text-primary);
}

.atribuicoes__acao-btn--perigo:hover:not(:disabled) {
  border-color: var(--status-critical);
  color: var(--text-critical);
}

.atribuicoes__acao-btn:disabled {
  opacity: 0.6;
  cursor: default;
}

.atribuicoes__paginacao {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 20px;
  border-top: 1px solid var(--border-hairline);
}

.atribuicoes__total {
  font-size: 12.5px;
  color: var(--text-muted);
}

.atribuicoes__paginacao-controles {
  display: flex;
  align-items: center;
  gap: 10px;
}

.atribuicoes__pagina-atual {
  font-size: 12.5px;
  color: var(--text-secondary);
  white-space: nowrap;
}

.atribuicoes__pagina-btn {
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

.atribuicoes__pagina-btn:hover:not(:disabled) {
  background: var(--bg-surface-sunken);
}

.atribuicoes__pagina-btn:disabled {
  opacity: 0.5;
  cursor: default;
}

@media (max-width: 720px) {
  .atribuicoes {
    padding: 16px 16px 32px;
  }

  .atribuicoes__toolbar {
    padding: 14px 16px;
  }

  .atribuicoes__filtro {
    width: 100%;
  }

  .atribuicoes__campo {
    flex: 1;
    min-width: 0;
  }

  .atribuicoes__campo input,
  .atribuicoes__campo select {
    width: 100%;
  }

  .atribuicoes__toolbar > .btn--primary {
    width: 100%;
    justify-content: center;
  }

  .atribuicoes__table thead {
    display: none;
  }

  .atribuicoes__table,
  .atribuicoes__table tbody,
  .atribuicoes__table tr,
  .atribuicoes__table td {
    display: block;
    width: 100%;
  }

  .atribuicoes__table tbody tr {
    padding: 14px 16px;
  }

  .atribuicoes__table tbody tr + tr {
    border-top: 1px solid var(--border-hairline);
  }

  .atribuicoes__table tbody tr + tr td {
    border-top: none;
  }

  .atribuicoes__table td {
    padding: 4px 0;
  }

  .atribuicoes__table td[data-label]::before {
    content: attr(data-label);
    display: block;
    font-size: 11px;
    font-weight: 600;
    letter-spacing: 0.05em;
    text-transform: uppercase;
    color: var(--text-muted);
    margin-bottom: 2px;
  }

  .atribuicoes__col-acoes {
    padding-top: 10px;
  }

  .atribuicoes__acoes {
    justify-content: flex-end;
  }

  .atribuicoes__paginacao {
    flex-wrap: wrap;
    justify-content: center;
    text-align: center;
    padding: 14px 16px;
  }
}
</style>
