<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppIcon from '../../components/AppIcon.vue'
import PermissaoTree from '../../components/PermissaoTree.vue'
import { ApiError } from '../../lib/http'
import { carregarTodas } from '../../lib/paginacao'
import { cargoService } from '../../services/cargo.service'
import { operacaoService } from '../../services/operacao.service'
import { permissaoService } from '../../services/permissao.service'
import type { CargoResponseDTO, OperacaoResponseDTO } from '../../types/api'

const route = useRoute()
const router = useRouter()

const idCargoRota = computed(() => route.params.idCargo as string | undefined)
const emEdicao = computed(() => !!idCargoRota.value)

const form = reactive({ idCargo: '', idsOperacao: [] as string[] })
const cargos = ref<CargoResponseDTO[]>([])
const operacoes = ref<OperacaoResponseDTO[]>([])

const carregando = ref(false)
const carregandoPermissoes = ref(false)
const salvando = ref(false)
const erro = ref('')

// Somente após carregar as permissões atuais do cargo é seguro salvar, pois o salvamento
// substitui todo o conjunto de operações liberadas.
const podeSalvar = computed(() => !!form.idCargo && !carregandoPermissoes.value && !salvando.value)

async function carregarCargos() {
  cargos.value = await carregarTodas((p, t) => cargoService.listar(p, t))

  // O cargo em edição pode não estar na lista, então é buscado individualmente.
  const id = idCargoRota.value
  if (id && !cargos.value.some((c) => c.id === id)) {
    cargos.value = [...cargos.value, await cargoService.buscarPorId(id)]
  }
}

async function carregarPermissoesDoCargo(idCargo: string) {
  carregandoPermissoes.value = true
  erro.value = ''
  form.idsOperacao = []

  try {
    const permissoes = await carregarTodas((p, t) => permissaoService.listar(p, t, { idCargo }))
    if (form.idCargo === idCargo) form.idsOperacao = permissoes.map((permissao) => permissao.operacao.id)
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível carregar as permissões do cargo.'
  } finally {
    if (form.idCargo === idCargo) carregandoPermissoes.value = false
  }
}

watch(
  () => form.idCargo,
  (idCargo) => {
    if (idCargo) {
      carregarPermissoesDoCargo(idCargo)
    } else {
      form.idsOperacao = []
    }
  },
)

onMounted(async () => {
  carregando.value = true
  erro.value = ''

  try {
    ;[, operacoes.value] = await Promise.all([carregarCargos(), carregarTodas((p, t) => operacaoService.listar(p, t))])
    if (idCargoRota.value) form.idCargo = idCargoRota.value
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível carregar os dados da tela.'
  } finally {
    carregando.value = false
  }
})

function voltar() {
  router.push('/permissoes')
}

async function onSubmit() {
  if (!podeSalvar.value) return

  salvando.value = true
  erro.value = ''

  try {
    await permissaoService.sincronizarCargo(form.idCargo, { idsOperacao: form.idsOperacao })
    voltar()
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível salvar as permissões.'
  } finally {
    salvando.value = false
  }
}
</script>

<template>
  <div class="permissao-manter">
    <button type="button" class="btn permissao-manter__voltar" @click="voltar">
      <AppIcon name="arrow-left" :size="15" />
      Voltar para permissões
    </button>

    <div class="permissao-manter__card">
      <div v-if="carregando" class="permissao-manter__state">
        <AppIcon name="loader" :size="20" class="permissao-manter__spinner" />
        <span>Carregando dados...</span>
      </div>

      <template v-else>
        <div class="permissao-manter__header">
          <h2>{{ emEdicao ? 'Editar permissões do cargo' : 'Gerenciar permissões' }}</h2>
          <p>
            Selecione o cargo e marque as operações que ele poderá executar. Marcar uma sessão ou um módulo libera todas as
            operações que ele contém.
          </p>
        </div>

        <form class="permissao-manter__form" @submit.prevent="onSubmit">
          <div class="permissao-manter__campo">
            <label for="permissao-cargo">Cargo</label>
            <select id="permissao-cargo" v-model="form.idCargo" required :disabled="salvando || emEdicao">
              <option value="" disabled>Selecione um cargo</option>
              <option v-for="cargo in cargos" :key="cargo.id" :value="cargo.id">
                {{ cargo.nome }} — {{ cargo.unidade?.nome }}
              </option>
            </select>
          </div>

          <div class="permissao-manter__arvore">
            <div v-if="!form.idCargo" class="permissao-manter__aviso">
              <AppIcon name="shield" :size="20" />
              <span>Selecione um cargo para escolher as operações liberadas.</span>
            </div>

            <div v-else-if="carregandoPermissoes" class="permissao-manter__aviso">
              <AppIcon name="loader" :size="20" class="permissao-manter__spinner" />
              <span>Carregando permissões do cargo...</span>
            </div>

            <PermissaoTree v-else v-model="form.idsOperacao" :operacoes="operacoes" :disabled="salvando" />
          </div>

          <p v-if="erro" class="permissao-manter__erro">
            <AppIcon name="alert-circle" :size="15" />
            {{ erro }}
          </p>

          <div class="permissao-manter__acoes">
            <button type="button" class="btn" :disabled="salvando" @click="voltar">Cancelar</button>
            <button type="submit" class="btn btn--primary" :disabled="!podeSalvar">
              <AppIcon v-if="salvando" name="loader" :size="15" class="permissao-manter__spinner" />
              {{ salvando ? 'Salvando...' : 'Salvar permissões' }}
            </button>
          </div>
        </form>
      </template>
    </div>
  </div>
</template>

<style scoped>
.permissao-manter {
  padding: 22px 28px 40px;
  display: flex;
  flex-direction: column;
  min-height: calc(100svh - 73px);
}

.permissao-manter__voltar {
  align-self: flex-start;
  margin-bottom: 18px;
}

.permissao-manter__card {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 8px clamp(0px, 5vw, 24px) 0;
}

.permissao-manter__header {
  padding-bottom: 24px;
  margin-bottom: 32px;
  border-bottom: 1px solid var(--border-hairline);
}

.permissao-manter__header h2 {
  font-size: 20px;
  font-weight: 700;
}

.permissao-manter__header p {
  margin-top: 6px;
  font-size: 13.5px;
  color: var(--text-muted);
}

.permissao-manter__state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  text-align: center;
  color: var(--text-muted);
  font-size: 13.5px;
}

.permissao-manter__spinner {
  animation: permissao-manter-spin 0.8s linear infinite;
  color: var(--brand-500);
}

@keyframes permissao-manter-spin {
  to {
    transform: rotate(360deg);
  }
}

.permissao-manter__form {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  align-content: start;
  gap: 24px 28px;
}

.permissao-manter__campo {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

@media (max-width: 760px) {
  .permissao-manter__form {
    grid-template-columns: 1fr;
  }
}

.permissao-manter__campo label {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
}

.permissao-manter__campo input,
.permissao-manter__campo select {
  width: 100%;
  border: none;
  border-radius: 6px;
  background: var(--bg-surface-sunken);
  padding: 13px 14px;
  font: inherit;
  font-size: 14px;
  color: var(--text-primary);
  outline: none;
  transition: box-shadow 0.15s ease;
}

.permissao-manter__campo input::placeholder {
  color: var(--text-muted);
}

.permissao-manter__campo input:focus,
.permissao-manter__campo select:focus {
  box-shadow: 0 0 0 2px var(--brand-100);
}

.permissao-manter__arvore {
  grid-column: 1 / -1;
}

.permissao-manter__aviso {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 48px 16px;
  text-align: center;
  color: var(--text-muted);
  font-size: 13.5px;
  border: 1px solid var(--border-hairline);
}

.permissao-manter__ajuda {
  font-size: 12.5px;
  color: var(--text-muted);
}

.permissao-manter__erro {
  grid-column: 1 / -1;
  display: flex;
  align-items: center;
  gap: 7px;
  font-size: 13px;
  color: var(--text-critical);
  background: color-mix(in srgb, var(--status-critical) 10%, transparent);
  border-radius: var(--radius-sm);
  padding: 9px 11px;
}

.permissao-manter__acoes {
  grid-column: 1 / -1;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
  margin-top: auto;
  padding-top: 24px;
  border-top: 1px solid var(--border-hairline);
}
</style>
