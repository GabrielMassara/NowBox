<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import AppIcon from './AppIcon.vue'
import type { OperacaoResponseDTO } from '../types/api'

interface ModuloNo {
  id: string
  nome: string
  operacoes: OperacaoResponseDTO[]
}

interface SessaoNo {
  id: string
  nome: string
  modulos: ModuloNo[]
}

type EstadoMarcacao = 'todas' | 'algumas' | 'nenhuma'

const props = defineProps<{
  operacoes: OperacaoResponseDTO[]
  modelValue: string[]
  disabled?: boolean
}>()

const emit = defineEmits<{ 'update:modelValue': [ids: string[]] }>()

const busca = ref('')
const expandidos = reactive(new Set<string>())

const selecionadas = computed(() => new Set(props.modelValue))
const buscando = computed(() => busca.value.trim() !== '')

function normalizar(texto: string) {
  return texto.normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase()
}

const arvore = computed<SessaoNo[]>(() => {
  const termo = normalizar(busca.value.trim())
  const sessoes = new Map<string, SessaoNo>()

  for (const operacao of props.operacoes) {
    const modulo = operacao.modulo
    const sessao = modulo.sessao

    const casa =
      !termo ||
      [operacao.nome, operacao.codigo, modulo.nome, sessao.nome].some((texto) => normalizar(texto ?? '').includes(termo))
    if (!casa) continue

    let noSessao = sessoes.get(sessao.id)
    if (!noSessao) {
      noSessao = { id: sessao.id, nome: sessao.nome, modulos: [] }
      sessoes.set(sessao.id, noSessao)
    }

    let noModulo = noSessao.modulos.find((m) => m.id === modulo.id)
    if (!noModulo) {
      noModulo = { id: modulo.id, nome: modulo.nome, operacoes: [] }
      noSessao.modulos.push(noModulo)
    }

    noModulo.operacoes.push(operacao)
  }

  const porNome = <T extends { nome: string }>(a: T, b: T) => a.nome.localeCompare(b.nome)
  const resultado = [...sessoes.values()].sort(porNome)
  for (const sessao of resultado) {
    sessao.modulos.sort(porNome)
    for (const modulo of sessao.modulos) modulo.operacoes.sort(porNome)
  }

  return resultado
})

const idsVisiveis = computed(() => arvore.value.flatMap(idsDaSessao))
const totalMarcadasVisiveis = computed(() => idsVisiveis.value.filter((id) => selecionadas.value.has(id)).length)

function idsDoModulo(modulo: ModuloNo) {
  return modulo.operacoes.map((o) => o.id)
}

function idsDaSessao(sessao: SessaoNo) {
  return sessao.modulos.flatMap(idsDoModulo)
}

function estadoDe(ids: string[]): EstadoMarcacao {
  const marcadas = ids.filter((id) => selecionadas.value.has(id)).length
  if (marcadas === 0) return 'nenhuma'
  return marcadas === ids.length ? 'todas' : 'algumas'
}

function contagem(ids: string[]) {
  return `${ids.filter((id) => selecionadas.value.has(id)).length}/${ids.length}`
}

// Marca ou desmarca todas as operações informadas de uma só vez.
function definir(ids: string[], marcar: boolean) {
  const proximas = new Set(props.modelValue)
  for (const id of ids) {
    if (marcar) proximas.add(id)
    else proximas.delete(id)
  }
  emit('update:modelValue', [...proximas])
}

function alternarGrupo(ids: string[]) {
  definir(ids, estadoDe(ids) !== 'todas')
}

function alternarOperacao(id: string) {
  definir([id], !selecionadas.value.has(id))
}

function chaveSessao(sessao: SessaoNo) {
  return `s:${sessao.id}`
}

function chaveModulo(modulo: ModuloNo) {
  return `m:${modulo.id}`
}

function estaExpandido(chave: string) {
  return buscando.value || expandidos.has(chave)
}

function alternarExpansao(chave: string) {
  if (expandidos.has(chave)) expandidos.delete(chave)
  else expandidos.add(chave)
}

function expandirTudo() {
  for (const sessao of arvore.value) {
    expandidos.add(chaveSessao(sessao))
    for (const modulo of sessao.modulos) expandidos.add(chaveModulo(modulo))
  }
}

function recolherTudo() {
  expandidos.clear()
}
</script>

<template>
  <div class="arvore">
    <div class="arvore__toolbar">
      <div class="arvore__busca">
        <AppIcon name="search" :size="16" />
        <input
          v-model="busca"
          type="text"
          placeholder="Buscar sessão, módulo ou operação"
          aria-label="Buscar sessão, módulo ou operação"
        />
      </div>

      <div class="arvore__atalhos">
        <button type="button" class="btn" :disabled="disabled" @click="definir(idsVisiveis, true)">
          Marcar {{ buscando ? 'resultados' : 'todas' }}
        </button>
        <button type="button" class="btn" :disabled="disabled" @click="definir(idsVisiveis, false)">
          Desmarcar {{ buscando ? 'resultados' : 'todas' }}
        </button>
        <button type="button" class="btn" @click="expandirTudo">Expandir</button>
        <button type="button" class="btn" @click="recolherTudo">Recolher</button>
      </div>
    </div>

    <p class="arvore__resumo">
      <strong>{{ modelValue.length }}</strong> de {{ operacoes.length }} operações liberadas
      <template v-if="buscando"> · {{ totalMarcadasVisiveis }}/{{ idsVisiveis.length }} nos resultados</template>
    </p>

    <div v-if="arvore.length === 0" class="arvore__vazio">
      <AppIcon name="search" :size="20" />
      <span>Nenhuma operação encontrada.</span>
    </div>

    <ul v-else class="arvore__lista" role="tree">
      <li v-for="sessao in arvore" :key="sessao.id" class="arvore__item" role="treeitem">
        <div class="arvore__linha arvore__linha--sessao">
          <button
            type="button"
            class="arvore__toggle"
            :aria-expanded="estaExpandido(chaveSessao(sessao))"
            :aria-label="`${estaExpandido(chaveSessao(sessao)) ? 'Recolher' : 'Expandir'} ${sessao.nome}`"
            @click="alternarExpansao(chaveSessao(sessao))"
          >
            <AppIcon :name="estaExpandido(chaveSessao(sessao)) ? 'chevron-down' : 'chevron-right'" :size="16" />
          </button>

          <label class="arvore__rotulo">
            <input
              type="checkbox"
              :checked="estadoDe(idsDaSessao(sessao)) === 'todas'"
              :indeterminate="estadoDe(idsDaSessao(sessao)) === 'algumas'"
              :disabled="disabled"
              @change="alternarGrupo(idsDaSessao(sessao))"
            />
            <AppIcon name="folder" :size="15" class="arvore__icone" />
            <span class="arvore__nome arvore__nome--sessao">{{ sessao.nome }}</span>
          </label>

          <span class="arvore__contagem">{{ contagem(idsDaSessao(sessao)) }}</span>
        </div>

        <ul v-if="estaExpandido(chaveSessao(sessao))" class="arvore__lista arvore__lista--filha" role="group">
          <li v-for="modulo in sessao.modulos" :key="modulo.id" class="arvore__item" role="treeitem">
            <div class="arvore__linha">
              <button
                type="button"
                class="arvore__toggle"
                :aria-expanded="estaExpandido(chaveModulo(modulo))"
                :aria-label="`${estaExpandido(chaveModulo(modulo)) ? 'Recolher' : 'Expandir'} ${modulo.nome}`"
                @click="alternarExpansao(chaveModulo(modulo))"
              >
                <AppIcon :name="estaExpandido(chaveModulo(modulo)) ? 'chevron-down' : 'chevron-right'" :size="16" />
              </button>

              <label class="arvore__rotulo">
                <input
                  type="checkbox"
                  :checked="estadoDe(idsDoModulo(modulo)) === 'todas'"
                  :indeterminate="estadoDe(idsDoModulo(modulo)) === 'algumas'"
                  :disabled="disabled"
                  @change="alternarGrupo(idsDoModulo(modulo))"
                />
                <AppIcon name="layers" :size="15" class="arvore__icone" />
                <span class="arvore__nome">{{ modulo.nome }}</span>
              </label>

              <span class="arvore__contagem">{{ contagem(idsDoModulo(modulo)) }}</span>
            </div>

            <ul v-if="estaExpandido(chaveModulo(modulo))" class="arvore__lista arvore__lista--filha" role="group">
              <li v-for="operacao in modulo.operacoes" :key="operacao.id" class="arvore__item" role="treeitem">
                <div class="arvore__linha arvore__linha--operacao">
                  <label class="arvore__rotulo">
                    <input
                      type="checkbox"
                      :checked="selecionadas.has(operacao.id)"
                      :disabled="disabled"
                      @change="alternarOperacao(operacao.id)"
                    />
                    <span class="arvore__nome">{{ operacao.nome }}</span>
                    <span class="arvore__codigo">{{ operacao.codigo }}</span>
                  </label>
                </div>
              </li>
            </ul>
          </li>
        </ul>
      </li>
    </ul>
  </div>
</template>

<style scoped>
.arvore {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.arvore__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.arvore__busca {
  display: flex;
  align-items: center;
  gap: 8px;
  background: var(--bg-surface);
  border: 1px solid var(--border-hairline);
  padding: 8px 12px;
  color: var(--text-muted);
  min-width: 280px;
}

.arvore__busca input {
  border: none;
  outline: none;
  background: transparent;
  font: inherit;
  font-size: 13.5px;
  color: var(--text-primary);
  width: 100%;
}

.arvore__busca input::placeholder {
  color: var(--text-muted);
}

.arvore__atalhos {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.arvore__atalhos .btn {
  border-radius: 0;
}

.arvore__atalhos .btn:disabled {
  opacity: 0.5;
  cursor: default;
}

.arvore__resumo {
  font-size: 12.5px;
  color: var(--text-muted);
}

.arvore__resumo strong {
  color: var(--text-primary);
}

.arvore__vazio {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 40px 16px;
  color: var(--text-muted);
  font-size: 13.5px;
  border: 1px solid var(--border-hairline);
}

.arvore__lista {
  list-style: none;
  margin: 0;
  padding: 0;
  border: 1px solid var(--border-hairline);
  background: var(--bg-surface);
}

.arvore__lista--filha {
  border: none;
  margin-left: 18px;
  padding-left: 14px;
  border-left: 1px solid var(--border-hairline);
}

.arvore__item + .arvore__item {
  border-top: 1px solid var(--border-hairline);
}

.arvore__lista--filha > .arvore__item + .arvore__item {
  border-top: none;
}

.arvore__linha {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 7px 12px;
  transition: background-color 0.15s ease;
}

.arvore__linha:hover {
  background: var(--brand-050);
}

.arvore__linha--sessao {
  background: var(--bg-surface-sunken);
}

.arvore__linha--operacao {
  padding-left: 34px;
}

.arvore__toggle {
  width: 24px;
  height: 24px;
  display: grid;
  place-items: center;
  border: none;
  background: transparent;
  color: var(--text-secondary);
  cursor: pointer;
  flex-shrink: 0;
}

.arvore__rotulo {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 13.5px;
}

.arvore__rotulo input[type='checkbox'] {
  width: 16px;
  height: 16px;
  accent-color: var(--brand-500);
  cursor: pointer;
  flex-shrink: 0;
}

.arvore__rotulo input[type='checkbox']:disabled {
  cursor: default;
}

.arvore__icone {
  color: var(--text-muted);
  flex-shrink: 0;
}

.arvore__nome {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
}

.arvore__nome--sessao {
  font-weight: 600;
}

.arvore__codigo {
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 12px;
  color: var(--text-muted);
  overflow: hidden;
  text-overflow: ellipsis;
}

.arvore__contagem {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-muted);
  white-space: nowrap;
}

@media (max-width: 720px) {
  .arvore__busca {
    min-width: 0;
    width: 100%;
  }

  .arvore__rotulo {
    flex-wrap: wrap;
  }

  .arvore__linha--operacao {
    padding-left: 12px;
  }
}
</style>
