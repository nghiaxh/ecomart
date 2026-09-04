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
  await page.waitForTimeout(5000);
  console.log('After products URL:', page.url());
  
  const links = await page.locator('a[href^="/products/"]').count();
  console.log('Product links found:', links);
  
  // Check page content
  const content = await page.content();
  console.log('Page has products:', content.includes('ProductCard') || content.includes('product') || content.includes('Rau'));
  
  await browser.close();
})();