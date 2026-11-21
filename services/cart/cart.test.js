import { test } from 'node:test'
import assert from 'node:assert/strict'
import { CART, itemCount } from './cart.js'

test('counts every item in the cart', () => assert.equal(itemCount(CART), 3))
test('an empty cart has no items', () => assert.equal(itemCount([]), 0))
