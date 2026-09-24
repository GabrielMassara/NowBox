<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import AppIcon from '../../components/AppIcon.vue'
import { ApiError } from '../../lib/http'
import { usuarioService } from '../../services/usuario.service'
import type { UsuarioResponseDTO } from '../../types/api'

const TAMANHO_PAGINA = 10

const router = useRouter()

const usuarios = ref<UsuarioResponseDTO[]>([])
const carregando = ref(false)
const erro = ref('')
const excluindoId = ref('')

const pagina = ref(0)
const totalPaginas = ref(0)
const totalElementos = ref(0)

const filtro = reactive({ nome: '', email: '', cpf: '' })

const SEXOS: Record<string, string> = { M: 'Masculino', F: 'Feminino' }

function formatarCpf(cpf: string) {
  return cpf.replace(/^(\d{3})(\d{3})(\d{3})(\d{2})$/, '$1.$2.$3-$4')
}

async function carregar() {
  carregando.value = true
  erro.value = ''

  try {
    const resultado = await usuarioService.listar(pagina.value, TAMANHO_PAGINA, {
      nome: filtro.nome.trim() || undefined,
      email: filtro.email.trim() || undefined,
      cpf: filtro.cpf.replace(/\D/g, '') || undefined,
    })
    usuarios.value = resultado.content
    totalPaginas.value = resultado.totalPages
    totalElementos.value = resultado.totalElements
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível carregar os usuários.'
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
  buscar()
}

function irParaPagina(novaPagina: number) {
  if (novaPagina < 0 || novaPagina >= totalPaginas.value) return
  pagina.value = novaPagina
  carregar()
}

function novoUsuario() {
  router.push('/usuarios/novo')
}

function editarUsuario(usuario: UsuarioResponseDTO) {
  router.push(`/usuarios/${usuario.id}/editar`)
}

async function excluirUsuario(usuario: UsuarioResponseDTO) {
  if (!confirm(`Excluir o usuário "${usuario.nome}"?`)) return

  excluindoId.value = usuario.id
  try {
    await usuarioService.excluir(usuario.id)
    if (usuarios.value.length === 1 && pagina.value > 0) pagina.value -= 1
    await carregar()
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível excluir o usuário.'
  } finally {
    excluindoId.value = ''
  }
}

onMounted(carregar)
</script>

<template>
  <div class="usuarios">
    <div class="usuarios__painel">
      <div class="usuarios__toolbar">
        <form class="usuarios__filtro" @submit.prevent="buscar">
          <div class="usuarios__campo">
            <AppIcon name="search" :size="16" />
            <input v-model="filtro.nome" type="text" placeholder="Buscar por nome" aria-label="Buscar por nome" />
          </div>

          <div class="usuarios__campo">
            <AppIcon name="mail" :size="16" />
            <input v-model="filtro.email" type="text" placeholder="E-mail" aria-label="Buscar por e-mail" />
          </div>

          <div class="usuarios__campo">
            <AppIcon name="search" :size="16" />
            <input
              v-model="filtro.cpf"
              type="text"
              inputmode="numeric"
              maxlength="14"
              placeholder="CPF"
              aria-label="Buscar por CPF"
            />
          </div>

          <button type="submit" class="btn">Filtrar</button>
          <button v-if="filtro.nome || filtro.email || filtro.cpf" type="button" class="btn" @click="limparFiltro">
            Limpar
          </button>
        </form>

        <button type="button" class="btn btn--primary" @click="novoUsuario">
          <AppIcon name="plus" :size="16" />
          Novo usuário
        </button>
      </div>

      <div v-if="carregando" class="usuarios__state">
        <AppIcon name="loader" :size="20" class="usuarios__spinner" />
        <span>Carregando usuários...</span>
      </div>

      <div v-else-if="erro" class="usuarios__state usuarios__state--error">
        <AppIcon name="alert-circle" :size="20" />
        <span>{{ erro }}</span>
        <button type="button" class="btn" @click="carregar">Tentar novamente</button>
      </div>

      <div v-else-if="usuarios.length === 0" class="usuarios__state">
        <AppIcon name="user-circle" :size="20" />
        <span>Nenhum usuário encontrado.</span>
      </div>

      <table v-else class="usuarios__table">
        <thead>
          <tr>
            <th>Nome</th>
            <th>E-mail</th>
            <th>CPF</th>
            <th>Sexo</th>
            <th class="usuarios__col-acoes">Ações</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="usuario in usuarios" :key="usuario.id">
            <td class="usuarios__nome" data-label="Nome">{{ usuario.nome }}</td>
            <td data-label="E-mail">{{ usuario.email }}</td>
            <td data-label="CPF">{{ formatarCpf(usuario.cpf) }}</td>
            <td data-label="Sexo">{{ SEXOS[usuario.sexo] ?? usuario.sexo }}</td>
            <td class="usuarios__col-acoes">
              <div class="usuarios__acoes">
                <button
                  type="button"
                  class="usuarios__acao-btn"
                  aria-label="Editar usuário"
                  title="Editar"
                  @click="editarUsuario(usuario)"
                >
                  <AppIcon name="pencil" :size="16" />
                </button>
                <button
                  type="button"
                  class="usuarios__acao-btn usuarios__acao-btn--perigo"
                  aria-label="Excluir usuário"
                  title="Excluir"
                  :disabled="excluindoId === usuario.id"
                  @click="excluirUsuario(usuario)"
                >
                  <AppIcon
                    :name="excluindoId === usuario.id ? 'loader' : 'trash'"
                    :size="16"
                    :class="{ 'usuarios__spinner': excluindoId === usuario.id }"
                  />
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>

      <div v-if="!carregando && !erro && usuarios.length > 0" class="usuarios__paginacao">
        <span class="usuarios__total">{{ totalElementos }} usuário(s) no total</span>

        <div class="usuarios__paginacao-controles">
          <button
            type="button"
            class="usuarios__pagina-btn"
            aria-label="Página anterior"
            :disabled="pagina === 0"
            @click="irParaPagina(pagina - 1)"
          >
            <AppIcon name="chevron-left" :size="16" />
          </button>

          <span class="usuarios__pagina-atual">Página {{ pagina + 1 }} de {{ Math.max(totalPaginas, 1) }}</span>

          <button
            type="button"
            class="usuarios__pagina-btn"
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
.usuarios {
  padding: 22px 28px 40px;
}

.usuarios__painel {
  background: var(--bg-surface);
  border: 1px solid var(--border-hairline);
  box-shadow: var(--shadow-card);
}

.usuarios__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-hairline);
}

.usuarios__filtro {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.usuarios__campo {
  display: flex;
  align-items: center;
  gap: 8px;
  background: var(--bg-surface);
  border: 1px solid var(--border-hairline);
  padding: 8px 12px;
  color: var(--text-muted);
}

.usuarios__campo input,
.usuarios__campo select {
  border: none;
  outline: none;
  background: transparent;
  font: inherit;
  font-size: 13.5px;
  color: var(--text-primary);
  width: 160px;
}

.usuarios__campo input::placeholder {
  color: var(--text-muted);
}

.usuarios__filtro .btn {
  border-radius: 0;
}

.usuarios__state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 48px 16px;
  text-align: center;
  color: var(--text-muted);
  font-size: 13.5px;
}

.usuarios__state--error {
  color: var(--text-critical);
}

.usuarios__spinner {
  animation: usuarios-spin 0.8s linear infinite;
  color: var(--brand-500);
}

@keyframes usuarios-spin {
  to {
    transform: rotate(360deg);
  }
}

.usuarios__table {
  width: 100%;
  border-collapse: collapse;
}

.usuarios__table th,
.usuarios__table td {
  text-align: left;
  padding: 13px 20px;
  font-size: 13.5px;
}

.usuarios__table thead th {
  background: var(--bg-surface-sunken);
  color: var(--text-muted);
  font-weight: 600;
  font-size: 12px;
  letter-spacing: 0.05em;
  text-transform: uppercase;
  border-bottom: 1px solid var(--border-hairline);
}

.usuarios__table tbody tr {
  transition: background-color 0.15s ease;
}

.usuarios__table tbody tr + tr td {
  border-top: 1px solid var(--border-hairline);
}

.usuarios__table tbody tr:hover {
  background: var(--brand-050);
}

.usuarios__nome {
  font-weight: 600;
}

.usuarios__col-acoes {
  width: 1%;
  white-space: nowrap;
}

.usuarios__acoes {
  display: flex;
  align-items: center;
  gap: 8px;
}

.usuarios__acao-btn {
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

.usuarios__acao-btn:hover:not(:disabled) {
  background: var(--bg-surface-sunken);
  color: var(--text-primary);
}

.usuarios__acao-btn--perigo:hover:not(:disabled) {
  border-color: var(--status-critical);
  color: var(--text-critical);
}

.usuarios__acao-btn:disabled {
  opacity: 0.6;
  cursor: default;
}

.usuarios__paginacao {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 20px;
  border-top: 1px solid var(--border-hairline);
}

.usuarios__total {
  font-size: 12.5px;
  color: var(--text-muted);
}

.usuarios__paginacao-controles {
  display: flex;
  align-items: center;
  gap: 10px;
}

.usuarios__pagina-atual {
  font-size: 12.5px;
  color: var(--text-secondary);
  white-space: nowrap;
}

.usuarios__pagina-btn {
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

.usuarios__pagina-btn:hover:not(:disabled) {
  background: var(--bg-surface-sunken);
}

.usuarios__pagina-btn:disabled {
  opacity: 0.5;
  cursor: default;
}

@media (max-width: 720px) {
  .usuarios {
    padding: 16px 16px 32px;
  }

  .usuarios__toolbar {
    padding: 14px 16px;
  }

  .usuarios__filtro {
    width: 100%;
  }

  .usuarios__campo {
    flex: 1;
    min-width: 0;
  }

  .usuarios__campo input,
  .usuarios__campo select {
    width: 100%;
  }

  .usuarios__toolbar > .btn--primary {
    width: 100%;
    justify-content: center;
  }

  .usuarios__table thead {
    display: none;
  }

  .usuarios__table,
  .usuarios__table tbody,
  .usuarios__table tr,
  .usuarios__table td {
    display: block;
    width: 100%;
  }

  .usuarios__table tbody tr {
    padding: 14px 16px;
  }

  .usuarios__table tbody tr + tr {
    border-top: 1px solid var(--border-hairline);
  }

  .usuarios__table tbody tr + tr td {
    border-top: none;
  }

  .usuarios__table td {
    padding: 4px 0;
  }

  .usuarios__table td[data-label]::before {
    content: attr(data-label);
    display: block;
    font-size: 11px;
    font-weight: 600;
    letter-spacing: 0.05em;
    text-transform: uppercase;
    color: var(--text-muted);
    margin-bottom: 2px;
  }

  .usuarios__col-acoes {
    padding-top: 10px;
  }

  .usuarios__acoes {
    justify-content: flex-end;
  }

  .usuarios__paginacao {
    flex-wrap: wrap;
    justify-content: center;
    text-align: center;
    padding: 14px 16px;
  }
}
</style>
