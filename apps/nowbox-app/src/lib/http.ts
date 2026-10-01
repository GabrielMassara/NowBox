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

// Envia a requisição já autenticada e trata as respostas de erro, que são iguais para JSON e para arquivos.
async function enviar(path: string, init: RequestInit, accept?: string): Promise<Response> {
  const token = localStorage.getItem(TOKEN_STORAGE_KEY)
  const headers = new Headers(init.headers)
  if (!(init.body instanceof FormData)) headers.set('Content-Type', 'application/json')
  if (accept) headers.set('Accept', accept)
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

  return response
}

async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  const response = await enviar(path, init)

  if (response.status === 204) return undefined as T

  return response.json() as Promise<T>
}

export interface ArquivoBaixado {
  blob: Blob
  nomeArquivo?: string
}

function lerNomeDoArquivo(response: Response): string | undefined {
  const disposicao = response.headers.get('Content-Disposition') ?? ''
  const codificado = /filename\*=UTF-8''([^;]+)/i.exec(disposicao)
  if (codificado) return decodeURIComponent(codificado[1])
  return /filename="?([^";]+)"?/i.exec(disposicao)?.[1]
}

async function baixar(path: string): Promise<ArquivoBaixado> {
  const response = await enviar(path, { method: 'GET' }, '*/*')
  return { blob: await response.blob(), nomeArquivo: lerNomeDoArquivo(response) }
}

export const http = {
  download: (path: string) => baixar(path),
  get: <T>(path: string) => request<T>(path, { method: 'GET' }),
  post: <T>(path: string, body?: unknown) =>
    request<T>(path, { method: 'POST', body: body ? JSON.stringify(body) : undefined }),
  put: <T>(path: string, body?: unknown) =>
    request<T>(path, { method: 'PUT', body: body ? JSON.stringify(body) : undefined }),
  postForm: <T>(path: string, body: FormData) => request<T>(path, { method: 'POST', body }),
  putForm: <T>(path: string, body: FormData) => request<T>(path, { method: 'PUT', body }),
  delete: <T>(path: string) => request<T>(path, { method: 'DELETE' }),
}

export { TOKEN_STORAGE_KEY }
