<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppIcon from '../../components/AppIcon.vue'
import { ApiError } from '../../lib/http'
import { cargoService } from '../../services/cargo.service'
import { unidadeService } from '../../services/unidade.service'
import type { UnidadeEntity } from '../../types/api'

const route = useRoute()
const router = useRouter()

const idCargo = computed(() => route.params.id as string | undefined)
const emEdicao = computed(() => !!idCargo.value)

const form = reactive({ idUnidade: '', nome: '' })
const unidades = ref<UnidadeEntity[]>([])

const carregando = ref(false)
const salvando = ref(false)
const erro = ref('')

async function carregarUnidades() {
  try {
    const resultado = await unidadeService.listar(0, 100)
    unidades.value = resultado.content
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível carregar as unidades.'
  }
}

async function carregarCargo(id: string) {
  carregando.value = true
  erro.value = ''

  try {
    const cargo = await cargoService.buscarPorId(id)
    form.idUnidade = cargo.unidade.id
    form.nome = cargo.nome
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível carregar o cargo.'
  } finally {
    carregando.value = false
  }
}

onMounted(async () => {
  await carregarUnidades()
  if (idCargo.value) await carregarCargo(idCargo.value)
})

function voltar() {
  router.push('/cargos')
}

async function onSubmit() {
  if (salvando.value) return

  salvando.value = true
  erro.value = ''

  const dados = { idUnidade: form.idUnidade, nome: form.nome.trim() }

  try {
    if (idCargo.value) {
      await cargoService.atualizar(idCargo.value, dados)
    } else {
      await cargoService.criar(dados)
    }
    voltar()
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível salvar o cargo.'
  } finally {
    salvando.value = false
  }
}
</script>

<template>
  <div class="cargo-manter">
    <button type="button" class="btn cargo-manter__voltar" @click="voltar">
      <AppIcon name="arrow-left" :size="15" />
      Voltar para cargos
    </button>

    <div class="cargo-manter__card">
      <div v-if="carregando" class="cargo-manter__state">
        <AppIcon name="loader" :size="20" class="cargo-manter__spinner" />
        <span>Carregando cargo...</span>
      </div>

      <template v-else>
        <div class="cargo-manter__header">
          <h2>{{ emEdicao ? 'Editar cargo' : 'Novo cargo' }}</h2>
          <p>Preencha os dados abaixo para {{ emEdicao ? 'atualizar o' : 'cadastrar um novo' }} cargo da unidade.</p>
        </div>

        <form class="cargo-manter__form" @submit.prevent="onSubmit">
          <div class="cargo-manter__campo">
            <label for="cargo-unidade">Unidade</label>
            <select id="cargo-unidade" v-model="form.idUnidade" required :disabled="salvando">
              <option value="" disabled>Selecione uma unidade</option>
              <option v-for="unidade in unidades" :key="unidade.id" :value="unidade.id">{{ unidade.nome }}</option>
            </select>
          </div>

          <div class="cargo-manter__campo">
            <label for="cargo-nome">Nome</label>
            <input
              id="cargo-nome"
              v-model="form.nome"
              type="text"
              placeholder="Ex.: Gerente"
              required
              :disabled="salvando"
            />
          </div>

          <p v-if="erro" class="cargo-manter__erro">
            <AppIcon name="alert-circle" :size="15" />
            {{ erro }}
          </p>

          <div class="cargo-manter__acoes">
            <button type="button" class="btn" :disabled="salvando" @click="voltar">Cancelar</button>
            <button type="submit" class="btn btn--primary" :disabled="salvando">
              <AppIcon v-if="salvando" name="loader" :size="15" class="cargo-manter__spinner" />
              {{ salvando ? 'Salvando...' : emEdicao ? 'Salvar alterações' : 'Cadastrar cargo' }}
            </button>
          </div>
        </form>
      </template>
    </div>
  </div>
</template>

<style scoped>
.cargo-manter {
  padding: 22px 28px 40px;
  display: flex;
  flex-direction: column;
  min-height: calc(100svh - 73px);
}

.cargo-manter__voltar {
  align-self: flex-start;
  margin-bottom: 18px;
}

.cargo-manter__card {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 8px clamp(0px, 5vw, 24px) 0;
}

.cargo-manter__header {
  padding-bottom: 24px;
  margin-bottom: 32px;
  border-bottom: 1px solid var(--border-hairline);
}

.cargo-manter__header h2 {
  font-size: 20px;
  font-weight: 700;
}

.cargo-manter__header p {
  margin-top: 6px;
  font-size: 13.5px;
  color: var(--text-muted);
}

.cargo-manter__state {
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

.cargo-manter__spinner {
  animation: cargo-manter-spin 0.8s linear infinite;
  color: var(--brand-500);
}

@keyframes cargo-manter-spin {
  to {
    transform: rotate(360deg);
  }
}

.cargo-manter__form {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  align-content: start;
  gap: 24px 28px;
}

.cargo-manter__campo {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

@media (max-width: 760px) {
  .cargo-manter__form {
    grid-template-columns: 1fr;
  }
}

.cargo-manter__campo label {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
}

.cargo-manter__campo input,
.cargo-manter__campo select {
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

.cargo-manter__campo input::placeholder {
  color: var(--text-muted);
}

.cargo-manter__campo input:focus,
.cargo-manter__campo select:focus {
  box-shadow: 0 0 0 2px var(--brand-100);
}

.cargo-manter__ajuda {
  font-size: 12.5px;
  color: var(--text-muted);
}

.cargo-manter__erro {
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

.cargo-manter__acoes {
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
