import type { PageResponse } from '../types/api'

const TAMANHO_LOTE = 200

export async function carregarTodas<T>(buscar: (pagina: number, tamanho: number) => Promise<PageResponse<T>>) {
  const itens: T[] = []
  let pagina = 0
  let totalPaginas = 1

  while (pagina < totalPaginas) {
    const resultado = await buscar(pagina, TAMANHO_LOTE)
    itens.push(...resultado.content)
    totalPaginas = resultado.totalPages
    pagina += 1
  }

  return itens
}
