<script setup lang="ts">
import { computed } from 'vue'
import { NIcon } from 'naive-ui'
import {
  AddOutline,
  AlertCircleOutline,
  ArrowBackOutline,
  ArrowForwardOutline,
  BagHandleOutline,
  CallOutline,
  CaretBackOutline,
  CaretForwardOutline,
  CartOutline,
  CashOutline,
  CheckmarkCircleOutline,
  CheckmarkOutline,
  CloseCircleOutline,
  CloseOutline,
  CubeOutline,
  EyeOffOutline,
  EyeOutline,
  FileTrayOutline,
  FlashOutline,
  FolderOutline,
  FunnelOutline,
  GridOutline,
  HomeOutline,
  ImageOutline,
  LeafOutline,
  LocationOutline,
  LockClosedOutline,
  LockOpenOutline,
  LogOutOutline,
  MailOutline,
  NutritionOutline,
  PencilOutline,
  PersonOutline,
  PeopleOutline,
  PricetagOutline,
  QrCodeOutline,
  ReceiptOutline,
  RefreshOutline,
  RemoveOutline,
  ReturnDownForwardOutline,
  SearchOutline,
  SendOutline,
  Star,
  StarOutline,
  StorefrontOutline,
  SunnyOutline,
  SyncOutline,
  TrashOutline
} from '@vicons/ionicons5'

const props = withDefaults(defineProps<{
  name: string
  size?: string | number
  color?: string
}>(), { size: undefined, color: undefined })

const iconMap: Record<string, unknown> = {
  'arrow-down-right': ReturnDownForwardOutline,
  'arrow-left': ArrowBackOutline,
  'arrow-right': ArrowForwardOutline,
  bolt: FlashOutline,
  box: CubeOutline,
  'caret-left': CaretBackOutline,
  'caret-right': CaretForwardOutline,
  check: CheckmarkOutline,
  'check-circle': CheckmarkCircleOutline,
  envelope: MailOutline,
  'exclamation-circle': AlertCircleOutline,
  eye: EyeOutline,
  'eye-slash': EyeOffOutline,
  filter: FunnelOutline,
  folder: FolderOutline,
  home: HomeOutline,
  image: ImageOutline,
  inbox: FileTrayOutline,
  leaf: LeafOutline,
  lock: LockClosedOutline,
  'lock-open': LockOpenOutline,
  'map-marker': LocationOutline,
  minus: RemoveOutline,
  'money-bill': CashOutline,
  pencil: PencilOutline,
  phone: CallOutline,
  plus: AddOutline,
  qrcode: QrCodeOutline,
  receipt: ReceiptOutline,
  refresh: RefreshOutline,
  search: SearchOutline,
  send: SendOutline,
  shop: StorefrontOutline,
  'shopping-bag': BagHandleOutline,
  'shopping-cart': CartOutline,
  'sign-out': LogOutOutline,
  spinner: SyncOutline,
  star: StarOutline,
  'star-fill': Star,
  sun: SunnyOutline,
  sync: SyncOutline,
  tag: PricetagOutline,
  'th-large': GridOutline,
  times: CloseOutline,
  'times-circle': CloseCircleOutline,
  trash: TrashOutline,
  user: PersonOutline,
  users: PeopleOutline,
  apple: NutritionOutline
}

const icon = computed(() => iconMap[props.name])
</script>

<template>
  <NIcon :size="size" :color="color">
    <component :is="icon" />
  </NIcon>
</template>