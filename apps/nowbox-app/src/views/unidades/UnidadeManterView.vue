<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppIcon from '../../components/AppIcon.vue'
import { ApiError } from '../../lib/http'
import { estadoService } from '../../services/estado.service'
import { unidadeService } from '../../services/unidade.service'
import type { EstadoEntity } from '../../types/api'

const route = useRoute()
const router = useRouter()

const idUnidade = computed(() => route.params.id as string | undefined)
const emEdicao = computed(() => !!idUnidade.value)

const form = reactive({
  idEstado: '',
  nome: '',
  cnpj: '',
  cep: '',
  cidade: '',
  bairro: '',
  endereco: '',
  numero: '',
  complemento: '',
})
const estados = ref<EstadoEntity[]>([])

const carregando = ref(false)
const salvando = ref(false)
const erro = ref('')

function somenteDigitos(valor: string) {
  return valor.replace(/\D/g, '')
}

async function carregarEstados() {
  try {
    const resultado = await estadoService.listar(0, 100)
    estados.value = resultado.content
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível carregar os estados.'
  }
}

async function carregarUnidade(id: string) {
  carregando.value = true
  erro.value = ''

  try {
    const unidade = await unidadeService.buscarPorId(id)
    form.idEstado = unidade.estado.id
    form.nome = unidade.nome
    form.cnpj = unidade.cnpj
    form.cep = unidade.cep
    form.cidade = unidade.cidade
    form.bairro = unidade.bairro
    form.endereco = unidade.endereco
    form.numero = unidade.numero
    form.complemento = unidade.complemento ?? ''
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível carregar a unidade.'
  } finally {
    carregando.value = false
  }
}

onMounted(async () => {
  await carregarEstados()
  if (idUnidade.value) await carregarUnidade(idUnidade.value)
})

function voltar() {
  router.push('/unidades')
}

async function onSubmit() {
  if (salvando.value) return

  salvando.value = true
  erro.value = ''

  const dados = {
    idEstado: form.idEstado,
    nome: form.nome.trim(),
    cnpj: somenteDigitos(form.cnpj),
    endereco: form.endereco.trim(),
    numero: form.numero.trim(),
    complemento: form.complemento.trim() || undefined,
    bairro: form.bairro.trim(),
    cep: somenteDigitos(form.cep),
    cidade: form.cidade.trim(),
  }

  try {
    if (idUnidade.value) {
      await unidadeService.atualizar(idUnidade.value, dados)
    } else {
      await unidadeService.criar(dados)
    }
    voltar()
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível salvar a unidade.'
  } finally {
    salvando.value = false
  }
}
</script>

<template>
  <div class="unidade-manter">
    <button type="button" class="btn unidade-manter__voltar" @click="voltar">
      <AppIcon name="arrow-left" :size="15" />
      Voltar para unidades
    </button>

    <div class="unidade-manter__card">
      <div v-if="carregando" class="unidade-manter__state">
        <AppIcon name="loader" :size="20" class="unidade-manter__spinner" />
        <span>Carregando unidade...</span>
      </div>

      <template v-else>
        <div class="unidade-manter__header">
          <h2>{{ emEdicao ? 'Editar unidade' : 'Nova unidade' }}</h2>
          <p>Preencha os dados abaixo para {{ emEdicao ? 'atualizar a' : 'cadastrar uma nova' }} unidade.</p>
        </div>

        <form class="unidade-manter__form" @submit.prevent="onSubmit">
          <div class="unidade-manter__campo">
            <label for="unidade-nome">Nome</label>
            <input
              id="unidade-nome"
              v-model="form.nome"
              type="text"
              maxlength="150"
              placeholder="Ex.: Matriz São Paulo"
              required
              :disabled="salvando"
            />
          </div>

          <div class="unidade-manter__campo">
            <label for="unidade-cnpj">CNPJ</label>
            <input
              id="unidade-cnpj"
              v-model="form.cnpj"
              type="text"
              inputmode="numeric"
              maxlength="18"
              pattern="\D*(\d\D*){14}"
              title="Informe os 14 dígitos do CNPJ"
              placeholder="00.000.000/0000-00"
              required
              :disabled="salvando"
            />
            <span class="unidade-manter__ajuda">Apenas os números são enviados.</span>
          </div>

          <div class="unidade-manter__campo">
            <label for="unidade-cep">CEP</label>
            <input
              id="unidade-cep"
              v-model="form.cep"
              type="text"
              inputmode="numeric"
              maxlength="9"
              pattern="\D*(\d\D*){8}"
              title="Informe os 8 dígitos do CEP"
              placeholder="00000-000"
              required
              :disabled="salvando"
            />
          </div>

          <div class="unidade-manter__campo">
            <label for="unidade-estado">Estado</label>
            <select id="unidade-estado" v-model="form.idEstado" required :disabled="salvando">
              <option value="" disabled>Selecione um estado</option>
              <option v-for="estado in estados" :key="estado.id" :value="estado.id">
                {{ estado.nome }} ({{ estado.uf }})
              </option>
            </select>
          </div>

          <div class="unidade-manter__campo">
            <label for="unidade-cidade">Cidade</label>
            <input
              id="unidade-cidade"
              v-model="form.cidade"
              type="text"
              maxlength="100"
              placeholder="Ex.: São Paulo"
              required
              :disabled="salvando"
            />
          </div>

          <div class="unidade-manter__campo">
            <label for="unidade-bairro">Bairro</label>
            <input
              id="unidade-bairro"
              v-model="form.bairro"
              type="text"
              maxlength="100"
              placeholder="Ex.: Centro"
              required
              :disabled="salvando"
            />
          </div>

          <div class="unidade-manter__campo">
            <label for="unidade-endereco">Endereço</label>
            <input
              id="unidade-endereco"
              v-model="form.endereco"
              type="text"
              maxlength="200"
              placeholder="Ex.: Avenida Paulista"
              required
              :disabled="salvando"
            />
          </div>

          <div class="unidade-manter__campo">
            <label for="unidade-numero">Número</label>
            <input
              id="unidade-numero"
              v-model="form.numero"
              type="text"
              maxlength="10"
              placeholder="Ex.: 1000"
              required
              :disabled="salvando"
            />
          </div>

          <div class="unidade-manter__campo">
            <label for="unidade-complemento">Complemento</label>
            <input
              id="unidade-complemento"
              v-model="form.complemento"
              type="text"
              maxlength="100"
              placeholder="Ex.: Sala 12 (opcional)"
              :disabled="salvando"
            />
          </div>

          <p v-if="erro" class="unidade-manter__erro">
            <AppIcon name="alert-circle" :size="15" />
            {{ erro }}
          </p>

          <div class="unidade-manter__acoes">
            <button type="button" class="btn" :disabled="salvando" @click="voltar">Cancelar</button>
            <button type="submit" class="btn btn--primary" :disabled="salvando">
              <AppIcon v-if="salvando" name="loader" :size="15" class="unidade-manter__spinner" />
              {{ salvando ? 'Salvando...' : emEdicao ? 'Salvar alterações' : 'Cadastrar unidade' }}
            </button>
          </div>
        </form>
      </template>
    </div>
  </div>
</template>

<style scoped>
.unidade-manter {
  padding: 22px 28px 40px;
  display: flex;
  flex-direction: column;
  min-height: calc(100svh - 73px);
}

.unidade-manter__voltar {
  align-self: flex-start;
  margin-bottom: 18px;
}

.unidade-manter__card {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 8px clamp(0px, 5vw, 24px) 0;
}

.unidade-manter__header {
  padding-bottom: 24px;
  margin-bottom: 32px;
  border-bottom: 1px solid var(--border-hairline);
}

.unidade-manter__header h2 {
  font-size: 20px;
  font-weight: 700;
}

.unidade-manter__header p {
  margin-top: 6px;
  font-size: 13.5px;
  color: var(--text-muted);
}

.unidade-manter__state {
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

.unidade-manter__spinner {
  animation: unidade-manter-spin 0.8s linear infinite;
  color: var(--brand-500);
}

@keyframes unidade-manter-spin {
  to {
    transform: rotate(360deg);
  }
}

.unidade-manter__form {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  align-content: start;
  gap: 24px 28px;
}

.unidade-manter__campo {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

@media (max-width: 760px) {
  .unidade-manter__form {
    grid-template-columns: 1fr;
  }
}

.unidade-manter__campo label {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
}

.unidade-manter__campo input,
.unidade-manter__campo select {
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

.unidade-manter__campo input::placeholder {
  color: var(--text-muted);
}

.unidade-manter__campo input:focus,
.unidade-manter__campo select:focus {
  box-shadow: 0 0 0 2px var(--brand-100);
}

.unidade-manter__ajuda {
  font-size: 12.5px;
  color: var(--text-muted);
}

.unidade-manter__erro {
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

.unidade-manter__acoes {
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
