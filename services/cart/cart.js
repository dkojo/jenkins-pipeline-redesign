// The cart logic and the HTTP handler, with no dependencies.
export const CART = [{ sku: 'KB-01', quantity: 1 }, { sku: 'HS-03', quantity: 2 }]

export const itemCount = (cart) => cart.reduce((n, line) => n + line.quantity, 0)

export function handle(req, res) {
  if (req.url === '/cart/health') {
    res.writeHead(200, { 'Content-Type': 'text/plain' }).end('ok')
  } else if (req.url === '/cart/items') {
    res.writeHead(200, { 'Content-Type': 'application/json' }).end(JSON.stringify({ items: CART, count: itemCount(CART) }))
  } else {
    res.writeHead(404).end()
  }
}
