<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppIcon from '../../components/AppIcon.vue'
import { ApiError } from '../../lib/http'
import { usuarioService } from '../../services/usuario.service'

const route = useRoute()
const router = useRouter()

const idUsuario = computed(() => route.params.id as string | undefined)
const emEdicao = computed(() => !!idUsuario.value)

const form = reactive({ nome: '', email: '', cpf: '', sexo: '', senha: '' })

const carregando = ref(false)
const salvando = ref(false)
const erro = ref('')

async function carregarUsuario(id: string) {
  carregando.value = true
  erro.value = ''

  try {
    const usuario = await usuarioService.buscarPorId(id)
    form.nome = usuario.nome
    form.email = usuario.email
    form.cpf = usuario.cpf
    form.sexo = usuario.sexo
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível carregar o usuário.'
  } finally {
    carregando.value = false
  }
}

onMounted(() => {
  if (idUsuario.value) carregarUsuario(idUsuario.value)
})

function voltar() {
  router.push('/usuarios')
}

async function onSubmit() {
  if (salvando.value) return

  salvando.value = true
  erro.value = ''

  const dados = {
    nome: form.nome.trim(),
    email: form.email.trim(),
    cpf: form.cpf.replace(/\D/g, ''),
    sexo: form.sexo,
    senha: form.senha,
  }

  try {
    if (idUsuario.value) {
      await usuarioService.atualizar(idUsuario.value, dados)
    } else {
      await usuarioService.criar(dados)
    }
    voltar()
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível salvar o usuário.'
  } finally {
    salvando.value = false
  }
}
</script>

<template>
  <div class="usuario-manter">
    <button type="button" class="btn usuario-manter__voltar" @click="voltar">
      <AppIcon name="arrow-left" :size="15" />
      Voltar para usuários
    </button>

    <div class="usuario-manter__card">
      <div v-if="carregando" class="usuario-manter__state">
        <AppIcon name="loader" :size="20" class="usuario-manter__spinner" />
        <span>Carregando usuário...</span>
      </div>

      <template v-else>
        <div class="usuario-manter__header">
          <h2>{{ emEdicao ? 'Editar usuário' : 'Novo usuário' }}</h2>
          <p>Preencha os dados abaixo para {{ emEdicao ? 'atualizar o' : 'cadastrar um novo' }} usuário.</p>
        </div>

        <form class="usuario-manter__form" @submit.prevent="onSubmit">
          <div class="usuario-manter__campo">
            <label for="usuario-nome">Nome</label>
            <input
              id="usuario-nome"
              v-model="form.nome"
              type="text"
              maxlength="150"
              placeholder="Ex.: Maria da Silva"
              required
              :disabled="salvando"
            />
          </div>

          <div class="usuario-manter__campo">
            <label for="usuario-email">E-mail</label>
            <input
              id="usuario-email"
              v-model="form.email"
              type="email"
              maxlength="150"
              placeholder="Ex.: maria@nowbox.com"
              required
              :disabled="salvando"
            />
          </div>

          <div class="usuario-manter__campo">
            <label for="usuario-cpf">CPF</label>
            <input
              id="usuario-cpf"
              v-model="form.cpf"
              type="text"
              inputmode="numeric"
              maxlength="14"
              pattern="\D*(\d\D*){11}"
              title="Informe os 11 dígitos do CPF"
              placeholder="000.000.000-00"
              required
              :disabled="salvando"
            />
            <span class="usuario-manter__ajuda">Apenas os números são enviados.</span>
          </div>

          <div class="usuario-manter__campo">
            <label for="usuario-sexo">Sexo</label>
            <select id="usuario-sexo" v-model="form.sexo" required :disabled="salvando">
              <option value="" disabled>Selecione o sexo</option>
              <option value="M">Masculino</option>
              <option value="F">Feminino</option>
            </select>
          </div>

          <div class="usuario-manter__campo">
            <label for="usuario-senha">{{ emEdicao ? 'Nova senha' : 'Senha' }}</label>
            <input
              id="usuario-senha"
              v-model="form.senha"
              type="password"
              autocomplete="new-password"
              placeholder="Digite a senha"
              required
              :disabled="salvando"
            />
            <span v-if="emEdicao" class="usuario-manter__ajuda">
              A senha é redefinida a cada atualização, então informe a atual ou uma nova.
            </span>
          </div>

          <p v-if="erro" class="usuario-manter__erro">
            <AppIcon name="alert-circle" :size="15" />
            {{ erro }}
          </p>

          <div class="usuario-manter__acoes">
            <button type="button" class="btn" :disabled="salvando" @click="voltar">Cancelar</button>
            <button type="submit" class="btn btn--primary" :disabled="salvando">
              <AppIcon v-if="salvando" name="loader" :size="15" class="usuario-manter__spinner" />
              {{ salvando ? 'Salvando...' : emEdicao ? 'Salvar alterações' : 'Cadastrar usuário' }}
            </button>
          </div>
        </form>
      </template>
    </div>
  </div>
</template>

<style scoped>
.usuario-manter {
  padding: 22px 28px 40px;
  display: flex;
  flex-direction: column;
  min-height: calc(100svh - 73px);
}

.usuario-manter__voltar {
  align-self: flex-start;
  margin-bottom: 18px;
}

.usuario-manter__card {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 8px clamp(0px, 5vw, 24px) 0;
}

.usuario-manter__header {
  padding-bottom: 24px;
  margin-bottom: 32px;
  border-bottom: 1px solid var(--border-hairline);
}

.usuario-manter__header h2 {
  font-size: 20px;
  font-weight: 700;
}

.usuario-manter__header p {
  margin-top: 6px;
  font-size: 13.5px;
  color: var(--text-muted);
}

.usuario-manter__state {
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

.usuario-manter__spinner {
  animation: usuario-manter-spin 0.8s linear infinite;
  color: var(--brand-500);
}

@keyframes usuario-manter-spin {
  to {
    transform: rotate(360deg);
  }
}

.usuario-manter__form {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  align-content: start;
  gap: 24px 28px;
}

.usuario-manter__campo {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

@media (max-width: 760px) {
  .usuario-manter__form {
    grid-template-columns: 1fr;
  }
}

.usuario-manter__campo label {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
}

.usuario-manter__campo input,
.usuario-manter__campo select {
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

.usuario-manter__campo input::placeholder {
  color: var(--text-muted);
}

.usuario-manter__campo input:focus,
.usuario-manter__campo select:focus {
  box-shadow: 0 0 0 2px var(--brand-100);
}

.usuario-manter__ajuda {
  font-size: 12.5px;
  color: var(--text-muted);
}

.usuario-manter__erro {
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

.usuario-manter__acoes {
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
