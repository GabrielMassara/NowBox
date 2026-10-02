<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppIcon from '../../components/AppIcon.vue'
import ConfirmacaoModal from '../../components/ConfirmacaoModal.vue'
import { ApiError } from '../../lib/http'
import { aluguelService } from '../../services/aluguel.service'
import { contratoService } from '../../services/contrato.service'
import type { AluguelResponseDTO, AluguelStatus } from '../../types/api'

const props = defineProps<{ tipo: 'contrato' | 'aditivo' | 'distrato' }>()

const INTERVALO_POLLING_MS = 3000

const route = useRoute()
const router = useRouter()

const idAluguel = route.params.id as string

const ROTULO = computed(() => props.tipo)
const STATUS_ESPERADO = computed<AluguelStatus>(
  () => `PENDENTE_ASSINATURA_${props.tipo.toUpperCase()}` as AluguelStatus,
)

const aluguel = ref<AluguelResponseDTO | null>(null)
const carregando = ref(true)
const baixando = ref(false)
const enviando = ref(false)
const erro = ref('')
const confirmandoCancelamento = ref(false)
const cancelando = ref(false)
const erroCancelamento = ref('')
const arquivo = ref<File | null>(null)
const inputArquivo = ref<HTMLInputElement | null>(null)

let temporizador: ReturnType<typeof setTimeout> | undefined
let desmontado = false

const pronto = computed(() => {
  if (props.tipo === 'contrato') return aluguel.value?.contratoGerado ?? false
  if (props.tipo === 'distrato') return aluguel.value?.distratoGerado ?? false
  return !!aluguel.value?.idAditivoPendente
})
const aguardandoAssinatura = computed(() => aluguel.value?.status === STATUS_ESPERADO.value)

async function consultar() {
  try {
    aluguel.value = await aluguelService.buscarPorId(idAluguel)
    erro.value = ''
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível consultar o aluguel.'
  } finally {
    carregando.value = false
  }

  if (!desmontado && (erro.value || (aguardandoAssinatura.value && !pronto.value))) {
    temporizador = setTimeout(consultar, INTERVALO_POLLING_MS)
  }
}

async function baixar() {
  baixando.value = true
  erro.value = ''

  try {
    if (props.tipo === 'contrato') await contratoService.baixarContrato(idAluguel)
    else if (props.tipo === 'aditivo') await contratoService.baixarAditivo(aluguel.value!.idAditivoPendente!)
    else await contratoService.baixarDistrato(idAluguel)
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : `Não foi possível baixar o ${ROTULO.value}.`
  } finally {
    baixando.value = false
  }
}

function aoEscolherArquivo(evento: Event) {
  const escolhido = (evento.target as HTMLInputElement).files?.[0] ?? null
  erro.value = ''

  if (escolhido && escolhido.type !== 'application/pdf' && !escolhido.name.toLowerCase().endsWith('.pdf')) {
    arquivo.value = null
    if (inputArquivo.value) inputArquivo.value.value = ''
    erro.value = 'O arquivo assinado precisa ser um PDF.'
    return
  }

  arquivo.value = escolhido
}

async function enviar() {
  if (!arquivo.value || enviando.value) return

  enviando.value = true
  erro.value = ''

  try {
    if (props.tipo === 'contrato') await contratoService.enviarContratoAssinado(idAluguel, arquivo.value)
    else if (props.tipo === 'aditivo') await contratoService.enviarAditivoAssinado(idAluguel, arquivo.value)
    else await contratoService.enviarDistratoAssinado(idAluguel, arquivo.value)
    voltar()
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : `Não foi possível enviar o ${ROTULO.value} assinado.`
  } finally {
    enviando.value = false
  }
}

const EFEITO_CANCELAMENTO = computed(() => {
  if (props.tipo === 'contrato') return ['O contrato não será assinado e o aluguel ficará inativo.', 'O box será liberado para um novo aluguel.']
  if (props.tipo === 'aditivo') return ['As alterações feitas no aluguel serão desfeitas e os dados originais voltam.', 'O aluguel continua ativo com o contrato original.']
  return ['O encerramento será desfeito e o aluguel continua ativo.']
})

async function cancelarPendencia() {
  cancelando.value = true
  erroCancelamento.value = ''

  try {
    await aluguelService.cancelarPendencia(idAluguel)
    voltar()
  } catch (e) {
    erroCancelamento.value = e instanceof ApiError ? e.message : 'Não foi possível cancelar a pendência.'
  } finally {
    cancelando.value = false
  }
}

function voltar() {
  router.push('/alugueis')
}

onMounted(consultar)

onBeforeUnmount(() => {
  desmontado = true
  clearTimeout(temporizador)
})
</script>

<template>
  <div class="assinatura">
    <button type="button" class="btn assinatura__voltar" @click="voltar">
      <AppIcon name="arrow-left" :size="15" />
      Voltar para aluguéis
    </button>

    <div class="assinatura__card">
      <div v-if="carregando" class="assinatura__estado">
        <AppIcon name="loader" :size="20" class="assinatura__spinner" />
        <span>Carregando...</span>
      </div>

      <div v-else-if="!aluguel" class="assinatura__estado assinatura__estado--erro">
        <AppIcon name="alert-circle" :size="20" />
        <span>{{ erro }}</span>
      </div>

      <div v-else-if="!aguardandoAssinatura" class="assinatura__estado">
        <AppIcon name="check-circle" :size="20" />
        <span>Este aluguel não está aguardando a assinatura do {{ ROTULO }}.</span>
        <button type="button" class="btn" @click="voltar">Voltar para aluguéis</button>
      </div>

      <template v-else>
        <div class="assinatura__header">
          <h2>Assinatura do {{ ROTULO }}</h2>
          <p>
            Box {{ aluguel.box?.numero }} — {{ aluguel.cliente?.nome }}.
            {{
              tipo === 'contrato'
                ? 'O aluguel só fica ativo depois que o contrato assinado for enviado.'
                : tipo === 'aditivo'
                  ? 'O aluguel só volta a ficar ativo depois que o aditivo assinado for enviado.'
                  : 'O aluguel só fica inativo depois que o distrato assinado por ambas as partes for enviado.'
            }}
          </p>
        </div>

        <ol class="assinatura__etapas">
          <li class="assinatura__etapa">
            <div class="assinatura__etapa-titulo">
              <span class="assinatura__numero">1</span>
              <h3>Baixe o {{ ROTULO }}</h3>
            </div>

            <div v-if="!pronto" class="assinatura__aguardando">
              <AppIcon name="loader" :size="16" class="assinatura__spinner" />
              <span>Gerando o {{ ROTULO }}... Isso leva alguns segundos e a tela é atualizada sozinha.</span>
            </div>

            <div v-else class="assinatura__acoes-etapa">
              <button type="button" class="btn" :disabled="baixando" @click="baixar">
                <AppIcon :name="baixando ? 'loader' : 'download'" :size="15" :class="{ assinatura__spinner: baixando }" />
                Baixar {{ ROTULO }}
              </button>
              <span class="assinatura__ajuda">Imprima, colete as assinaturas e digitalize em PDF.</span>
            </div>
          </li>

          <li class="assinatura__etapa" :class="{ 'assinatura__etapa--desabilitada': !pronto }">
            <div class="assinatura__etapa-titulo">
              <span class="assinatura__numero">2</span>
              <h3>Envie o {{ ROTULO }} assinado</h3>
            </div>

            <form class="assinatura__upload" @submit.prevent="enviar">
              <input
                ref="inputArquivo"
                type="file"
                accept="application/pdf,.pdf"
                :aria-label="`Arquivo do ${ROTULO} assinado`"
                :disabled="!pronto || enviando"
                @change="aoEscolherArquivo"
              />

              <button type="submit" class="btn btn--primary" :disabled="!pronto || !arquivo || enviando">
                <AppIcon v-if="enviando" name="loader" :size="15" class="assinatura__spinner" />
                {{ enviando ? 'Enviando...' : 'Enviar arquivo' }}
              </button>
            </form>
          </li>
        </ol>

        <p v-if="erro" class="assinatura__erro">
          <AppIcon name="alert-circle" :size="15" />
          {{ erro }}
        </p>

        <div class="assinatura__desistir">
          <span>Não vai assinar?</span>
          <button type="button" class="btn" :disabled="enviando" @click="confirmandoCancelamento = true">
            Desistir do {{ ROTULO }}
          </button>
        </div>
      </template>
    </div>

    <ConfirmacaoModal
      v-if="confirmandoCancelamento"
      :titulo="`Desistir do ${ROTULO}`"
      :mensagem="`Você está cancelando a assinatura do ${ROTULO} do box ${aluguel?.box?.numero}.`"
      :avisos="EFEITO_CANCELAMENTO"
      texto-confirmar="Desistir"
      :processando="cancelando"
      :erro="erroCancelamento"
      @confirmar="cancelarPendencia"
      @cancelar="confirmandoCancelamento = false"
    />
  </div>
</template>

<style scoped>
.assinatura {
  padding: 22px 28px 40px;
  display: flex;
  flex-direction: column;
  min-height: calc(100svh - 73px);
}

.assinatura__voltar {
  align-self: flex-start;
  margin-bottom: 18px;
}

.assinatura__card {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 8px clamp(0px, 5vw, 24px) 0;
}

.assinatura__header {
  padding-bottom: 24px;
  margin-bottom: 28px;
  border-bottom: 1px solid var(--border-hairline);
}

.assinatura__header h2 {
  font-size: 20px;
  font-weight: 700;
}

.assinatura__header p {
  margin-top: 6px;
  font-size: 13.5px;
  color: var(--text-muted);
}

.assinatura__estado {
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

.assinatura__estado--erro {
  color: var(--text-critical);
}

.assinatura__spinner {
  animation: assinatura-spin 0.8s linear infinite;
  color: var(--brand-500);
}

@keyframes assinatura-spin {
  to {
    transform: rotate(360deg);
  }
}

.assinatura__etapas {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 28px;
  max-width: 640px;
}

.assinatura__etapa {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.assinatura__etapa--desabilitada {
  opacity: 0.55;
}

.assinatura__etapa-titulo {
  display: flex;
  align-items: center;
  gap: 10px;
}

.assinatura__etapa-titulo h3 {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
}

.assinatura__numero {
  width: 24px;
  height: 24px;
  display: grid;
  place-items: center;
  border-radius: 999px;
  background: var(--brand-500);
  color: var(--brand-contrast);
  font-size: 12.5px;
  font-weight: 700;
}

.assinatura__aguardando,
.assinatura__acoes-etapa {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  padding-left: 34px;
  font-size: 13.5px;
  color: var(--text-muted);
}

.assinatura__ajuda {
  font-size: 12.5px;
  color: var(--text-muted);
}

.assinatura__upload {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  padding-left: 34px;
}

.assinatura__upload input[type='file'] {
  max-width: 100%;
  font: inherit;
  font-size: 13.5px;
  color: var(--text-secondary);
}

.assinatura__desistir {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  max-width: 640px;
  margin-top: 32px;
  padding-top: 20px;
  border-top: 1px solid var(--border-hairline);
  font-size: 13.5px;
  color: var(--text-muted);
}

.assinatura__erro {
  display: flex;
  align-items: center;
  gap: 7px;
  max-width: 640px;
  margin-top: 24px;
  font-size: 13px;
  color: var(--text-critical);
  background: color-mix(in srgb, var(--status-critical) 10%, transparent);
  border-radius: var(--radius-sm);
  padding: 9px 11px;
}

@media (max-width: 760px) {
  .assinatura {
    padding: 18px 16px 32px;
  }

  .assinatura__aguardando,
  .assinatura__acoes-etapa,
  .assinatura__upload {
    padding-left: 0;
  }
}
</style>
