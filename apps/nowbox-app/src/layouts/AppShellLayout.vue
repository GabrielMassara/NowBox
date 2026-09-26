<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import AppSidebar from '../components/layout/AppSidebar.vue'
import AppTopbar from '../components/layout/AppTopbar.vue'

const route = useRoute()

const consultaEstreita = window.matchMedia('(max-width: 1080px)')
const telaEstreita = ref(consultaEstreita.matches)
const recolhidaManual = ref(false)

const consultaMobile = window.matchMedia('(max-width: 760px)')
const telaMobile = ref(consultaMobile.matches)
const menuMobileAberto = ref(false)

const recolhida = computed(() => !telaMobile.value && (recolhidaManual.value || telaEstreita.value))

function aoMudarLargura(evento: MediaQueryListEvent) {
  telaEstreita.value = evento.matches
}

function aoMudarMobile(evento: MediaQueryListEvent) {
  telaMobile.value = evento.matches
  if (!evento.matches) menuMobileAberto.value = false
}

function aoAlternarSidebar() {
  if (telaMobile.value) menuMobileAberto.value = false
  else recolhidaManual.value = !recolhidaManual.value
}

function aoPressionarTecla(evento: KeyboardEvent) {
  if (evento.key === 'Escape') menuMobileAberto.value = false
}

watch(() => route.fullPath, () => (menuMobileAberto.value = false))

onMounted(() => {
  consultaEstreita.addEventListener('change', aoMudarLargura)
  consultaMobile.addEventListener('change', aoMudarMobile)
  document.addEventListener('keydown', aoPressionarTecla)
})
onBeforeUnmount(() => {
  consultaEstreita.removeEventListener('change', aoMudarLargura)
  consultaMobile.removeEventListener('change', aoMudarMobile)
  document.removeEventListener('keydown', aoPressionarTecla)
})
</script>

<template>
  <div class="app-shell" :class="{ 'app-shell--recolhida': recolhida }">
    <AppSidebar
      :recolhida="recolhida"
      :mobile="telaMobile"
      :aberta="menuMobileAberto"
      @alternar="aoAlternarSidebar"
    />
    <div
      v-if="telaMobile && menuMobileAberto"
      class="app-shell__backdrop"
      aria-hidden="true"
      @click="menuMobileAberto = false"
    ></div>
    <div class="app-shell__main">
      <AppTopbar
        :title="route.meta.title ?? ''"
        :subtitle="route.meta.subtitle"
        @abrir-menu="menuMobileAberto = true"
      />
      <router-view />
    </div>
  </div>
</template>

<style scoped>
.app-shell {
  display: grid;
  grid-template-columns: 260px 1fr;
  min-height: 100svh;
  background: var(--bg-page);
  transition: grid-template-columns 0.2s ease;
}

.app-shell--recolhida {
  grid-template-columns: 76px 1fr;
}

.app-shell__main {
  min-width: 0;
}

.app-shell__backdrop {
  position: fixed;
  inset: 0;
  z-index: 20;
  background: rgb(0 0 0 / 0.45);
}

@media (max-width: 760px) {
  .app-shell,
  .app-shell--recolhida {
    grid-template-columns: 1fr;
  }
}
</style>
