export default defineEventHandler(async (event) => {
  const target = useRuntimeConfig(event).apiTarget as string || 'http://localhost:8080'
  const headers = getRequestHeaders(event)
  delete headers.host
  delete headers.origin
  delete headers.referer
  delete headers['sec-fetch-mode']
  delete headers['sec-fetch-site']
  delete headers['sec-fetch-dest']
  delete headers['sec-ch-ua']
  delete headers['sec-ch-ua-mobile']
  delete headers['sec-ch-ua-platform']

  const init: RequestInit = {
    method: event.method,
    headers
  }

  if (!['GET', 'HEAD'].includes(event.method)) {
    const raw = await readRawBody(event, false)
    if (raw) {
      init.body = Buffer.from(raw)
    }
  }

  const res = await fetch(`${target}${event.path}`, init)
  setResponseStatus(event, res.status)
  for (const [key, value] of res.headers) {
    if (key !== 'transfer-encoding') {
      setResponseHeader(event, key, value)
    }
  }
  const arrayBuffer = await res.arrayBuffer()
  return Buffer.from(arrayBuffer)
})
