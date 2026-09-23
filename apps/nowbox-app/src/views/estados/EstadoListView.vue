<script setup lang="ts">
import { ref } from 'vue'
import AppIcon from '../../components/AppIcon.vue'
import { ApiError } from '../../lib/http'
import { estadoService } from '../../services/estado.service'
import type { EstadoEntity } from '../../types/api'

const TAMANHO_PAGINA = 10

const estados = ref<EstadoEntity[]>([])
const carregando = ref(false)
const erro = ref('')

const pagina = ref(0)
const totalPaginas = ref(0)
const totalElementos = ref(0)

async function carregar() {
  carregando.value = true
  erro.value = ''

  try {
    const resultado = await estadoService.listar(pagina.value, TAMANHO_PAGINA)
    estados.value = resultado.content
    totalPaginas.value = resultado.page.totalPages
    totalElementos.value = resultado.page.totalElements
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível carregar os estados.'
  } finally {
    carregando.value = false
  }
}

function irParaPagina(novaPagina: number) {
  if (novaPagina < 0 || novaPagina >= totalPaginas.value) return
  pagina.value = novaPagina
  carregar()
}

carregar()
</script>

<template>
  <div class="estados">
    <div class="estados__painel">
      <div v-if="carregando" class="estados__state">
        <AppIcon name="loader" :size="20" class="estados__spinner" />
        <span>Carregando estados...</span>
      </div>

      <div v-else-if="erro" class="estados__state estados__state--error">
        <AppIcon name="alert-circle" :size="20" />
        <span>{{ erro }}</span>
        <button type="button" class="btn" @click="carregar">Tentar novamente</button>
      </div>

      <div v-else-if="estados.length === 0" class="estados__state">
        <AppIcon name="map-pin" :size="20" />
        <span>Nenhum estado encontrado.</span>
      </div>

      <table v-else class="estados__table">
        <thead>
          <tr>
            <th>Nome</th>
            <th>UF</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="estado in estados" :key="estado.id">
            <td class="estados__nome" data-label="Nome">{{ estado.nome }}</td>
            <td data-label="UF"><span class="estados__uf">{{ estado.uf }}</span></td>
          </tr>
        </tbody>
      </table>

      <div v-if="!carregando && !erro && estados.length > 0" class="estados__paginacao">
        <span class="estados__total">{{ totalElementos }} estado(s) no total</span>

        <div class="estados__paginacao-controles">
          <button
            type="button"
            class="estados__pagina-btn"
            aria-label="Página anterior"
            :disabled="pagina === 0"
            @click="irParaPagina(pagina - 1)"
          >
            <AppIcon name="chevron-left" :size="16" />
          </button>

          <span class="estados__pagina-atual">Página {{ pagina + 1 }} de {{ Math.max(totalPaginas, 1) }}</span>

          <button
            type="button"
            class="estados__pagina-btn"
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
.estados {
  padding: 22px 28px 40px;
}

.estados__painel {
  background: var(--bg-surface);
  border: 1px solid var(--border-hairline);
  box-shadow: var(--shadow-card);
}

.estados__state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 48px 16px;
  text-align: center;
  color: var(--text-muted);
  font-size: 13.5px;
}

.estados__state--error {
  color: var(--text-critical);
}

.estados__spinner {
  animation: estados-spin 0.8s linear infinite;
  color: var(--brand-500);
}

@keyframes estados-spin {
  to {
    transform: rotate(360deg);
  }
}

.estados__table {
  width: 100%;
  border-collapse: collapse;
}

.estados__table th,
.estados__table td {
  text-align: left;
  padding: 13px 20px;
  font-size: 13.5px;
}

.estados__table thead th {
  background: var(--bg-surface-sunken);
  color: var(--text-muted);
  font-weight: 600;
  font-size: 12px;
  letter-spacing: 0.05em;
  text-transform: uppercase;
  border-bottom: 1px solid var(--border-hairline);
}

.estados__table tbody tr {
  transition: background-color 0.15s ease;
}

.estados__table tbody tr + tr td {
  border-top: 1px solid var(--border-hairline);
}

.estados__table tbody tr:hover {
  background: var(--brand-050);
}

.estados__nome {
  font-weight: 600;
}

.estados__uf {
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 12.5px;
  color: var(--text-secondary);
}

.estados__paginacao {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 20px;
  border-top: 1px solid var(--border-hairline);
}

.estados__total {
  font-size: 12.5px;
  color: var(--text-muted);
}

.estados__paginacao-controles {
  display: flex;
  align-items: center;
  gap: 10px;
}

.estados__pagina-atual {
  font-size: 12.5px;
  color: var(--text-secondary);
  white-space: nowrap;
}

.estados__pagina-btn {
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

.estados__pagina-btn:hover:not(:disabled) {
  background: var(--bg-surface-sunken);
}

.estados__pagina-btn:disabled {
  opacity: 0.5;
  cursor: default;
}

@media (max-width: 720px) {
  .estados {
    padding: 16px 16px 32px;
  }

  .estados__table thead {
    display: none;
  }

  .estados__table,
  .estados__table tbody,
  .estados__table tr,
  .estados__table td {
    display: block;
    width: 100%;
  }

  .estados__table tbody tr {
    padding: 14px 16px;
  }

  .estados__table tbody tr + tr {
    border-top: 1px solid var(--border-hairline);
  }

  .estados__table tbody tr + tr td {
    border-top: none;
  }

  .estados__table td {
    padding: 4px 0;
  }

  .estados__table td[data-label]::before {
    content: attr(data-label);
    display: block;
    font-size: 11px;
    font-weight: 600;
    letter-spacing: 0.05em;
    text-transform: uppercase;
    color: var(--text-muted);
    margin-bottom: 2px;
  }

  .estados__paginacao {
    flex-wrap: wrap;
    justify-content: center;
    text-align: center;
    padding: 14px 16px;
  }
}
</style>
