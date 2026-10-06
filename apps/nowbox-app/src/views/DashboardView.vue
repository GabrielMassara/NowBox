<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { RouterLink } from 'vue-router'
import AppIcon from '../components/AppIcon.vue'
import { ApiError } from '../lib/http'
import { dashboardService } from '../services/dashboard.service'
import { unidadeStore } from '../stores/unidade'
import type {
  AluguelStatus,
  DashboardAluguelStatusDTO,
  DashboardEvolucaoDTO,
  DashboardOcupacaoDTO,
  DashboardPendenciaDTO,
  DashboardResumoDTO,
} from '../types/api'

interface Bloco<T> {
  dados: T | null
  carregando: boolean
  erro: string
  negado: boolean
}

function criarBloco<T>(buscar: (idUnidade: string) => Promise<T>, mensagemErro: string) {
  const bloco = reactive({ dados: null, carregando: true, erro: '', negado: false }) as Bloco<T>

  async function carregar() {
    const unidade = unidadeStore.state.selecionada
    if (!unidade) return

    bloco.carregando = true
    bloco.erro = ''

    try {
      bloco.dados = await buscar(unidade.id)
      bloco.negado = false
    } catch (e) {
      if (e instanceof ApiError && e.status === 403) bloco.negado = true
      else bloco.erro = e instanceof ApiError ? e.message : mensagemErro
    } finally {
      bloco.carregando = false
    }
  }

  return { bloco, carregar }
}

const resumo = criarBloco<DashboardResumoDTO>(dashboardService.resumo, 'Não foi possível carregar o resumo.')
const ocupacao = criarBloco<DashboardOcupacaoDTO>(dashboardService.ocupacao, 'Não foi possível carregar a ocupação dos boxes.')
const situacoes = criarBloco<DashboardAluguelStatusDTO[]>(dashboardService.alugueisPorStatus, 'Não foi possível carregar os aluguéis por situação.')
const evolucao = criarBloco<DashboardEvolucaoDTO[]>(dashboardService.evolucaoAlugueis, 'Não foi possível carregar a evolução dos aluguéis.')
const pendencias = criarBloco<DashboardPendenciaDTO[]>(dashboardService.pendenciasAssinatura, 'Não foi possível carregar as pendências de assinatura.')

const blocos = [resumo, ocupacao, situacoes, evolucao, pendencias]

const semAcesso = computed(() => blocos.every(({ bloco }) => bloco.negado))

const resumoVisivel = computed(() => !resumo.bloco.negado && !resumo.bloco.erro)

const inteiro = new Intl.NumberFormat('pt-BR')
const decimal = new Intl.NumberFormat('pt-BR', { maximumFractionDigits: 1 })
const moeda = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })
const valorMonetario = new Intl.NumberFormat('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })


const FATIAS_OCUPACAO = [
  { chave: 'ocupados', rotulo: 'Ocupados', cor: 'var(--brand-500)' },
  { chave: 'livres', rotulo: 'Livres', cor: 'var(--azul-claro)' },
  { chave: 'bloqueados', rotulo: 'Bloqueados', cor: 'var(--azul-acinzentado)' },
] as const

const fatiasOcupacao = computed(() => {
  const dados = ocupacao.bloco.dados
  if (!dados) return []

  return FATIAS_OCUPACAO.map((fatia) => ({
    ...fatia,
    quantidade: dados[fatia.chave],
    percentual: dados.total === 0 ? 0 : (dados[fatia.chave] / dados.total) * 100,
  }))
})


const ROTULO_STATUS: Record<AluguelStatus, string> = {
  PENDENTE_ASSINATURA_CONTRATO: 'Contrato pendente',
  ATIVO: 'Ativos',
  PENDENTE_ASSINATURA_ADITIVO: 'Aditivo pendente',
  PENDENTE_ASSINATURA_DISTRATO: 'Distrato pendente',
  INATIVO: 'Inativos',
}

const maiorQuantidadeStatus = computed(() =>
  Math.max(1, ...(situacoes.bloco.dados ?? []).map((item) => item.quantidade)),
)

const totalAlugueis = computed(() => (situacoes.bloco.dados ?? []).reduce((soma, item) => soma + item.quantidade, 0))


// O gráfico é desenhado em pixels reais, no tamanho medido do cartão: ele ocupa o espaço
// disponível sem esticar o texto e os traços junto com a tela.
const G = { esquerda: 30, direita: 10, topo: 10, base: 24, folga: 12 }

const areaGrafico = ref<HTMLElement | null>(null)
const tamanhoGrafico = reactive({ largura: 600, altura: 220 })

const observador = new ResizeObserver(([entrada]) => {
  const { width, height } = entrada.contentRect
  if (width > 0 && height > 0) {
    tamanhoGrafico.largura = Math.round(width)
    tamanhoGrafico.altura = Math.round(height)
  }
})

watch(areaGrafico, (elemento) => {
  observador.disconnect()
  if (elemento) observador.observe(elemento)
})

onBeforeUnmount(() => observador.disconnect())

function topoDoEixo(maior: number) {
  if (maior <= 4) return 4
  const passo = 10 ** Math.floor(Math.log10(maior / 4))
  const base = [1, 2, 2.5, 5, 10].map((m) => m * passo).find((candidato) => candidato * 4 >= maior) ?? passo * 10
  return base * 4
}

const grafico = computed(() => {
  const dados = evolucao.bloco.dados ?? []
  const { largura, altura } = tamanhoGrafico
  const areaLargura = largura - G.esquerda - G.direita
  const areaAltura = altura - G.topo - G.base
  const yBase = G.topo + areaAltura
  const topo = topoDoEixo(Math.max(0, ...dados.map((m) => m.quantidade)))
  const yDe = (quantidade: number) => G.topo + areaAltura * (1 - quantidade / topo)

  const meses = dados.map((mes, i) => ({
    ...mes,
    x: dados.length > 1
      ? G.esquerda + G.folga + ((areaLargura - G.folga * 2) * i) / (dados.length - 1)
      : G.esquerda + areaLargura / 2,
    y: yDe(mes.quantidade),
    rotulo: new Date(mes.ano, mes.mes - 1, 1).toLocaleDateString('pt-BR', { month: 'short' }).replace('.', ''),
    rotuloCompleto: new Date(mes.ano, mes.mes - 1, 1).toLocaleDateString('pt-BR', { month: 'long', year: 'numeric' }),
  }))

  // Curva suave: as alças são horizontais, então ela passa por cada ponto sem ultrapassar o valor dele
  const linha = meses.reduce((caminho, ponto, i) => {
    if (i === 0) return `M ${ponto.x} ${ponto.y}`
    const anterior = meses[i - 1]
    const meio = (ponto.x - anterior.x) * 0.45
    return `${caminho} C ${anterior.x + meio} ${anterior.y}, ${ponto.x - meio} ${ponto.y}, ${ponto.x} ${ponto.y}`
  }, '')

  return {
    largura,
    altura,
    yBase,
    ticks: [0, 1, 2, 3, 4].map((i) => ({ valor: (topo / 4) * i, y: yDe((topo / 4) * i) })),
    meses,
    linha,
    area: meses.length ? `${linha} L ${meses[meses.length - 1].x} ${yBase} L ${meses[0].x} ${yBase} Z` : '',
    larguraBanda: areaLargura / Math.max(dados.length, 1),
  }
})

const semAlugueisNoPeriodo = computed(() => grafico.value.meses.every((mes) => mes.quantidade === 0))


const SITUACAO_PENDENCIA: Partial<Record<AluguelStatus, { rotulo: string; rota: string }>> = {
  PENDENTE_ASSINATURA_CONTRATO: { rotulo: 'Contrato', rota: 'assinatura-contrato' },
  PENDENTE_ASSINATURA_ADITIVO: { rotulo: 'Aditivo', rota: 'assinatura-aditivo' },
  PENDENTE_ASSINATURA_DISTRATO: { rotulo: 'Distrato', rota: 'assinatura-distrato' },
}

function diasAguardando(criadoEm: string) {
  const dias = Math.floor((Date.now() - new Date(criadoEm).getTime()) / 86_400_000)
  if (dias <= 0) return 'Hoje'
  return dias === 1 ? '1 dia' : `${dias} dias`
}


const dica = reactive({ visivel: false, x: 0, y: 0, titulo: '', valor: '', detalhe: '' })

function mostrarDica(evento: PointerEvent | FocusEvent, titulo: string, valor: string, detalhe = '') {
  const alvo = evento.currentTarget as Element
  const retangulo = alvo.getBoundingClientRect()
  const ponteiro = evento instanceof PointerEvent

  dica.x = ponteiro ? evento.clientX : retangulo.left + retangulo.width / 2
  dica.y = ponteiro ? evento.clientY : retangulo.top
  dica.titulo = titulo
  dica.valor = valor
  dica.detalhe = detalhe
  dica.visivel = true
}

function esconderDica() {
  dica.visivel = false
}

onMounted(() => blocos.forEach(({ carregar }) => carregar()))
</script>

<template>
  <div class="dashboard">
    <div v-if="semAcesso" class="dashboard__painel dashboard__estado">
      <AppIcon name="lock" :size="18" />
      <span>Seu cargo não possui acesso aos blocos do dashboard.</span>
    </div>

    <template v-else>
      <section v-if="!resumo.bloco.negado" class="dashboard__tiles" aria-label="Resumo da unidade">
        <div v-if="resumo.bloco.erro" class="dashboard__estado dashboard__estado--erro dashboard__tiles-erro">
          <AppIcon name="alert-circle" :size="18" />
          <span>{{ resumo.bloco.erro }}</span>
          <button type="button" class="btn" @click="resumo.carregar">Tentar novamente</button>
        </div>

        <template v-else>
          <div class="dashboard__tile">
            <span class="dashboard__tile-rotulo">Boxes cadastrados</span>
            <span v-if="resumo.bloco.carregando" class="skeleton dashboard__tile-skeleton"></span>
            <strong v-else class="dashboard__tile-valor">{{ inteiro.format(resumo.bloco.dados?.totalBoxes ?? 0) }}</strong>
          </div>

          <div class="dashboard__tile">
            <span class="dashboard__tile-rotulo">Taxa de ocupação</span>
            <span v-if="resumo.bloco.carregando" class="skeleton dashboard__tile-skeleton"></span>
            <strong v-else class="dashboard__tile-valor">{{ decimal.format(resumo.bloco.dados?.taxaOcupacao ?? 0) }}%</strong>
          </div>

          <div class="dashboard__tile">
            <span class="dashboard__tile-rotulo">Clientes ativos</span>
            <span v-if="resumo.bloco.carregando" class="skeleton dashboard__tile-skeleton"></span>
            <strong v-else class="dashboard__tile-valor">{{ inteiro.format(resumo.bloco.dados?.clientesAtivos ?? 0) }}</strong>
          </div>

          <div class="dashboard__tile">
            <span class="dashboard__tile-rotulo">Aluguéis vigentes</span>
            <span v-if="resumo.bloco.carregando" class="skeleton dashboard__tile-skeleton"></span>
            <strong v-else class="dashboard__tile-valor">{{ inteiro.format(resumo.bloco.dados?.alugueisVigentes ?? 0) }}</strong>
          </div>
        </template>
      </section>

      <div v-if="!evolucao.bloco.negado || resumoVisivel || !ocupacao.bloco.negado" class="dashboard__linha">
        <section v-if="!evolucao.bloco.negado" class="dashboard__painel dashboard__painel--grafico">
          <header class="dashboard__cabecalho">
            <div>
              <h2>Evolução dos aluguéis</h2>
              <p>Aluguéis registrados por mês nos últimos 6 meses</p>
            </div>
            <span class="dashboard__legenda">
              <span class="dashboard__marcador" style="background: var(--brand-500)"></span>
              Aluguéis
            </span>
          </header>

          <div v-if="evolucao.bloco.carregando" class="dashboard__corpo">
            <span class="skeleton dashboard__skeleton-grafico"></span>
          </div>

          <div v-else-if="evolucao.bloco.erro" class="dashboard__estado dashboard__estado--erro">
            <AppIcon name="alert-circle" :size="18" />
            <span>{{ evolucao.bloco.erro }}</span>
            <button type="button" class="btn" @click="evolucao.carregar">Tentar novamente</button>
          </div>

          <div v-else-if="semAlugueisNoPeriodo" class="dashboard__estado">
            <span>Nenhum aluguel registrado nos últimos 6 meses.</span>
          </div>

          <div v-else class="dashboard__corpo dashboard__corpo--grafico">
            <div ref="areaGrafico" class="evolucao__area">
              <svg
                class="evolucao"
                :width="grafico.largura"
                :height="grafico.altura"
                :viewBox="`0 0 ${grafico.largura} ${grafico.altura}`"
                role="group"
                aria-label="Aluguéis registrados por mês"
              >
                <defs>
                  <linearGradient id="evolucao-gradiente" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0%" class="evolucao__gradiente-topo" />
                    <stop offset="100%" class="evolucao__gradiente-base" />
                  </linearGradient>
                </defs>

                <g aria-hidden="true">
                  <line
                    v-for="tick in grafico.ticks"
                    :key="`grade-${tick.valor}`"
                    class="evolucao__grade"
                    :x1="G.esquerda"
                    :x2="grafico.largura - G.direita"
                    :y1="tick.y"
                    :y2="tick.y"
                  />
                  <text
                    v-for="tick in grafico.ticks"
                    :key="`eixo-${tick.valor}`"
                    class="evolucao__eixo"
                    :x="G.esquerda - 10"
                    :y="tick.y + 4"
                    text-anchor="end"
                  >{{ inteiro.format(tick.valor) }}</text>
                </g>

                <path :d="grafico.area" fill="url(#evolucao-gradiente)" />
                <path class="evolucao__linha" :d="grafico.linha" />

                <g
                  v-for="mes in grafico.meses"
                  :key="`${mes.ano}-${mes.mes}`"
                  class="evolucao__ponto"
                  tabindex="0"
                  role="img"
                  :aria-label="`${mes.rotuloCompleto}: ${mes.quantidade} aluguel(is)`"
                  @pointermove="mostrarDica($event, mes.rotuloCompleto, `${inteiro.format(mes.quantidade)} aluguel(is)`, `${moeda.format(mes.valor)} em contratos`)"
                  @pointerleave="esconderDica"
                  @focus="mostrarDica($event, mes.rotuloCompleto, `${inteiro.format(mes.quantidade)} aluguel(is)`, `${moeda.format(mes.valor)} em contratos`)"
                  @blur="esconderDica"
                >
                  <rect class="evolucao__zona" :x="mes.x - grafico.larguraBanda / 2" :y="0" :width="grafico.larguraBanda" :height="grafico.altura" />
                  <line class="evolucao__guia" :x1="mes.x" :x2="mes.x" :y1="mes.y" :y2="grafico.yBase" />
                  <circle class="evolucao__marca" :cx="mes.x" :cy="mes.y" r="3.5" />
                </g>

                <text
                  v-for="mes in grafico.meses"
                  :key="`rotulo-${mes.ano}-${mes.mes}`"
                  class="evolucao__eixo evolucao__mes"
                  :x="mes.x"
                  :y="grafico.altura - 5"
                  text-anchor="middle"
                  aria-hidden="true"
                >{{ mes.rotulo }}</text>
              </svg>
            </div>
          </div>
        </section>

        <div v-if="resumoVisivel || !ocupacao.bloco.negado" class="dashboard__lateral">
          <section v-if="resumoVisivel" class="dashboard__receita" aria-label="Receita mensal">
            <h2>Receita mensal</h2>
            <span class="dashboard__receita-moeda">R$</span>
            <span v-if="resumo.bloco.carregando" class="skeleton dashboard__receita-skeleton"></span>
            <strong v-else class="dashboard__receita-valor">{{ valorMonetario.format(resumo.bloco.dados?.receitaMensal ?? 0) }}</strong>
          </section>

          <section v-if="!ocupacao.bloco.negado" class="dashboard__painel">
            <header class="dashboard__cabecalho">
              <div>
                <h2>Ocupação dos boxes</h2>
              </div>
              <span v-if="ocupacao.bloco.dados?.total" class="dashboard__legenda">
                {{ inteiro.format(ocupacao.bloco.dados.total) }} box(es)
              </span>
            </header>

            <div v-if="ocupacao.bloco.carregando" class="dashboard__corpo">
              <span class="skeleton dashboard__skeleton-barra"></span>
              <span class="skeleton" v-for="n in 3" :key="n"></span>
            </div>

            <div v-else-if="ocupacao.bloco.erro" class="dashboard__estado dashboard__estado--erro">
              <AppIcon name="alert-circle" :size="18" />
              <span>{{ ocupacao.bloco.erro }}</span>
              <button type="button" class="btn" @click="ocupacao.carregar">Tentar novamente</button>
            </div>

            <div v-else-if="!ocupacao.bloco.dados?.total" class="dashboard__estado">
              <span>Nenhum box cadastrado na unidade.</span>
            </div>

            <div v-else class="dashboard__corpo">
              <div class="ocupacao__barra" role="img" :aria-label="`${ocupacao.bloco.dados?.total} boxes na unidade`">
                <span
                  v-for="fatia in fatiasOcupacao.filter((f) => f.quantidade > 0)"
                  :key="fatia.chave"
                  class="ocupacao__fatia"
                  :style="{ flexGrow: fatia.quantidade, background: fatia.cor }"
                  tabindex="0"
                  @pointermove="mostrarDica($event, fatia.rotulo, `${inteiro.format(fatia.quantidade)} box(es)`, `${decimal.format(fatia.percentual)}% da unidade`)"
                  @pointerleave="esconderDica"
                  @focus="mostrarDica($event, fatia.rotulo, `${inteiro.format(fatia.quantidade)} box(es)`, `${decimal.format(fatia.percentual)}% da unidade`)"
                  @blur="esconderDica"
                ></span>
              </div>

              <ul class="ocupacao__legenda">
                <li v-for="fatia in fatiasOcupacao" :key="fatia.chave">
                  <span class="dashboard__marcador" :style="{ background: fatia.cor }"></span>
                  <span class="ocupacao__rotulo">{{ fatia.rotulo }}</span>
                  <span class="ocupacao__percentual">{{ decimal.format(fatia.percentual) }}%</span>
                  <strong class="ocupacao__quantidade">{{ inteiro.format(fatia.quantidade) }}</strong>
                </li>
              </ul>
            </div>
          </section>
        </div>
      </div>

      <div v-if="!pendencias.bloco.negado || !situacoes.bloco.negado" class="dashboard__linha">
        <section v-if="!pendencias.bloco.negado" class="dashboard__painel">
          <header class="dashboard__cabecalho">
            <div>
              <h2>Pendências de assinatura</h2>
            </div>
            <span v-if="pendencias.bloco.dados?.length" class="dashboard__legenda">
              {{ inteiro.format(pendencias.bloco.dados.length) }} aguardando assinatura.
            </span>
          </header>

          <div v-if="pendencias.bloco.carregando" class="dashboard__corpo">
            <span class="skeleton" v-for="n in 4" :key="n"></span>
          </div>

          <div v-else-if="pendencias.bloco.erro" class="dashboard__estado dashboard__estado--erro">
            <AppIcon name="alert-circle" :size="18" />
            <span>{{ pendencias.bloco.erro }}</span>
            <button type="button" class="btn" @click="pendencias.carregar">Tentar novamente</button>
          </div>

          <div v-else-if="!pendencias.bloco.dados?.length" class="dashboard__estado">
            <span>Nenhuma assinatura pendente.</span>
          </div>

          <ul v-else class="atividade">
            <li v-for="pendencia in pendencias.bloco.dados" :key="pendencia.idAluguel" class="atividade__item">
              <p class="atividade__texto">
                <span class="atividade__nome">{{ pendencia.nomeCliente }}</span>
                aguarda assinatura de {{ SITUACAO_PENDENCIA[pendencia.status]?.rotulo.toLowerCase() }}
                <span class="atividade__detalhe">· Box {{ pendencia.numeroBox }} · {{ moeda.format(pendencia.valor) }}</span>
              </p>
              <span class="atividade__tempo">{{ diasAguardando(pendencia.createdAt) }}</span>
              <RouterLink
                v-if="SITUACAO_PENDENCIA[pendencia.status]"
                class="atividade__acao"
                :to="`/alugueis/${pendencia.idAluguel}/${SITUACAO_PENDENCIA[pendencia.status]?.rota}`"
              >
                Assinar
              </RouterLink>
            </li>
          </ul>
        </section>

        <section v-if="!situacoes.bloco.negado" class="dashboard__painel">
          <header class="dashboard__cabecalho">
            <div>
              <h2>Aluguéis por situação</h2>
            </div>
            <span class="dashboard__legenda">{{ inteiro.format(totalAlugueis) }} aluguel(is)</span>
          </header>

          <div v-if="situacoes.bloco.carregando" class="dashboard__corpo">
            <span class="skeleton" v-for="n in 5" :key="n"></span>
          </div>

          <div v-else-if="situacoes.bloco.erro" class="dashboard__estado dashboard__estado--erro">
            <AppIcon name="alert-circle" :size="18" />
            <span>{{ situacoes.bloco.erro }}</span>
            <button type="button" class="btn" @click="situacoes.carregar">Tentar novamente</button>
          </div>

          <ul v-else class="dashboard__corpo situacoes">
            <li v-for="item in situacoes.bloco.dados" :key="item.status" class="situacoes__linha">
              <span class="situacoes__rotulo">{{ ROTULO_STATUS[item.status] }}</span>
              <strong class="situacoes__quantidade">{{ inteiro.format(item.quantidade) }}</strong>
              <span class="situacoes__trilho">
                <span
                  v-if="item.quantidade > 0"
                  class="situacoes__barra"
                  :style="{ width: `${(item.quantidade / maiorQuantidadeStatus) * 100}%` }"
                  tabindex="0"
                  @pointermove="mostrarDica($event, ROTULO_STATUS[item.status], `${inteiro.format(item.quantidade)} aluguel(is)`)"
                  @pointerleave="esconderDica"
                  @focus="mostrarDica($event, ROTULO_STATUS[item.status], `${inteiro.format(item.quantidade)} aluguel(is)`)"
                  @blur="esconderDica"
                ></span>
              </span>
            </li>
          </ul>
        </section>
      </div>
    </template>

    <div
      v-if="dica.visivel"
      class="dashboard__dica"
      :style="{ left: `${dica.x}px`, top: `${dica.y}px` }"
      role="tooltip"
    >
      <strong>{{ dica.valor }}</strong>
      <span>{{ dica.titulo }}</span>
      <span v-if="dica.detalhe">{{ dica.detalhe }}</span>
    </div>
  </div>
</template>

<style scoped>
.dashboard {
  --azul-claro: #4fc3f7;
  --azul-acinzentado: #c9d0ea;
  --raio-painel: 14px;
  --lateral: clamp(260px, 26%, 420px);

  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 20px 28px 40px;
  font-size: 13.5px;
}

/* Painéis: cartões brancos sem borda, como na referência */
.dashboard__painel {
  display: flex;
  flex-direction: column;
  min-width: 0;
  background: var(--bg-surface);
  border-radius: var(--raio-painel);
  box-shadow: 0 1px 2px rgba(11, 11, 11, 0.04);
}

.dashboard__cabecalho {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 22px 0;
}

.dashboard__cabecalho h2 {
  font-size: 15px;
  font-weight: 700;
}

.dashboard__cabecalho p {
  margin-top: 3px;
  font-size: 13px;
  color: var(--text-muted);
}

.dashboard__legenda {
  flex: none;
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding-top: 2px;
  font-size: 12.5px;
  color: var(--text-secondary);
}

.dashboard__corpo {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 16px 22px 20px;
}

.dashboard__estado {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 28px 16px;
  text-align: center;
  color: var(--text-muted);
  font-size: 13px;
}

.dashboard__estado--erro {
  color: var(--text-critical);
}

.dashboard__marcador {
  flex: none;
  width: 8px;
  height: 8px;
  border-radius: 999px;
}

/* Resumo: números soltos sobre o fundo da página, espalhados pela largura toda */
.dashboard__tiles {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  padding: 16px 2px 4px;
  border-top: 1px solid var(--border-hairline);
}

.dashboard__tiles-erro {
  flex: 1;
}

.dashboard__tile {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.dashboard__tile-rotulo {
  font-size: 13px;
  color: var(--text-muted);
}

.dashboard__tile-valor {
  font-size: 22px;
  font-weight: 700;
  line-height: 1.3;
}

.dashboard__tile-skeleton {
  width: 56px;
  height: 24px;
  margin-top: 4px;
}

/* Linhas de duas colunas: conteúdo principal + coluna lateral, que cresce com a tela */
.dashboard__linha {
  display: grid;
  grid-template-columns: minmax(0, 1fr) var(--lateral);
  gap: 20px;
}

.dashboard__linha > :only-child {
  grid-column: 1 / -1;
}

.dashboard__lateral {
  display: flex;
  flex-direction: column;
  gap: 20px;
  min-width: 0;
}

.dashboard__lateral > .dashboard__painel {
  flex: 1;
}

/* Cartão azul de destaque */
.dashboard__receita {
  display: flex;
  flex-direction: column;
  padding: 18px 22px 24px;
  border-radius: var(--raio-painel);
  background: var(--brand-500);
  color: var(--brand-contrast);
}

.dashboard__receita h2 {
  font-size: 15px;
  font-weight: 700;
}

.dashboard__receita-moeda {
  margin-top: 16px;
  font-size: 12px;
  color: color-mix(in srgb, var(--brand-contrast) 65%, transparent);
}

.dashboard__receita-valor {
  font-size: 24px;
  font-weight: 500;
  line-height: 1.25;
  color: var(--azul-claro);
  overflow-wrap: anywhere;
}

.dashboard__receita-skeleton {
  width: 60%;
  height: 28px;
  background: color-mix(in srgb, var(--brand-contrast) 18%, transparent);
  animation: none;
}

.dashboard__skeleton-barra {
  height: 8px;
}

.dashboard__skeleton-grafico {
  height: 220px;
}

/* Evolução: o SVG fica solto dentro da área medida, para ela poder encolher e crescer */
.dashboard__corpo--grafico {
  flex: 1;
}

.evolucao__area {
  position: relative;
  flex: 1;
  min-height: 220px;
  max-height: 340px;
  overflow: hidden;
}

.evolucao {
  position: absolute;
  inset: 0 auto auto 0;
  display: block;
}

.evolucao__gradiente-topo {
  stop-color: var(--azul-claro);
  stop-opacity: 0.4;
}

.evolucao__gradiente-base {
  stop-color: var(--azul-claro);
  stop-opacity: 0.04;
}

.evolucao__grade {
  stroke: var(--border-hairline);
  stroke-width: 1;
}

.evolucao__eixo {
  fill: var(--text-muted);
  font-size: 11px;
}

.evolucao__mes {
  text-transform: uppercase;
}

.evolucao__linha {
  fill: none;
  stroke: var(--brand-500);
  stroke-width: 2;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.evolucao__ponto {
  outline: none;
}

.evolucao__zona {
  fill: transparent;
}

.evolucao__guia {
  stroke: var(--brand-500);
  stroke-width: 1;
  stroke-dasharray: 2 3;
  opacity: 0;
}

.evolucao__marca {
  fill: var(--brand-500);
  stroke: var(--bg-surface);
  stroke-width: 1.5;
}

.evolucao__ponto:hover .evolucao__guia,
.evolucao__ponto:focus-visible .evolucao__guia {
  opacity: 0.45;
}

.evolucao__ponto:hover .evolucao__marca,
.evolucao__ponto:focus-visible .evolucao__marca {
  stroke: var(--brand-500);
}

/* Ocupação */
.ocupacao__barra {
  display: flex;
  gap: 2px;
  height: 8px;
}

.ocupacao__fatia {
  flex-basis: 0;
  min-width: 5px;
  border-radius: 999px;
  transition: opacity 0.15s ease;
}

.ocupacao__barra:hover .ocupacao__fatia:not(:hover),
.ocupacao__barra:focus-within .ocupacao__fatia:not(:focus) {
  opacity: 0.5;
}

.ocupacao__fatia:focus-visible,
.situacoes__barra:focus-visible {
  outline: 2px solid var(--brand-500);
  outline-offset: 2px;
}

.ocupacao__legenda {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 9px;
}

.ocupacao__legenda li {
  display: flex;
  align-items: center;
  gap: 9px;
}

.ocupacao__rotulo {
  flex: 1;
  color: var(--text-secondary);
}

.ocupacao__percentual {
  font-size: 12.5px;
  color: var(--text-muted);
}

.ocupacao__quantidade {
  min-width: 30px;
  text-align: right;
  font-variant-numeric: tabular-nums;
}

/* Aluguéis por situação: rótulo e número em cima, traço fino embaixo */
.situacoes {
  list-style: none;
  margin: 0;
}

.situacoes__linha {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 5px 8px;
}

.situacoes__rotulo {
  color: var(--text-secondary);
}

.situacoes__quantidade {
  font-variant-numeric: tabular-nums;
}

.situacoes__trilho {
  grid-column: 1 / -1;
  display: flex;
  height: 4px;
  border-radius: 999px;
  background: var(--bg-surface-sunken);
}

.situacoes__barra {
  display: block;
  min-width: 4px;
  border-radius: 999px;
  background: var(--brand-500);
}

/* Pendências: linha do tempo com marcadores, como a "Atividade" da referência */
.atividade {
  list-style: none;
  margin: 0;
  padding: 10px 22px 16px;
}

.atividade__item {
  position: relative;
  display: flex;
  align-items: baseline;
  gap: 16px;
  padding: 9px 0 9px 26px;
}

.atividade__item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 16px;
  width: 7px;
  height: 7px;
  border-radius: 999px;
  background: var(--bg-surface);
  border: 2px solid var(--brand-500);
  box-sizing: border-box;
  z-index: 1;
}

.atividade__item::after {
  content: '';
  position: absolute;
  left: 3px;
  top: 0;
  bottom: 0;
  width: 1px;
  background: var(--border-hairline);
}

.atividade__item:first-child::after {
  top: 18px;
}

.atividade__item:last-child::after {
  bottom: calc(100% - 18px);
}

.atividade__texto {
  flex: 1;
  min-width: 0;
  color: var(--text-primary);
}

.atividade__nome {
  color: var(--brand-500);
}

.atividade__detalhe,
.atividade__tempo {
  color: var(--text-muted);
}

.atividade__tempo {
  flex: none;
  font-size: 12.5px;
  font-variant-numeric: tabular-nums;
}

.atividade__acao {
  flex: none;
  font-size: 12.5px;
  font-weight: 600;
  color: var(--brand-500);
  text-decoration: none;
}

.atividade__acao:hover {
  text-decoration: underline;
}

.dashboard__dica {
  position: fixed;
  z-index: 40;
  display: flex;
  flex-direction: column;
  padding: 7px 11px;
  background: var(--bg-surface);
  border: 1px solid var(--border-hairline);
  border-radius: 8px;
  box-shadow: var(--shadow-card);
  font-size: 12px;
  color: var(--text-secondary);
  pointer-events: none;
  transform: translate(-50%, calc(-100% - 10px));
  white-space: nowrap;
}

.dashboard__dica strong {
  font-size: 13px;
  color: var(--text-primary);
}

@media (max-width: 980px) {
  .dashboard__linha {
    grid-template-columns: minmax(0, 1fr);
  }
}

@media (max-width: 720px) {
  .dashboard {
    gap: 14px;
    padding: 14px 16px 28px;
  }

  .dashboard__linha,
  .dashboard__lateral {
    gap: 14px;
  }

  .dashboard__tiles {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 14px;
  }

  .dashboard__cabecalho {
    padding: 16px 16px 0;
  }

  .dashboard__corpo {
    padding: 14px 16px 16px;
  }

  .atividade {
    padding: 8px 16px 12px;
  }

  .atividade__item {
    flex-wrap: wrap;
    gap: 2px 12px;
  }

  .atividade__texto {
    flex-basis: 100%;
  }
}
</style>
