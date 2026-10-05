const escapeHtml = (s) =>
  s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;')

function inline(text) {
  const codes = []
  let out = escapeHtml(text).replace(/`([^`]+)`/g, (_, c) => {
    codes.push(c)
    return `\u0000${codes.length - 1}\u0000`
  })
  out = out
    .replace(/\[([^\]]+)\]\((https?:\/\/[^)\s]+)\)/g, '<a href="$2" target="_blank" rel="noopener">$1</a>')
    .replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
    .replace(/\*([^*]+)\*/g, '<em>$1</em>')
  return out.replace(/\u0000(\d+)\u0000/g, (_, i) => `<code>${codes[i]}</code>`)
}

export function renderMarkdown(source) {
  const lines = source.replace(/\r\n/g, '\n').split('\n')
  const html = []
  let i = 0
  while (i < lines.length) {
    const line = lines[i]
    if (line.trim() === '') { i++; continue }

    if (line.startsWith('```')) {
      const code = []
      i++
      while (i < lines.length && !lines[i].startsWith('```')) code.push(lines[i++])
      i++
      html.push(`<pre><code>${escapeHtml(code.join('\n'))}</code></pre>`)
      continue
    }
    const heading = /^(#{1,6})\s+(.*)$/.exec(line)
    if (heading) {
      html.push(`<h${heading[1].length}>${inline(heading[2])}</h${heading[1].length}>`)
      i++
      continue
    }
    if (/^---+$/.test(line.trim())) { html.push('<hr>'); i++; continue }
    if (line.startsWith('>')) {
      const quote = []
      while (i < lines.length && lines[i].startsWith('>')) quote.push(lines[i++].replace(/^>\s?/, ''))
      html.push(`<blockquote>${inline(quote.join(' '))}</blockquote>`)
      continue
    }
    const listType = /^[-*]\s+/.test(line) ? 'ul' : /^\d+\.\s+/.test(line) ? 'ol' : null
    if (listType) {
      const marker = listType === 'ul' ? /^[-*]\s+/ : /^\d+\.\s+/
      const items = []
      while (i < lines.length && marker.test(lines[i])) items.push(`<li>${inline(lines[i++].replace(marker, ''))}</li>`)
      html.push(`<${listType}>${items.join('')}</${listType}>`)
      continue
    }
    const para = []
    while (i < lines.length && lines[i].trim() !== '' && !/^(#{1,6}\s|```|>|[-*]\s|\d+\.\s)/.test(lines[i])) para.push(lines[i++])
    html.push(`<p>${inline(para.join(' '))}</p>`)
  }
  return html.join('\n')
}

export function countWords(source) {
  const words = source.trim().split(/\s+/).filter(Boolean)
  return words.length
}
