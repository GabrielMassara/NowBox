<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppIcon from '../../components/AppIcon.vue'
import { ApiError } from '../../lib/http'
import { carregarTodas } from '../../lib/paginacao'
import { atribuicaoService } from '../../services/atribuicao.service'
import { cargoService } from '../../services/cargo.service'
import { usuarioService } from '../../services/usuario.service'
import type { CargoResponseDTO, UsuarioResponseDTO } from '../../types/api'

const route = useRoute()
const router = useRouter()

const idAtribuicao = computed(() => route.params.id as string | undefined)
const emEdicao = computed(() => !!idAtribuicao.value)

const form = reactive({ idUsuario: '', idCargo: '' })
const usuarios = ref<UsuarioResponseDTO[]>([])
const cargos = ref<CargoResponseDTO[]>([])

const carregando = ref(false)
const salvando = ref(false)
const erro = ref('')

async function carregarOpcoes() {
  ;[usuarios.value, cargos.value] = await Promise.all([
    carregarTodas((p, t) => usuarioService.listar(p, t)),
    carregarTodas((p, t) => cargoService.listar(p, t)),
  ])
}

async function carregarAtribuicao(id: string) {
  const atribuicao = await atribuicaoService.buscarPorId(id)

  // O usuário e o cargo em edição podem não estar nas listas, então são incluídos a partir da atribuição.
  if (!usuarios.value.some((u) => u.id === atribuicao.usuario.id)) {
    usuarios.value = [...usuarios.value, atribuicao.usuario]
  }
  if (!cargos.value.some((c) => c.id === atribuicao.cargo.id)) {
    cargos.value = [...cargos.value, atribuicao.cargo]
  }

  form.idUsuario = atribuicao.usuario.id
  form.idCargo = atribuicao.cargo.id
}

onMounted(async () => {
  carregando.value = true
  erro.value = ''

  try {
    await carregarOpcoes()
    if (idAtribuicao.value) await carregarAtribuicao(idAtribuicao.value)
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível carregar os dados da tela.'
  } finally {
    carregando.value = false
  }
})

function voltar() {
  router.push('/atribuicoes')
}

async function onSubmit() {
  if (salvando.value) return

  salvando.value = true
  erro.value = ''

  const dados = { idUsuario: form.idUsuario, idCargo: form.idCargo }

  try {
    if (idAtribuicao.value) {
      await atribuicaoService.atualizar(idAtribuicao.value, dados)
    } else {
      await atribuicaoService.criar(dados)
    }
    voltar()
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível salvar a atribuição.'
  } finally {
    salvando.value = false
  }
}
</script>

<template>
  <div class="atribuicao-manter">
    <button type="button" class="btn atribuicao-manter__voltar" @click="voltar">
      <AppIcon name="arrow-left" :size="15" />
      Voltar para atribuições
    </button>

    <div class="atribuicao-manter__card">
      <div v-if="carregando" class="atribuicao-manter__state">
        <AppIcon name="loader" :size="20" class="atribuicao-manter__spinner" />
        <span>Carregando atribuição...</span>
      </div>

      <template v-else>
        <div class="atribuicao-manter__header">
          <h2>{{ emEdicao ? 'Editar atribuição' : 'Nova atribuição' }}</h2>
          <p>Preencha os dados abaixo para {{ emEdicao ? 'atualizar a' : 'cadastrar uma nova' }} atribuição de cargo.</p>
        </div>

        <form class="atribuicao-manter__form" @submit.prevent="onSubmit">
          <div class="atribuicao-manter__campo">
            <label for="atribuicao-usuario">Usuário</label>
            <select id="atribuicao-usuario" v-model="form.idUsuario" required :disabled="salvando">
              <option value="" disabled>Selecione um usuário</option>
              <option v-for="usuario in usuarios" :key="usuario.id" :value="usuario.id">
                {{ usuario.nome }} ({{ usuario.email }})
              </option>
            </select>
          </div>

          <div class="atribuicao-manter__campo">
            <label for="atribuicao-cargo">Cargo</label>
            <select id="atribuicao-cargo" v-model="form.idCargo" required :disabled="salvando">
              <option value="" disabled>Selecione um cargo</option>
              <option v-for="cargo in cargos" :key="cargo.id" :value="cargo.id">
                {{ cargo.nome }} — {{ cargo.unidade?.nome }}
              </option>
            </select>
          </div>

          <p v-if="erro" class="atribuicao-manter__erro">
            <AppIcon name="alert-circle" :size="15" />
            {{ erro }}
          </p>

          <div class="atribuicao-manter__acoes">
            <button type="button" class="btn" :disabled="salvando" @click="voltar">Cancelar</button>
            <button type="submit" class="btn btn--primary" :disabled="salvando">
              <AppIcon v-if="salvando" name="loader" :size="15" class="atribuicao-manter__spinner" />
              {{ salvando ? 'Salvando...' : emEdicao ? 'Salvar alterações' : 'Cadastrar atribuição' }}
            </button>
          </div>
        </form>
      </template>
    </div>
  </div>
</template>

<style scoped>
.atribuicao-manter {
  padding: 22px 28px 40px;
  display: flex;
  flex-direction: column;
  min-height: calc(100svh - 73px);
}

.atribuicao-manter__voltar {
  align-self: flex-start;
  margin-bottom: 18px;
}

.atribuicao-manter__card {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 8px clamp(0px, 5vw, 24px) 0;
}

.atribuicao-manter__header {
  padding-bottom: 24px;
  margin-bottom: 32px;
  border-bottom: 1px solid var(--border-hairline);
}

.atribuicao-manter__header h2 {
  font-size: 20px;
  font-weight: 700;
}

.atribuicao-manter__header p {
  margin-top: 6px;
  font-size: 13.5px;
  color: var(--text-muted);
}

.atribuicao-manter__state {
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

.atribuicao-manter__spinner {
  animation: atribuicao-manter-spin 0.8s linear infinite;
  color: var(--brand-500);
}

@keyframes atribuicao-manter-spin {
  to {
    transform: rotate(360deg);
  }
}

.atribuicao-manter__form {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  align-content: start;
  gap: 24px 28px;
}

.atribuicao-manter__campo {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

@media (max-width: 760px) {
  .atribuicao-manter__form {
    grid-template-columns: 1fr;
  }
}

.atribuicao-manter__campo label {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
}

.atribuicao-manter__campo input,
.atribuicao-manter__campo select {
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

.atribuicao-manter__campo input::placeholder {
  color: var(--text-muted);
}

.atribuicao-manter__campo input:focus,
.atribuicao-manter__campo select:focus {
  box-shadow: 0 0 0 2px var(--brand-100);
}

.atribuicao-manter__ajuda {
  font-size: 12.5px;
  color: var(--text-muted);
}

.atribuicao-manter__erro {
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

.atribuicao-manter__acoes {
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
