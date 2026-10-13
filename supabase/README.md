# DZ Delivery Pro — Supabase Backend & Database Architecture

## Overview
**DZ Delivery Pro** is a full-stack on-demand delivery platform tailored specifically for the Algerian market (58 Wilayas, DZD currency, Arabic RTL and French LTR). The backend and PostgreSQL database are powered by **Supabase**.

---

## 1. Directory Structure

```
supabase/
├── migrations/
│   ├── 20261013000001_initial_schema.sql       # 30+ relational tables with foreign keys and constraints
│   ├── 20261013000002_rls_policies.sql         # Row Level Security (RLS) policies for all tables
│   ├── 20261013000003_functions_triggers.sql  # Order state machine transitions, concurrency lock, OTP verification
│   └── 20261013000004_seed_data.sql            # Seed Algerian merchants, products, wilayas, and demo users
├── functions/
│   ├── create-order/index.ts                  # Edge function: Server-side pricing & coupon validation
│   ├── assign-driver/index.ts                 # Edge function: Row-level lock driver assignment
│   ├── update-order-status/index.ts           # Edge function: State transitions & OTP check
│   └── payment-webhook/index.ts               # Edge function: Edahabia / BaridiMob / CIB webhook handler
├── types/
│   └── database.types.ts                      # Generated TypeScript types from PostgreSQL schema
└── README.md                                  # Setup, deployment, and security guide
```

---

## 2. Supabase Setup & Local Development

### Prerequisites
- [Supabase CLI](https://supabase.com/docs/guides/cli) installed (`npm install -g supabase` or `brew install supabase/tap/supabase`)
- Docker Desktop running (for local PostgreSQL instance)

### Local Development Quickstart
1. **Initialize and start local Supabase instance**:
   ```bash
   supabase start
   ```
   This provisions:
   - PostgreSQL database on `localhost:54322`
   - Supabase Studio UI on `http://localhost:54323`
   - Supabase REST API & Auth on `http://localhost:54321`

2. **Apply migrations and seed data**:
   ```bash
   supabase db reset
   ```
   This automatically runs migrations in alphabetical order:
   - `20261013000001_initial_schema.sql`
   - `20261013000002_rls_policies.sql`
   - `20261013000003_functions_triggers.sql`
   - `20261013000004_seed_data.sql`

3. **Deploy Edge Functions**:
   ```bash
   supabase functions deploy create-order
   supabase functions deploy assign-driver
   supabase functions deploy update-order-status
   supabase functions deploy payment-webhook
   ```

---

## 3. Production Deployment to Supabase Cloud

1. **Link your Supabase Cloud project**:
   ```bash
   supabase link --project-ref YOUR_PROJECT_REF
   ```

2. **Push migrations to production**:
   ```bash
   supabase db push
   ```

3. **Deploy Edge Functions**:
   ```bash
   supabase functions deploy --project-ref YOUR_PROJECT_REF
   ```

4. **Configure Secrets**:
   ```bash
   supabase secrets set PAYMENT_WEBHOOK_SECRET=your_production_secret
   ```

5. **Configure Android Client**:
   In AI Studio Secrets or `.env`:
   ```env
   SUPABASE_URL=https://YOUR_PROJECT_REF.supabase.co
   SUPABASE_ANON_KEY=your_supabase_anon_public_key
   ```

---

## 4. Security & Row Level Security (RLS) Model

- **Zero Trust**: `USING (true)` is strictly prohibited on private application tables.
- **Customer Privacy**: Customers can only query their own profiles, addresses, and orders.
- **Driver Scoping**: Drivers only access their assigned deliveries and location updates.
- **Merchant Isolation**: Merchants can only read and manage orders placed at their specific establishment.
- **Administrative Access**: Admin operations are guarded by `public.is_admin()`, verifying explicit database-level roles.

---

## 5. Order State Machine Transitions

| Previous Status | Permitted New Statuses | Responsible Actor |
|---|---|---|
| `pending` | `confirmed`, `cancelled` | Merchant / Customer |
| `confirmed` | `preparing`, `cancelled` | Merchant |
| `preparing` | `ready_for_pickup`, `cancelled` | Merchant |
| `ready_for_pickup` | `assigned`, `cancelled` | Driver (Atomic lock) / Admin |
| `assigned` | `driver_to_pickup`, `picked_up`, `cancelled` | Driver |
| `picked_up` | `driver_to_customer` | Driver |
| `driver_to_customer` | `delivered` (Requires 4-digit OTP), `failed` | Driver + Customer OTP |
