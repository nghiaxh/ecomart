export interface HomeStat {
  value: string
  label: string
}

export interface HomeFeature {
  icon: string
  title: string
  desc: string
  photo: string
}

export interface HomeStep {
  title: string
  desc: string
  photo: string
}

export interface HomeTestimonial {
  name: string
  role: string
  initial: string
  content: string
}

export const homeStats: HomeStat[] = [
  { value: '100%', label: 'Nguồn gốc rõ ràng' },
  { value: '5,000+', label: 'Sản phẩm đa dạng' },
  { value: '12,000', label: 'Khách hàng tin dùng' }
]

export function homeFeatures(supportPhone: string): HomeFeature[] {
  return [
    { icon: 'i-ph-leaf', title: 'Sạch & an toàn', desc: 'Sản phẩm có nguồn gốc rõ ràng, đảm bảo vệ sinh thực phẩm.', photo: '/images/feature-safe.jpg' },
    { icon: 'i-ph-truck', title: 'Giao hàng nhanh', desc: 'Nội thành nhận hàng trong ngày, toàn quốc 2-4 ngày.', photo: '/images/feature-truck.jpg' },
    { icon: 'i-ph-wallet', title: 'Thanh toán linh hoạt', desc: 'COD hoặc quét mã QR qua PayOS tiện lợi.', photo: '/images/feature-pay.jpg' },
    { icon: 'i-ph-headset', title: 'Hỗ trợ tận tâm', desc: `Hotline ${supportPhone} hỗ trợ 8h-20h mỗi ngày.`, photo: '/images/feature-support.jpg' }
  ]
}

export const homeSteps: HomeStep[] = [
  { title: 'Chọn sản phẩm', desc: 'Duyệt danh mục và chọn món bạn cần.', photo: '/images/step-browse.jpg' },
  { title: 'Thêm vào giỏ', desc: 'Đặt số lượng và thêm vào giỏ hàng.', photo: '/images/step-cart.jpg' },
  { title: 'Thanh toán', desc: 'Chọn COD hoặc quét mã QR PayOS.', photo: '/images/step-pay.jpg' },
  { title: 'Nhận hàng', desc: 'Nhận hàng tận nơi, kiểm tra và thưởng thức.', photo: '/images/step-delivery.jpg' }
]

export const homeTestimonials: HomeTestimonial[] = [
  { name: 'Chị Thu Hà', role: 'Quận 7, TP.HCM', initial: 'TH', content: 'Rau củ luôn tươi, đóng gói cẩn thận. Giao hàng đúng giờ, tôi đặt hằng tuần cho cả gia đình.' },
  { name: 'Anh Minh Quang', role: 'Đà Nẵng', initial: 'MQ', content: 'Đặt xoài và trái cây giờ chỉ quen EcoMart. Giá hợp lý, chất lượng ổn định, thanh toán rất dễ.' },
  { name: 'Chị Ngọc Anh', role: 'Thanh Xuân, Hà Nội', initial: 'NA', content: 'Nhân viên hỗ trợ nhanh và thân thiện. Có lần giao hàng bị sai sót, bên mình đổi ngay trong ngày.' }
]

export const homeAboutPoints: string[] = [
  'Sản phẩm đa dạng, nguồn gốc rõ ràng',
  'Giao hàng nhanh, thanh toán linh hoạt',
  'Chăm sóc khách hàng tận tâm',
  'Đóng gói thân thiện môi trường'
]

const categoryImageMap: Record<string, string> = {
  'rau-cu-sach': '/images/cat-vegetables.jpg',
  'trai-cay-tuoi': '/images/cat-fruits.jpg',
  'thuc-pham-kho': '/images/cat-grains.jpg',
  'rau-cu': '/images/cat-vegetables.jpg',
  'trai-cay': '/images/cat-fruits.jpg',
  'ngu-co': '/images/cat-grains.jpg',
  'thit-ca': '/images/cat-meat.jpg',
  vegetables: '/images/cat-vegetables.jpg',
  fruits: '/images/cat-fruits.jpg',
  grains: '/images/cat-grains.jpg',
  'meat-seafood': '/images/cat-meat.jpg'
}

const fallbackCategoryImages = [
  '/images/cat-vegetables.jpg',
  '/images/cat-fruits.jpg',
  '/images/cat-grains.jpg',
  '/images/cat-meat.jpg'
]

export function categoryImage(slug: string, index: number): string {
  return categoryImageMap[slug] || fallbackCategoryImages[index % fallbackCategoryImages.length]
}
