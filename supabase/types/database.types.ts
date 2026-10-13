export type Json =
  | string
  | number
  | boolean
  | null
  | { [key: string]: Json | undefined }
  | Json[]

export type OrderStatus =
  | 'pending'
  | 'confirmed'
  | 'preparing'
  | 'ready_for_pickup'
  | 'assigned'
  | 'driver_to_pickup'
  | 'picked_up'
  | 'driver_to_customer'
  | 'delivered'
  | 'cancelled'
  | 'failed'

export type UserRole =
  | 'customer'
  | 'driver'
  | 'merchant'
  | 'admin'
  | 'support_agent'

export type PaymentMethod =
  | 'cash_on_delivery'
  | 'edahabia_baridimob'
  | 'cib_card'

export interface Database {
  public: {
    Tables: {
      profiles: {
        Row: {
          id: string
          phone: string
          full_name: string
          avatar_url: string | null
          preferred_language: 'ar' | 'fr'
          created_at: string
          updated_at: string
        }
        Insert: {
          id: string
          phone: string
          full_name: string
          avatar_url?: string | null
          preferred_language?: 'ar' | 'fr'
          created_at?: string
          updated_at?: string
        }
        Update: {
          phone?: string
          full_name?: string
          avatar_url?: string | null
          preferred_language?: 'ar' | 'fr'
          updated_at?: string
        }
      }
      user_roles: {
        Row: {
          id: string
          user_id: string
          role: UserRole
          created_at: string
        }
        Insert: {
          id?: string
          user_id: string
          role?: UserRole
          created_at?: string
        }
        Update: {
          role?: UserRole
        }
      }
      merchants: {
        Row: {
          id: string
          owner_id: string | null
          category_id: string | null
          service_area_id: string | null
          name_ar: string
          name_fr: string
          address: string
          rating: number
          prep_time_minutes: number
          delivery_fee: number
          min_order_amount: number
          is_open: boolean
          is_verified: boolean
          tag_ar: string | null
          tag_fr: string | null
          created_at: string
          updated_at: string
        }
      }
      products: {
        Row: {
          id: string
          merchant_id: string
          name_ar: string
          name_fr: string
          desc_ar: string | null
          desc_fr: string | null
          price: number
          category_name: string | null
          image_url: string | null
          is_available: boolean
          is_popular: boolean
          created_at: string
          updated_at: string
        }
      }
      orders: {
        Row: {
          id: string
          order_number: string
          customer_id: string | null
          customer_phone: string
          customer_name: string
          customer_address: string
          wilaya_code: string
          merchant_id: string
          merchant_name: string
          driver_id: string | null
          driver_name: string | null
          driver_phone: string | null
          driver_vehicle: string | null
          status: OrderStatus
          subtotal: number
          delivery_fee: number
          discount: number
          total: number
          payment_method: PaymentMethod
          payment_status: string
          notes: string | null
          otp_code: string
          created_at: string
          updated_at: string
        }
      }
      order_items: {
        Row: {
          id: string
          order_id: string
          product_id: string | null
          product_name_ar: string
          product_name_fr: string
          unit_price: number
          quantity: number
          created_at: string
        }
      }
      driver_profiles: {
        Row: {
          id: string
          user_id: string
          vehicle_type: string
          wilaya_code: string
          is_online: boolean
          is_approved: boolean
          current_latitude: number | null
          current_longitude: number | null
          rating: number
          total_deliveries: number
          today_earnings: number
          created_at: string
          updated_at: string
        }
      }
      service_areas: {
        Row: {
          id: string
          wilaya_code: string
          name_ar: string
          name_fr: string
          base_delivery_fee: number
          is_active: boolean
          created_at: string
        }
      }
      coupons: {
        Row: {
          id: string
          code: string
          discount_amount: number
          min_order_amount: number
          expires_at: string | null
          is_active: boolean
          created_at: string
        }
      }
    }
  }
}
