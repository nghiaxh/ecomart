import { test as base, type Page } from '@playwright/test'
import {
  loginAsNewCustomer,
  makeUniqueUser,
  setAuthStorage,
  gotoReady,
  loginViaAPI,
  SEED_ADMIN,
  type AuthPayload,
  type TestUser
} from './helpers'

interface CustomerFixtures {
  newCustomer: TestUser
  customerAuth: AuthPayload
  authedPage: Page
}

interface AdminFixtures {
  adminAuth: AuthPayload
}

/**
 * Isolated customer per test: registers a unique user via API, injects the
 * JWT session into `authedPage` before navigation. Write-flows should use
 * `authedPage` + `newCustomer` instead of the shared seed account.
 */
export const test = base.extend<CustomerFixtures & AdminFixtures>({
  newCustomer: async ({ request }, use) => {
    await use(makeUniqueUser())
  },
  customerAuth: async ({ request, newCustomer }, use) => {
    const user = newCustomer
    const res = await request.post(
      `${process.env.E2E_API_BASE || 'http://localhost:8080'}/api/auth/register`,
      { data: user }
    )
    if (!res.ok()) {
      throw new Error(`fixture register failed: ${res.status()} ${await res.text()}`)
    }
    await use((await res.json()) as AuthPayload)
  },
  authedPage: async ({ page, customerAuth }, use) => {
    await setAuthStorage(page, customerAuth)
    await use(page)
  },
  adminAuth: async ({ request }, use) => {
    const auth = await loginViaAPI(request, SEED_ADMIN.identifier, SEED_ADMIN.password)
    await use(auth)
  }
})

export { expect } from '@playwright/test'
export { loginAsNewCustomer, gotoReady }

