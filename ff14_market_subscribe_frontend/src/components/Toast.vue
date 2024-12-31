<template>
    <transition name="toast">
        <div v-if="visible" class="toast" :class="type">
            {{ message }}
        </div>
    </transition>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

export default defineComponent({
    name: 'Toast',
    props: {
        visible: {
            type: Boolean,
            required: true
        },
        message: {
            type: String,
            required: true
        },
        type: {
            type: String,
            default: 'success'
        }
    }
});
</script>

<style scoped>
.toast {
    position: fixed;
    top: 20px;
    left: 50%;
    transform: translateX(-50%);
    padding: 10px 20px;
    border-radius: 4px;
    color: white;
    font-size: 14px;
    z-index: 1000;
}

.success {
    background-color: #4caf50;
}

.error {
    background-color: #f44336;
}

.toast-enter-active,
.toast-leave-active {
    transition: all 0.3s ease;
}

.toast-enter-from,
.toast-leave-to {
    opacity: 0;
    transform: translateX(-50%) translateY(-20px);
}
</style>