<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppIcon from '../../components/AppIcon.vue'
import { ApiError } from '../../lib/http'
import { sessaoService } from '../../services/sessao.service'

const route = useRoute()
const router = useRouter()

const idSessao = computed(() => route.params.id as string | undefined)
const emEdicao = computed(() => !!idSessao.value)

const form = reactive({ nome: '', rota: '' })

const carregando = ref(false)
const salvando = ref(false)
const erro = ref('')

async function carregarSessao(id: string) {
  carregando.value = true
  erro.value = ''

  try {
    const sessao = await sessaoService.buscarPorId(id)
    form.nome = sessao.nome
    form.rota = sessao.rota
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível carregar a sessão.'
  } finally {
    carregando.value = false
  }
}

onMounted(() => {
  if (idSessao.value) carregarSessao(idSessao.value)
})

function voltar() {
  router.push('/sessoes')
}

async function onSubmit() {
  if (salvando.value) return

  salvando.value = true
  erro.value = ''

  const dados = { nome: form.nome.trim(), rota: form.rota.trim() }

  try {
    if (idSessao.value) {
      await sessaoService.atualizar(idSessao.value, dados)
    } else {
      await sessaoService.criar(dados)
    }
    voltar()
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível salvar a sessão.'
  } finally {
    salvando.value = false
  }
}
</script>

<template>
  <div class="sessao-manter">
    <button type="button" class="btn sessao-manter__voltar" @click="voltar">
      <AppIcon name="arrow-left" :size="15" />
      Voltar para sessões
    </button>

    <div class="sessao-manter__card">
      <div v-if="carregando" class="sessao-manter__state">
        <AppIcon name="loader" :size="20" class="sessao-manter__spinner" />
        <span>Carregando sessão...</span>
      </div>

      <template v-else>
        <div class="sessao-manter__header">
          <h2>{{ emEdicao ? 'Editar sessão' : 'Nova sessão' }}</h2>
          <p>Preencha os dados abaixo para {{ emEdicao ? 'atualizar a' : 'cadastrar uma nova' }} sessão do menu.</p>
        </div>

        <form class="sessao-manter__form" @submit.prevent="onSubmit">
          <div class="sessao-manter__campo">
            <label for="sessao-nome">Nome</label>
            <input
              id="sessao-nome"
              v-model="form.nome"
              type="text"
              placeholder="Ex.: Administração"
              required
              :disabled="salvando"
            />
          </div>

          <div class="sessao-manter__campo">
            <label for="sessao-rota">Rota</label>
            <input
              id="sessao-rota"
              v-model="form.rota"
              type="text"
              placeholder="Ex.: /administracao"
              required
              :disabled="salvando"
            />
            <p class="sessao-manter__ajuda">Caminho usado para agrupar os módulos dessa sessão no menu.</p>
          </div>

          <p v-if="erro" class="sessao-manter__erro">
            <AppIcon name="alert-circle" :size="15" />
            {{ erro }}
          </p>

          <div class="sessao-manter__acoes">
            <button type="button" class="btn" :disabled="salvando" @click="voltar">Cancelar</button>
            <button type="submit" class="btn btn--primary" :disabled="salvando">
              <AppIcon v-if="salvando" name="loader" :size="15" class="sessao-manter__spinner" />
              {{ salvando ? 'Salvando...' : emEdicao ? 'Salvar alterações' : 'Cadastrar sessão' }}
            </button>
          </div>
        </form>
      </template>
    </div>
  </div>
</template>

<style scoped>
.sessao-manter {
  padding: 22px 28px 40px;
  display: flex;
  flex-direction: column;
  min-height: calc(100svh - 73px);
}

.sessao-manter__voltar {
  align-self: flex-start;
  margin-bottom: 18px;
}

.sessao-manter__card {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 8px clamp(0px, 5vw, 24px) 0;
}

.sessao-manter__header {
  padding-bottom: 24px;
  margin-bottom: 32px;
  border-bottom: 1px solid var(--border-hairline);
}

.sessao-manter__header h2 {
  font-size: 20px;
  font-weight: 700;
}

.sessao-manter__header p {
  margin-top: 6px;
  font-size: 13.5px;
  color: var(--text-muted);
}

.sessao-manter__state {
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

.sessao-manter__spinner {
  animation: sessao-manter-spin 0.8s linear infinite;
  color: var(--brand-500);
}

@keyframes sessao-manter-spin {
  to {
    transform: rotate(360deg);
  }
}

.sessao-manter__form {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  align-content: start;
  gap: 24px 28px;
}

.sessao-manter__campo {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

@media (max-width: 760px) {
  .sessao-manter__form {
    grid-template-columns: 1fr;
  }
}

.sessao-manter__campo label {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
}

.sessao-manter__campo input {
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

.sessao-manter__campo input::placeholder {
  color: var(--text-muted);
}

.sessao-manter__campo input:focus {
  box-shadow: 0 0 0 2px var(--brand-100);
}

.sessao-manter__ajuda {
  font-size: 12.5px;
  color: var(--text-muted);
}

.sessao-manter__erro {
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

.sessao-manter__acoes {
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
