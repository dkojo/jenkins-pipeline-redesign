import { createServer } from 'node:http'
import { handle } from './cart.js'

createServer(handle).listen(process.env.PORT || 8080)
