<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppIcon from '../../components/AppIcon.vue'
import { ApiError } from '../../lib/http'
import { carregarTodas } from '../../lib/paginacao'
import { aluguelService } from '../../services/aluguel.service'
import { boxService } from '../../services/box.service'
import { clienteService } from '../../services/cliente.service'
import { unidadeStore } from '../../stores/unidade'
import type { BoxResponseDTO, ClienteResponseDTO } from '../../types/api'

const route = useRoute()
const router = useRouter()

const idAluguel = computed(() => route.params.id as string | undefined)
const emEdicao = computed(() => !!idAluguel.value)

const form = reactive({ idBox: '', idCliente: '', valor: '', observacao: '', status: true })
const boxes = ref<BoxResponseDTO[]>([])
const clientes = ref<ClienteResponseDTO[]>([])

const carregando = ref(false)
const salvando = ref(false)
const erro = ref('')

const moeda = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })

// Sugere o preço do box como valor do aluguel, sem sobrescrever um valor digitado pela pessoa.
let valorSugerido = ''

watch(
  () => form.idBox,
  (idBox) => {
    if (emEdicao.value) return

    const box = boxes.value.find((b) => b.id === idBox)
    if (!box || (form.valor !== '' && form.valor !== valorSugerido)) return

    valorSugerido = String(box.preco)
    form.valor = valorSugerido
  },
)

async function carregarOpcoes() {
  const unidade = unidadeStore.state.selecionada

  // Só entram boxes liberados para locação e sem aluguel ativo; na edição o box atual é incluído depois.
  ;[boxes.value, clientes.value] = await Promise.all([
    unidade
      ? carregarTodas((p, t) => boxService.listar(p, t, { idUnidade: unidade.id, disponivel: true, alugado: false }))
      : Promise.resolve([]),
    carregarTodas((p, t) => clienteService.listar(p, t)),
  ])
}

async function carregarAluguel(id: string) {
  const aluguel = await aluguelService.buscarPorId(id)

  // O box e o cliente em edição podem não estar nas listas, então são incluídos a partir do aluguel.
  if (!boxes.value.some((b) => b.id === aluguel.box.id)) {
    boxes.value = [...boxes.value, aluguel.box]
  }
  if (!clientes.value.some((c) => c.id === aluguel.cliente.id)) {
    clientes.value = [...clientes.value, aluguel.cliente]
  }

  form.idBox = aluguel.box.id
  form.idCliente = aluguel.cliente.id
  form.valor = String(aluguel.valor)
  form.observacao = aluguel.observacao ?? ''
  form.status = aluguel.status
}

onMounted(async () => {
  carregando.value = true
  erro.value = ''

  try {
    await carregarOpcoes()
    if (idAluguel.value) await carregarAluguel(idAluguel.value)
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível carregar os dados da tela.'
  } finally {
    carregando.value = false
  }
})

function voltar() {
  router.push('/alugueis')
}

async function onSubmit() {
  if (salvando.value) return

  salvando.value = true
  erro.value = ''

  const dados = {
    idBox: form.idBox,
    idCliente: form.idCliente,
    valor: Number(form.valor),
    observacao: form.observacao.trim() || undefined,
    status: form.status,
  }

  try {
    if (idAluguel.value) {
      await aluguelService.atualizar(idAluguel.value, dados)
    } else {
      await aluguelService.criar(dados)
    }
    voltar()
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível salvar o aluguel.'
  } finally {
    salvando.value = false
  }
}
</script>

<template>
  <div class="aluguel-manter">
    <button type="button" class="btn aluguel-manter__voltar" @click="voltar">
      <AppIcon name="arrow-left" :size="15" />
      Voltar para aluguéis
    </button>

    <div class="aluguel-manter__card">
      <div v-if="carregando" class="aluguel-manter__state">
        <AppIcon name="loader" :size="20" class="aluguel-manter__spinner" />
        <span>Carregando aluguel...</span>
      </div>

      <template v-else>
        <div class="aluguel-manter__header">
          <h2>{{ emEdicao ? 'Editar aluguel' : 'Novo aluguel' }}</h2>
          <p>Preencha os dados abaixo para {{ emEdicao ? 'atualizar o' : 'registrar um novo' }} aluguel.</p>
        </div>

        <form class="aluguel-manter__form" @submit.prevent="onSubmit">
          <div class="aluguel-manter__campo">
            <label for="aluguel-box">Box</label>
            <select id="aluguel-box" v-model="form.idBox" required :disabled="salvando">
              <option value="" disabled>Selecione um box</option>
              <option v-for="box in boxes" :key="box.id" :value="box.id">
                {{ box.numero }} — {{ box.dimensoes }} ({{ moeda.format(box.preco) }})
              </option>
            </select>
            <span v-if="!emEdicao" class="aluguel-manter__ajuda">Apenas boxes liberados para locação e sem aluguel ativo.</span>
          </div>

          <div class="aluguel-manter__campo">
            <label for="aluguel-cliente">Cliente</label>
            <select id="aluguel-cliente" v-model="form.idCliente" required :disabled="salvando">
              <option value="" disabled>Selecione um cliente</option>
              <option v-for="cliente in clientes" :key="cliente.id" :value="cliente.id">
                {{ cliente.nome }}
              </option>
            </select>
          </div>

          <div class="aluguel-manter__campo">
            <label for="aluguel-valor">Valor (R$)</label>
            <input
              id="aluguel-valor"
              v-model="form.valor"
              type="number"
              min="0"
              max="99999999.99"
              step="0.01"
              placeholder="Ex.: 250,00"
              required
              :disabled="salvando"
            />
          </div>

          <div class="aluguel-manter__campo aluguel-manter__campo--cheio">
            <label for="aluguel-observacao">Observação</label>
            <textarea
              id="aluguel-observacao"
              v-model="form.observacao"
              rows="4"
              placeholder="Anotações sobre o aluguel (opcional)"
              :disabled="salvando"
            />
          </div>

          <label class="aluguel-manter__check">
            <input v-model="form.status" type="checkbox" :disabled="salvando" />
            Aluguel ativo (desmarque para marcá-lo como inativo)
          </label>

          <p v-if="erro" class="aluguel-manter__erro">
            <AppIcon name="alert-circle" :size="15" />
            {{ erro }}
          </p>

          <div class="aluguel-manter__acoes">
            <button type="button" class="btn" :disabled="salvando" @click="voltar">Cancelar</button>
            <button type="submit" class="btn btn--primary" :disabled="salvando">
              <AppIcon v-if="salvando" name="loader" :size="15" class="aluguel-manter__spinner" />
              {{ salvando ? 'Salvando...' : emEdicao ? 'Salvar alterações' : 'Cadastrar aluguel' }}
            </button>
          </div>
        </form>
      </template>
    </div>
  </div>
</template>

<style scoped>
.aluguel-manter {
  padding: 22px 28px 40px;
  display: flex;
  flex-direction: column;
  min-height: calc(100svh - 73px);
}

.aluguel-manter__voltar {
  align-self: flex-start;
  margin-bottom: 18px;
}

.aluguel-manter__card {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 8px clamp(0px, 5vw, 24px) 0;
}

.aluguel-manter__header {
  padding-bottom: 24px;
  margin-bottom: 32px;
  border-bottom: 1px solid var(--border-hairline);
}

.aluguel-manter__header h2 {
  font-size: 20px;
  font-weight: 700;
}

.aluguel-manter__header p {
  margin-top: 6px;
  font-size: 13.5px;
  color: var(--text-muted);
}

.aluguel-manter__state {
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

.aluguel-manter__spinner {
  animation: aluguel-manter-spin 0.8s linear infinite;
  color: var(--brand-500);
}

@keyframes aluguel-manter-spin {
  to {
    transform: rotate(360deg);
  }
}

.aluguel-manter__form {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  align-content: start;
  gap: 24px 28px;
}

.aluguel-manter__campo {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

@media (max-width: 760px) {
  .aluguel-manter__form {
    grid-template-columns: 1fr;
  }
}

.aluguel-manter__campo label {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
}

.aluguel-manter__campo input,
.aluguel-manter__campo select,
.aluguel-manter__campo textarea {
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

.aluguel-manter__campo input::placeholder,
.aluguel-manter__campo textarea::placeholder {
  color: var(--text-muted);
}

.aluguel-manter__campo input:focus,
.aluguel-manter__campo select:focus,
.aluguel-manter__campo textarea:focus {
  box-shadow: 0 0 0 2px var(--brand-100);
}

.aluguel-manter__campo textarea {
  resize: vertical;
}

.aluguel-manter__campo--cheio {
  grid-column: 1 / -1;
}

.aluguel-manter__ajuda {
  font-size: 12.5px;
  color: var(--text-muted);
}

.aluguel-manter__erro {
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

.aluguel-manter__acoes {
  grid-column: 1 / -1;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
  margin-top: auto;
  padding-top: 24px;
  border-top: 1px solid var(--border-hairline);
}

.aluguel-manter__check {
  grid-column: 1 / -1;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13.5px;
  color: var(--text-secondary);
  cursor: pointer;
}

.aluguel-manter__check input {
  width: 16px;
  height: 16px;
  accent-color: var(--brand-500);
  cursor: pointer;
  flex-shrink: 0;
}

.aluguel-manter__check input:disabled {
  cursor: default;
}
</style>
