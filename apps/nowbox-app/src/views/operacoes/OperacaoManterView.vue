<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppIcon from '../../components/AppIcon.vue'
import { ApiError } from '../../lib/http'
import { moduloService } from '../../services/modulo.service'
import { operacaoService } from '../../services/operacao.service'
import type { ModuloResponseDTO } from '../../types/api'

const route = useRoute()
const router = useRouter()

const idOperacao = computed(() => route.params.id as string | undefined)
const emEdicao = computed(() => !!idOperacao.value)

const form = reactive({ idModulo: '', nome: '', codigo: '' })
const modulos = ref<ModuloResponseDTO[]>([])

const carregando = ref(false)
const salvando = ref(false)
const erro = ref('')

async function carregarModulos() {
  try {
    const resultado = await moduloService.listar(0, 100)
    modulos.value = resultado.content
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível carregar os módulos.'
  }
}

async function carregarOperacao(id: string) {
  carregando.value = true
  erro.value = ''

  try {
    const operacao = await operacaoService.buscarPorId(id)
    form.idModulo = operacao.modulo.id
    form.nome = operacao.nome
    form.codigo = operacao.codigo
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível carregar a operação.'
  } finally {
    carregando.value = false
  }
}

onMounted(async () => {
  await carregarModulos()
  if (idOperacao.value) await carregarOperacao(idOperacao.value)
})

function voltar() {
  router.push('/operacoes-sistema')
}

async function onSubmit() {
  if (salvando.value) return

  salvando.value = true
  erro.value = ''

  const dados = { idModulo: form.idModulo, nome: form.nome.trim(), codigo: form.codigo.trim() }

  try {
    if (idOperacao.value) {
      await operacaoService.atualizar(idOperacao.value, dados)
    } else {
      await operacaoService.criar(dados)
    }
    voltar()
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível salvar a operação.'
  } finally {
    salvando.value = false
  }
}
</script>

<template>
  <div class="operacao-manter">
    <button type="button" class="btn operacao-manter__voltar" @click="voltar">
      <AppIcon name="arrow-left" :size="15" />
      Voltar para operações
    </button>

    <div class="operacao-manter__card">
      <div v-if="carregando" class="operacao-manter__state">
        <AppIcon name="loader" :size="20" class="operacao-manter__spinner" />
        <span>Carregando operação...</span>
      </div>

      <template v-else>
        <div class="operacao-manter__header">
          <h2>{{ emEdicao ? 'Editar operação' : 'Nova operação' }}</h2>
          <p>Preencha os dados abaixo para {{ emEdicao ? 'atualizar a' : 'cadastrar uma nova' }} operação do sistema.</p>
        </div>

        <form class="operacao-manter__form" @submit.prevent="onSubmit">
          <div class="operacao-manter__campo">
            <label for="operacao-modulo">Módulo</label>
            <select id="operacao-modulo" v-model="form.idModulo" required :disabled="salvando">
              <option value="" disabled>Selecione um módulo</option>
              <option v-for="modulo in modulos" :key="modulo.id" :value="modulo.id">{{ modulo.nome }}</option>
            </select>
          </div>

          <div class="operacao-manter__campo">
            <label for="operacao-nome">Nome</label>
            <input
              id="operacao-nome"
              v-model="form.nome"
              type="text"
              placeholder="Ex.: Consultar módulos"
              required
              :disabled="salvando"
            />
          </div>

          <div class="operacao-manter__campo">
            <label for="operacao-codigo">Código</label>
            <input
              id="operacao-codigo"
              v-model="form.codigo"
              type="text"
              placeholder="Ex.: MOD_MODULO_OPE_CONSULTAR"
              required
              :disabled="salvando"
            />
            <p class="operacao-manter__ajuda">Código único usado para verificar a permissão da operação.</p>
          </div>

          <p v-if="erro" class="operacao-manter__erro">
            <AppIcon name="alert-circle" :size="15" />
            {{ erro }}
          </p>

          <div class="operacao-manter__acoes">
            <button type="button" class="btn" :disabled="salvando" @click="voltar">Cancelar</button>
            <button type="submit" class="btn btn--primary" :disabled="salvando">
              <AppIcon v-if="salvando" name="loader" :size="15" class="operacao-manter__spinner" />
              {{ salvando ? 'Salvando...' : emEdicao ? 'Salvar alterações' : 'Cadastrar operação' }}
            </button>
          </div>
        </form>
      </template>
    </div>
  </div>
</template>

<style scoped>
.operacao-manter {
  padding: 22px 28px 40px;
  display: flex;
  flex-direction: column;
  min-height: calc(100svh - 73px);
}

.operacao-manter__voltar {
  align-self: flex-start;
  margin-bottom: 18px;
}

.operacao-manter__card {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 8px clamp(0px, 5vw, 24px) 0;
}

.operacao-manter__header {
  padding-bottom: 24px;
  margin-bottom: 32px;
  border-bottom: 1px solid var(--border-hairline);
}

.operacao-manter__header h2 {
  font-size: 20px;
  font-weight: 700;
}

.operacao-manter__header p {
  margin-top: 6px;
  font-size: 13.5px;
  color: var(--text-muted);
}

.operacao-manter__state {
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

.operacao-manter__spinner {
  animation: operacao-manter-spin 0.8s linear infinite;
  color: var(--brand-500);
}

@keyframes operacao-manter-spin {
  to {
    transform: rotate(360deg);
  }
}

.operacao-manter__form {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  align-content: start;
  gap: 24px 28px;
}

.operacao-manter__campo {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

@media (max-width: 760px) {
  .operacao-manter__form {
    grid-template-columns: 1fr;
  }
}

.operacao-manter__campo label {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
}

.operacao-manter__campo input,
.operacao-manter__campo select {
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

.operacao-manter__campo input::placeholder {
  color: var(--text-muted);
}

.operacao-manter__campo input:focus,
.operacao-manter__campo select:focus {
  box-shadow: 0 0 0 2px var(--brand-100);
}

.operacao-manter__ajuda {
  font-size: 12.5px;
  color: var(--text-muted);
}

.operacao-manter__erro {
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

.operacao-manter__acoes {
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
