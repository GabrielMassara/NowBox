<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppIcon from '../../components/AppIcon.vue'
import { ApiError } from '../../lib/http'
import { boxService } from '../../services/box.service'
import { unidadeStore } from '../../stores/unidade'

const route = useRoute()
const router = useRouter()

const idBox = computed(() => route.params.id as string | undefined)
const emEdicao = computed(() => !!idBox.value)

const form = reactive({ numero: '', tamanho: '', dimensoes: '', preco: '', disponivel: true })
// O box pertence à unidade selecionada; na edição vale a unidade em que ele já está cadastrado.
const unidade = ref(unidadeStore.state.selecionada)

const carregando = ref(false)
const salvando = ref(false)
const erro = ref('')

async function carregarBox(id: string) {
  carregando.value = true
  erro.value = ''

  try {
    const box = await boxService.buscarPorId(id)
    unidade.value = box.unidade
    form.numero = box.numero
    form.tamanho = String(box.tamanho)
    form.dimensoes = box.dimensoes
    form.preco = String(box.preco)
    form.disponivel = box.disponivel
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível carregar o box.'
  } finally {
    carregando.value = false
  }
}

onMounted(() => {
  if (idBox.value) carregarBox(idBox.value)
})

function voltar() {
  router.push('/boxes')
}

async function onSubmit() {
  if (salvando.value || !unidade.value) return

  salvando.value = true
  erro.value = ''

  const dados = {
    idUnidade: unidade.value.id,
    numero: form.numero.trim(),
    tamanho: Number(form.tamanho),
    dimensoes: form.dimensoes.trim(),
    disponivel: form.disponivel,
    preco: Number(form.preco),
  }

  try {
    if (idBox.value) {
      await boxService.atualizar(idBox.value, dados)
    } else {
      await boxService.criar(dados)
    }
    voltar()
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível salvar o box.'
  } finally {
    salvando.value = false
  }
}
</script>

<template>
  <div class="box-manter">
    <button type="button" class="btn box-manter__voltar" @click="voltar">
      <AppIcon name="arrow-left" :size="15" />
      Voltar para boxes
    </button>

    <div class="box-manter__card">
      <div v-if="carregando" class="box-manter__state">
        <AppIcon name="loader" :size="20" class="box-manter__spinner" />
        <span>Carregando box...</span>
      </div>

      <template v-else>
        <div class="box-manter__header">
          <h2>{{ emEdicao ? 'Editar box' : 'Novo box' }}</h2>
          <p>Preencha os dados abaixo para {{ emEdicao ? 'atualizar o' : 'cadastrar um novo' }} box da unidade.</p>
        </div>

        <form class="box-manter__form" @submit.prevent="onSubmit">
          <div class="box-manter__campo">
            <label for="box-unidade">Unidade</label>
            <input id="box-unidade" type="text" :value="unidade?.nome ?? '—'" disabled />
          </div>

          <div class="box-manter__campo">
            <label for="box-numero">Número</label>
            <input
              id="box-numero"
              v-model="form.numero"
              type="text"
              maxlength="20"
              placeholder="Ex.: 101"
              required
              :disabled="salvando"
            />
          </div>

          <div class="box-manter__campo">
            <label for="box-tamanho">Tamanho (m²)</label>
            <input
              id="box-tamanho"
              v-model="form.tamanho"
              type="number"
              min="0.01"
              max="999999.99"
              step="0.01"
              placeholder="Ex.: 6"
              required
              :disabled="salvando"
            />
          </div>

          <div class="box-manter__campo">
            <label for="box-dimensoes">Dimensões</label>
            <input
              id="box-dimensoes"
              v-model="form.dimensoes"
              type="text"
              maxlength="100"
              placeholder="Ex.: 2m x 3m x 2,5m"
              required
              :disabled="salvando"
            />
          </div>

          <div class="box-manter__campo">
            <label for="box-preco">Preço (R$)</label>
            <input
              id="box-preco"
              v-model="form.preco"
              type="number"
              min="0"
              max="99999999.99"
              step="0.01"
              placeholder="Ex.: 250,00"
              required
              :disabled="salvando"
            />
          </div>

          <label class="box-manter__check">
            <input v-model="form.disponivel" type="checkbox" :disabled="salvando" />
            Box liberado para locação (desmarque para bloqueá-lo)
          </label>

          <p v-if="erro" class="box-manter__erro">
            <AppIcon name="alert-circle" :size="15" />
            {{ erro }}
          </p>

          <div class="box-manter__acoes">
            <button type="button" class="btn" :disabled="salvando" @click="voltar">Cancelar</button>
            <button type="submit" class="btn btn--primary" :disabled="salvando || !unidade">
              <AppIcon v-if="salvando" name="loader" :size="15" class="box-manter__spinner" />
              {{ salvando ? 'Salvando...' : emEdicao ? 'Salvar alterações' : 'Cadastrar box' }}
            </button>
          </div>
        </form>
      </template>
    </div>
  </div>
</template>

<style scoped>
.box-manter {
  padding: 22px 28px 40px;
  display: flex;
  flex-direction: column;
  min-height: calc(100svh - 73px);
}

.box-manter__voltar {
  align-self: flex-start;
  margin-bottom: 18px;
}

.box-manter__card {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 8px clamp(0px, 5vw, 24px) 0;
}

.box-manter__header {
  padding-bottom: 24px;
  margin-bottom: 32px;
  border-bottom: 1px solid var(--border-hairline);
}

.box-manter__header h2 {
  font-size: 20px;
  font-weight: 700;
}

.box-manter__header p {
  margin-top: 6px;
  font-size: 13.5px;
  color: var(--text-muted);
}

.box-manter__state {
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

.box-manter__spinner {
  animation: box-manter-spin 0.8s linear infinite;
  color: var(--brand-500);
}

@keyframes box-manter-spin {
  to {
    transform: rotate(360deg);
  }
}

.box-manter__form {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  align-content: start;
  gap: 24px 28px;
}

.box-manter__campo {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

@media (max-width: 760px) {
  .box-manter__form {
    grid-template-columns: 1fr;
  }
}

.box-manter__campo label {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
}

.box-manter__campo input,
.box-manter__campo select {
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

.box-manter__campo input::placeholder {
  color: var(--text-muted);
}

.box-manter__campo input:focus,
.box-manter__campo select:focus {
  box-shadow: 0 0 0 2px var(--brand-100);
}

.box-manter__ajuda {
  font-size: 12.5px;
  color: var(--text-muted);
}

.box-manter__erro {
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

.box-manter__acoes {
  grid-column: 1 / -1;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
  margin-top: auto;
  padding-top: 24px;
  border-top: 1px solid var(--border-hairline);
}

.box-manter__check {
  grid-column: 1 / -1;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13.5px;
  color: var(--text-secondary);
  cursor: pointer;
}

.box-manter__check input {
  width: 16px;
  height: 16px;
  accent-color: var(--brand-500);
  cursor: pointer;
  flex-shrink: 0;
}

.box-manter__check input:disabled {
  cursor: default;
}
</style>
