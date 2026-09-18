<script setup lang="ts">
defineProps<{ name: string; size?: number }>()
</script>

<template>
  <svg
    :width="size ?? 18"
    :height="size ?? 18"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    stroke-width="1.7"
    stroke-linecap="round"
    stroke-linejoin="round"
    aria-hidden="true"
  >
    <template v-if="name === 'menu'">
      <line x1="4" y1="7" x2="20" y2="7" />
      <line x1="4" y1="12" x2="20" y2="12" />
      <line x1="4" y1="17" x2="20" y2="17" />
    </template>

    <template v-else-if="name === 'dashboard'">
      <rect x="3" y="3" width="8" height="8" rx="1.6" />
      <rect x="13" y="3" width="8" height="8" rx="1.6" />
      <rect x="3" y="13" width="8" height="8" rx="1.6" />
      <rect x="13" y="13" width="8" height="8" rx="1.6" />
    </template>

    <template v-else-if="name === 'box'">
      <path d="M12 3 3 8v8l9 5 9-5V8z" />
      <polyline points="3 8 12 13 21 8" />
      <line x1="12" y1="13" x2="12" y2="21" />
    </template>

    <template v-else-if="name === 'users'">
      <circle cx="9" cy="8" r="3" />
      <path d="M4 19c0-3 2.4-5 5-5s5 2 5 5" />
      <circle cx="17" cy="9" r="2.3" />
      <path d="M15 19c0-2.2 1.6-4 3.5-4" />
    </template>

    <template v-else-if="name === 'user-circle'">
      <circle cx="12" cy="12" r="9" />
      <circle cx="12" cy="10" r="3" />
      <path d="M6.5 18.3c1-2.4 3-3.8 5.5-3.8s4.5 1.4 5.5 3.8" />
    </template>

    <template v-else-if="name === 'bag'">
      <path d="M6 8h12l1 12H5z" />
      <path d="M9 8V6a3 3 0 0 1 6 0v2" />
    </template>

    <template v-else-if="name === 'clipboard-check'">
      <rect x="6" y="4" width="12" height="17" rx="2" />
      <rect x="9" y="2" width="6" height="3" rx="1" />
      <polyline points="9 13 11 15 15 10.5" />
    </template>

    <template v-else-if="name === 'building'">
      <rect x="4" y="3" width="16" height="18" rx="1" />
      <rect x="7" y="6" width="2" height="2" />
      <rect x="15" y="6" width="2" height="2" />
      <rect x="7" y="11" width="2" height="2" />
      <rect x="15" y="11" width="2" height="2" />
      <rect x="10" y="16" width="4" height="5" />
    </template>

    <template v-else-if="name === 'shield'">
      <path d="M12 3l7 3v6c0 5-3.5 8-7 9-3.5-1-7-4-7-9V6z" />
      <polyline points="9 12 11 14 15 9.5" />
    </template>

    <template v-else-if="name === 'tag'">
      <path d="M2 11V4a2 2 0 0 1 2-2h7l11 11-9 9L2 11z" />
      <circle cx="7.5" cy="7.5" r="1.3" fill="currentColor" stroke="none" />
    </template>

    <template v-else-if="name === 'settings'">
      <line x1="4" y1="6" x2="20" y2="6" />
      <circle cx="9" cy="6" r="2" fill="currentColor" stroke="none" />
      <line x1="4" y1="12" x2="20" y2="12" />
      <circle cx="16" cy="12" r="2" fill="currentColor" stroke="none" />
      <line x1="4" y1="18" x2="20" y2="18" />
      <circle cx="11" cy="18" r="2" fill="currentColor" stroke="none" />
    </template>

    <template v-else-if="name === 'help-circle'">
      <circle cx="12" cy="12" r="9" />
      <path d="M9.5 9.2a2.5 2.5 0 1 1 3.6 2.25c-.85.42-1.1.9-1.1 1.75" />
      <line x1="12" y1="16.8" x2="12" y2="16.81" />
    </template>

    <template v-else-if="name === 'search'">
      <circle cx="11" cy="11" r="7" />
      <line x1="16.3" y1="16.3" x2="21" y2="21" />
    </template>

    <template v-else-if="name === 'bell'">
      <path d="M6 10a6 6 0 0 1 12 0v4l1.5 3h-15L6 14z" />
      <circle cx="12" cy="20" r="1.1" fill="currentColor" stroke="none" />
    </template>

    <template v-else-if="name === 'chevron-down'">
      <polyline points="6 9 12 15 18 9" />
    </template>

    <template v-else-if="name === 'chevron-right'">
      <polyline points="9 6 15 12 9 18" />
    </template>

    <template v-else-if="name === 'calendar'">
      <rect x="3" y="5" width="18" height="16" rx="2" />
      <line x1="8" y1="3" x2="8" y2="7" />
      <line x1="16" y1="3" x2="16" y2="7" />
      <line x1="3" y1="10" x2="21" y2="10" />
    </template>

    <template v-else-if="name === 'download'">
      <line x1="12" y1="3" x2="12" y2="15" />
      <polyline points="7 11 12 16 17 11" />
      <line x1="5" y1="20" x2="19" y2="20" />
    </template>

    <template v-else-if="name === 'arrow-up-right'">
      <line x1="7" y1="17" x2="17" y2="7" />
      <polyline points="9 7 17 7 17 15" />
    </template>

    <template v-else-if="name === 'arrow-down-right'">
      <line x1="7" y1="7" x2="17" y2="17" />
      <polyline points="17 9 17 17 9 17" />
    </template>

    <template v-else-if="name === 'dots'">
      <circle cx="5" cy="12" r="1.3" fill="currentColor" stroke="none" />
      <circle cx="12" cy="12" r="1.3" fill="currentColor" stroke="none" />
      <circle cx="19" cy="12" r="1.3" fill="currentColor" stroke="none" />
    </template>

    <template v-else-if="name === 'logout'">
      <path d="M9 4H5a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h4" />
      <polyline points="16 17 21 12 16 7" />
      <line x1="21" y1="12" x2="9" y2="12" />
    </template>

    <template v-else-if="name === 'mail'">
      <rect x="3" y="5" width="18" height="14" rx="2" />
      <polyline points="3 7 12 13 21 7" />
    </template>

    <template v-else-if="name === 'lock'">
      <rect x="5" y="11" width="14" height="10" rx="2" />
      <path d="M8 11V7a4 4 0 0 1 8 0v4" />
    </template>

    <template v-else-if="name === 'eye'">
      <path d="M2 12s3.6-7 10-7 10 7 10 7-3.6 7-10 7-10-7-10-7Z" />
      <circle cx="12" cy="12" r="3" />
    </template>

    <template v-else-if="name === 'eye-off'">
      <path d="M3 3l18 18" />
      <path d="M10.6 5.2A10.6 10.6 0 0 1 12 5c6.4 0 10 7 10 7a15.8 15.8 0 0 1-4.2 4.9M6.5 6.6C3.7 8.4 2 12 2 12s3.6 7 10 7c1.4 0 2.7-.3 3.8-.8" />
      <path d="M9.9 9.9a3 3 0 0 0 4.2 4.2" />
    </template>

    <template v-else-if="name === 'alert-circle'">
      <circle cx="12" cy="12" r="9" />
      <line x1="12" y1="8" x2="12" y2="13" />
      <line x1="12" y1="16.2" x2="12" y2="16.21" />
    </template>

    <template v-else-if="name === 'check-circle'">
      <circle cx="12" cy="12" r="9" />
      <polyline points="8 12.5 10.7 15 16 9.5" />
    </template>

    <template v-else-if="name === 'loader'">
      <line x1="12" y1="2" x2="12" y2="6" />
      <line x1="12" y1="18" x2="12" y2="22" />
      <line x1="4.9" y1="4.9" x2="7.8" y2="7.8" />
      <line x1="16.2" y1="16.2" x2="19.1" y2="19.1" />
      <line x1="2" y1="12" x2="6" y2="12" />
      <line x1="18" y1="12" x2="22" y2="12" />
      <line x1="4.9" y1="19.1" x2="7.8" y2="16.2" />
      <line x1="16.2" y1="7.8" x2="19.1" y2="4.9" />
    </template>
  </svg>
</template>
