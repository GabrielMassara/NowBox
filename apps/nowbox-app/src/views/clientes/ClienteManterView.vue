<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppIcon from '../../components/AppIcon.vue'
import { ApiError } from '../../lib/http'
import { mascaraCep, mascaraCpf, mascaraRg, mascaraTelefone, semMascaraRg } from '../../lib/mascaras'
import { clienteService } from '../../services/cliente.service'
import { estadoService } from '../../services/estado.service'
import type { EstadoEntity } from '../../types/api'

const route = useRoute()
const router = useRouter()

const idCliente = computed(() => route.params.id as string | undefined)
const emEdicao = computed(() => !!idCliente.value)

const form = reactive({
  nome: '',
  email: '',
  cpf: '',
  rg: '',
  telefone: '',
  sexo: '',
  nascimento: '',
  profissao: '',
  idEstado: '',
  cep: '',
  cidade: '',
  bairro: '',
  endereco: '',
  numero: '',
  complemento: '',
  enderecoCorrespondencia: false,
  senha: '',
})
const estados = ref<EstadoEntity[]>([])
// A API regrava esse campo a cada atualização, então ele é preservado na edição.
const senhaTemporariaStatus = ref(false)

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

async function carregarCliente(id: string) {
  carregando.value = true
  erro.value = ''

  try {
    const cliente = await clienteService.buscarPorId(id)
    form.nome = cliente.nome
    form.email = cliente.email
    form.cpf = mascaraCpf(cliente.cpf)
    form.rg = mascaraRg(cliente.rg)
    form.telefone = mascaraTelefone(cliente.telefone)
    form.sexo = cliente.sexo
    form.nascimento = cliente.nascimento
    form.profissao = cliente.profissao
    form.idEstado = cliente.estado.id
    form.cep = mascaraCep(cliente.cep)
    form.cidade = cliente.cidade
    form.bairro = cliente.bairro
    form.endereco = cliente.endereco
    form.numero = cliente.numero
    form.complemento = cliente.complemento ?? ''
    form.enderecoCorrespondencia = cliente.enderecoCorrespondencia ?? false
    senhaTemporariaStatus.value = cliente.senhaTemporariaStatus ?? false
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível carregar o cliente.'
  } finally {
    carregando.value = false
  }
}

onMounted(async () => {
  await carregarEstados()
  if (idCliente.value) await carregarCliente(idCliente.value)
})

function voltar() {
  router.push('/clientes')
}

async function onSubmit() {
  if (salvando.value) return

  salvando.value = true
  erro.value = ''

  const dados = {
    idEstado: form.idEstado,
    nome: form.nome.trim(),
    profissao: form.profissao.trim(),
    cpf: somenteDigitos(form.cpf),
    rg: semMascaraRg(form.rg),
    email: form.email.trim(),
    telefone: somenteDigitos(form.telefone),
    sexo: form.sexo,
    nascimento: form.nascimento,
    endereco: form.endereco.trim(),
    numero: form.numero.trim(),
    complemento: form.complemento.trim() || undefined,
    bairro: form.bairro.trim(),
    cep: somenteDigitos(form.cep),
    cidade: form.cidade.trim(),
    enderecoCorrespondencia: form.enderecoCorrespondencia,
    senha: form.senha,
    senhaTemporariaStatus: senhaTemporariaStatus.value,
  }

  try {
    if (idCliente.value) {
      await clienteService.atualizar(idCliente.value, dados)
    } else {
      await clienteService.criar(dados)
    }
    voltar()
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível salvar o cliente.'
  } finally {
    salvando.value = false
  }
}
</script>

<template>
  <div class="cliente-manter">
    <button type="button" class="btn cliente-manter__voltar" @click="voltar">
      <AppIcon name="arrow-left" :size="15" />
      Voltar para clientes
    </button>

    <div class="cliente-manter__card">
      <div v-if="carregando" class="cliente-manter__state">
        <AppIcon name="loader" :size="20" class="cliente-manter__spinner" />
        <span>Carregando cliente...</span>
      </div>

      <template v-else>
        <div class="cliente-manter__header">
          <h2>{{ emEdicao ? 'Editar cliente' : 'Novo cliente' }}</h2>
          <p>Preencha os dados abaixo para {{ emEdicao ? 'atualizar o' : 'cadastrar um novo' }} cliente.</p>
        </div>

        <form class="cliente-manter__form" @submit.prevent="onSubmit">
          <div class="cliente-manter__campo">
            <label for="cliente-nome">Nome</label>
            <input
              id="cliente-nome"
              v-model="form.nome"
              type="text"
              maxlength="150"
              placeholder="Ex.: Maria da Silva"
              required
              :disabled="salvando"
            />
          </div>

          <div class="cliente-manter__campo">
            <label for="cliente-email">E-mail</label>
            <input
              id="cliente-email"
              v-model="form.email"
              type="email"
              maxlength="150"
              placeholder="Ex.: maria@email.com"
              required
              :disabled="salvando"
            />
          </div>

          <div class="cliente-manter__campo">
            <label for="cliente-cpf">CPF</label>
            <input
              id="cliente-cpf"
              v-model="form.cpf"
              type="text"
              inputmode="numeric"
              maxlength="14"
              @input="form.cpf = mascaraCpf(form.cpf)"
              pattern="\D*(\d\D*){11}"
              title="Informe os 11 dígitos do CPF"
              placeholder="000.000.000-00"
              required
              :disabled="salvando"
            />
            <span class="cliente-manter__ajuda">Apenas os números são enviados.</span>
          </div>

          <div class="cliente-manter__campo">
            <label for="cliente-rg">RG</label>
            <input
              id="cliente-rg"
              v-model="form.rg"
              type="text"
              maxlength="20"
              @input="form.rg = mascaraRg(form.rg)"
              placeholder="Ex.: 12.345.678-9"
              required
              :disabled="salvando"
            />
          </div>

          <div class="cliente-manter__campo">
            <label for="cliente-nascimento">Data de nascimento</label>
            <input id="cliente-nascimento" v-model="form.nascimento" type="date" required :disabled="salvando" />
          </div>

          <div class="cliente-manter__campo">
            <label for="cliente-sexo">Sexo</label>
            <select id="cliente-sexo" v-model="form.sexo" required :disabled="salvando">
              <option value="" disabled>Selecione o sexo</option>
              <option value="M">Masculino</option>
              <option value="F">Feminino</option>
            </select>
          </div>

          <div class="cliente-manter__campo">
            <label for="cliente-telefone">Telefone</label>
            <input
              id="cliente-telefone"
              v-model="form.telefone"
              type="tel"
              inputmode="numeric"
              maxlength="15"
              @input="form.telefone = mascaraTelefone(form.telefone)"
              pattern="\D*(\d\D*){10,11}"
              title="Informe o DDD e o número, com 10 ou 11 dígitos"
              placeholder="(00) 00000-0000"
              required
              :disabled="salvando"
            />
          </div>

          <div class="cliente-manter__campo">
            <label for="cliente-profissao">Profissão</label>
            <input
              id="cliente-profissao"
              v-model="form.profissao"
              type="text"
              maxlength="100"
              placeholder="Ex.: Engenheira"
              required
              :disabled="salvando"
            />
          </div>

          <div class="cliente-manter__campo">
            <label for="cliente-cep">CEP</label>
            <input
              id="cliente-cep"
              v-model="form.cep"
              type="text"
              inputmode="numeric"
              maxlength="9"
              @input="form.cep = mascaraCep(form.cep)"
              pattern="\D*(\d\D*){8}"
              title="Informe os 8 dígitos do CEP"
              placeholder="00000-000"
              required
              :disabled="salvando"
            />
          </div>

          <div class="cliente-manter__campo">
            <label for="cliente-estado">Estado</label>
            <select id="cliente-estado" v-model="form.idEstado" required :disabled="salvando">
              <option value="" disabled>Selecione um estado</option>
              <option v-for="estado in estados" :key="estado.id" :value="estado.id">
                {{ estado.nome }} ({{ estado.uf }})
              </option>
            </select>
          </div>

          <div class="cliente-manter__campo">
            <label for="cliente-cidade">Cidade</label>
            <input
              id="cliente-cidade"
              v-model="form.cidade"
              type="text"
              maxlength="100"
              placeholder="Ex.: São Paulo"
              required
              :disabled="salvando"
            />
          </div>

          <div class="cliente-manter__campo">
            <label for="cliente-bairro">Bairro</label>
            <input
              id="cliente-bairro"
              v-model="form.bairro"
              type="text"
              maxlength="100"
              placeholder="Ex.: Centro"
              required
              :disabled="salvando"
            />
          </div>

          <div class="cliente-manter__campo">
            <label for="cliente-endereco">Endereço</label>
            <input
              id="cliente-endereco"
              v-model="form.endereco"
              type="text"
              maxlength="200"
              placeholder="Ex.: Avenida Paulista"
              required
              :disabled="salvando"
            />
          </div>

          <div class="cliente-manter__campo">
            <label for="cliente-numero">Número</label>
            <input
              id="cliente-numero"
              v-model="form.numero"
              type="text"
              maxlength="10"
              placeholder="Ex.: 1000"
              required
              :disabled="salvando"
            />
          </div>

          <div class="cliente-manter__campo">
            <label for="cliente-complemento">Complemento</label>
            <input
              id="cliente-complemento"
              v-model="form.complemento"
              type="text"
              maxlength="100"
              placeholder="Ex.: Apto 12 (opcional)"
              :disabled="salvando"
            />
          </div>

          <div class="cliente-manter__campo">
            <label for="cliente-senha">{{ emEdicao ? 'Nova senha' : 'Senha' }}</label>
            <input
              id="cliente-senha"
              v-model="form.senha"
              type="password"
              maxlength="60"
              autocomplete="new-password"
              placeholder="Digite a senha"
              required
              :disabled="salvando"
            />
            <span v-if="emEdicao" class="cliente-manter__ajuda">
              A senha é redefinida a cada atualização, então informe a atual ou uma nova.
            </span>
          </div>

          <label class="cliente-manter__check">
            <input v-model="form.enderecoCorrespondencia" type="checkbox" :disabled="salvando" />
            Usar este endereço para correspondência
          </label>

          <p v-if="erro" class="cliente-manter__erro">
            <AppIcon name="alert-circle" :size="15" />
            {{ erro }}
          </p>

          <div class="cliente-manter__acoes">
            <button type="button" class="btn" :disabled="salvando" @click="voltar">Cancelar</button>
            <button type="submit" class="btn btn--primary" :disabled="salvando">
              <AppIcon v-if="salvando" name="loader" :size="15" class="cliente-manter__spinner" />
              {{ salvando ? 'Salvando...' : emEdicao ? 'Salvar alterações' : 'Cadastrar cliente' }}
            </button>
          </div>
        </form>
      </template>
    </div>
  </div>
</template>

<style scoped>
.cliente-manter {
  padding: 22px 28px 40px;
  display: flex;
  flex-direction: column;
  min-height: calc(100svh - 73px);
}

.cliente-manter__voltar {
  align-self: flex-start;
  margin-bottom: 18px;
}

.cliente-manter__card {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 8px clamp(0px, 5vw, 24px) 0;
}

.cliente-manter__header {
  padding-bottom: 24px;
  margin-bottom: 32px;
  border-bottom: 1px solid var(--border-hairline);
}

.cliente-manter__header h2 {
  font-size: 20px;
  font-weight: 700;
}

.cliente-manter__header p {
  margin-top: 6px;
  font-size: 13.5px;
  color: var(--text-muted);
}

.cliente-manter__state {
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

.cliente-manter__spinner {
  animation: cliente-manter-spin 0.8s linear infinite;
  color: var(--brand-500);
}

@keyframes cliente-manter-spin {
  to {
    transform: rotate(360deg);
  }
}

.cliente-manter__form {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  align-content: start;
  gap: 24px 28px;
}

.cliente-manter__campo {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

@media (max-width: 760px) {
  .cliente-manter__form {
    grid-template-columns: 1fr;
  }
}

.cliente-manter__campo label {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
}

.cliente-manter__campo input,
.cliente-manter__campo select {
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

.cliente-manter__campo input::placeholder {
  color: var(--text-muted);
}

.cliente-manter__campo input:focus,
.cliente-manter__campo select:focus {
  box-shadow: 0 0 0 2px var(--brand-100);
}

.cliente-manter__ajuda {
  font-size: 12.5px;
  color: var(--text-muted);
}

.cliente-manter__erro {
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

.cliente-manter__acoes {
  grid-column: 1 / -1;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
  margin-top: auto;
  padding-top: 24px;
  border-top: 1px solid var(--border-hairline);
}

.cliente-manter__check {
  grid-column: 1 / -1;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13.5px;
  color: var(--text-secondary);
  cursor: pointer;
}

.cliente-manter__check input {
  width: 16px;
  height: 16px;
  accent-color: var(--brand-500);
  cursor: pointer;
  flex-shrink: 0;
}

.cliente-manter__check input:disabled {
  cursor: default;
}
</style>
