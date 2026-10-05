import test from 'node:test'
import assert from 'node:assert/strict'
import { renderMarkdown, countWords } from './markdown.js'

test('escapes raw html', () => {
  assert.equal(renderMarkdown('<script>x</script>'), '<p>&lt;script&gt;x&lt;/script&gt;</p>')
})
test('headings and emphasis', () => {
  assert.equal(renderMarkdown('## Hi **there** *you*'), '<h2>Hi <strong>there</strong> <em>you</em></h2>')
})
test('lists', () => {
  assert.equal(renderMarkdown('- a\n- b'), '<ul><li>a</li><li>b</li></ul>')
  assert.equal(renderMarkdown('1. a\n2. b'), '<ol><li>a</li><li>b</li></ol>')
})
test('code is not formatted inside', () => {
  assert.equal(renderMarkdown('`*a*`'), '<p><code>*a*</code></p>')
})
test('rejects javascript links', () => {
  assert.equal(renderMarkdown('[x](javascript:alert(1))'), '<p>[x](javascript:alert(1))</p>')
})
test('word count', () => {
  assert.equal(countWords('  one two\nthree '), 3)
  assert.equal(countWords(''), 0)
})
