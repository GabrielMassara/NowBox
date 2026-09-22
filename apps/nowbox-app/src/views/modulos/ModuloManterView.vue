<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppIcon from '../../components/AppIcon.vue'
import { ApiError } from '../../lib/http'
import { moduloService } from '../../services/modulo.service'
import { sessaoService } from '../../services/sessao.service'
import type { SessaoResponseDTO } from '../../types/api'

const route = useRoute()
const router = useRouter()

const idModulo = computed(() => route.params.id as string | undefined)
const emEdicao = computed(() => !!idModulo.value)

const form = reactive({ idSessao: '', nome: '', rota: '' })
const sessoes = ref<SessaoResponseDTO[]>([])

const carregando = ref(false)
const salvando = ref(false)
const erro = ref('')

async function carregarSessoes() {
  try {
    const resultado = await sessaoService.listar(0, 100)
    sessoes.value = resultado.content
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível carregar as sessões.'
  }
}

async function carregarModulo(id: string) {
  carregando.value = true
  erro.value = ''

  try {
    const modulo = await moduloService.buscarPorId(id)
    form.idSessao = modulo.sessao.id
    form.nome = modulo.nome
    form.rota = modulo.rota
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível carregar o módulo.'
  } finally {
    carregando.value = false
  }
}

onMounted(async () => {
  await carregarSessoes()
  if (idModulo.value) await carregarModulo(idModulo.value)
})

function voltar() {
  router.push('/modulos')
}

async function onSubmit() {
  if (salvando.value) return

  salvando.value = true
  erro.value = ''

  const dados = { idSessao: form.idSessao, nome: form.nome.trim(), rota: form.rota.trim() }

  try {
    if (idModulo.value) {
      await moduloService.atualizar(idModulo.value, dados)
    } else {
      await moduloService.criar(dados)
    }
    voltar()
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível salvar o módulo.'
  } finally {
    salvando.value = false
  }
}
</script>

<template>
  <div class="modulo-manter">
    <button type="button" class="btn modulo-manter__voltar" @click="voltar">
      <AppIcon name="arrow-left" :size="15" />
      Voltar para módulos
    </button>

    <div class="modulo-manter__card">
      <div v-if="carregando" class="modulo-manter__state">
        <AppIcon name="loader" :size="20" class="modulo-manter__spinner" />
        <span>Carregando módulo...</span>
      </div>

      <template v-else>
        <div class="modulo-manter__header">
          <h2>{{ emEdicao ? 'Editar módulo' : 'Novo módulo' }}</h2>
          <p>Preencha os dados abaixo para {{ emEdicao ? 'atualizar o' : 'cadastrar um novo' }} módulo do menu.</p>
        </div>

        <form class="modulo-manter__form" @submit.prevent="onSubmit">
          <div class="modulo-manter__campo">
            <label for="modulo-sessao">Sessão</label>
            <select id="modulo-sessao" v-model="form.idSessao" required :disabled="salvando">
              <option value="" disabled>Selecione uma sessão</option>
              <option v-for="sessao in sessoes" :key="sessao.id" :value="sessao.id">{{ sessao.nome }}</option>
            </select>
          </div>

          <div class="modulo-manter__campo">
            <label for="modulo-nome">Nome</label>
            <input
              id="modulo-nome"
              v-model="form.nome"
              type="text"
              placeholder="Ex.: Módulos"
              required
              :disabled="salvando"
            />
          </div>

          <div class="modulo-manter__campo">
            <label for="modulo-rota">Rota</label>
            <input
              id="modulo-rota"
              v-model="form.rota"
              type="text"
              placeholder="Ex.: /modulos"
              required
              :disabled="salvando"
            />
            <p class="modulo-manter__ajuda">Caminho usado para acessar o módulo no menu.</p>
          </div>

          <p v-if="erro" class="modulo-manter__erro">
            <AppIcon name="alert-circle" :size="15" />
            {{ erro }}
          </p>

          <div class="modulo-manter__acoes">
            <button type="button" class="btn" :disabled="salvando" @click="voltar">Cancelar</button>
            <button type="submit" class="btn btn--primary" :disabled="salvando">
              <AppIcon v-if="salvando" name="loader" :size="15" class="modulo-manter__spinner" />
              {{ salvando ? 'Salvando...' : emEdicao ? 'Salvar alterações' : 'Cadastrar módulo' }}
            </button>
          </div>
        </form>
      </template>
    </div>
  </div>
</template>

<style scoped>
.modulo-manter {
  padding: 22px 28px 40px;
  display: flex;
  flex-direction: column;
  min-height: calc(100svh - 73px);
}

.modulo-manter__voltar {
  align-self: flex-start;
  margin-bottom: 18px;
}

.modulo-manter__card {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 8px clamp(0px, 5vw, 24px) 0;
}

.modulo-manter__header {
  padding-bottom: 24px;
  margin-bottom: 32px;
  border-bottom: 1px solid var(--border-hairline);
}

.modulo-manter__header h2 {
  font-size: 20px;
  font-weight: 700;
}

.modulo-manter__header p {
  margin-top: 6px;
  font-size: 13.5px;
  color: var(--text-muted);
}

.modulo-manter__state {
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

.modulo-manter__spinner {
  animation: modulo-manter-spin 0.8s linear infinite;
  color: var(--brand-500);
}

@keyframes modulo-manter-spin {
  to {
    transform: rotate(360deg);
  }
}

.modulo-manter__form {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  align-content: start;
  gap: 24px 28px;
}

.modulo-manter__campo {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

@media (max-width: 760px) {
  .modulo-manter__form {
    grid-template-columns: 1fr;
  }
}

.modulo-manter__campo label {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
}

.modulo-manter__campo input,
.modulo-manter__campo select {
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

.modulo-manter__campo input::placeholder {
  color: var(--text-muted);
}

.modulo-manter__campo input:focus,
.modulo-manter__campo select:focus {
  box-shadow: 0 0 0 2px var(--brand-100);
}

.modulo-manter__ajuda {
  font-size: 12.5px;
  color: var(--text-muted);
}

.modulo-manter__erro {
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

.modulo-manter__acoes {
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
