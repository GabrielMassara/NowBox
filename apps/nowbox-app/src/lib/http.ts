const TOKEN_STORAGE_KEY = 'nowbox.token'

export class ApiError extends Error {
  status: number

  constructor(status: number, message: string) {
    super(message)
    this.status = status
  }
}

// A API responde os erros de negócio (404, 400, 409) com a mensagem em texto puro e os demais em JSON.
async function lerMensagemDeErro(response: Response): Promise<string | undefined> {
  const tipo = response.headers.get('Content-Type') ?? ''

  if (tipo.startsWith('text/plain')) {
    const texto = (await response.text().catch(() => '')).trim()
    return texto || undefined
  }

  const body = await response.json().catch(() => null)
  return body?.message || undefined
}

async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  const token = localStorage.getItem(TOKEN_STORAGE_KEY)
  const headers = new Headers(init.headers)
  headers.set('Content-Type', 'application/json')
  if (token) headers.set('Authorization', `Bearer ${token}`)

  const response = await fetch(path, { ...init, headers })

  if (response.status === 401) {
    localStorage.removeItem(TOKEN_STORAGE_KEY)
    window.dispatchEvent(new Event('nowbox:unauthorized'))
    throw new ApiError(401, 'Sessão expirada. Faça login novamente.')
  }

  if (!response.ok) {
    throw new ApiError(response.status, (await lerMensagemDeErro(response)) ?? 'Não foi possível completar a solicitação.')
  }

  if (response.status === 204) return undefined as T

  return response.json() as Promise<T>
}

export const http = {
  get: <T>(path: string) => request<T>(path, { method: 'GET' }),
  post: <T>(path: string, body?: unknown) =>
    request<T>(path, { method: 'POST', body: body ? JSON.stringify(body) : undefined }),
  put: <T>(path: string, body?: unknown) =>
    request<T>(path, { method: 'PUT', body: body ? JSON.stringify(body) : undefined }),
  delete: <T>(path: string) => request<T>(path, { method: 'DELETE' }),
}

export { TOKEN_STORAGE_KEY }
