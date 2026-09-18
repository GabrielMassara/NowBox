<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import AppSidebar from '../components/layout/AppSidebar.vue'
import AppTopbar from '../components/layout/AppTopbar.vue'

const route = useRoute()

const consultaEstreita = window.matchMedia('(max-width: 1080px)')
const telaEstreita = ref(consultaEstreita.matches)
const recolhidaManual = ref(false)

const recolhida = computed(() => recolhidaManual.value || telaEstreita.value)

function aoMudarLargura(evento: MediaQueryListEvent) {
  telaEstreita.value = evento.matches
}

onMounted(() => consultaEstreita.addEventListener('change', aoMudarLargura))
onBeforeUnmount(() => consultaEstreita.removeEventListener('change', aoMudarLargura))
</script>

<template>
  <div class="app-shell" :class="{ 'app-shell--recolhida': recolhida }">
    <AppSidebar :recolhida="recolhida" @alternar="recolhidaManual = !recolhidaManual" />
    <div class="app-shell__main">
      <AppTopbar :title="route.meta.title ?? ''" :subtitle="route.meta.subtitle" />
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

@media (max-width: 760px) {
  .app-shell,
  .app-shell--recolhida {
    grid-template-columns: 1fr;
  }
}
</style>
