const { chromium } = require('playwright');

(async () => {
  const browser = await chromium.launch();
  const page = await browser.newPage();
  
  page.on('request', req => {
    if (req.url().includes('products') || req.url().includes('auth')) console.log('REQ:', req.method(), req.url());
  });
  page.on('response', res => {
    if (res.url().includes('products') || res.url().includes('auth')) console.log('RES:', res.status(), res.url());
  });
  page.on('console', msg => {
    if (msg.type() === 'error') console.log('CONSOLE ERROR:', msg.text().substring(0, 300));
  });
  
  // Login first
  await page.goto('http://127.0.0.1:5173/login');
  await page.waitForTimeout(1000);
  await page.locator('#login-identifier').fill('customer');
  await page.locator('#login-password').fill('customer123');
  await page.getByRole('button', { name: 'Đăng nhập' }).click();
  await page.waitForTimeout(3000);
  console.log('After login URL:', page.url());
  
  // Go to products
  await page.goto('http://127.0.0.1:5173/products');
  await page.waitForTimeout(8000);
  console.log('After products URL:', page.url());
  
  // Check all links on page
  const allLinks = await page.locator('a').all();
  console.log('Total links on page:', allLinks.length);
  for (const link of allLinks.slice(0, 20)) {
    const href = await link.getAttribute('href');
    const text = await link.textContent();
    if (href && href.includes('products')) {
      console.log('  Product link:', href, text?.substring(0, 30));
    }
  }
  
  // Check for ProductCard
  const productCards = await page.locator('.group.block').count();
  console.log('ProductCard-like elements:', productCards);
  
  // Get page HTML snippet
  const content = await page.content();
  const idx = content.indexOf('Rau');
  if (idx >= 0) {
    console.log('Found "Rau" at:', idx);
    console.log('Context:', content.substring(Math.max(0, idx-100), idx+200));
  }
  
  await browser.close();
})();