#include <stdint.h>

// Stub definitions for Compose $artificial stability symbols that navigation-compose klib
// references but compose-runtime never defines for Kotlin/Native static frameworks.
// KMP 2.2 / Compose 1.8 bug: https://youtrack.jetbrains.com/issue/KT-XXXXX
// Returns 0 (unstable) — affects composable skipping optimisation only, never correctness.

__attribute__((used))
static int32_t kfun_SeekableTransitionState_artificial(void)
    __asm__("_kfun:androidx.compose.animation.core#androidx_compose_animation_core_SeekableTransitionState$stableprop_getter$artificial(){}kotlin.Int");
__attribute__((used))
static int32_t kfun_SeekableTransitionState_artificial(void) { return 0; }

__attribute__((used))
static int32_t kfun_ProvidedValue_artificial(void)
    __asm__("_kfun:androidx.compose.runtime#androidx_compose_runtime_ProvidedValue$stableprop_getter$artificial(){}kotlin.Int");
__attribute__((used))
static int32_t kfun_ProvidedValue_artificial(void) { return 0; }
