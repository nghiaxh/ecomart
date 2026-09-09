# Migration Plan: PrimeVue → Naive UI (EcoMart client)

Target: replace PrimeVue 4.5 (Aura preset) with Naive UI using the **native light theme** (no `theme-overrides`), purge custom color tokens to stock Naive defaults, and migrate the icon set from PrimeIcons (`pi pi-*`) to xicons (`@vicons/ionicons5`).

> **Status: DONE** — implemented Mar 9, 2026. All PrimeVue usages removed from `client/src`; `primevue`/`@primevue/themes`/`primeicons` uninstalled. Verified: `npm run typecheck`, `npm test` (50/50), `npm run build` all green. See the sections below for the reference implementation.

## Decisions locked in

- **Icons:** xicons (`@vicons/ionicons5`) behind a local `UiIcon.vue` wrapper.
- **Theme:** native Naive **light** theme, plain `<NConfigProvider>` — no `theme` prop, no `theme-overrides`.
- **Colors:** full purge of the custom green-tinted palette in `client/src/assets/css/main.css` → stock Naive tokens.
- **Scope:** full single-pass migration; strip `primevue` dependencies at the end.

## 1. Dependencies

- Add: `naive-ui`, `@vicons/ionicons5` (MIT, covers all 47 distinct icons in use).
- Remove: `primevue`, `@primevue/themes`, `primeicons`.
- No `@vicons/utils` needed — Naive's `<NIcon>` is the icon wrapper.
- No Vite config changes; Naive is CSS-in-JS (`css-render`), no CSS import required, coexists with Tailwind 4.

## 2. Global wiring

### `client/src/main.ts`

Delete the PrimeVue imports/setup — they become unused:

- `import PrimeVue from 'primevue/config'`
- `import Aura from '@primevue/themes/aura'`
- `import ToastService from 'primevue/toastservice'`
- `import ConfirmationService from 'primevue/confirmationservice'`
- `import 'primeicons/primeicons.css'`
- `app.use(PrimeVue, {...})`, `app.use(ToastService)`, `app.use(ConfirmationService)`

Naive UI registers nothing at the app level; all components/hooks are per-file imports (matches the project's no-auto-import convention).

### `client/src/App.vue`

Replace the `<Toast position="top-center" />` + `<ConfirmDialog />` mounts and wrap the router:

```vue
<NConfigProvider :locale="enUS" :date-locale="dateEnUS">
  <NMessageProvider placement="top" :max="3">
    <NDialogProvider>
      <NGlobalStyle />
      <RouterView />
    </NDialogProvider>
  </NMessageProvider>
</NConfigProvider>
```

- Light theme is the Naive default; passing `lightTheme`/`theme` is not required.
- `locale`/`date-locale` = `enUS` defaults to Chinese otherwise (Naive has no Vietnamese locale; all app-authored text stays Vietnamese).
- Toast moves from top-center → top (Naive message has no center placement).
- `NGlobalStyle` applies the theme's font/color base styles.

### `client/src/assets/css/main.css` — color purge (stock tokens)

Delete / neutralise the PrimeVue-compensation colors:

- **Delete** `[role="alert"][aria-busy="true"] { background-color: #eef1ef }` — was a PrimeVue Skeleton workaround; `NSkeleton` handles its own tone.
- **Body:** drop `background-color: #f8faf9` and `color: #33423a`; keep `color-scheme: light`, Inter `font-family`, `scrollbar-gutter`, `-webkit-font-smoothing`.
- **Custom scrollbar** `#c8d8c9` / `#9fbfa1` → remove the color block (native browser scrollbar).
- **`.cat-card:hover`** green shadow `rgba(5, 150, 105, 0.18)` → remove; keep the `translateY` hover.
- **Keep:** reveal animation, cursor rules, `.cat-card` transition, `.text-balance`, `@theme { --font-sans }`.

Untouched (app data, not UI-theme): `AuthShell.vue` backdrop `#065f46`, `admin/statistic.vue` Chart.js colors.

## 3. Composables (minimal churn — call sites unchanged)

### New `client/src/composables/useToast.ts`

Back PrimeVue's `toast.add(...)` shape with `useMessage` so all 45 existing call sites keep working. Only the import line changes across files:

```ts
import { useMessage } from 'naive-ui'

export const useToast = () => {
  const message = useMessage()

  const add = (opts: { severity?: 'success' | 'info' | 'warn' | 'error'; summary: string; detail?: string; life?: number }) => {
    const content = opts.detail ? `${opts.summary} — ${opts.detail}` : opts.summary
    const duration = opts.life ?? 4000
    switch (opts.severity) {
      case 'error': return message.error(content, { duration })
      case 'success': return message.success(content, { duration })
      case 'warn': return message.warning(content, { duration })
      default: return message.info(content, { duration })
    }
  }

  return { add }
}
```

### `client/src/composables/useConfirm.ts`

Rewrite on `useDialog` with the same `confirm(message, title?, options?) => Promise<boolean>` signature (5 call sites untouched):

```ts
import { useDialog } from 'naive-ui'

export const useConfirm = () => {
  const dialog = useDialog()

  const confirm = (message: string, title = 'Xác nhận', options?: { confirmLabel?: string; cancelLabel?: string }): Promise<boolean> => {
    return new Promise<boolean>((resolve) => {
      dialog.warning({
        title,
        content: message,
        positiveText: options?.confirmLabel ?? 'Xác nhận',
        negativeText: options?.cancelLabel ?? 'Hủy',
        onPositiveClick: () => { resolve(true); return true },
        onNegativeClick: () => { resolve(false); return true },
        onClose: () => resolve(false),
        onMaskClick: () => resolve(false),
        onEsc: () => resolve(false)
      })
    })
  }

  return { confirm }
}
```

### `client/src/composables/useStatusLabels.ts`

Replace `badgeSeverity` (PrimeVue `Severity`) with `badgeType` mapping `BadgeColor` → `NTag` `type`:

- `primary → 'primary'`, `info → 'info'`, `success → 'success'`, `warning → 'warning'`, `error → 'error'`, `secondary / neutral → 'default'`.

Consumers change `:severity="badgeSeverity(x)"` → `:type="badgeType(x)"`.

## 4. Icons

- New `client/src/components/UiIcon.vue`: `name` prop → Ionicons5 component rendered inside `<NIcon>`. Centralises the mapping (explicit imports, no unplugin).
- Sweep all 109 `pi pi-*` references (23 files, 47 distinct icons) as part of each file's migration:
  - `<i class="pi pi-*">` → `<UiIcon name="..." />`
  - Button `icon="pi pi-*"` / `:icon` → `#icon` slot with `<UiIcon>`
  - `icon-pos="right"` → reorder children (icon after label)
- Dynamic icon-name maps become component maps:
  - `admin/categories.vue` (`pi-${icon}` per slug)
  - `products/[slug].vue` (material icons)
  - `admin/statistic.vue` (stat card icons)
  - `PasswordInput.vue` (eye / eye-slash toggle)

## 5. Component map

| PrimeVue | Naive UI | Key changes |
|---|---|---|
| `Button` | `NButton` | `severity="primary"` → `type="primary"`; `"secondary"` → `tertiary`; `"danger"` → `type="error"` + `secondary`; `text` → `quaternary`; `outlined` → `secondary`; drop `icon-pos` (reorder children); `:loading`/`:disabled`/`size` unchanged |
| `InputText` | `NInput` | `v-model` → `v-model:value`; `size="large"` → `size="large"` |
| `PasswordInput` | `NInput type="password"` | keep local wrapper; eye toggle via `@suffix`/prefix + `UiIcon` |
| `InputNumber` | `NInputNumber` | `:min-fraction-digits="0"` → `:precision="0"`; drop `mode="decimal"`; `input-class` → wrapper class |
| `Textarea` | `NInput type="textarea"` | `rows` → `:autosize` |
| `Select` | `NSelect` | drop `option-label`/`option-value` (`{label, value}` options); `@update:model-value` → `@update:value` |
| `Checkbox` (binary) | `NCheckbox` | `v-model:checked` (native boolean); `input-id` → slot label |
| `Tag` | `NTag` | `:value` → default slot; `severity` → `:type` via `badgeType` |
| `Avatar` | `NAvatar` | `:image` → `:src`; `size` remapped; label fallback via slot |
| `Skeleton` | `NSkeleton` | `width` → class/style |
| `DataTable` + `Column` | `NDataTable` | **biggest refactor** — `columns` prop with `h()` render functions (3 admin pages); `stripedRows` → `:striped`; keep `#empty` / `#loading` slots |
| `Dialog` | `NModal preset="card"` | `v-model:visible` → `v-model:show`; `:header` → `title`; keep `#footer` |
| `Carousel` | `NCarousel` | `:autoplay-interval="5000"` → `autoplay` + `:interval="5000"`; `:circular` → `:loop`; `:show-navigators` → `:show-arrow`; `:show-indicators` → `:show-dots` |
| `<Toast position="top-center">` | `NMessageProvider` | `placement="top"` (no center placement) |
| `<ConfirmDialog>` | `NDialogProvider` + `useDialog` | replaced by the `useConfirm` composable |

### DataTable rewrite shape (admin pages)

`admin/users.vue`, `admin/categories.vue`, `admin/products.vue`: move template `#body` cells into a `columns` array:

```ts
const columns: DataTableColumns<Product> = [
  { title: 'Sản phẩm', render: (row) => h('div', { class: 'flex items-center gap-3' }, [h(UiImg, ...), h('span', row.name)]) },
  { title: 'Giá', render: (row) => h('span', { class: 'tabular-nums' }, formatVND(row.price)) },
  { title: 'Trạng thái', render: (row) => h(NTag, { type: row.active ? 'success' : 'default' }, { default: () => row.active ? 'Bán' : 'Ẩn' }) },
  { title: 'Thao tác', align: 'right', render: (row) => h('div', { class: 'flex justify-end gap-1' }, [h(NButton, { onClick: ... }, ...)]) }
]
```

## 6. Execution phases (app stays buildable)

1. **Providers + composables:** install deps; new `App.vue` providers; create `useToast.ts`, rewrite `useConfirm.ts`, update `useStatusLabels.ts`; migrate `useCart.ts` + all 14 `useToast` imports; drop Toast/ConfirmDialog mounts.
2. **Shared components:** `PaginationBar`, `PasswordInput`, `AddressForm`, `SectionHeader`, `AddToCartButton`; create `UiIcon.vue`.
3. **User-facing pages (13):** `layouts/default`, `login`, `register`, `index` (Carousel), `products/index`, `products/[slug]`, `cart`, `checkout`, `payment-result`, `orders/index`, `orders/[id]`, `account`, plus `admin/statistic.vue` (Skeleton + icons).
4. **Admin pages (3):** `users`, `categories`, `products` — DataTable `columns` + `h()` rewrite, `NModal preset="card"` forms, dynamic icon maps.
5. **Removal + verify:** remove `primevue`, `@primevue/themes`, `primeicons`; repoint the 2 test mocks from `vi.mock('primevue/usetoast')` to `vi.mock('@/composables/useToast')` (`useCart.test.ts`, `products/index.test.ts`); add a Vitest setup stub for `ResizeObserver`/`matchMedia` if happy-dom requires it; run `npm run typecheck`, `npm test`, `npm run build`.

## 7. Risks / deltas

- **DataTable** is the only non-mechanical rewrite (render functions instead of template slots).
- `useMessage` / `useDialog` require their providers — mitigated by the composable wrappers, which also keep unit tests mockable.
- Visual shifts (intended): white body background, native scrollbar, stock Naive green primary `#18a058`, toasts pinned to the top; Aura paddings/radii replaced by Naive light defaults.
- Naive built-in strings become English (zhCN avoided via `enUS`); all app-authored text remains Vietnamese.