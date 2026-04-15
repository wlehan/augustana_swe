import { test, expect } from '@playwright/test'

test('user can sign up and then log in', async ({ page }) => {
  const uniqueUser = `user${Date.now()}`
  const password = 'verysecurepass123'
  const email = `${uniqueUser}@test.com`

  await page.goto('/signup')

  await page.getByLabel('Username').fill(uniqueUser)
  await page.getByLabel('Email (optional)').fill(email)
  await page.getByLabel('Password').fill(password)
  await page.getByRole('button', { name: /create account/i }).click()

  await expect(page).toHaveURL(/game-selection/)

  await page.evaluate(() => localStorage.clear())

  await page.goto('/login')
  await page.getByLabel('Username').fill(uniqueUser)
  await page.getByLabel('Password').fill(password)
  await page.getByRole('button', { name: /go!/i }).click()

  await expect(page).toHaveURL(/game-selection/)
})

test('shows error for invalid login', async ({ page }) => {
  await page.goto('/login')

  await page.getByLabel('Username').fill('notARealUser')
  await page.getByLabel('Password').fill('wrongpassword123')
  await page.getByRole('button', { name: /go!/i }).click()

  await expect(page.getByText(/invalid username or password/i)).toBeVisible()
})

test('duplicate username signup shows error', async ({ page }) => {
  const uniqueUser = `user${Date.now()}`
  const password = 'verysecurepass123'
  const email = `${uniqueUser}@test.com`

  await page.goto('/signup')
  await page.getByLabel('Username').fill(uniqueUser)
  await page.getByLabel('Email (optional)').fill(email)
  await page.getByLabel('Password').fill(password)
  await page.getByRole('button', { name: /create account/i }).click()

  await expect(page).toHaveURL(/game-selection/)

  await page.goto('/signup')
  await page.getByLabel('Username').fill(uniqueUser)
  await page.getByLabel('Email (optional)').fill(`second-${email}`)
  await page.getByLabel('Password').fill(password)
  await page.getByRole('button', { name: /create account/i }).click()

  await expect(page.getByText(/username already exists/i)).toBeVisible()
})

test('short password signup shows error', async ({ page }) => {
  const uniqueUser = `user${Date.now()}`

  await page.goto('/signup')
  await page.getByLabel('Username').fill(uniqueUser)
  await page.getByLabel('Email (optional)').fill(`${uniqueUser}@test.com`)
  await page.getByLabel('Password').fill('short')
  await page.getByRole('button', { name: /create account/i }).click()

  await expect(page.getByText(/password must be at least 12 characters/i)).toBeVisible()
})

test('blank username login shows error', async ({ page }) => {
  await page.goto('/login')

  await page.getByLabel('Password').fill('wrongpassword123')
  await page.getByRole('button', { name: /go!/i }).click()

  await expect(page.getByText(/username is required/i)).toBeVisible()
})

test('blank password login shows error', async ({ page }) => {
  await page.goto('/login')

  await page.getByLabel('Username').fill('someuser')
  await page.getByRole('button', { name: /go!/i }).click()

  await expect(page.getByText(/password is required/i)).toBeVisible()
})

test('blank email signup still works', async ({ page }) => {
  const uniqueUser = `user${Date.now()}`
  const password = 'verysecurepass123'

  await page.goto('/signup')
  await page.getByLabel('Username').fill(uniqueUser)
  await page.getByLabel('Password').fill(password)
  await page.getByRole('button', { name: /create account/i }).click()

  await expect(page).toHaveURL(/game-selection/)
})

test('homepage buttons navigate correctly', async ({ page }) => {
  await page.goto('/')

  await page.getByRole('button', { name: /log in/i }).click()
  await expect(page).toHaveURL(/\/login$/)

  await page.goto('/')

  await page.getByRole('button', { name: /create an account/i }).click()
  await expect(page).toHaveURL(/\/signup$/)
})