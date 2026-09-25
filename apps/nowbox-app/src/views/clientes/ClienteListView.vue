<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import AppIcon from '../../components/AppIcon.vue'
import { ApiError } from '../../lib/http'
import { mascaraCpf, mascaraTelefone } from '../../lib/mascaras'
import { clienteService } from '../../services/cliente.service'
import { estadoService } from '../../services/estado.service'
import type { ClienteResponseDTO, EstadoEntity } from '../../types/api'

const TAMANHO_PAGINA = 10

const router = useRouter()

const clientes = ref<ClienteResponseDTO[]>([])
const estados = ref<EstadoEntity[]>([])
const carregando = ref(false)
const erro = ref('')
const excluindoId = ref('')

const pagina = ref(0)
const totalPaginas = ref(0)
const totalElementos = ref(0)

const filtro = reactive({ nome: '', email: '', cpf: '', idEstado: '' })

async function carregarEstados() {
  try {
    const resultado = await estadoService.listar(0, 100)
    estados.value = resultado.content
  } catch {
    // A listagem de clientes ainda funciona sem os estados para o filtro.
  }
}

async function carregar() {
  carregando.value = true
  erro.value = ''

  try {
    const resultado = await clienteService.listar(pagina.value, TAMANHO_PAGINA, {
      nome: filtro.nome.trim() || undefined,
      email: filtro.email.trim() || undefined,
      cpf: filtro.cpf.replace(/\D/g, '') || undefined,
      idEstado: filtro.idEstado || undefined,
    })
    clientes.value = resultado.content
    totalPaginas.value = resultado.totalPages
    totalElementos.value = resultado.totalElements
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível carregar os clientes.'
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
  filtro.email = ''
  filtro.cpf = ''
  filtro.idEstado = ''
  buscar()
}

function irParaPagina(novaPagina: number) {
  if (novaPagina < 0 || novaPagina >= totalPaginas.value) return
  pagina.value = novaPagina
  carregar()
}

function novoCliente() {
  router.push('/clientes/novo')
}

function editarCliente(cliente: ClienteResponseDTO) {
  router.push(`/clientes/${cliente.id}/editar`)
}

async function excluirCliente(cliente: ClienteResponseDTO) {
  if (!confirm(`Excluir o cliente "${cliente.nome}"?`)) return

  excluindoId.value = cliente.id
  try {
    await clienteService.excluir(cliente.id)
    if (clientes.value.length === 1 && pagina.value > 0) pagina.value -= 1
    await carregar()
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível excluir o cliente.'
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
  <div class="clientes">
    <div class="clientes__painel">
      <div class="clientes__toolbar">
        <form class="clientes__filtro" @submit.prevent="buscar">
          <div class="clientes__campo">
            <AppIcon name="search" :size="16" />
            <input v-model="filtro.nome" type="text" placeholder="Buscar por nome" aria-label="Buscar por nome" />
          </div>

          <div class="clientes__campo">
            <AppIcon name="mail" :size="16" />
            <input v-model="filtro.email" type="text" placeholder="E-mail" aria-label="Buscar por e-mail" />
          </div>

          <div class="clientes__campo">
            <AppIcon name="search" :size="16" />
            <input
              v-model="filtro.cpf"
              type="text"
              inputmode="numeric"
              maxlength="14"
              @input="filtro.cpf = mascaraCpf(filtro.cpf)"
              placeholder="CPF"
              aria-label="Buscar por CPF"
            />
          </div>

          <div class="clientes__campo">
            <AppIcon name="map-pin" :size="16" />
            <select v-model="filtro.idEstado" aria-label="Filtrar por estado">
              <option value="">Todos os estados</option>
              <option v-for="estado in estados" :key="estado.id" :value="estado.id">{{ estado.nome }}</option>
            </select>
          </div>

          <button type="submit" class="btn">Filtrar</button>
          <button
            v-if="filtro.nome || filtro.email || filtro.cpf || filtro.idEstado"
            type="button"
            class="btn"
            @click="limparFiltro"
          >
            Limpar
          </button>
        </form>

        <button type="button" class="btn btn--primary" @click="novoCliente">
          <AppIcon name="plus" :size="16" />
          Novo cliente
        </button>
      </div>

      <div v-if="carregando" class="clientes__state">
        <AppIcon name="loader" :size="20" class="clientes__spinner" />
        <span>Carregando clientes...</span>
      </div>

      <div v-else-if="erro" class="clientes__state clientes__state--error">
        <AppIcon name="alert-circle" :size="20" />
        <span>{{ erro }}</span>
        <button type="button" class="btn" @click="carregar">Tentar novamente</button>
      </div>

      <div v-else-if="clientes.length === 0" class="clientes__state">
        <AppIcon name="users" :size="20" />
        <span>Nenhum cliente encontrado.</span>
      </div>

      <table v-else class="clientes__table">
        <thead>
          <tr>
            <th>Nome</th>
            <th>E-mail</th>
            <th>CPF</th>
            <th>Telefone</th>
            <th>Cidade</th>
            <th class="clientes__col-acoes">Ações</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="cliente in clientes" :key="cliente.id">
            <td class="clientes__nome" data-label="Nome">{{ cliente.nome }}</td>
            <td data-label="E-mail">{{ cliente.email }}</td>
            <td data-label="CPF">{{ mascaraCpf(cliente.cpf) }}</td>
            <td data-label="Telefone">{{ mascaraTelefone(cliente.telefone) }}</td>
            <td data-label="Cidade">{{ cliente.cidade }}/{{ cliente.estado?.uf }}</td>
            <td class="clientes__col-acoes">
              <div class="clientes__acoes">
                <button
                  type="button"
                  class="clientes__acao-btn"
                  aria-label="Editar cliente"
                  title="Editar"
                  @click="editarCliente(cliente)"
                >
                  <AppIcon name="pencil" :size="16" />
                </button>
                <button
                  type="button"
                  class="clientes__acao-btn clientes__acao-btn--perigo"
                  aria-label="Excluir cliente"
                  title="Excluir"
                  :disabled="excluindoId === cliente.id"
                  @click="excluirCliente(cliente)"
                >
                  <AppIcon
                    :name="excluindoId === cliente.id ? 'loader' : 'trash'"
                    :size="16"
                    :class="{ 'clientes__spinner': excluindoId === cliente.id }"
                  />
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>

      <div v-if="!carregando && !erro && clientes.length > 0" class="clientes__paginacao">
        <span class="clientes__total">{{ totalElementos }} cliente(s) no total</span>

        <div class="clientes__paginacao-controles">
          <button
            type="button"
            class="clientes__pagina-btn"
            aria-label="Página anterior"
            :disabled="pagina === 0"
            @click="irParaPagina(pagina - 1)"
          >
            <AppIcon name="chevron-left" :size="16" />
          </button>

          <span class="clientes__pagina-atual">Página {{ pagina + 1 }} de {{ Math.max(totalPaginas, 1) }}</span>

          <button
            type="button"
            class="clientes__pagina-btn"
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
.clientes {
  padding: 22px 28px 40px;
}

.clientes__painel {
  background: var(--bg-surface);
  border: 1px solid var(--border-hairline);
  box-shadow: var(--shadow-card);
}

.clientes__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-hairline);
}

.clientes__filtro {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.clientes__campo {
  display: flex;
  align-items: center;
  gap: 8px;
  background: var(--bg-surface);
  border: 1px solid var(--border-hairline);
  padding: 8px 12px;
  color: var(--text-muted);
}

.clientes__campo input,
.clientes__campo select {
  border: none;
  outline: none;
  background: transparent;
  font: inherit;
  font-size: 13.5px;
  color: var(--text-primary);
  width: 160px;
}

.clientes__campo input::placeholder {
  color: var(--text-muted);
}

.clientes__filtro .btn {
  border-radius: 0;
}

.clientes__state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 48px 16px;
  text-align: center;
  color: var(--text-muted);
  font-size: 13.5px;
}

.clientes__state--error {
  color: var(--text-critical);
}

.clientes__spinner {
  animation: clientes-spin 0.8s linear infinite;
  color: var(--brand-500);
}

@keyframes clientes-spin {
  to {
    transform: rotate(360deg);
  }
}

.clientes__table {
  width: 100%;
  border-collapse: collapse;
}

.clientes__table th,
.clientes__table td {
  text-align: left;
  padding: 13px 20px;
  font-size: 13.5px;
}

.clientes__table thead th {
  background: var(--bg-surface-sunken);
  color: var(--text-muted);
  font-weight: 600;
  font-size: 12px;
  letter-spacing: 0.05em;
  text-transform: uppercase;
  border-bottom: 1px solid var(--border-hairline);
}

.clientes__table tbody tr {
  transition: background-color 0.15s ease;
}

.clientes__table tbody tr + tr td {
  border-top: 1px solid var(--border-hairline);
}

.clientes__table tbody tr:hover {
  background: var(--brand-050);
}

.clientes__nome {
  font-weight: 600;
}

.clientes__col-acoes {
  width: 1%;
  white-space: nowrap;
}

.clientes__acoes {
  display: flex;
  align-items: center;
  gap: 8px;
}

.clientes__acao-btn {
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

.clientes__acao-btn:hover:not(:disabled) {
  background: var(--bg-surface-sunken);
  color: var(--text-primary);
}

.clientes__acao-btn--perigo:hover:not(:disabled) {
  border-color: var(--status-critical);
  color: var(--text-critical);
}

.clientes__acao-btn:disabled {
  opacity: 0.6;
  cursor: default;
}

.clientes__paginacao {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 20px;
  border-top: 1px solid var(--border-hairline);
}

.clientes__total {
  font-size: 12.5px;
  color: var(--text-muted);
}

.clientes__paginacao-controles {
  display: flex;
  align-items: center;
  gap: 10px;
}

.clientes__pagina-atual {
  font-size: 12.5px;
  color: var(--text-secondary);
  white-space: nowrap;
}

.clientes__pagina-btn {
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

.clientes__pagina-btn:hover:not(:disabled) {
  background: var(--bg-surface-sunken);
}

.clientes__pagina-btn:disabled {
  opacity: 0.5;
  cursor: default;
}

@media (max-width: 720px) {
  .clientes {
    padding: 16px 16px 32px;
  }

  .clientes__toolbar {
    padding: 14px 16px;
  }

  .clientes__filtro {
    width: 100%;
  }

  .clientes__campo {
    flex: 1;
    min-width: 0;
  }

  .clientes__campo input,
  .clientes__campo select {
    width: 100%;
  }

  .clientes__toolbar > .btn--primary {
    width: 100%;
    justify-content: center;
  }

  .clientes__table thead {
    display: none;
  }

  .clientes__table,
  .clientes__table tbody,
  .clientes__table tr,
  .clientes__table td {
    display: block;
    width: 100%;
  }

  .clientes__table tbody tr {
    padding: 14px 16px;
  }

  .clientes__table tbody tr + tr {
    border-top: 1px solid var(--border-hairline);
  }

  .clientes__table tbody tr + tr td {
    border-top: none;
  }

  .clientes__table td {
    padding: 4px 0;
  }

  .clientes__table td[data-label]::before {
    content: attr(data-label);
    display: block;
    font-size: 11px;
    font-weight: 600;
    letter-spacing: 0.05em;
    text-transform: uppercase;
    color: var(--text-muted);
    margin-bottom: 2px;
  }

  .clientes__col-acoes {
    padding-top: 10px;
  }

  .clientes__acoes {
    justify-content: flex-end;
  }

  .clientes__paginacao {
    flex-wrap: wrap;
    justify-content: center;
    text-align: center;
    padding: 14px 16px;
  }
}
</style>
