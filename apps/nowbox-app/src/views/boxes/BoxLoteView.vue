<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import AppIcon from '../../components/AppIcon.vue'
import { ApiError } from '../../lib/http'
import { carregarTodas } from '../../lib/paginacao'
import { boxService } from '../../services/box.service'
import { unidadeStore } from '../../stores/unidade'

const LIMITE_LOTE = 200
const TAMANHO_NUMERO = 20

const router = useRouter()

const unidade = unidadeStore.state.selecionada

const form = reactive({
  prefixo: '',
  inicio: '',
  fim: '',
  zeros: false,
  tamanho: '',
  dimensoes: '',
  preco: '',
  disponivel: true,
})

// Números já cadastrados na unidade, para não criar boxes duplicados.
const existentes = ref<Set<string>>(new Set())

const carregando = ref(false)
const erroCarga = ref('')
const salvando = ref(false)
const erro = ref('')

function normalizar(numero: string) {
  return numero.trim().toLowerCase()
}

const inicio = computed(() => (form.inicio === '' ? NaN : Number(form.inicio)))
const fim = computed(() => (form.fim === '' ? NaN : Number(form.fim)))

const intervaloValido = computed(
  () => Number.isInteger(inicio.value) && Number.isInteger(fim.value) && inicio.value >= 0 && fim.value >= inicio.value,
)

const numeros = computed(() => {
  if (!intervaloValido.value) return []

  const total = fim.value - inicio.value + 1
  if (total > LIMITE_LOTE) return []

  const largura = form.zeros ? String(fim.value).length : 0
  const prefixo = form.prefixo.trim()

  return Array.from({ length: total }, (_, i) => `${prefixo}${String(inicio.value + i).padStart(largura, '0')}`)
})

const jaCadastrados = computed(() => numeros.value.filter((numero) => existentes.value.has(normalizar(numero))))
const novos = computed(() => numeros.value.filter((numero) => !existentes.value.has(normalizar(numero))))

const problema = computed(() => {
  if (form.inicio === '' || form.fim === '') return ''
  if (!intervaloValido.value) return 'Informe números inteiros, com o número final maior ou igual ao inicial.'
  if (fim.value - inicio.value + 1 > LIMITE_LOTE) return `O lote aceita no máximo ${LIMITE_LOTE} boxes por vez.`
  if (numeros.value.some((numero) => numero.length > TAMANHO_NUMERO)) {
    return `O número do box pode ter no máximo ${TAMANHO_NUMERO} caracteres.`
  }
  return ''
})

function resumir(lista: string[]) {
  return lista.length > 4 ? `${lista.slice(0, 2).join(', ')} … ${lista.slice(-2).join(', ')}` : lista.join(', ')
}

async function carregarExistentes() {
  if (!unidade) return

  carregando.value = true
  erroCarga.value = ''

  try {
    const boxes = await carregarTodas((pagina, tamanho) =>
      boxService.listar(pagina, tamanho, { idUnidade: unidade.id }),
    )
    existentes.value = new Set(boxes.map((box) => normalizar(box.numero)))
  } catch (e) {
    erroCarga.value = e instanceof ApiError ? e.message : 'Não foi possível verificar os boxes já cadastrados.'
  } finally {
    carregando.value = false
  }
}

onMounted(carregarExistentes)

function voltar() {
  router.push('/boxes')
}

async function onSubmit() {
  if (salvando.value || !unidade || problema.value || novos.value.length === 0) return

  salvando.value = true
  erro.value = ''

  try {
    await boxService.criarEmLote({
      idUnidade: unidade.id,
      prefixo: form.prefixo.trim() || undefined,
      numeroInicial: inicio.value,
      numeroFinal: fim.value,
      completarComZeros: form.zeros,
      tamanho: Number(form.tamanho),
      dimensoes: form.dimensoes.trim(),
      disponivel: form.disponivel,
      preco: Number(form.preco),
    })
    voltar()
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível cadastrar os boxes.'
  } finally {
    salvando.value = false
  }
}
</script>

<template>
  <div class="box-lote">
    <button type="button" class="btn box-lote__voltar" @click="voltar">
      <AppIcon name="arrow-left" :size="15" />
      Voltar para boxes
    </button>

    <div class="box-lote__card">
      <div v-if="carregando" class="box-lote__state">
        <AppIcon name="loader" :size="20" class="box-lote__spinner" />
        <span>Verificando os boxes já cadastrados...</span>
      </div>

      <div v-else-if="erroCarga" class="box-lote__state box-lote__state--error">
        <AppIcon name="alert-circle" :size="20" />
        <span>{{ erroCarga }}</span>
        <button type="button" class="btn" @click="carregarExistentes">Tentar novamente</button>
      </div>

      <template v-else>
        <div class="box-lote__header">
          <h2>Cadastro em lote</h2>
          <p>Informe um intervalo de números e os dados em comum para cadastrar vários boxes de uma vez.</p>
        </div>

        <form class="box-lote__form" @submit.prevent="onSubmit">
          <div class="box-lote__campo">
            <label for="lote-unidade">Unidade</label>
            <input id="lote-unidade" type="text" :value="unidade?.nome ?? '—'" disabled />
          </div>

          <div class="box-lote__campo">
            <label for="lote-prefixo">Prefixo</label>
            <input
              id="lote-prefixo"
              v-model="form.prefixo"
              type="text"
              maxlength="10"
              placeholder="Ex.: BOX- (opcional)"
              :disabled="salvando"
            />
            <span class="box-lote__ajuda">Texto que antecede o número, como em "BOX-1".</span>
          </div>

          <div class="box-lote__campo">
            <label for="lote-inicio">Número inicial</label>
            <input
              id="lote-inicio"
              v-model="form.inicio"
              type="number"
              min="0"
              step="1"
              placeholder="Ex.: 1"
              required
              :disabled="salvando"
            />
          </div>

          <div class="box-lote__campo">
            <label for="lote-fim">Número final</label>
            <input
              id="lote-fim"
              v-model="form.fim"
              type="number"
              min="0"
              step="1"
              placeholder="Ex.: 12"
              required
              :disabled="salvando"
            />
          </div>

          <label class="box-lote__check">
            <input v-model="form.zeros" type="checkbox" :disabled="salvando" />
            Completar com zeros à esquerda (01, 02, ... 12)
          </label>

          <div class="box-lote__resumo" :class="{ 'box-lote__resumo--erro': problema }" aria-live="polite">
            <template v-if="problema">
              <AppIcon name="alert-circle" :size="15" />
              <span>{{ problema }}</span>
            </template>
            <template v-else-if="numeros.length === 0">
              <AppIcon name="box" :size="15" />
              <span>Informe o intervalo para ver quais boxes serão criados.</span>
            </template>
            <template v-else>
              <AppIcon name="box" :size="15" />
              <span>
                <strong>{{ novos.length }}</strong> box(es) serão criados
                <template v-if="novos.length > 0">({{ resumir(novos) }})</template>.
                <template v-if="jaCadastrados.length > 0">
                  {{ jaCadastrados.length }} já cadastrado(s) e será(ão) ignorado(s): {{ resumir(jaCadastrados) }}.
                </template>
              </span>
            </template>
          </div>

          <div class="box-lote__campo">
            <label for="lote-tamanho">Tamanho (m²)</label>
            <input
              id="lote-tamanho"
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

          <div class="box-lote__campo">
            <label for="lote-dimensoes">Dimensões</label>
            <input
              id="lote-dimensoes"
              v-model="form.dimensoes"
              type="text"
              maxlength="100"
              placeholder="Ex.: 2m x 3m x 2,5m"
              required
              :disabled="salvando"
            />
          </div>

          <div class="box-lote__campo">
            <label for="lote-preco">Preço (R$)</label>
            <input
              id="lote-preco"
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

          <label class="box-lote__check">
            <input v-model="form.disponivel" type="checkbox" :disabled="salvando" />
            Boxes liberados para locação (desmarque para bloqueá-los)
          </label>

          <p v-if="erro" class="box-lote__erro">
            <AppIcon name="alert-circle" :size="15" />
            {{ erro }}
          </p>

          <div class="box-lote__acoes">
            <button type="button" class="btn" :disabled="salvando" @click="voltar">Cancelar</button>
            <button
              type="submit"
              class="btn btn--primary"
              :disabled="salvando || !unidade || !!problema || novos.length === 0"
            >
              <AppIcon v-if="salvando" name="loader" :size="15" class="box-lote__spinner" />
              {{
                salvando ? 'Cadastrando...' : novos.length > 0 ? `Cadastrar ${novos.length} box(es)` : 'Cadastrar boxes'
              }}
            </button>
          </div>
        </form>
      </template>
    </div>
  </div>
</template>

<style scoped>
.box-lote {
  padding: 22px 28px 40px;
  display: flex;
  flex-direction: column;
  min-height: calc(100svh - 73px);
}

.box-lote__voltar {
  align-self: flex-start;
  margin-bottom: 18px;
}

.box-lote__card {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 8px clamp(0px, 5vw, 24px) 0;
}

.box-lote__header {
  padding-bottom: 24px;
  margin-bottom: 32px;
  border-bottom: 1px solid var(--border-hairline);
}

.box-lote__header h2 {
  font-size: 20px;
  font-weight: 700;
}

.box-lote__header p {
  margin-top: 6px;
  font-size: 13.5px;
  color: var(--text-muted);
}

.box-lote__state {
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

.box-lote__spinner {
  animation: box-lote-spin 0.8s linear infinite;
  color: var(--brand-500);
}

@keyframes box-lote-spin {
  to {
    transform: rotate(360deg);
  }
}

.box-lote__form {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  align-content: start;
  gap: 24px 28px;
}

.box-lote__campo {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

@media (max-width: 760px) {
  .box-lote__form {
    grid-template-columns: 1fr;
  }
}

.box-lote__campo label {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
}

.box-lote__campo input,
.box-lote__campo select {
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

.box-lote__campo input::placeholder {
  color: var(--text-muted);
}

.box-lote__campo input:focus,
.box-lote__campo select:focus {
  box-shadow: 0 0 0 2px var(--brand-100);
}

.box-lote__ajuda {
  font-size: 12.5px;
  color: var(--text-muted);
}

.box-lote__erro {
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

.box-lote__acoes {
  grid-column: 1 / -1;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
  margin-top: auto;
  padding-top: 24px;
  border-top: 1px solid var(--border-hairline);
}

.box-lote__check {
  grid-column: 1 / -1;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13.5px;
  color: var(--text-secondary);
  cursor: pointer;
}

.box-lote__check input {
  width: 16px;
  height: 16px;
  accent-color: var(--brand-500);
  cursor: pointer;
  flex-shrink: 0;
}

.box-lote__check input:disabled {
  cursor: default;
}

.box-lote__resumo {
  grid-column: 1 / -1;
  display: flex;
  align-items: flex-start;
  gap: 8px;
  font-size: 13px;
  color: var(--text-secondary);
  background: var(--bg-surface-sunken);
  border-radius: var(--radius-sm);
  padding: 11px 13px;
}

.box-lote__resumo svg {
  flex-shrink: 0;
  margin-top: 2px;
}

.box-lote__resumo--erro {
  color: var(--text-critical);
  background: color-mix(in srgb, var(--status-critical) 10%, transparent);
}
</style>
